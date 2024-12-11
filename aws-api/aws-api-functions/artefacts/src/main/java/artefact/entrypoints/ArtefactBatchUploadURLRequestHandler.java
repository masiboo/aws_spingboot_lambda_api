package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.*;
import artefact.aws.AwsServiceCache;
import artefact.dto.Operator;
import artefact.dto.input.ArtefactInput;
import artefact.dto.input.ArtefactItemInput;
import artefact.dto.input.BatchInput;
import artefact.dto.input.BatchInputDynamoDb;
import artefact.entity.*;
import artefact.usecase.*;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;
import artefact.util.CsvConverterUtil;
import artefact.util.ScannedAppType;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static artefact.apigateway.ApiResponseStatus.*;
import static artefact.util.AppConstants.*;

public class ArtefactBatchUploadURLRequestHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>, ApiRequestEventUtil {

    private static final int DEFAULT_DURATION_HOURS = 48;
    private static final int SIZE_LIMIT = 1000;
    private static final Logger logger = LoggerFactory.getLogger(ArtefactBatchUploadURLRequestHandler.class);
    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();

	private final String S3_PREFIX = "Aws-";
	private final String MIRIS_DOC_ID_KEY = "mirisDocId";
	private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private MirisDocIdValidatorService validatorService =  new MirisDocIdValidatorService();

    public ArtefactBatchUploadURLRequestHandler() {
    }

    // TODO: 1. single artefact and multiple artefact-items (multiple page tiff)
    // TODO: 2. batch sequence persistence
    // TODO; fetch by batch-sequence-id

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("Event Dump: " + event);
        artefact.entity.DocumentFormatType documentFormat;

        try {
            String scannedApp = null;
            try {
                scannedApp = event.getPathParameters().get("scannedApp");
                logger.info("Document type: " + scannedApp);
            } catch (Exception x) {
                logger.error("error getting  STRING PARAMETER MAP", x);
            }
            if (!ScannedAppType.isAllowedType(scannedApp)) {
                String errorMsg = scannedApp == null
                        ? "Missing 'scannedApp' parameter in path"
                        : "Invalid 'scannedApp' provided :'" + scannedApp + "'  and allowed values are " + Arrays.toString(ScannedAppType.values());
                logger.error(errorMsg);
                return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, errorMsg, true);
            }

            if (event.getBody() == null || event.getBody().isEmpty()) {

                Map<String, Object> map = new HashMap<>();
                map.put("message", "Empty request body");
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                return gatewayV2HTTPResponse;
            }

            Date date = new Date();
            SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
            String shortdate = yyyymmddFormat.format(date);

            logger.info("1a. Loading body: ");
            String jsonString = event.getBody();

            logger.info("Loading body complete String: " + jsonString);
            List<ArtefactBatch> artefactBatchList = new ArrayList<ArtefactBatch>();
            if (jsonString.startsWith("[")) {
                logger.info("INPUT IN JSON FORMAT");
                artefactBatchList = getArtefactBatchesForJson(jsonString);
                documentFormat = DocumentFormatType.JSON;

            } else {
                logger.info("INPUT IN CSV FORMAT");
                artefactBatchList = getArtefactBatchesForCsv(jsonString);
                documentFormat = DocumentFormatType.CSV;
            }


            if (artefactBatchList.size() > SIZE_LIMIT) {

                Map<String, Object> map = new HashMap<>();
                map.put("message", "Too many records. Maximum permitted: " + SIZE_LIMIT);
                ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(map));
                APIGatewayV2HTTPResponse gatewayV2HTTPResponse = null;
                try {
                    gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return gatewayV2HTTPResponse;
            }

            List<Map<String, Object>> plist = new ArrayList<>();
            List<String> unmergedArtefactsId = new ArrayList<>();
            List<String> jobs = new ArrayList<>();

            String requestId = UUID.randomUUID().toString();
            List<Map<String, String>> validationReport = new ArrayList<>();

