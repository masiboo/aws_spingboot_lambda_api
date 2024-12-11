package org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.errorhandling.MetadataExtractionFailureException;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageResponse;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MergeFilesRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MultimediaFileResponse;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class MediaProcessingServiceImpl implements MediaProcessingService {

	private final ObjectMapper objectMapper;

	private final RestTemplate restTemplate;

	@Override
	public MultimediaFileResponse insertMultimediaMetadata(Map<String, String> bucketDtlMap)
			throws MetadataExtractionFailureException {
		MultimediaFileResponse multimediaFileResponse;
		try {
			String requestBody = objectMapper.writeValueAsString(bucketDtlMap);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI
					.create(SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL + "/api/v1/multimedia/metadata"))
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.header("Content-Type", "application/json")
				.build();
			HttpResponse<String> mediaProcessResp;
			mediaProcessResp = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (mediaProcessResp.statusCode() != 200)
				throw new MetadataExtractionFailureException(
						"Metadata extraction unsuccessful : " + mediaProcessResp.body());

			multimediaFileResponse = objectMapper.readValue(mediaProcessResp.body(), MultimediaFileResponse.class);

		}
		catch (IOException | InterruptedException | MetadataExtractionFailureException e) {
			log.error("Exception while calling mediaprocessing api", e);
			throw new MetadataExtractionFailureException("Metadata extraction failed ", e);
		}
		return multimediaFileResponse;
	}

	@Override
	public String mergeFiles(MergeFilesRequest mergeFilesRequest) {
		try {
			return restTemplate
				.exchange(RequestEntity
					.post(new URI(
							SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL + "/api/v1/merge-files-to-pdf"))
					.body(mergeFilesRequest), String.class)
				.getBody();
		}
		catch (URISyntaxException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public String convertGIFToJPG(ConvertImageRequest request) {
		try {
			return restTemplate
				.exchange(RequestEntity
					.post(new URI(
							SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL + "/api/v1/convert-gif-to-jpg"))
					.body(request), String.class)
				.getBody();
		}
		catch (URISyntaxException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public ConvertImageResponse convertResizeImageToTif(ConvertImageRequest request, String resolutionInDpi) {
		try {
			URI uri = new URI(SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL
					+ "/api/v1/convert-resize-image-to-tif" + "?dpi=" + resolutionInDpi);

			RequestEntity<ConvertImageRequest> requestEntity = RequestEntity.post(uri).body(request);

			ResponseEntity<ConvertImageResponse> responseEntity = restTemplate.exchange(requestEntity,
					ConvertImageResponse.class);

			if (responseEntity.getBody() != null) {
				String statusCode = responseEntity.getBody().getHttpStatus();
				log.info("HTTP response code from responseEntity.getBody().getHttpStatus() : {}", statusCode);
				if (statusCode.contains("201")) {
					responseEntity.getBody().setHttpStatus("201 CREATED");
				}
				else if (statusCode.contains("500")) {
					responseEntity.getBody().setHttpStatus("500 INTERNAL_SERVER_ERROR");
				}
			}
			return responseEntity.getBody();
		}
		catch (URISyntaxException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
