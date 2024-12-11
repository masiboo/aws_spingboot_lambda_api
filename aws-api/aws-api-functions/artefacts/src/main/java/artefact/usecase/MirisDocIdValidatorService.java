package artefact.usecase;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class MirisDocIdValidatorService {

    private final static Integer MIRIS_DOC_ID_MINIMUM_LENGTH = 5;

    private final static Integer MIRIS_DOC_ID_MAXIMUM_LENGTH = 8;
    public boolean isValid(String docId) {
        if (docId == null || docId.length() < MIRIS_DOC_ID_MINIMUM_LENGTH
                || docId.length() > MIRIS_DOC_ID_MAXIMUM_LENGTH) {
            return false;
        }
        String mirisCheckApiUrl = System.getenv("Aws_CORE_MIRIS_CHECK_API_URL");
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mirisCheckApiUrl + "/" + docId))
                    .GET()
                    .build();
            HttpResponse<String> validatorResp = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Boolean.parseBoolean(validatorResp.body());
        } catch (IOException | InterruptedException e) {
            log.error("Exception while validating the mirisDocId",e);
        }
        return false;
    }
}
