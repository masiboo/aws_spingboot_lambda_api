package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.bulkuploadrequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiRequestHandlerResponse;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.*;
import software.amazon.awssdk.utils.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiMapResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.*;
import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.*;

@AllArgsConstructor
@Slf4j
public class BulkUploadRequest implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private static final int SIZE_LIMIT = 500;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	@Override
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: {}", in);
		APIGatewayV2HTTPEvent event = parseEvent(in);
		log.info("Event Dump: {}", event);

		if (event.getBody() == null || event.getBody().isEmpty()) {
			log.error("Empty request body {}", event.getBody());
			return createErrorResponse("Empty request body", SC_BAD_REQUEST);
		}
		try {
			log.info("STEP 1 Loading body: ");
			String requestBodyString = event.getBody();
			List<ArtefactInput> artefactInputList;
			artefactInputList = parseRequestBody(requestBodyString);
			log.info("Deserialize artefactInputList done: {}", artefactInputList.toString());

			if (artefactInputList.size() > SIZE_LIMIT) {
				log.warn("Requested too many records: {}. Maximum permitted: " + SIZE_LIMIT, artefactInputList.size());
				return createErrorResponse("Requested too many records: " + artefactInputList.size()
						+ ". Maximum permitted: " + SIZE_LIMIT, SC_BAD_REQUEST);
			}
			Map<String, Object> artefactInputMap = processArtefactInputs(artefactInputList);
			log.info("All processing done for Map<String, Object> artefactInputMap = {}", artefactInputMap);
			ObjectMapper objectMapper = new ObjectMapper();
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(SC_CREATED.getStatusCode(),
					objectMapper.writeValueAsString(artefactInputMap), false);
		}
		catch (Exception e) {
			log.error("Error message: {}", e.getMessage());
			return createErrorResponse("Error: " + e.getMessage(), SC_ERROR);
		}
	}

	private APIGatewayV2HTTPEvent parseEvent(InputStream in) {
		return GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);
	}

	private List<ArtefactInput> parseRequestBody(String jsonString) throws IOException {
		if (jsonString.startsWith("[")) {
			log.info("INPUT IN JSON FORMAT");
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(jsonString, new TypeReference<>() {
			});
		}
		else {
			throw new BadRequestException("Input is not in JSON format");
		}
	}

	private Map<String, Object> processArtefactInputs(List<ArtefactInput> artefactInputList) {
		Map<String, Object> artefactInputMap = new HashMap<>();
		String requestId = UUID.randomUUID().toString();
		log.info("Newly generated requestId {}", requestId);
		artefactInputMap.put("requestId", requestId);

		List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

		for (ArtefactInput artefactInput : artefactInputList) {
			CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
				log.info("Start processSingleArtefactInput for artefactInput = {}", artefactInput);
				return processSingleArtefactInput(artefactInput, artefactInputList.size(), requestId);
			});
			futures.add(future);
		}

		List<Map<String, Object>> artefactList = futures.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toList());

		artefactInputMap.put("artefacts", artefactList);
		return artefactInputMap;
	}

	private Map<String, Object> processSingleArtefactInput(ArtefactInput artefactInput, int artefactInputListSize,
			String requestId) {
		String shortDate = DateUtils.getCurrentDateShortStr();
		String artefactId = UUID.randomUUID().toString();
		String jobId = UUID.randomUUID().toString();
		String username = "anonymous";
		String validation = "OK";

		Map<String, String> inputValidation = artefactService.validateArtefact(artefactInput);
		String validationErrorMsg = ArtefactUploadRequestUtil.logValidationErrors(inputValidation);
		if (!inputValidation.isEmpty()) {
			validation = "ERROR";
			log.error("Artefact validation errors: {}", validationErrorMsg);
		}
		log.info("Start createArtefactItem");
		IArtefact item = createArtefactItem(artefactInput, artefactId, shortDate, username, artefactInputListSize);
		log.info("IArtefact item = {}", item);
		log.info("Start createArtefactTags");
		List<ArtefactTag> tags = createArtefactTags(artefactId, username);
		log.info("List<ArtefactTag> tags = {}", tags);
		try {
			artefactService.saveDocument(item, tags);
		}
		catch (Exception e) {
			log.warn("artefactService.saveDocument(item, tags) exception: {}", e.getMessage());
		}

		log.info("Saved artefact: {}", artefactId);
		log.info("Start createMetadataMap");
		Map<String, String> metadata = createMetadataMap(artefactId, jobId, artefactInput.getMirisDocId());
		log.info("Map<String, String> metadata = {}", metadata);
		String preSignedGetUrl = "";
		try {
			preSignedGetUrl = ArtefactUploadRequestUtil.generatePresidedUrl(s3Service, item.getKey(),
					ArtefactUploadRequestUtil.createDurationMap(), metadata);
		}
		catch (Exception e) {
			log.warn("ArtefactUploadRequestUtil.generatePresidedUrl() exception: {}", e.getMessage());
		}

		log.info("String preSignedGetUrl = {}", preSignedGetUrl);
		saveArtefactJob(jobId, artefactId, requestId, preSignedGetUrl);
		return populateArtefactDetailsMap(artefactInput, artefactId, jobId, username, preSignedGetUrl, validation,
				validationErrorMsg, item.getSizeWarning());
	}

	private IArtefact createArtefactItem(ArtefactInput artefactInput, String artefactId, String shortDate,
			String username, int totalSize) {
		IArtefact item = new ArtefactDynamoDb(artefactId, DateUtils.getCurrentDatetimeUtc(), username);
		String key = "Aws-" + shortDate + "/" + artefactId + "/" + artefactInput.getArtefactName();
		item.setBucket(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET);
		item.setKey(key);
		item.setFileName(artefactInput.getArtefactName());
		item.setMirisDocId(artefactInput.getMirisDocId());
		item.setArtefactClassType(artefactInput.getArtefactClassType());
		item.setTotalPages(String.valueOf(totalSize));
		Long contentLength = artefactInput.getItems()
			.stream()
			.findFirst()
			.map(ArtefactItemInput::getContentLength)
			.map(SafeParserUtil::safeParseLong)
			.orElse(0L);
		log.info("Content length: {}", contentLength);
		item.setContentLength(contentLength);
		item.setSizeWarning(
				StringUtils.isNotBlank(validateFileSize(contentLength, artefactInput.getArtefactClassType())));
		// existing Artefact will be marked as DELETE and new one will become
		// INDEXED , MPD-431, MPD-439 and MPD-747
		item.setStatus(getArtefactStatus(artefactInput.getMirisDocId(), artefactInput.getArtefactClassType()));
		return item;
	}

	private String validateFileSize(Long contentLength, String artefactClassType) {
		FileSizeValidator fileSizeValidator = new FileSizeValidator();
		return fileSizeValidator.validate(contentLength, artefactClassType);
	}

	private String getArtefactStatus(String mirisDocId, String artefactClassType) {
		boolean hasMirisDocId = StringUtils.isNotBlank(mirisDocId);
		try {
			boolean hasFileWithSameDocId = artefactService.hasFileWithSameDocId(mirisDocId, artefactClassType);
			if (hasMirisDocId && hasFileWithSameDocId) {
				return ArtefactStatus.INDEXED.toString();
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			log.warn("artefactService.hasFileWithSameDocId(mirisDocId, artefactClassType) exception: {}",
					e.getMessage());
		}
		return null;
	}

	private List<ArtefactTag> createArtefactTags(String artefactId, String username) {
		List<ArtefactTag> tags = new ArrayList<>();
		tags.add(ArtefactTag.builder()
			.documentId(artefactId)
			.key("untagged")
			.value("true")
			.insertedDate(DateUtils.getCurrentDatetimeUtc())
			.userId(username)
			.documentTagType(DocumentTagType.USERDEFINED)
			.build());
		return tags;
	}

	private Map<String, String> createMetadataMap(String artefactId, String jobId, String mirisDocId) {
		Map<String, String> metadata = new HashMap<>();
		metadata.put(METADATA_KEY_ARTEFACT_ID, artefactId);
		metadata.put(METADATA_KEY_TRACE_ID, jobId);
		metadata.put(METADATA_KEY_MIRIS_DOCID, mirisDocId);
		return metadata;
	}

	private void saveArtefactJob(String jobId, String artefactId, String requestId, String preSignedGetUrl) {
		ArtefactJob job = new ArtefactJob().withId(jobId)
			.withRequestId(requestId)
			.withArtefactId(artefactId)
			.withS3SignedUrl(preSignedGetUrl)
			.withStatus("INIT");
		try {
			artefactJobService.saveJob(job);
			log.info("Saved artefact job details: {}", job);
		}
		catch (Exception e) {
			log.warn("artefactJobService.saveJob(job) failed: {}", e.getMessage());
		}
	}

	private Map<String, Object> populateArtefactDetailsMap(ArtefactInput artefactInput, String artefactId, String jobId,
			String username, String preSignedGetUrl, String validation, String validationErrorMsg,
			boolean sizeWarning) {
		Map<String, Object> artefactDetails = new HashMap<>();
		artefactDetails.put("artefactName", artefactInput.getArtefactName());
		artefactDetails.put("signedS3Url", preSignedGetUrl);
		artefactDetails.put("mirisDocId", artefactInput.getMirisDocId());
		artefactDetails.put("artefactClassType", artefactInput.getArtefactClassType());
		artefactDetails.put("jobId", jobId);
		artefactDetails.put("artefactId", artefactId);
		artefactDetails.put("user", username);
		artefactDetails.put("validation", validation);
		artefactDetails.put("status", "INIT");
		if (sizeWarning) {
			artefactDetails.put("warning", "File size exceeds the limit");
		}
		if (validationErrorMsg != null) {
			artefactDetails.put("validationErrorMsg", validationErrorMsg);
			artefactDetails.put("statusCode", SC_BAD_REQUEST + " " + SC_BAD_REQUEST.getStatusCode());
		}
		else {
			artefactDetails.put("statusCode", SC_CREATED + " " + SC_CREATED.getStatusCode());
		}
		log.info("populateArtefactDetailsMap: {}", artefactDetails);
		return artefactDetails;
	}

	private APIGatewayV2HTTPResponse createErrorResponse(String message, ApiResponseStatus statusCode) {
		Map<String, Object> map = new HashMap<>();
		map.put("message", message);

		ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(statusCode, new ApiMapResponse(map));
		try {
			return ArtefactUploadRequestUtil.buildResponse(response.getStatus(), response.getHeaders(),
					response.getResponse());
		}
		catch (Exception e) {
			log.error("Error creating createErrorResponse: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
					"Error creating createErrorResponse: " + e.getMessage(), true);
		}
	}

}