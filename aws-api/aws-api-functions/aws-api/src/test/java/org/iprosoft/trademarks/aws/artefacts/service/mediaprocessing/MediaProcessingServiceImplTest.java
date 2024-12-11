package org.wipo.trademarks.Aws.artefacts.service.mediaprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.EasyMock;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageResponse;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.wipo.trademarks.Aws.artefacts.configuration.SystemEnvironmentVariables;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MediaProcessingServiceImplTest {

	private MediaProcessingServiceImpl mediaProcessingService;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		restTemplate = EasyMock.createMock(RestTemplate.class);
		ObjectMapper objectMapper = new ObjectMapper();
		mediaProcessingService = new MediaProcessingServiceImpl(objectMapper, restTemplate);

	}

	@Test
	void testConvertResizeImageToTifWith500() throws URISyntaxException {
		// Arrange
		String expectedSignedS3Url = "http://signedS3Url.com/image.tif";
		String expectedTifFile = "test.tif";
		String expectedMediaType = "COLORLOGO";

		ConvertImageRequest convertImageRequest = new ConvertImageRequest();
		convertImageRequest.setBucket(TestData.S3_BUCKET_NAME);
		convertImageRequest.setKey(TestData.S3_BUCKET_KEY);
		Map<String, String> metaData = Map.of("artefactId", expectedTifFile, "bitDepth", "24", "resolutionInDpi", "266",
				"fileType", "tif", "mediaType", expectedMediaType);
		ConvertImageResponse convertImageResponse = ConvertImageResponse.builder()
			.signedS3Url(expectedSignedS3Url)
			.metaData(metaData)
			.httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString())
			.errorMessage("Internal sever error")
			.build();

		ConvertImageRequest request = new ConvertImageRequest();
		request.setBucket(TestData.S3_BUCKET_NAME);
		request.setKey(TestData.S3_BUCKET_KEY);
		String resolutionInDpi = "300";
		URI uri = new URI(SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL
				+ "/api/v1/convert-resize-image-to-tif" + "?dpi=" + resolutionInDpi);
		RequestEntity<ConvertImageRequest> requestEntity = RequestEntity.post(uri).body(request);
		ResponseEntity<ConvertImageResponse> responseEntity = new ResponseEntity<>(convertImageResponse,
				HttpStatus.INTERNAL_SERVER_ERROR);

		expect(restTemplate.exchange(eq(requestEntity), eq(ConvertImageResponse.class))).andReturn(responseEntity);
		replay(restTemplate);

		ConvertImageResponse response = mediaProcessingService.convertResizeImageToTif(request, resolutionInDpi);

		assertNotNull(response);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getHttpStatus());
		verify(restTemplate);
	}

	@Test
	void testConvertResizeImageToTifWith201() throws URISyntaxException {
		// Arrange
		String expectedSignedS3Url = "http://signedS3Url.com/image.tif";
		String expectedTifFile = "test.tif";
		String expectedMediaType = "COLORLOGO";

		ConvertImageRequest convertImageRequest = new ConvertImageRequest();
		convertImageRequest.setBucket(TestData.S3_BUCKET_NAME);
		convertImageRequest.setKey(TestData.S3_BUCKET_KEY);
		Map<String, String> metaData = Map.of("artefactId", expectedTifFile, "bitDepth", "24", "resolutionInDpi", "266",
				"fileType", "tif", "mediaType", expectedMediaType);
		ConvertImageResponse convertImageResponse = ConvertImageResponse.builder()
			.signedS3Url(expectedSignedS3Url)
			.metaData(metaData)
			.httpStatus(HttpStatus.CREATED.toString())
			.errorMessage("Internal sever error")
			.build();

		ConvertImageRequest request = new ConvertImageRequest();
		request.setBucket(TestData.S3_BUCKET_NAME);
		request.setKey(TestData.S3_BUCKET_KEY);
		String resolutionInDpi = "300";
		URI uri = new URI(SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL
				+ "/api/v1/convert-resize-image-to-tif" + "?dpi=" + resolutionInDpi);
		RequestEntity<ConvertImageRequest> requestEntity = RequestEntity.post(uri).body(request);
		ResponseEntity<ConvertImageResponse> responseEntity = new ResponseEntity<>(convertImageResponse,
				HttpStatus.CREATED);

		expect(restTemplate.exchange(eq(requestEntity), eq(ConvertImageResponse.class))).andReturn(responseEntity);
		replay(restTemplate);

		ConvertImageResponse response = mediaProcessingService.convertResizeImageToTif(request, resolutionInDpi);

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED.toString(), response.getHttpStatus());
		verify(restTemplate);
	}

}