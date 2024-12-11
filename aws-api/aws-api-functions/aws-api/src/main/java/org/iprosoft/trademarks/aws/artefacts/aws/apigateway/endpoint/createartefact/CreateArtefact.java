package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createartefact;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.*;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class CreateArtefact implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	private final FileSizeValidator fileSizeValidator;

	@Override
	@SneakyThrows
	@RegisterReflectionForBinding(ArtefactInput.class)
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("Event: " + event);
		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}
		List<String> paramList = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);
		String artefactId = UUID.randomUUID().toString();
		String jobId = UUID.randomUUID().toString();
		String eventBody = event.getBody();
		ArtefactInput artefact;
		try {
			artefact = objectMapper.readValue(eventBody, ArtefactInput.class);
		}
		catch (IOException e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Failed to parse artefact from request body " + e.getMessage(), true);
		}
		String filename = artefact.getItems().get(0).getFilename();
		String shortDate = DateUtils.getCurrentDateShortStr();
		String key = "Aws-" + shortDate + "/" + artefactId + "/" + filename;

		// TODO: change to transaction item
		IArtefact item = new ArtefactDynamoDb(artefactId, DateUtils.getCurrentDatetimeUtc(), "Anonymous");

		List<ArtefactTag> tags = new ArrayList<>();
		Map<String, String> query = event.getQueryStringParameters();

		// TODO: validate JSON body input with JSON schema
		// TODO: business validation
		item.setBucket(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET);
		item.setKey(key);
		item.setFileName(filename);
		Map<String, String> inputValidation = artefactService.validateArtefact(artefact);
		if (!inputValidation.isEmpty()) {
			Map<String, Object> outputMap = new HashMap<>();
			outputMap.put("artefactId", artefactId);
			outputMap.put("validation", inputValidation);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					objectMapper.writeValueAsString(outputMap), true);
		}

		Long contentLength = artefact.getItems()
			.stream()
			.findFirst()
			.map(ArtefactItemInput::getContentLength)
			.map(Long::valueOf)
			.orElse(0L);

		String sizeWarning = fileSizeValidator.validate(contentLength, artefact.getArtefactClassType());
		item.setSizeWarning(StringUtils.hasText(sizeWarning));
		item.setContentLength(contentLength);
		// existing Artefact will be marked as DELETE and new one will become INDEXED
		// MPD-381, MPD-431, MPD-439 and MPD-747
		if (StringUtils.hasText(artefact.getMirisDocId())
				&& artefactService.hasFileWithSameDocId(artefact.getMirisDocId(), artefact.getArtefactClassType())) {
			item.setStatus(ArtefactStatus.INDEXED.toString());
		}
		// Persist object to DynamoDB
		item.setMirisDocId(artefact.getMirisDocId());
		item.setArtefactClassType(artefact.getArtefactClassType());
		tags.add(ArtefactTag.builder()
			.documentId(artefactId)
			.key("untagged")
			.value("true")
			.insertedDate(DateUtils.getCurrentDatetimeUtc())
			.userId("Anonymous")
			.documentTagType(DocumentTagType.USERDEFINED)
			.build());
		artefactService.saveDocument(item, tags);

		Map<String, String> metadata = new HashMap<>();
		metadata.put(AppConstants.METADATA_KEY_ARTEFACT_ID, artefactId);
		metadata.put(AppConstants.METADATA_KEY_TRACE_ID, jobId);
		metadata.put(AppConstants.METADATA_KEY_MIRIS_DOCID, artefact.getMirisDocId());

		String urlString = generatePresignedUrl(key, query, metadata);

		ArtefactJob job = new ArtefactJob().withId(jobId)
			.withArtefactId(artefactId)
			.withS3SignedUrl(urlString)
			.withStatus("INIT");
		artefactJobService.saveJob(job);

		// Prepare response
		Map<String, Object> outputMap = new HashMap<>();
		outputMap.put("artefactId", artefactId);
		outputMap.put("signedS3Url", urlString);
		outputMap.put("jobId", jobId);
		if (StringUtils.hasText(sizeWarning))
			outputMap.put("warning", sizeWarning);

		return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.CREATED,
				objectMapper.writeValueAsString(outputMap), false);
	}

	private String generatePresignedUrl(final String key, final Map<String, String> query,
			final Map<String, String> metadata) {
		Duration duration = caculateDuration(query);
		URL url = s3Service.presignPutUrl(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET, key, duration, metadata);
		return url.toString();
	}

	private Duration caculateDuration(final Map<String, String> query) {
		Integer durationHours = query != null && query.containsKey("duration") ? Integer.valueOf(query.get("duration"))
				: Integer.valueOf(48);
		return Duration.ofHours(durationHours);
	}

}
