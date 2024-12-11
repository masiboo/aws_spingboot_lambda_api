package artefact.entrypoints;

import artefact.dto.output.ArtefactOutput;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.CoreRequestHandler;
import artefact.dto.BatchEventDTO;
import artefact.entity.Artefact;
import artefact.usecase.ArtefactServiceInterface;
import artefact.usecase.BatchServiceInterface;
import artefact.usecase.MediaProcessorService;
import artefact.usecase.MetadataValidator;
import artefact.util.ArtefactStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SQSBatchEventHandler extends CoreRequestHandler implements RequestHandler<SQSEvent, String> {

    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();

    private MetadataValidator metadataValidator = new MetadataValidator();

    private MediaProcessorService mediaProcessorService = new MediaProcessorService();


    private static final Logger logger = LoggerFactory.getLogger(SQSBatchEventHandler.class);


    public String handleRequest(SQSEvent s3event, Context context) {

        try {
            logger.info("Event Dump: " + s3event);
            SQSEvent.SQSMessage record = s3event.getRecords().get(0);
            String body  = record.getBody();
            String parsed = gsonGlobal.toJson(body);

            logger.info("parsed Event Dump: " + parsed);

            for (SQSEvent.SQSMessage message : s3event.getRecords()) {
                String messageBody  = message.getBody();
                BatchEventDTO sqsBatchData = gsonGlobal.fromJson(messageBody, BatchEventDTO.class);
                List<ArtefactOutput> batchItems = extractBatchItems(sqsBatchData.getDetail().getBatchSequence());
                if(isMergeEligibleEvent(sqsBatchData, batchItems)){
                    mergeFiles(sqsBatchData.getDetail().getBatchSequence(), batchItems);
                }
            }
        } catch (Exception e) {
            logger.error("error while handling the SQSBatchEvents",e);
            throw new RuntimeException(e);
        }
        return "";

    }

    private void mergeFiles(String batchSeqId, List<ArtefactOutput> batchItems) {
        String AwsCoreMediaService = System.getenv("Aws_CORE_MEDIA_PROCESS_API_URL");
        String AwsCoreApiUrl = AwsCoreMediaService + "/api/v1/merge-files-to-pdf";
        try {
            String requestBody = prepareRequest(batchSeqId,batchItems);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(AwsCoreApiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (JsonProcessingException e) {
            logger.error("Exception while preparing the merge request",e);
            throw new RuntimeException(e);
        } catch (IOException | InterruptedException e) {
            logger.error("Exception while merge api call",e);
            throw new RuntimeException(e);
        }
        logger.info("..CONVERTED");
    }

    private String prepareRequest(String batchSequence,List<ArtefactOutput> batchItems) throws JsonProcessingException {
        ArtefactServiceInterface docService = getAwsServices().documentService();
        String bucket = getAwsServices().documents3bucket();

        List<String> s3Keys = batchItems.stream().map(x-> docService.getArtefactById(x.getId()))
                .map(Artefact::getS3Key)
                .collect(Collectors.toList());

        Map<String, String> metaData = Map.of("batchSequence",batchSequence);
        Map<String,Object> mergeReqMap= Map.of("bucket", bucket,"key", s3Keys,"metadata", metaData);

        var objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(mergeReqMap);
    }

    private boolean isMergeEligibleEvent(BatchEventDTO sqsBatchData,List<ArtefactOutput> batchItems) {
        String batchStatus = sqsBatchData.getDetail().getBatch().getBatchStatus();
        return isEligibleUpdate(batchStatus) && isMergeEligibleArtefact(batchItems);
    }

    private boolean isMergeEligibleArtefact(List<ArtefactOutput> batchItems) {
        if(batchItems == null || batchItems.size() <= 1)
            return false;
        Predicate<ArtefactOutput> isTiffArtefacts = artefact -> artefact.getArtefactName().toLowerCase().endsWith(".tiff")
                || artefact.getArtefactName().toLowerCase().endsWith(".tif");
        return batchItems.stream().allMatch(isTiffArtefacts);
    }

    private List<ArtefactOutput> extractBatchItems(String batchSequence){
        BatchServiceInterface batchService = getAwsServices().batchService();
        return batchService.getAllArtefactsForBatch(batchSequence, "artefact");
    }

    private boolean isEligibleUpdate(String batchStatus) {
        return ArtefactStatus.INSERTED.getStatus().equalsIgnoreCase(batchStatus)
                || ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(batchStatus);
    }
}
