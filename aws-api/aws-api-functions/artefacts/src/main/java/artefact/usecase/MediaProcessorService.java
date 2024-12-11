package artefact.usecase;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import artefact.dto.MultimediaFileResponse;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
public class MediaProcessorService {

    /**
     * This method is to process the media file uploaded in s3 and extract the following information
     * <br>1.bitdepth
     * <br>2.sampling frequency
     *
     * @param bucketDtlMap
     * @return MultimediaFileResponse
     */
    public MultimediaFileResponse process(Map<String, String> bucketDtlMap) {
        MultimediaFileResponse multimediaFileResponse = null;
        String AwsCoreMediaService = System.getenv("Aws_CORE_MEDIA_PROCESS_API_URL");
        String AwsCoreApiUrl = AwsCoreMediaService + "/api/v1/multimedia/metadata";
        String requestBody = null;
        try {
            requestBody = prepareRequest(bucketDtlMap);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AwsCoreApiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> mediaProcessResp = null;
            mediaProcessResp = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (mediaProcessResp.statusCode() == HttpStatusCode.OK) {
                multimediaFileResponse = new Gson().fromJson(mediaProcessResp.body(), MultimediaFileResponse.class);
            } else {
                log.error("Unable to process the media file {}", mediaProcessResp.body());
            }
        } catch (JsonProcessingException e) {
            log.error("Unable to prepare the request to mediaprocessing api ", e);
        } catch (IOException e) {
            log.error("Exception while making mediaprocessing api ", e);
        } catch (InterruptedException e) {
            log.error("Exception while making mediaprocessing api ", e);
        }
        return multimediaFileResponse;
    }



    private String prepareRequest(Map<String, String> bucketDtlMap) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(bucketDtlMap);
    }
}
