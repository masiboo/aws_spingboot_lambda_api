package artefact.usecase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.dto.ArtefactsDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DBAccessService  {

    private static final Logger logger = LoggerFactory.getLogger(DBAccessService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;

    public DBAccessService() {
        this.httpClient = HttpClient.newHttpClient();;
        this.objectMapper = new ObjectMapper();;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.baseUrl = System.getenv("Aws_CORE_DB_ACCESS_API_URL");

    }

    public DBAccessResponse getArtefactsByMirisDocId(String mirisDocId) throws IOException, InterruptedException  {
        if (this.baseUrl == null){
            this.baseUrl = System.getenv("Aws_CORE_DB_ACCESS_API_URL");
        }
        String url = baseUrl + "/api/v1/artefacts/filter?mirisDocId=" + mirisDocId;
        HttpRequest request = buildGetRequest(url);


        try {
            HttpResponse<String> response = sendRequest(request);
            logger.info("response{}", response);

            if (response.statusCode() == 200) {
                List<ArtefactsDTO> artefacts = objectMapper.readValue(response.body(), new TypeReference<List<ArtefactsDTO>>() {});
                return createDBAccessResponse(artefacts);
            } else {
                logger.error("Failed to fetch artefact details: Status Code {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Exception occurred while fetching artefacts by MirisDocId", e);
        }

        return new DBAccessResponse();
    }

    public ArtefactsDTO getArtefactById(String artefactId) throws IOException, InterruptedException   {

        if (this.baseUrl == null){
            this.baseUrl = System.getenv("Aws_CORE_DB_ACCESS_API_URL");
        }

        logger.info("baseUrl {}", baseUrl);
        String url = baseUrl + "/api/v1/artefacts/" + artefactId;
        HttpRequest request = buildGetRequest(url);

        logger.info("request{}", request);

        try {
            HttpResponse<String> response = sendRequest(request);

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<ArtefactsDTO>() {});
            } else {
                logger.error("Failed to fetch artefact details: Status Code {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Exception occurred while fetching artefact by ID", e);
        }

        return null;
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private DBAccessResponse createDBAccessResponse(List<ArtefactsDTO> artefacts) {
        DBAccessResponse dbAccessResponse = new DBAccessResponse();
        dbAccessResponse.setArtefactDBAccesses(artefacts);
        return dbAccessResponse;
    }
}