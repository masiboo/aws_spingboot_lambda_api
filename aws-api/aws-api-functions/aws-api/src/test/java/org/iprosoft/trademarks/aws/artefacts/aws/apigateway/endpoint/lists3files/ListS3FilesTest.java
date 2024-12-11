package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.lists3files;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.wipo.trademarks.Aws.artefacts.TestSetupUtils;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.getjobstatus.GetJobStatus;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.aws.s3.S3Service;
import org.wipo.trademarks.Aws.artefacts.configuration.SystemEnvironmentVariables;
import org.wipo.trademarks.Aws.artefacts.service.artefactjob.ArtefactJobService;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
import org.wipo.trademarks.Aws.artefacts.util.AppConstants;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class ListS3FilesTest {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private S3Service s3Service;

	private ListS3Files listS3Files;

	@BeforeAll
	static void setUp() {
		// AwsServicesSetup.prepareS3();
	}

	@AfterEach
	void cleanUp() {
		// AwsServicesSetup.deleteAllFiles();
	}

	@Test
	@Disabled
	public void testNotFound_WhenNoFiles() throws Exception {
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/lists3files")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void testListS3FilesSuccessfully() throws Exception {
		// Preparing TestData
		AwsServicesSetup.putObject("Aws-2024-01-19/merged_artefact_documents.pdf",
				Map.of(AppConstants.METADATA_KEY_IS_MERGED_FILE, Boolean.toString(false),
						AppConstants.METADATA_KEY_ARTEFACT_ID, "4b917a69-c1e5-420e-8eb8-78195bdfa174",
						AppConstants.METADATA_KEY_BATCH_SEQ, "1001"));
		AwsServicesSetup.putObject("Aws-2023-01-31/verify_jobstatus_documents.pdf",
				Map.of(AppConstants.METADATA_KEY_IS_MERGED_FILE, Boolean.toString(false),
						AppConstants.METADATA_KEY_ARTEFACT_ID, "4b917a69-c1e5-420e-8eb8-78195bdmpd688",
						AppConstants.METADATA_KEY_BATCH_SEQ, "1001", AppConstants.METADATA_KEY_TRACE_ID,
						"4d37ad36-883b-4c64-b17f-mpd688"));

		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/lists3files")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());

		JsonNode jsonNodeBody = objectMapper.readTree(result.getBody().getBody());
		String[] actualFiles = objectMapper.treeToValue(jsonNodeBody.get("files"), String[].class);
		log.info("Actual files {}", Arrays.toString(actualFiles));
		List<String> expectedFile = List.of("Aws-2023-01-31/verify_jobstatus_documents.pdf",
				"Aws-2024-01-19/merged_artefact_documents.pdf");
		Assertions.assertArrayEquals(expectedFile.toArray(), actualFiles,
				"Verify the order and equality of elements in the lists");
	}

	@Test
	void testApplySuccess() throws MalformedURLException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isS3ObjectKeysSet = true;
		TestSetupUtils.commonSetup();
		listS3Files = TestSetupUtils.createListS3Files();
		// act
		APIGatewayV2HTTPResponse response = listS3Files.apply(new APIGatewayV2HTTPEvent());
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("{\"files\":[\"key1\",\"key2\"]}"));
	}

	@Test
	void testApplyNotFound() throws MalformedURLException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isS3ObjectKeysSet = false;
		TestSetupUtils.commonSetup();
		listS3Files = TestSetupUtils.createListS3Files();
		// act
		APIGatewayV2HTTPResponse response = listS3Files.apply(new APIGatewayV2HTTPEvent());
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("No S3 files found"));
	}

}