            for (ArtefactBatch artefactBatch : artefactBatchList) {

                BatchInput batchInput = new BatchInput();

                String artefactId = artefactBatch.getArtefactItemFileName();
                String jobId = UUID.randomUUID().toString();

                String username = getCallingCognitoUsername(event);
                String validation = "OK";
                String urlstring = "";

                //Validate item
                Map<String, String> inputValidation = validateInputDocument(artefactBatch, scannedApp);

                if (!inputValidation.isEmpty()) {
                    validation = "ERROR";
                    validationReport.add(inputValidation);
                    logger.error("Validation errors {}", inputValidation);
                }
                else {

                    // TODO: change to transaction item
                    // Fixme: wrong heirachy!!!!!!!!

                    ArtefactInput artefactInput = new ArtefactInput();
                    ArtefactItemInput artefactItemInput = new ArtefactItemInput();

                    List<ArtefactTag> tags = new ArrayList<>();
                    Map<String, String> map = event.getPathParameters(); // post body conversion !!
                    Map<String, String> query = event.getQueryStringParameters();

                    // TODO: validate JSON body input with JSON schema
                    // TODO: business validation

                    String bucket = getAwsServices().documents3bucket();
                    String mergeId = requestId + artefactBatch.getBatchSequence();
                    String key = S3_PREFIX + shortdate + "/" + mergeId + "/" + artefactBatch.getFilename();

                    ArtefactDynamoDb item = new ArtefactDynamoDb(artefactId, ZonedDateTime.now(), username);
                    item.setBucket(bucket);
                    item.setKey(key);
                    item.setArtefactClassType(artefactBatch.getArtefactClassType());

                    item.setBelongsToArtefactId(artefactBatch.getArtefactMergeId());
                    item.setArtefactContainerName(artefactBatch.getArtefactMergeId());

                    logger.info("unmergedArtefactsId add " + artefactBatch.getArtefactMergeId());
                    boolean result = unmergedArtefactsId.add(artefactBatch.getArtefactMergeId());
                    logger.info("results " + result);


                    item.setBatchSequenceId(artefactBatch.getBatchSequence());
                    item.setFileName(artefactBatch.getFilename());
                    item.setPart(true); // batch upload page by page
                    item.setPageNumber(artefactBatch.getPage());

                    item.setArtefactMergeId(artefactBatch.getArtefactMergeId());

                    item.setArtefactItemFileName(artefactBatch.getArtefactItemFileName());
                    item.setTotalPages(String.valueOf(artefactBatchList.size()));

                    item.setScannedType(ScannedAppType.forTypeIgnoreCase(scannedApp));
                    if (ScannedAppType.ADDENDUM.toString().equalsIgnoreCase(scannedApp)) {
                        item.setMirisDocId(artefactBatch.getMirisDocId());
                    }

                    tags.add(new ArtefactTag(artefactId, "untagged", "true", ZonedDateTime.now(), username,
                            DocumentTagType.SYSTEMDEFINED)); // add artefact metadata here..

                    ArtefactServiceInterface service = getAwsServices().documentService();
                    service.saveDocument(item, tags); // save transaction

                    Map<String, String> metadata = new HashMap<>();
                    metadata.put(METADATA_KEY_ARTEFACT_ID, artefactId);
                    metadata.put(METADATA_KEY_TRACE_ID, jobId);
                    if (StringUtils.isNotBlank(artefactBatch.getMirisDocId()))
                        metadata.put(METADATA_KEY_MIRIS_DOCID, artefactBatch.getMirisDocId());
                    if (StringUtils.isNotBlank(artefactBatch.getBatchSequence()))
                        metadata.put(METADATA_KEY_BATCH_SEQ, artefactBatch.getBatchSequence());

                    urlstring = generatePresignedUrl(getAwsServices(), key, query, metadata);
                    ArtefactJobServiceInterface jobService = getAwsServices().jobService();

                    createAndSaveJobRecord(requestId, artefactBatch, artefactId, jobId, urlstring, jobService);

                    logger.info("STEP 5");
                    createAndSaveBatchNodes(requestId, artefactBatch, artefactId, jobId);

                }

                logger.info("STEP 12");
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
						if( inputValidation.get(MIRIS_DOC_ID_KEY) == null ) {
							outputMap.put(MIRIS_DOC_ID_KEY, artefactBatch.getMirisDocId());
						}else{
							outputMap.put(MIRIS_DOC_ID_KEY, inputValidation.get(MIRIS_DOC_ID_KEY));
						}
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


                if (artefactBatch.getRequestType() != null){
                    outputMap.put("requestType", artefactBatch.getRequestType());
                } else {
                    outputMap.put("requestType", "Req"); // workaround for bad input
                }

                if (artefactBatch.getUser() != null) {
                    outputMap.put("user", artefactBatch.getUser());
                } else {
                    outputMap.put("user", "Anonymous"); // workaround for bad input
                }

                logger.info("STEP 13");
                plist.add(outputMap);
            }

