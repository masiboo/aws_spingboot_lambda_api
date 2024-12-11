package artefact.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import lombok.extern.slf4j.Slf4j;
import artefact.aws.AwsServiceCache;
import artefact.entity.Artefact;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class S3Util {

    public static boolean bucketExists(AwsServiceCache awsServiceCache, final String bucket) {
        try {
            awsServiceCache.s3Client().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket exists: {}", bucket);
            return true;
        } catch (S3Exception e) {
            DebugInfoUtil.logExceptionDetails(e);
            if (e.statusCode() == 404) {
                log.warn("S3 bucket does not exist: {}", bucket);
                return false;
            } else {
                log.error("Error checking bucket existence for {}: {}", bucket, e.awsErrorDetails().errorMessage());
                throw e;
            }
        }
    }

    public static boolean doesObjectExist(AwsServiceCache awsServiceCache, Artefact artefact) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(artefact.gets3Bucket())
                    .key(artefact.getS3Key())
                    .build();
            HeadObjectResponse response = awsServiceCache.s3Client().headObject(headObjectRequest);
            return response != null; 
        } catch (S3Exception e) {
            DebugInfoUtil.logExceptionDetails(e);
            if (e.statusCode() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public static Boolean isFileExist(AwsServiceCache awsServiceCache, String s3Bucket, String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Bucket)
                .key(s3Key)
                .build();
        try (ResponseInputStream<GetObjectResponse> response = awsServiceCache.s3Client().getObject(request)) {
            log.info("Content-Type: {}", response.response().contentType());
            log.info("Content-Length: {}", response.response().contentLength());
            log.info("File exists in S3.");
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("File does not exist in S3 bucket.");
                return false;
            } else {
                log.error("Failed to check file in S3. HTTP Status Code: {}", e.statusCode());
                return false;
            }
        } catch (IOException e) {
            log.error("I/O error while reading S3 object content.");
            throw new RuntimeException("I/O error while reading S3 object content.", e);
        }
    }

    public static String  generatePresignedUrl(AwsServiceCache awsServiceCache, Artefact artefact, Duration duration, String version) {
        URL url = awsServiceCache.s3Service().presignGetUrl(artefact.gets3Bucket(), artefact.getS3Key(), duration, version);
        String urlstring = url.toString();
        return urlstring;
    }

    public static int getDurationHours(final APIGatewayV2HTTPEvent event) {
        final int defaultDurationHours = 1;

        Map<String, String> map =
                event.getQueryStringParameters() != null ? event.getQueryStringParameters()
                        : new HashMap<>();
        String durationHours = map.getOrDefault("duration", "" + defaultDurationHours);

        try {
            return Integer.parseInt(durationHours);
        } catch (NumberFormatException e) {
            return defaultDurationHours;
        }
    }


    public static boolean verifyS3UrlForObjectExist(String s3url) {
        try {
            URL url = new URL(s3url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            log.warn("IOException {}",e.getMessage());
            return false;
        }
    }

}
