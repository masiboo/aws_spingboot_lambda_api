package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.indexartefact;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactIndexDto;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
*/
public class IndexArtefactsTests {

	/*
	 *
	 * private final ObjectMapper objectMapper;
	 *
	 * private final TestRestTemplate testRestTemplate;
	 *
	 * private final BatchService batchService;
	 *
	 * @BeforeAll static void setUp() { AwsServicesSetup.prepareDynamoDB();
	 * AwsServicesSetup.prepareS3(); AwsServicesSetup.populateDynamoDB();
	 * AwsServicesSetup.populateDynamoDB(
	 * "Aws-table-batch-status-last-item-index-event.json"); }
	 *
	 * @Test public void IndexArtefactsSuccessfully() throws Exception { // Set query
	 * string parameters Map<String, String> queryStringParameters = new HashMap<>();
	 * queryStringParameters.put("artefactId", "8ab7ed96-0799-4c4f-8a3b-4739fa0ec738"); //
	 * Set event body ArtefactIndexDto artefactIndexDto = new ArtefactIndexDto();
	 * artefactIndexDto.setMirisDocId("12233"); // Create API Gateway event
	 * APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withQueryStringParameters(queryStringParameters)
	 * .withBody(objectMapper.writeValueAsString(artefactIndexDto)) .build(); // Call the
	 * Spring Cloud Function endpoint ResponseEntity<APIGatewayV2HTTPResponse> result =
	 * testRestTemplate .exchange(RequestEntity.post(new
	 * URI("/indexartefact")).body(event), APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 200 log.info("Response HTTP Status Code: " +
	 * Objects.requireNonNull(result.getBody()).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.OK,
	 * Objects.requireNonNull(result.getBody()).getStatusCode()); }
	 *
	 * @Test public void IndexArtefactsAlreadyIndexed() throws Exception { // Set query
	 * string parameters Map<String, String> queryStringParameters = new HashMap<>();
	 * queryStringParameters.put("artefactId", "4b917a69-c1e5-420e-8eb8-78195bdfa174"); //
	 * Set event body ArtefactIndexDto artefactIndexDto = new ArtefactIndexDto();
	 * artefactIndexDto.setMirisDocId("12232"); // Create API Gateway event
	 * APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withQueryStringParameters(queryStringParameters)
	 * .withBody(objectMapper.writeValueAsString(artefactIndexDto)) .build(); // Call the
	 * Spring Cloud Function endpoint ResponseEntity<APIGatewayV2HTTPResponse> result =
	 * testRestTemplate .exchange(RequestEntity.post(new
	 * URI("/indexartefact")).body(event), APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 400 log.info("Response HTTP Status Code: " +
	 * Objects.requireNonNull(result.getBody()).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.BAD_REQUEST,
	 * Objects.requireNonNull(result.getBody()).getStatusCode()); }
	 *
	 * @Test public void testLastItemFromBatchIndexed() throws Exception { // Set query
	 * string parameters Map<String, String> queryStringParameters = new HashMap<>();
	 * queryStringParameters.put("artefactId", "0221123.053-0000D-merged"); // Set event
	 * body ArtefactIndexDto artefactIndexDto = new ArtefactIndexDto();
	 * artefactIndexDto.setMirisDocId("12233"); // Create API Gateway event
	 * APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withQueryStringParameters(queryStringParameters)
	 * .withBody(objectMapper.writeValueAsString(artefactIndexDto)) .build(); // Call the
	 * Spring Cloud Function endpoint ResponseEntity<APIGatewayV2HTTPResponse> result =
	 * testRestTemplate .exchange(RequestEntity.post(new
	 * URI("/indexartefact")).body(event), APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 200 log.info("Response HTTP Status Code: " +
	 * Objects.requireNonNull(result.getBody()).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.OK,
	 * Objects.requireNonNull(result.getBody()).getStatusCode());
	 *
	 * // verify the batchStatus and lockState BatchOutput batchDetail =
	 * batchService.getBatchDetail("0221123.053");
	 * Assertions.assertEquals(BatchStatus.INDEXED.getStatus(), batchDetail.getStatus());
	 * Assertions.assertFalse(batchDetail.isLocked()); }
	 */

	private IndexArtefact indexArtefact;

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		indexArtefact = TestSetupUtils.createIndexArtefact();
	}

	@Test
	public void testApply_Success() throws Exception {
		// Arrange
		String artefactId = "12345";
		String requestBody = "{\"mirisDocId\":\"16529262\"}";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, requestBody);
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isDocIdValid = true;
		TestSetupUtils.commonSetup();
		indexArtefact = TestSetupUtils.createIndexArtefact();

		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);

		// Assert
		Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Successfully indexed"));
	}

	@Test
	public void testApply_EmptyArtefactId() {
		// Arrange
		APIGatewayV2HTTPEvent event = createEventWithBody("", "{\"mirisDocId\":\"16529262\"}");

		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("'artefactId' parameter is empty"));
	}

	@Test
	public void testApply_EmptyRequestBody() {
		// Arrange
		String artefactId = "12345";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, "");

		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);

		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Empty request body"));
	}

	@Test
	public void testApply_InvalidJsonRequestBody() {
		// Arrange
		String artefactId = "12345";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, "{invalid json}");
		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Failed to parse mirisDocId"));
	}

	@Test
	public void testApply_InvalidMirisDocId() {
		// Arrange
		String artefactId = "12345";
		String requestBody = "{\"mirisDocId\":\"invalidId\"}";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, requestBody);
		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("The given mirisDocId is invalid"));
	}

	@Test
	public void testApply_ArtefactNotFound() throws MalformedURLException {
		// Arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setNullArtefact = true;
		TestSetupUtils.isDocIdValid = true;
		TestSetupUtils.commonSetup();
		indexArtefact = TestSetupUtils.createIndexArtefact();
		String artefactId = "12345";
		String requestBody = "{\"mirisDocId\":\"16529262\"}";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, requestBody);
		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("The artefact is not found"));
	}

	@Test
	public void testApply_ArtefactAlreadyIndexed() throws MalformedURLException {
		// Arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isDocIdValid = true;
		TestSetupUtils.commonSetup();
		TestSetupUtils.setArtefactStatusIndexed();
		indexArtefact = TestSetupUtils.createIndexArtefact();
		String artefactId = "12345";
		String requestBody = "{\"mirisDocId\":\"16529262\"}";
		APIGatewayV2HTTPEvent event = createEventWithBody(artefactId, requestBody);
		// Act
		APIGatewayV2HTTPResponse response = indexArtefact.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Artefact is already in INDEXED"));
	}

	private APIGatewayV2HTTPEvent createEventWithBody(String artefactId, String body) {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setQueryStringParameters(Map.of("artefactId", artefactId));
		event.setBody(body);
		return event;
	}

}