            logger.info("validationReport" + validationReport.toString());
            logger.info("jobs" + jobs.toString());
            logger.info("unmergedArtefactsId" + unmergedArtefactsId.toString());
            logger.info("plist " + plist.toString());
            if (validationReport.isEmpty()) {
                BatchServiceInterface batchService = getAwsServices().batchService();
                createAndSaveBatchRecord(requestId, plist, unmergedArtefactsId, jobs, batchService,
                        ScannedAppType.forTypeIgnoreCase(scannedApp));
            }

            // return even on error.
            ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_CREATED, new ApiMapBatchResponse(plist, documentFormat));
            logger.info("Response created");
            APIGatewayV2HTTPResponse gatewayV2HTTPResponse = buildResponse(logger, response.getStatus(), response.getHeaders(), response.getResponse());
            logger.info("APIGatewayV2HTTPResponse created");
            return gatewayV2HTTPResponse;

        } catch (Exception e) {
            logger.error(e.getMessage());
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                    "Error: " + e.getMessage(), true);
        }
    }



    public static List<ArtefactBatch> getArtefactBatchesForCsv(String jsonString) {
        List<ArtefactBatch> artefactBatchList;
        try {
            artefactBatchList = (List<ArtefactBatch>) (Object) CsvConverterUtil.convertCSVClass(jsonString, ArtefactBatch.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return artefactBatchList;
    }

    public static List<ArtefactBatch> getArtefactBatchesForJson(String jsonString) throws JsonProcessingException {
        List<ArtefactBatch> artefactBatchList;
        TypeReference<List<ArtefactBatch>> jacksonTypeReference = new TypeReference<List<ArtefactBatch>>() {};
        ObjectMapper mapper = new ObjectMapper();
        artefactBatchList = mapper.readValue(jsonString, jacksonTypeReference);
        return artefactBatchList;
    }

    private static void createAndSaveJobRecord(String requestId, ArtefactBatch artefactBatch, String artefactId,
                                               String jobId, String urlstring, ArtefactJobServiceInterface jobService) {
        ArtefactJob job = new ArtefactJob().withId(jobId)
                .withArtefactId(artefactId)
                .withRequestId(requestId)
                .withBatchSequence(artefactBatch.getBatchSequence())
                .withS3SignedUrl(urlstring)
                .withStatus("INIT");
        jobService.saveJob(job);
    }

    private static void createAndSaveBatchRecord(String requestId,  List<Map<String, Object>> plist,
                                                 List<String> unmergedArtefactsId, List<String> jobs,
                                                 BatchServiceInterface batchService,  ScannedAppType scanType) {

        BatchInputDynamoDb batch = new BatchInputDynamoDb()
                .withBatchSequence(plist.get(0).get("batchSequence").toString());
        batch.setUnmergedArtefacts(unmergedArtefactsId);
        batch.setJobs(jobs);
        batch.setStatus("INIT");
        batch.setRequestId(requestId);
        batch.setOperator(new Operator().withUsername(plist.get(0).get("user").toString()));
        batch.setRequestType(plist.get(0).get("requestType").toString());
        batch.setScannedType(scanType);

        // save validation error state..
        batchService.saveBatchSequence(batch);

    }

    private static void createAndSaveBatchChildren(String requestId,  List<Map<String, Object>> plist,
                                                 List<String> unmergedArtefactsId, List<String> jobs,
                                                 BatchServiceInterface batchService,  ScannedAppType scanType) {

        BatchInputDynamoDb batch = new BatchInputDynamoDb()
                .withBatchSequence(plist.get(0).get("batchSequence").toString());
        batch.setUnmergedArtefacts(unmergedArtefactsId);
        batch.setJobs(jobs);
        batch.setStatus("INIT");
        batch.setRequestId(requestId);
        batch.setOperator(new Operator().withUsername(plist.get(0).get("user").toString()));
        batch.setRequestType(plist.get(0).get("requestType").toString());
        batch.setScannedType(scanType);

        // save validation error state..

//        batchService.saveBatchSequence(batch);
        batchService.saveBatchSequenceWithChildren(batch);

    }

    private void createAndSaveBatchNodes(String requestId, ArtefactBatch artefactBatch, String artefactId, String jobId) {
        BatchServiceInterface batchService = getAwsServices().batchService();
        BatchInputDynamoDb batch = new BatchInputDynamoDb()
                .withBatchSequence(artefactBatch.getBatchSequence());

        batch.setArtefactId(artefactId); // using artefact name

        batch.setJobId(jobId);
        batch.setStatus("INIT");
        batch.setRequestId(requestId);
        batch.setOperator(new Operator().withUsername(artefactBatch.getUser()));
        batch.setRequestType(artefactBatch.getRequestType());
//					batch.setCreationDate(new Date(artefactBatch.getCreationDate()));
//                  batchService.saveBatchSequence(batch);
        batchService.saveBatchSequenceWithChildren(batch);
    }
    private Artefact createArtefact(APIGatewayV2HTTPEvent event) {
        String artefactId = UUID.randomUUID().toString();
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

        return contentLength != null ? Optional.of(contentLength) : Optional.empty();
    }

    private Duration caculateDuration(final Map<String, String> query) {

        Integer durationHours =
                query != null && query.containsKey("duration") ? Integer.valueOf(query.get("duration"))
                        : Integer.valueOf(DEFAULT_DURATION_HOURS);

        Duration duration = Duration.ofHours(durationHours.intValue());
        return duration;
    }

    public Map<String, String> validateInputDocument(final ArtefactBatch document, String scanedApp) {

        Map<String, String> errorMessage = new HashMap<>();

        if (document.getContentType() == null || document.getContentType().isBlank()) {
            errorMessage.put("contentType", "must be present");

        } else if (Arrays.stream(DocumentContentType.values()).noneMatch(t -> t.name().equalsIgnoreCase(document.getContentType()))) {
            errorMessage.put("contentType", "not valid");
        }

        if (document.getFilename() == null || document.getFilename().isBlank()) {
            errorMessage.put("filename", "must be present");
        }

        if (document.getBatchSequence() == null || document.getBatchSequence().isBlank()) {
            errorMessage.put("batchsequence", "must be present");
        }

        if (Arrays.stream(ArtefactValidation.ArtefactClassType.values()).noneMatch(t -> t.name().equalsIgnoreCase(document.getArtefactClassType()))) {
            errorMessage.put("artefactClassType", "ArtefactClassType invalid value.");
        }

        if (ScannedAppType.ADDENDUM.toString().equalsIgnoreCase(scanedApp)) {
            if (document.getMirisDocId() == null || document.getMirisDocId().isBlank()) {
                errorMessage.put(MIRIS_DOC_ID_KEY, "must be present");
            }
            // Calling Aws-core proxy service to validate the mirisDocId
            if(StringUtils.isNotBlank(document.getMirisDocId())
                    && Boolean.parseBoolean(System.getenv().get("MIRIS_CHECK_ENABLED")) ) {
                boolean isValid = validatorService.isValid(document.getMirisDocId());
                if(!isValid){
                    errorMessage.put(MIRIS_DOC_ID_KEY, "Invalid mirisDocId");
                }
            }
        }

        if (ScannedAppType.NEW_REQUEST.toString().equalsIgnoreCase(scanedApp)) {
            if (document.getUser() == null || document.getUser().isBlank()) {
                errorMessage.put("Author", "must be present");
            }
        }
        return errorMessage;
    }
}