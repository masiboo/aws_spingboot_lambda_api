package artefact.entrypoints;

import artefact.aws.s3.S3ObjectMetadata;
import artefact.dto.MultimediaFileResponse;
import artefact.dto.S3EventDTO;
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
import artefact.aws.S3Service;
import artefact.dto.input.BatchInputDynamoDb;
import artefact.usecase.*;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static artefact.util.AppConstants.*;
import static software.amazon.awssdk.utils.StringUtils.isNotBlank;

public class SQSEventTriggerHandler extends CoreRequestHandler implements RequestHandler<SQSEvent, String> {

    private static final float MAX_WIDTH = 100;
    private static final float MAX_HEIGHT = 100;
    private final String JPG_TYPE = (String) "jpg";
    private final String JPG_MIME = (String) "image/jpeg";
    private final String PNG_TYPE = (String) "png";
    private final String TIF_TYPE = (String) "TIF";

    private final String PNG_MIME = (String) "image/png";


    private final String PDF_TYPE = (String) "pdf";
    private final String PDF_MIME = (String) "pdf ";

    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();

    private MetadataValidator metadataValidator = new MetadataValidator();

    private MediaProcessorService mediaProcessorService = new MediaProcessorService();


    private static final Logger logger = LoggerFactory.getLogger(SQSEventTriggerHandler.class);


    public String handleRequest(SQSEvent s3event, Context context) {

        try {

            logger.info("Event Dump: " + s3event);
            SQSEvent.SQSMessage record = s3event.getRecords().get(0);
            String body  = record.getBody();
            String parsed = gsonGlobal.toJson(body);

            logger.info("parsed Event Dump: " + parsed);

            for (SQSEvent.SQSMessage message : s3event.getRecords()) {

                String messageBody  = message.getBody();
                S3EventDTO payloadDto = gsonGlobal.fromJson(messageBody, S3EventDTO.class);

                String srcBucket = payloadDto.getDetail().getBucket().getName();
                String srcKey = payloadDto.getDetail().getObject().getKey();

                S3Service s3Service = getAwsServices().s3Service();

                try (S3Client s3 = s3Service.buildClient()) {
                    S3ObjectMetadata md = s3Service.getObjectMetadata(s3, srcBucket, srcKey);

                    if (md.isObjectExists()) {

                        logger.info("object tgs " + md.getMetadata().toString());
                        logger.info("artefact id: " + md.getMetadata().get(METADATA_KEY_ARTEFACT_ID));
                        logger.info("job id: " + md.getMetadata().get(METADATA_KEY_TRACE_ID));

                        ArtefactJobServiceInterface jsi = getAwsServices().jobService();
                        ArtefactServiceInterface asi = getAwsServices().documentService();
                        BatchServiceInterface batchService = getAwsServices().batchService();

                        String status = null;
                        String jobStatus;
                        if(!metadataValidator.isValid(md.getMetadata())){
                            logger.error("Metadata validation failed artefact&Job status will become ERROR");
                            status = jobStatus = ArtefactStatus.ERROR.getStatus();
                        }else {
                            // Since the validation has been done already either mirisDocId or batchSeqId will be present
                            if(isNotBlank(md.getMetadata().get(METADATA_KEY_MIRIS_DOCID))){
                                status = ArtefactStatus.INDEXED.getStatus();
                            }else if(isNotBlank(md.getMetadata().get(METADATA_KEY_BATCH_SEQ))) {
                                status = ArtefactStatus.INSERTED.getStatus();
                            }


                            jobStatus = ArtefactStatus.UPLOADED.getStatus();

                            // update S3Object metadata tags for media items
                            String artefactId = md.getMetadata().get(METADATA_KEY_ARTEFACT_ID);
                            String mediaType = asi.getArtefactById(artefactId).getArtefactClassType();

                            List<String> mediaTypes  = List.of("SOUND","BWLOGO", "COLOURLOGO","MULTIMEDIA");
                            if(mediaTypes.stream().anyMatch(type -> type.equalsIgnoreCase(mediaType))) {
                                Map<String,String> bucketDtlMap = Map.of("bucket", srcBucket,"key", srcKey);
                                updateArtefactAndTags(s3,md,bucketDtlMap,mediaType);
                            }

                        }

                        convertTiffToPdf(srcBucket, srcKey, md, batchService);

                        jsi.updateJobWithStatus(md.getMetadata().get(METADATA_KEY_TRACE_ID), jobStatus);
                        asi.updateArtefactWithStatus(md.getMetadata().get(METADATA_KEY_ARTEFACT_ID), status);
                    }
                }

            }

            // TODO : Run Task on ECS Media Processor (check for batch sequence?)
            // TODO : EVENTBUS event publish
            return "";

        } catch (Exception e) {
            logger.error("error while handling the SQSEvents",e);
            throw new RuntimeException(e);
        }
    }

    /*

    {
           "bucket": "",
            "key": [
                    ""
               ],
            "metadata": {
                "artifactId": ""
              }
     }
     */

