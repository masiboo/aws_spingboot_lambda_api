package artefact.entrypoints;

import artefact.dto.input.ArtefactInput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.*;
import artefact.aws.AwsServiceCache;
import artefact.entity.*;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.usecase.ArtefactServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ArtefactStatus;
import artefact.util.ConstantUtil;
import artefact.util.FileSizeValidator;
import software.amazon.awssdk.utils.StringUtils;
import artefact.dto.input.ArtefactItemInput;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static artefact.apigateway.ApiResponseStatus.SC_BAD_REQUEST;
import static artefact.apigateway.ApiResponseStatus.SC_CREATED;
import static artefact.util.AppConstants.*;

public class ArtefactUploadURLRequestHandler extends CoreRequestHandler
		implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>, ApiRequestEventUtil {

	private static final int DEFAULT_DURATION_HOURS = 48;
	private static final Logger logger = LoggerFactory.getLogger(ArtefactUploadURLRequestHandler.class);
	public static final long MAX_CONTENT_LENGHT_SOUND = 5120L;
	Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final String S3_PREFIX = (String) "Aws-";
	private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public ArtefactUploadURLRequestHandler() {
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
//		logger.info("Event json Dump: " + gsonGlobal.toJson(event));
//		logger.info("Event Dump: " + event);

		try {

			if (event.getBody() == null || event.getBody().isEmpty()) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Empty request body");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
				APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
			}

			Date date = new Date();
			String artefactId = UUID.randomUUID().toString(); // will be changed transaction id (since artefact id is generated at DB level)
			String jobId = UUID.randomUUID().toString();
			String username = getCallingCognitoUsername(event);

			String jsonString = event.getBody();
			JsonObject jsonObjectAlt = JsonParser.parseString(jsonString).getAsJsonObject();
			JsonObject jsonObjectAltInt = jsonObjectAlt.getAsJsonArray("items").get(0).getAsJsonObject();
			String filename = jsonObjectAltInt.get("filename").getAsString();

			SimpleDateFormat yyyymmddFormat =  new SimpleDateFormat("yyyy-MM-dd");
			String shortdate = yyyymmddFormat.format(date);

			// TODO: change to transaction item
			IArtefact item = new ArtefactDynamoDb(artefactId, ZonedDateTime.now(), username);

			List<ArtefactTag> tags = new ArrayList<>();
			Map<String, String> map = event.getPathParameters(); // post body conversion !!
			Map<String, String> query = event.getQueryStringParameters();

			// TODO: validate JSON body input with JSON schema
			// TODO: business validation

			ArtefactServiceInterface service = getAwsServices().documentService();
			ArtefactJobServiceInterface jobService = getAwsServices().jobService();

			String bucket = getAwsServices().documents3bucket();
			String key = S3_PREFIX + shortdate + "/" + artefactId + "/" + filename;

			item.setBucket(bucket);
			item.setKey(key);
			item.setFileName(filename);

			ArtefactInput artefact;
			try{
				artefact = objectMapper.readValue(event.getBody(), ArtefactInput.class);
			}catch (IOException e) {
				logger.error(e.getMessage());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_BAD_REQUEST,
										"Failed to parse artefact from request body "+e.getMessage(), true);
			}
			Map<String, String> inputValidation = service.validateInputDocument(artefact);
			APIGatewayV2HTTPResponse gatewayV2HTTPResponse;

			if(!inputValidation.isEmpty()) {
				Map<String, Object> outputmap = new HashMap<>();
				outputmap.put("artefactId", artefactId);
				outputmap.put("validation", inputValidation);
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(outputmap));
				gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
				return gatewayV2HTTPResponse;
			}

			Long contentLength = artefact.getItems().stream().findFirst()
												.map(ArtefactItemInput::getContentLength)
												.map(Long::valueOf)
												.orElse(0L);
			logger.info("contentLength in bytes: " + contentLength);
			FileSizeValidator fileSizeValidator = new FileSizeValidator();
			String sizeWarning = fileSizeValidator.validate(contentLength,artefact.getArtefactClassType());
			item.setSizeWarning(StringUtils.isNotBlank(sizeWarning));
			item.setContentLength(contentLength);


			// TODO uncomment the condition once Aws-core is deployed
			// Calling Aws-core proxy service to validate the mirisDocId
