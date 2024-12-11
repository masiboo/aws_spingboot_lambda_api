package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.artefactbatchuploadurlrequesthandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiMapBatchResponse;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiMapResponse;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiRequestHandlerResponse;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.*;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BaseApiGatewayHandler;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_CREATED;
import static org.iprosoft.trademarks.aws.artefacts.util.ArtefactUploadRequestUtil.MIRIS_DOC_ID_KEY;
import static org.iprosoft.trademarks.aws.artefacts.configuration.AppConstants.METADATA_KEY_ARTEFACT_ID;
import static org.iprosoft.trademarks.aws.artefacts.configuration.AppConstants.METADATA_KEY_TRACE_ID;

@AllArgsConstructor
@Slf4j
public class ArtefactBatchUploadURLRequestHandler extends BaseApiGatewayHandler
		implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private static final int SIZE_LIMIT = 1000;

	private final String S3_PREFIX = "Aws/";

	private final BatchService batchService;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	@Override
	public APIGatewayV2HTTPResponse apply(InputStream input) {
		APIGatewayV2HTTPEvent event = processEventWithGson(input);
		if (event == null) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}

		boolean overwriteBatch;
		String scannedApp;
		try {
			if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
				scannedApp = event.getQueryStringParameters().get("scannedApp");
				overwriteBatch = Boolean.parseBoolean(event.getQueryStringParameters().get("overwriteBatch"));
				log.info("Document type: {}", scannedApp);
				log.info("overwriteBatch: {}", overwriteBatch);
				String validationErrors = ArtefactUploadRequestUtil.validateQueryParams(scannedApp,
						String.valueOf(overwriteBatch));
				if (StringUtils.isNotBlank(validationErrors)) {
					log.error("Query parameter validation error {}", validationErrors);
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, validationErrors, true);
				}
			}
			else {
				log.error("Query parameter is event.getQueryStringParameters(): " + event.getQueryStringParameters());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, "Query parameter is empty/null", true);
			}
		}
		catch (Exception e) {
			log.error("error getting  query parameters", e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400,
					"Exception for query parameter is handling " + e.getMessage(), true);
		}

		try {
			if (event.getBody() == null || event.getBody().isEmpty()) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Empty request body");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST,
						new ApiMapResponse(map));
				return ArtefactUploadRequestUtil.buildResponse(response.getStatus(), response.getHeaders(),
						response.getResponse());
			}

			Date date = new Date();
			SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
			String shortDate = yyyymmddFormat.format(date);

			log.info("STEP 1 Loading body: ");
			String requestBody = event.getBody();

			log.info("Loading body complete String: " + requestBody);
			DocumentFormatType documentFormat;
			List<ArtefactBatch> artefactBatchList;
			List<BatchInputDynamoDb> distinctArtefactBatch = null;

			if (requestBody.startsWith("[")) {
				log.info("INPUT IN JSON FORMAT");
				try {
					artefactBatchList = JsonConverterUtil.getArtefactBatchesFromJson(requestBody);
					documentFormat = DocumentFormatType.JSON;
					distinctArtefactBatch = JsonConverterUtil.extractDistinctBatchSequences(artefactBatchList);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				log.info("INPUT IN CSV FORMAT");
				try {
					artefactBatchList = CsvConverterUtil.getArtefactBatchesForCsv(requestBody);
					distinctArtefactBatch = CsvConverterUtil.extractDistinctBatchSequences(requestBody);
					documentFormat = DocumentFormatType.CSV;
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			if (artefactBatchList.size() > SIZE_LIMIT) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Too many records. Maximum permitted: " + SIZE_LIMIT);
				return ArtefactUploadRequestUtil.createErrorResponse(map);
			}

			log.info("STEP 2 check existing batch and overwrite {}", overwriteBatch);
			// overwrite batch's artefacts if true
			if (overwriteBatch) {
				List<String> batchSequences = artefactBatchList.stream()
					.map(ArtefactBatch::getBatchSequence)
					.filter(sequence -> sequence != null && !sequence.isEmpty())
					.toList();
				Set<String> existingBatchSequence = ArtefactUploadRequestUtil.getExistedBatchSequence(batchService,
						batchSequences);
				log.info("Found existing BatchSequence: {}", existingBatchSequence);
				ArtefactUploadRequestUtil.deleteExistedBatchSequence(batchService, existingBatchSequence);
			}
			else {
				log.warn("overwriteBatch parameter can't accept false. We will remove overwriteBatch soon");
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(SC_BAD_REQUEST.getStatusCode(),
						"overwriteBatch parameter can't accept false. We will remove overwriteBatch soon", true);
			}

			List<Map<String, Object>> plist = new ArrayList<>();
			List<String> unmergedArtefactsId = new ArrayList<>();
			// List<ArtefactJob> jobs = new ArrayList<>();

			String requestId = UUID.randomUUID().toString(); //
			List<Map<String, String>> validationReport = new ArrayList<>();
			List<Map<String, String>> eachBatchValidation = new ArrayList<>();

			Collection<IArtefact> artefacts = new ArrayList<>();
			Collection<ArtefactJob> jobs = new ArrayList<>();

			for (ArtefactBatch artefactBatch : artefactBatchList) {
				// need the merge to be unique for this session due to duplicate batch
				// sequences and files names
				String mergeId = requestId + "/" + artefactBatch.getBatchSequence();
				String artefactId = requestId + "-" + artefactBatch.getArtefactItemFileName();
				String jobId = UUID.randomUUID().toString();

				String username = "Anonymous"; // ?? operator name
				String validation = "OK";
				String urlstring = "";
				// Validate item
				Map<String, String> inputValidation = artefactService.validateArtefactBatch(artefactBatch, scannedApp);

				if (!inputValidation.isEmpty()) {
					validation = "ERROR: " + ArtefactUploadRequestUtil.mapToString(inputValidation);
					validationReport.add(inputValidation);
					eachBatchValidation.add(inputValidation);
					log.error("Validation errors {}", inputValidation);
				}
				else {

					log.info("STEP 3 Create tags and item");
					List<ArtefactTag> tags = new ArrayList<>();
					Map<String, String> query = event.getQueryStringParameters();

					String bucket = SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET;
					String key = S3_PREFIX + shortDate + "/" + mergeId + "/" + artefactBatch.getFilename();

					ArtefactDynamoDb item = new ArtefactDynamoDb(artefactId, DateUtils.getCurrentDatetimeUtc(),
							username);
					item.setBucket(bucket);
					item.setKey(key);
					item.setArtefactClassType(artefactBatch.getArtefactClassType());
					item.setBelongsToArtefactId(artefactBatch.getArtefactMergeId());
					item.setArtefactContainerName(mergeId);

					log.info("unmergedArtefactsId add {}", mergeId);
					boolean result = unmergedArtefactsId.add(mergeId);
					log.info("results {}", result);

					item.setPageNumber(artefactBatch.getPage());
					item.setArtefactMergeId(artefactBatch.getArtefactMergeId());

					item.setBatchSequenceId(artefactBatch.getBatchSequence());
					item.setFileName(artefactBatch.getFilename());
					item.setPart(true); // batch upload page by page

					item.setArtefactItemFileName(artefactBatch.getArtefactItemFileName());
					item.setTotalPages(String.valueOf(distinctArtefactBatch.stream()
						.filter(batch -> batch.getBatchSequence().equals(artefactBatch.getBatchSequence()))
						.count()));

					item.setScannedType(ScannedAppType.forTypeIgnoreCase(scannedApp));
					if (ScannedAppType.ADDENDUM.toString().equalsIgnoreCase(scannedApp)) {
						item.setMirisDocId(artefactBatch.getMirisDocId());
					}

					tags.add(new ArtefactTag(artefactId, "untagged", "true", DateUtils.getCurrentDatetimeUtc(),
							username, DocumentTagType.SYSTEMDEFINED)); // add artefact
					// metadata
					// here..

					log.info("STEP 4 Save tags and item");
					// artefactService.saveDocument(item, tags);
					artefacts.add(item);

					// save to batch input
					assert distinctArtefactBatch != null;
					distinctArtefactBatch.stream()
						.filter(batch -> batch.getBatchSequence().equals(artefactBatch.getBatchSequence()))
						.findFirst()
						.get()
						.addArtefact(item);

					Map<String, String> metadata = new HashMap<>();
					metadata.put(METADATA_KEY_ARTEFACT_ID, artefactId);
					metadata.put(METADATA_KEY_TRACE_ID, jobId);
					if (StringUtils.isNotBlank(artefactBatch.getMirisDocId()))
						metadata.put(AppConstants.METADATA_KEY_MIRIS_DOCID, artefactBatch.getMirisDocId());
					if (StringUtils.isNotBlank(artefactBatch.getBatchSequence()))
						metadata.put(AppConstants.METADATA_KEY_BATCH_SEQ, artefactBatch.getBatchSequence());

					urlstring = ArtefactUploadRequestUtil.generatePresidedUrl(s3Service, key, query, metadata);
					log.info("urlstring {}", urlstring);

					// save to batchinput
					ArtefactJob job = createAndSaveJobRecord(requestId, artefactBatch, artefactId, jobId, urlstring);
					jobs.add(job);

					distinctArtefactBatch.stream()
						.filter(batch -> batch.getBatchSequence().equals(artefactBatch.getBatchSequence()))
						.findFirst()
						.get()
						.addJob(job);

					String user = "Anonymous";

					if (artefactBatch.getUser() != null) {
						user = artefactBatch.getUser();
					}

					distinctArtefactBatch.stream()
						.filter(batch -> batch.getBatchSequence().equals(artefactBatch.getBatchSequence()))
						.findFirst()
						.get()
						.setLastModUser(new LastModUser().withUsername(user));

					// save batch - child edge
					createAndSaveBatchChildren(requestId, artefactBatch, artefactId);

				}

				log.info("STEP 7 Create outputMap");
				Map<String, Object> outputMap = new HashMap<>();
				outputMap.put("type", artefactBatch.getType());
				outputMap.put("artefactName", artefactBatch.getArtefactName());
				outputMap.put("artefactClassType", artefactBatch.getArtefactClassType());
				outputMap.put("filename", artefactBatch.getFilename());
				outputMap.put("path", artefactBatch.getPath());
				outputMap.put("contentType", artefactBatch.getContentType());
				outputMap.put("batchSequence", artefactBatch.getBatchSequence());
				outputMap.put("creationDate", artefactBatch.getCreationDate());

				if (ScannedAppType.ADDENDUM.toString().equalsIgnoreCase(scannedApp)) {
					// inputValidation was filled with validation message previously.
					// If it is empty, artefact should have a valid mirisDocId
					if (inputValidation.get(MIRIS_DOC_ID_KEY) == null) {
						outputMap.put(MIRIS_DOC_ID_KEY, artefactBatch.getMirisDocId());
					}
					else {
						outputMap.put(MIRIS_DOC_ID_KEY, inputValidation.get(MIRIS_DOC_ID_KEY));
					}

					// artefactBatch.setRequestType(;
					outputMap.put("requestType", artefactBatch.getRequestType());
				}

				if (ScannedAppType.NEW_REQUEST.toString().equalsIgnoreCase(scannedApp)) {
					outputMap.put("requestType", artefactBatch.getRequestType());
				}

				outputMap.put("jobId", jobId);
				outputMap.put("artefactId", artefactId);
				outputMap.put("requestId", requestId);
				outputMap.put("s3Url", urlstring);
				outputMap.put("status", "INIT");
				outputMap.put("validation", validation);
				if (!eachBatchValidation.isEmpty()) {
					outputMap.put("statusCode", SC_BAD_REQUEST + " " + SC_BAD_REQUEST.getStatusCode());
					eachBatchValidation.clear();
				}
				else {
					outputMap.put("statusCode", SC_CREATED + " " + SC_CREATED.getStatusCode());
				}

				if (artefactBatch.getRequestType() != null) {
					outputMap.put("requestType", artefactBatch.getRequestType());
				}
				else {
					outputMap.put("requestType", "null_request_type");
				}

				if (artefactBatch.getUser() != null) {
					outputMap.put("user", artefactBatch.getUser());
				}
				else {
					outputMap.put("user", "Anonymous"); // workaround for bad input
				}

				log.info("STEP 8 Add outputMap in plist");
				plist.add(outputMap);

				distinctArtefactBatch.stream().map(batchInputDynamoDb -> {
					extractedBatchWrite(requestId, artefactBatch, batchInputDynamoDb);
					return null;
				});
			}

			// This is the hot region!
			if (validationReport.isEmpty()) {
				log.info("@@ STEP 9A: saving artefacts ....");
				if (!artefacts.isEmpty()) {
					log.info("..");
					artefactService.saveArtefactsAtomic(artefacts);
					log.info("..artefacts done! ");
				}

				log.info("@@ STEP 9B: distinct batches");
				Collection<BatchInputDynamoDb> batchInputDynamoDbCollection = new ArrayList<>();
				for (BatchInputDynamoDb batch : distinctArtefactBatch) {
					BatchInputDynamoDb batchInputDynamoDbItem = createAndSaveDistinctBatch(requestId, plist, batch);
					batchInputDynamoDbCollection.add(batchInputDynamoDbItem);
				}

				log.info("@@ STEP 9C: saving jobs ....");
				if (!jobs.isEmpty()) {
					log.info("Continue saveJobAtomic ..");
					artefactJobService.saveJobAtomic(jobs);
					log.info("..saveJobAtomic job done! ");
				}
			}

			ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_CREATED,
					new ApiMapBatchResponse(plist, documentFormat));
			log.info("Response created");
			APIGatewayV2HTTPResponse gatewayV2HTTPResponse = ArtefactUploadRequestUtil
				.buildResponse(response.getStatus(), response.getHeaders(), response.getResponse());
			log.info("APIGatewayV2HTTPResponse created");
			return gatewayV2HTTPResponse;
		}
		catch (Exception e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
					e.getMessage(), true);
		}
	}

	private void extractedBatchWrite(String requestId, ArtefactBatch artefactBatch, BatchInputDynamoDb batch) {
		batch.setStatus("INIT");
		batch.setRequestId(requestId);
		log.info("STEP @@@@10000@@@@ Create batch");
		batchService.saveBatchSequenceWithChildren(batch);
	}

	private ArtefactJob createAndSaveJobRecord(String requestId, ArtefactBatch artefactBatch, String artefactId,
			String jobId, String urlstring) {
		ArtefactJob job = new ArtefactJob().withId(jobId)
			.withArtefactId(artefactId)
			.withRequestId(requestId)
			.withBatchSequence(artefactBatch.getBatchSequence())
			.withS3SignedUrl(urlstring)
			.withStatus("INIT");

		// artefactJobService.saveJob(job);

		return job;
	}

	private BatchInputDynamoDb createAndSaveDistinctBatch(String requestId, List<Map<String, Object>> plist,
			BatchInputDynamoDb batch) {
		// log.info("STEP @@@@22220@@@@ Creating batch {}", batch);
		batch.setStatus("INIT");
		batch.setRequestId(requestId);
		batch.setOperator(new Operator().withUsername(plist.get(0).get("user").toString())); // ???
		batch.setRequestType(plist.get(0).get("requestType").toString()); /// ???
		log.info("STEP @@@@33330@@@@ Creating batch {}", batch);
		batchService.saveBatchSequence(batch);
		return batch;
	}

	private List<Map<String, String>> validateArtefactBatch(ArtefactService artefactService,
			List<ArtefactBatch> artefactBatchList) {
		List<Map<String, String>> inputValidationList = new ArrayList<>();
		AtomicReference<Map<String, String>> inputValidation = new AtomicReference<>(new HashMap<>());
		artefactBatchList.forEach(artefactBatch -> {
			inputValidation.set(artefactService.validateArtefactBatch(artefactBatch, artefactBatch.getType()));
			if (!inputValidation.get().isEmpty()) {
				inputValidationList.add(inputValidation.get());
			}
		});
		return inputValidationList;
	}

	private void createAndSaveBatchChildren(String requestId, ArtefactBatch artefactBatch, String artefactId) {

		BatchInputDynamoDb batch = new BatchInputDynamoDb().withBatchSequence(artefactBatch.getBatchSequence());

		ArtefactDynamoDb item = new ArtefactDynamoDb();
		item.setArtefactItemId(artefactId);

		batch.setArtefacts(List.of(item));

		batch.setRequestId(requestId);
		batch.setRequestType(artefactBatch.getRequestType());
		// batch.setCreationDate(new Date(artefactBatch.getCreationDate()));
		// batchService.saveBatchSequence(batch);

		log.info("STEP @@@@ createAndSaveBatchChildren @@@@ Creating batch {}", batch);
		batchService.saveBatchSequenceWithChildren(batch);
	}

}