    private String convertTiffToPdf(String srcBucket, String srcKey,
                                    S3ObjectMetadata md, BatchServiceInterface batchService ) throws IOException, InterruptedException {

        String suffix = null;

//        Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(srcKey);
//        if (!matcher.matches()) {
//            System.out.println("Unable to infer image type for key "
//                    + srcKey);
//            return "";
//        }
//        String imageType = matcher.group(0);
//        if (!(JPG_TYPE.equals(imageType)) && !(PNG_TYPE.equals(imageType))) {
//            System.out.println("Skipping non-image " + srcKey);
//            return "";
//        }


        // Aws-2023-04-18/20221124.009-0002D/00020000.TIF
        String[] parts = srcKey.split("/");
        if (parts.length > 0) {
            String fileName = parts[parts.length - 1];

            String[] fileArray = fileName.split(".");
            suffix = parts[parts.length - 1];
        }

        if(Objects.equals(suffix, "TIF")) {

            logger.info("CONVERT...");

            String AwsCoreMediaService = System.getenv("Aws_CORE_MEDIA_PROCESS_API_URL");
            String AwsCoreApiUrl = AwsCoreMediaService + "/api/v1/convert/tiff/to/pdf";
            String requestBody = prepareRequest(srcBucket, srcKey, md.getMetadata().get(METADATA_KEY_ARTEFACT_ID), md.getMetadata().get(METADATA_KEY_BATCH_SEQ) );
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(AwsCoreApiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("..CONVERTED");

        } else {

            logger.info("STEP 5 PDF Converted file saving Event only for Batch Uploads");

            if(isNotBlank(md.getMetadata().get(METADATA_KEY_BATCH_SEQ))) {

                BatchInputDynamoDb batch = new BatchInputDynamoDb()
                        .withBatchSequence(md.getMetadata().get(METADATA_KEY_BATCH_SEQ));
                batch.setArtefactId(md.getMetadata().get(METADATA_KEY_ARTEFACT_ID)); // using artefact name
                batch.setStatus("INSERTED");
//                              baetRequestId(requestId);
//                              batch.setOperator(new Operator().withUsername(artefactBatch.getUser()));
//                              batch.setRequestType(artefactBatch.getRequestType());
//						        batch.setCreationDate(new Date(artefactBatch.getCreationDate()));

                batchService.saveBatchSequence(batch);
                batchService.saveBatchSequenceWithChildren(batch);

                // create artefact record
//                IArtefact item = new ArtefactDynamoDb(artefactId, date, username);
            }


            return "";

        }

        return "";
    }

    private String prepareRequest(String bucketKey, String objectKey, String artefactId, String batchSequenceId) throws JsonProcessingException {

        var key  = new ArrayList<String>();
        List<String> keys = Collections.singletonList(objectKey);
        HashMap<String, Object> metaData =  new HashMap<>() {
            {
                put("artifactId", artefactId);
                put("batchSequenceId", batchSequenceId);
            }
        };
        var values = new HashMap<String, Object>() {
            {
                put("bucket", bucketKey);
                put("key", keys);
                put("metadata", metaData);

            }
        };

        var objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(values);
    }

    private void updateArtefactAndTags(S3Client s3, S3ObjectMetadata md,Map<String,String> bucketDtlMap,String mediaType) throws IOException, InterruptedException {
        Map<String, String> objectTagMap = new HashMap<>();
        objectTagMap.put("fileType",md.getContentType());
        objectTagMap.put("size",md.getContentLength().toString());
        objectTagMap.put("mediaType",mediaType);

//         TODO uncomment the API call once the Aws-CORE is ready
//         invoke Aws-core to calculate bit depth for sound items
        if("SOUND".equalsIgnoreCase(mediaType) || "MULTIMEDIA".equalsIgnoreCase(mediaType) ){
            MultimediaFileResponse mediaProcessResp = mediaProcessorService.process(bucketDtlMap);
            if(mediaProcessResp != null && mediaProcessResp.getS3ObjectTags() != null){
                if("SOUND".equalsIgnoreCase(mediaType)){
                    // Tags for Audio items
                    String bit_depth = mediaProcessResp.getS3ObjectTags().getBitDepth();
                    String samplingFrequency = mediaProcessResp.getS3ObjectTags().getSamplingFrequency();
                    objectTagMap.put("bit_depth", bit_depth);
                    objectTagMap.put("sampling_frequency", samplingFrequency);
                }else if ("BWLOGO".equalsIgnoreCase(mediaType) || "COLOURLOGO".equalsIgnoreCase(mediaType)) {
                    String resolutionInDpi = mediaProcessResp.getS3ObjectTags().getResolutionInDpi();
                    objectTagMap.put("resolutionInDpi", resolutionInDpi);
                }
                else {
                    // Tags for Audio items
                    String format = mediaProcessResp.getS3ObjectTags().getFormat();
                    String codec = mediaProcessResp.getS3ObjectTags().getCodec();
                    String frameRate = mediaProcessResp.getS3ObjectTags().getFrameRate();
                    objectTagMap.put("format", format);
                    objectTagMap.put("codec", codec);
                    objectTagMap.put("frameRate", frameRate);
                }
                logger.info("Invoked Aws-core to extract the media inforomation and updated s3tags");
                getAwsServices().s3Service().setObjectTag(s3, bucketDtlMap.get("bucket"),bucketDtlMap.get("key"),
                        objectTagMap);
                String artefactId = md.getMetadata().get(METADATA_KEY_ARTEFACT_ID);
                getAwsServices().documentService().updateArtefact(artefactId,objectTagMap);
            }else{
                logger.error("Unable to process and extract information from the media file");
            }
        }
    }

}
