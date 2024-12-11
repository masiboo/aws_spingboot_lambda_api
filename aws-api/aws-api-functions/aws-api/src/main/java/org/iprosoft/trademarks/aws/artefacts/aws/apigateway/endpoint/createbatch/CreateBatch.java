package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createbatch;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.*;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.*;

@AllArgsConstructor
@Slf4j
public class CreateBatch implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		String scannedApp;
		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}
		if (event.getPathParameters().containsKey("scannedApp")) {
			scannedApp = event.getPathParameters().get("scannedApp");
			if (scannedApp != null) {
				if (!RequestType.isAllowedType(scannedApp)) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							objectMapper
								.writeValueAsString(Map.of("message", "Invalid 'scannedApp' provided :'" + scannedApp
										+ "'  and allowed values are " + Arrays.toString(RequestType.values()))),
							true);
				}
				String shortDate = DateUtils.getCurrentDateShortStr();
				String eventBody = event.getBody();
				List<ArtefactBatch> artefactBatchList;
				DocumentFormatType documentFormat;
				if (eventBody.startsWith("[")) {
					log.info("INPUT IN JSON FORMAT");
					artefactBatchList = objectMapper.readValue(eventBody, new TypeReference<>() {
					});
					documentFormat = DocumentFormatType.JSON;
				}
				else {
					log.info("INPUT IN CSV FORMAT");
					try {
						artefactBatchList = (List<ArtefactBatch>) (Object) CsvConverterUtils.convertCSVClass(eventBody,
								ArtefactBatch.class);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					documentFormat = DocumentFormatType.CSV;
				}
				if (artefactBatchList.size() > 500) {
					log.warn("Too many records. Maximum permitted: 500");
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Too many records. Maximum permitted: 500", true);
				}
				// All object validated
				List<Map<String, Object>> plist = new ArrayList<>();
				String requestId = UUID.randomUUID().toString();
				for (ArtefactBatch artefactBatch : artefactBatchList) {
					// convert artefactBatchList to artefactBatchInput
					// for each line item - generate a job-id
					// generate an artefact set
					log.info("STEP 0");
					String artefactIdWithTrail = artefactBatch.getArtefactName() + "-" + artefactBatch.getFilename();
					String originalFilename = null;
					String[] parts = artefactIdWithTrail.split(",");
					if (parts.length > 0) {
						originalFilename = parts[0];
						log.debug("Entries ");
						log.debug("Entry originalFilename" + originalFilename);
						log.debug("Entry dir" + originalFilename);
					}
					String artefactId = (originalFilename != null) ? originalFilename : artefactIdWithTrail;
					String jobId = UUID.randomUUID().toString();
					String username = artefactBatch.getUser() != null ? artefactBatch.getUser() : "Anonymous";
					String validation = "OK";
					String urlString = "";

					log.info("STEP 1");
					// Validate item
					Map<String, String> inputValidation = validateInputDocument(artefactBatch, scannedApp);
					if (!inputValidation.isEmpty()) {
						validation = "ERROR";
						log.error("Validation errors {}", inputValidation);
					}
					else {
						// TODO: change to transaction item
						// Fixme: wrong hierarchy!!!!!!!!
						IArtefact item = new ArtefactDynamoDb(artefactId, DateUtils.getCurrentDatetimeUtc(), username);
						List<ArtefactTag> tags = new ArrayList<>();
						Map<String, String> query = event.getQueryStringParameters();
						// for batch use artefact name
						String key = "Aws-" + shortDate + "/" + artefactBatch.getArtefactName() + "/"
								+ artefactBatch.getFilename();
						item.setBucket(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET);
						item.setKey(key);
						item.setMirisDocId(artefactBatch.getMirisDocId());
						item.setArtefactClassType(artefactBatch.getArtefactClassType());
						item.setBelongsToArtefactId(artefactBatch.getArtefactName());
						item.setArtefactContainerName(artefactBatch.getArtefactName());
						item.setBatchSequenceId(artefactBatch.getBatchSequence());
						item.setFileName(artefactBatch.getFilename());
						item.setPart(true); // batch upload page by page
						item.setPageNumber(artefactBatch.getPage());
						item.setTotalPages(String.valueOf(artefactBatchList.size()));

						log.info("STEP 2");
						tags.add(ArtefactTag.builder()
							.documentId(artefactId)
							.key("untagged")
							.value("true")
							.insertedDate(DateUtils.getCurrentDatetimeUtc())
							.userId("Anonymous")
							.documentTagType(DocumentTagType.USERDEFINED)
							.build());
						log.info("saving transaction|correlation-id: " + item.getArtefactItemId() + " on path");

						// save batch-item (FIXME - this is not a batch item!!)
						artefactService.saveDocument(item, tags); // save transaction

						log.info("STEP 3");
						Map<String, String> metadata = new HashMap<>();
						metadata.put(METADATA_KEY_ARTEFACT_ID, artefactId);
						metadata.put(METADATA_KEY_TRACE_ID, jobId);
						if (StringUtils.isNotBlank(artefactBatch.getMirisDocId()))
							metadata.put(METADATA_KEY_MIRIS_DOCID, artefactBatch.getMirisDocId());
						if (StringUtils.isNotBlank(artefactBatch.getBatchSequence()))
							metadata.put(METADATA_KEY_BATCH_SEQ, artefactBatch.getBatchSequence());

						urlString = generatePresignedUrl(key, query, metadata);

						log.info("STEP 4");
						ArtefactJob job = new ArtefactJob().withId(jobId)
							.withArtefactId(artefactId)
							.withRequestId(requestId)
							.withBatchSequence(artefactBatch.getBatchSequence())
							.withS3SignedUrl(urlString)
							.withStatus("INIT");
						artefactJobService.saveJob(job);

						log.info("STEP 5");
						BatchInputDynamoDb batch = new BatchInputDynamoDb()
							.withBatchSequence(artefactBatch.getBatchSequence());
						// batch.setArtefactId(artefactId); // using artefact name
						// batch.setJobId(jobId);
						batch.setStatus("INIT");
						batch.setRequestId(requestId);
						batch.setOperator(new Operator().withUsername(artefactBatch.getUser()));
						batch.setRequestType(scannedApp);
						batchService.saveBatchSequence(batch);
						batchService.saveBatchSequenceWithChildren(batch);
					}

					log.info("STEP 12");
					Map<String, Object> outputMap = new HashMap<>();
					outputMap.put("type", artefactBatch.getType());
					outputMap.put("artefactName", artefactBatch.getArtefactName());
					outputMap.put("artefactClassType", artefactBatch.getArtefactClassType());
					outputMap.put("filename", artefactBatch.getFilename());
					outputMap.put("path", artefactBatch.getPath());
					outputMap.put("contentType", artefactBatch.getContentType());
					outputMap.put("batchSequence", artefactBatch.getBatchSequence());
					outputMap.put("creationDate", artefactBatch.getCreationDate());

					if (RequestType.ADDENDUM.toString().equalsIgnoreCase(scannedApp)) {
						outputMap.put("mirisDocId", artefactBatch.getMirisDocId());
					}
					if (RequestType.NEW_REQUEST.toString().equalsIgnoreCase(scannedApp)) {
						outputMap.put("requestType", artefactBatch.getRequestType());
					}
					outputMap.put("user", username);
					outputMap.put("jobId", jobId);
					outputMap.put("artefactId", artefactId);
					outputMap.put("requestId", requestId);
					outputMap.put("s3Url", urlString);
					outputMap.put("status", "INIT");
					outputMap.put("validation", validation);
					outputMap.put("documentFormat", documentFormat);

					log.info("STEP 13");
					plist.add(outputMap);
				}
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.CREATED,
						objectMapper.writeValueAsString(plist), false);
			}
			else {
				log.warn("'scannedApp' parameter is empty");
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'scannedApp' parameter is empty", true);
			}
		}
		else {
			log.warn("Missing 'scannedApp' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'scannedApp' parameter in path", true);
		}
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

	public Map<String, String> validateInputDocument(final ArtefactBatch document, String scannedApp) {
		Map<String, String> errorMessage = new HashMap<>();
		if (document.getContentType() == null || document.getContentType().isBlank()) {
			errorMessage.put("contentType", "must be present");
		}
		else if (Arrays.stream(DocumentContentType.values())
			.noneMatch(t -> t.name().equalsIgnoreCase(document.getContentType()))) {
			errorMessage.put("contentType", "not valid");
		}
		if (document.getFilename() == null || document.getFilename().isBlank()) {
			errorMessage.put("filename", "must be present");
		}
		if (document.getBatchSequence() == null || document.getBatchSequence().isBlank()) {
			errorMessage.put("batchsequence", "must be present");
		}
		if (RequestType.ADDENDUM.toString().equalsIgnoreCase(scannedApp)) {
			if (StringUtils.isBlank(document.getMirisDocId())) {
				errorMessage.put("mirisDocId", "must be present");
			}
			// Calling Aws-core proxy service to validate the mirisDocId
			if (StringUtils.isNotBlank(document.getMirisDocId())
					&& Boolean.parseBoolean(SystemEnvironmentVariables.MIRIS_CHECK_ENABLED)) {
				boolean isValid = artefactService.isDocIdValid(document.getMirisDocId());
				if (!isValid) {
					errorMessage.put("mirisDocId", "Invalid mirisDocId");
				}
			}
		}
		if (RequestType.NEW_REQUEST.toString().equalsIgnoreCase(scannedApp)) {
			if (document.getUser() == null || document.getUser().isBlank()) {
				errorMessage.put("Author", "must be present");
			}
		}
		return errorMessage;
	}

}
