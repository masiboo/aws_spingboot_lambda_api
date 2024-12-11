package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.CoreRequestHandler;
import artefact.aws.S3Service;
import artefact.aws.s3.S3ObjectMetadata;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.usecase.ArtefactServiceInterface;
import artefact.usecase.MediaProcessorService;
import artefact.usecase.MetadataValidator;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static artefact.util.AppConstants.*;
import static software.amazon.awssdk.utils.StringUtils.isNotBlank;

public class S3EventTriggerHandler extends CoreRequestHandler implements RequestHandler<S3Event, String> {

    private static final float MAX_WIDTH = 100;
    private static final float MAX_HEIGHT = 100;
    private final String JPG_TYPE = (String) "jpg";
    private final String JPG_MIME = (String) "image/jpeg";
    private final String PNG_TYPE = (String) "png";
    private final String PNG_MIME = (String) "image/png";


    private final String PDF_TYPE = (String) "pdf";
    private final String PDF_MIME = (String) "pdf ";

    Gson gsonGlobal = new GsonBuilder().setPrettyPrinting().create();

    private MetadataValidator metadataValidator = new MetadataValidator();

    private MediaProcessorService mediaProcessorService = new MediaProcessorService();

    private static final Logger logger = LoggerFactory.getLogger(S3EventTriggerHandler.class);


    public String handleRequest(S3Event s3event, Context context) {

        try {

            logger.info("Event json Dump: " + gsonGlobal.toJson(s3event));
            logger.info("Event Dump: " + s3event);

            S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);

            String srcBucket = record.getS3().getBucket().getName();

            // Object key may have spaces or unicode non-ASCII characters.
            String srcKey = record.getS3().getObject().getUrlDecodedKey();

            S3Service s3Service = getAwsServices().s3Service();
            try (S3Client s3 = s3Service.buildClient()) {
                S3ObjectMetadata md = s3Service.getObjectMetadata(s3, srcBucket, srcKey);

                if (md.isObjectExists()) {

                    logger.info("object tgs " + md.getMetadata().toString());
                    logger.info("artefact id: " + md.getMetadata().get(METADATA_KEY_ARTEFACT_ID));
                    logger.info("job id: " + md.getMetadata().get(METADATA_KEY_TRACE_ID));

                    ArtefactJobServiceInterface jsi = getAwsServices().jobService();
                    ArtefactServiceInterface asi = getAwsServices().documentService();

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

                        List<String> mediaTypes  = List.of("SOUND","LOGO","MULTIMEDIA");
                        if(mediaTypes.stream().anyMatch(type -> type.equalsIgnoreCase(mediaType))) {
                            Map<String,String> bucketDtlMap = Map.of("bucket", srcBucket,"key", srcKey);
                            updateArtefactAndTags(s3,md,bucketDtlMap,mediaType);
                        }
                    }

                    jsi.updateJobWithStatus(md.getMetadata().get(METADATA_KEY_TRACE_ID), jobStatus);
                    asi.updateArtefactWithStatus(md.getMetadata().get(METADATA_KEY_ARTEFACT_ID), status);
                }
            }

//                GetObjectTaggingRequest getTaggingRequest = GetObjectTaggingRequest.builder().key(keyName).bucket(bucketName).build();
//
//                GetObjectTaggingResponse tags = getAwsServices().s3Service().getObjectTags(getAwsServices().) s3.getObjectTagging(getTaggingRequest);
//                List<Tag> tagSet= tags.tagSet();
//                for (Tag tag : tagSet) {
//                    System.out.println(tag.key());
//                    System.out.println(tag.value());
//                }

            // Infer the image type.
            Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(srcKey);
            if (!matcher.matches()) {
                System.out.println("Unable to infer image type for key "
                        + srcKey);
                return "";
            }
            String imageType = matcher.group(0);
            if (!(JPG_TYPE.equals(imageType)) && !(PNG_TYPE.equals(imageType))) {
                System.out.println("Skipping non-image " + srcKey);
                return "";
            }

            return "";

        } catch (Exception e) {
            logger.error("error while handling the s3Events",e);
            throw new RuntimeException(e);
        }
    }

    private void updateArtefactAndTags(S3Client s3, S3ObjectMetadata md,Map<String,String> bucketDtlMap,String mediaType) throws IOException, InterruptedException {
        Map<String, String> objectTagMap = new HashMap<>();
        objectTagMap.put("fileType",md.getContentType());
        objectTagMap.put("size",md.getContentLength().toString());
        objectTagMap.put("mediaType",mediaType);

//         TODO uncomment the API call once the Aws-CORE is ready
//         invoke Aws-core to calculate bit depth for sound items
//        if("SOUND".equalsIgnoreCase(mediaType) || "MULTIMEDIA".equalsIgnoreCase(mediaType) ){
//            MultimediaFileResponse  mediaProcessResp = mediaProcessorService.process(bucketDtlMap);
//            if(mediaProcessResp != null && mediaProcessResp.getS3ObjectTags() != null){
//                if("SOUND".equalsIgnoreCase(mediaType)){
//                    // Tags for Audio items
//                    String bit_depth = mediaProcessResp.getS3ObjectTags().getBitDepth();
//                    String samplingFrequency = mediaProcessResp.getS3ObjectTags().getSamplingFrequency();
//                    objectTagMap.put("bit_depth", bit_depth);
//                    objectTagMap.put("sampling_frequency", samplingFrequency);
//                }else {
//                    // Tags for Audio items
//                    String format = mediaProcessResp.getS3ObjectTags().getFormat();
//                    String codec = mediaProcessResp.getS3ObjectTags().getCodec();
//                    String frameRate = mediaProcessResp.getS3ObjectTags().getFrameRate();
//                    objectTagMap.put("format", format);
//                    objectTagMap.put("codec", codec);
//                    objectTagMap.put("frameRate", frameRate);
//                }
//                logger.info("Invoked Aws-core to extract the media inforomation and updated s3tags");
//            }else{
//                logger.error("Unable to process and extract information from the media file");
//            }
//        }

        // TODO remove sample value for bit_depth and sampling_frequency once Aws-CORE API is deployed
        objectTagMap.put("bit_depth", "32767");
        objectTagMap.put("sampling_frequency","48 kHz");

        getAwsServices().s3Service().setObjectTag(s3, bucketDtlMap.get("bucket"),bucketDtlMap.get("key"),
                objectTagMap);
        String artefactId = md.getMetadata().get(METADATA_KEY_ARTEFACT_ID);
        getAwsServices().documentService().updateArtefact(artefactId,objectTagMap);
    }
}