//			String mirisCheckApiUrl = System.getenv("Aws_CORE_MIRIS_CHECK_API_URL");
//			String isMirisCheckEnabled = System.getenv("MIRIS_CHECK_ENABLED");
//			if(StringUtils.isNotBlank(artefact.getMirisDocId())
//					&& mirisCheckApiUrl != null ) {
//				MirisDocIdValidatorService validatorService = new MirisDocIdValidatorService();
//				boolean isValid = validatorService.isValid(artefact.getMirisDocId());
//				if(Boolean.valueOf(isMirisCheckEnabled) && !isValid){
//					ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST,
//							new ApiMapResponse(Map.of(KEY_MESSAGE,"ArtefactItem ContentLength should be less than or equal to 5MB")));
//					return buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
//				}
//			}

			// existing Artefact will be marked as DELETE and new one will become INDEXED , MPD-431, MPD-439 and MPD-747
			if (StringUtils.isNotBlank(artefact.getMirisDocId())
					&& service.hasFileWithSameDocId(artefact.getMirisDocId(), artefact.getArtefactClassType())) {
				item.setStatus(ArtefactStatus.INDEXED.toString());
			}


			item.setMirisDocId(artefact.getMirisDocId());
			item.setArtefactClassType(artefact.getArtefactClassType());

			tags.add(new ArtefactTag(artefactId, "untagged", "true", ZonedDateTime.now(), username,
					DocumentTagType.SYSTEMDEFINED)); // add artefact metadata here..

			logger.info("saving transaction|correlation-id: " + item.getArtefactiTemId() + " on path" );
			service.saveDocument(item, tags); // save transaction

			Map<String, String> metadata = new HashMap<>();
			metadata.put(METADATA_KEY_ARTEFACT_ID,artefactId);
			metadata.put(METADATA_KEY_TRACE_ID,jobId);
			metadata.put(METADATA_KEY_MIRIS_DOCID, artefact.getMirisDocId());

			String urlstring = generatePresignedUrl(getAwsServices(), key, query, metadata);

			ArtefactJob job = new ArtefactJob().withId(jobId)
					.withArtefactId(artefactId)
					.withS3SignedUrl(urlstring)
					.withStatus("INIT");
			jobService.saveJob(job);

			Map<String, Object> outputmap = new HashMap<>();
			outputmap.put("artefactId", artefactId);
			outputmap.put("signedS3Url", urlstring);
			outputmap.put("jobId", jobId);
			if(StringUtils.isNotBlank(sizeWarning))
				outputmap.put("warning",sizeWarning);

			ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_CREATED, new ApiMapResponse(outputmap));
			gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
			return gatewayV2HTTPResponse;

		} catch (Exception e) {
			logger.error("Exception while uploading single artefact ",e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
										"Exception while uploading single artefact "+e.getMessage(), true);
			}
	}

	private Artefact createArtefact(APIGatewayV2HTTPEvent event) {
		Date date = new Date();
		String artefactId = UUID.randomUUID().toString();
		String username = getCallingCognitoUsername(event);
		Artefact artefact = new ArtefactBuilder()
				.setId(artefactId)
				.setArtefactName("artefactName")
				.setArchiveDate(ZonedDateTime.now())
				.createArtefact();

		return artefact;

	}

	private String getCallingCognitoUsername(APIGatewayV2HTTPEvent event) {
		return "Anonymous";
	}

	private String generatePresignedUrl(final AwsServiceCache awsservice,
										final String key, final Map<String, String> query,
										final Map<String, String> metadata) throws BadException {

		Duration duration = caculateDuration(query);
		Optional<Long> contentLength = calculateContentLength(awsservice, query);
		URL url = awsservice.s3Service().presignPutUrl(awsservice.documents3bucket(), key, duration, metadata);

		String urlstring = url.toString();
		return urlstring;
	}

	private Optional<Long> calculateContentLength(final AwsServiceCache awsservice,
												  final Map<String, String> query) throws BadException {

		Long contentLength = query != null && query.containsKey("contentLength")
				? Long.valueOf(query.get("contentLength"))
				: null;

//		String value = this.restrictionMaxContentLength.getValue(awsservice);
//		if (value != null
//				&& this.restrictionMaxContentLength.enforced(awsservice, value, contentLength)) {
//
//			if (contentLength == null) {
//				throw new BadException("'contentLength' is required");
//			}
//
//			String maxContentLengthBytes =
//					this.restrictionMaxContentLength.getValue(awsservice;
//			throw new BadException("'contentLength' cannot exceed " + maxContentLengthBytes + " bytes");
//		}

		return contentLength != null ? Optional.of(contentLength) : Optional.empty();
	}

	private Duration caculateDuration(final Map<String, String> query) {

		Integer durationHours =
				query != null && query.containsKey("duration") ? Integer.valueOf(query.get("duration"))
						: Integer.valueOf(DEFAULT_DURATION_HOURS);

		Duration duration = Duration.ofHours(durationHours.intValue());
		return duration;
	}

}
