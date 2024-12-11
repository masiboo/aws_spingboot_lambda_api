package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.reportjobstatus;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
import org.wipo.trademarks.Aws.artefacts.util.BatchStatus;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class ReportJobStatusTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
		AwsServicesSetup.populateDynamoDB("Aws-table-batch-status-by-req-type.json");
	}

	@Test
	public void ReportJobStatusSuccessfully() throws Exception {
		Map<String, String> pathParameters = Map.of("requestId", "8bdf287c-5bac-4d92-bb07-d3d099bc8826");
		APIGatewayV2HTTPResponse response = testGetJobStatusReport(pathParameters);
		assertBatchStatus(response, BatchStatus.INIT);
	}

	@Test
	public void testBatchStatusUpdated_ReqType_Addendum() throws Exception {
		Map<String, String> pathParameters = Map.of("requestId", "8bdf287c-5bac-4d92-bb07-e12a14a864df");
		APIGatewayV2HTTPResponse response = testGetJobStatusReport(pathParameters);
		assertBatchStatus(response, BatchStatus.INDEXED);

	}

	@Test
	public void testBatchStatusUpdated_ReqType_NewRequest() throws Exception {
		Map<String, String> pathParameters = Map.of("requestId", "8bdf287c-5bac-4d92-bb07-d3d099bc8825");
		APIGatewayV2HTTPResponse response = testGetJobStatusReport(pathParameters);
		assertBatchStatus(response, BatchStatus.INSERTED);
	}

	private APIGatewayV2HTTPResponse testGetJobStatusReport(Map<String, String> pathParameters) throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/reportjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
		return result.getBody();
	}

	private void assertBatchStatus(APIGatewayV2HTTPResponse response, BatchStatus batchStatus)
			throws JsonProcessingException {
		JsonNode jsonNodeBody = objectMapper.readTree(response.getBody());
		String statusByRequestType = jsonNodeBody.get("batchStatus").asText();
		Assertions.assertEquals(batchStatus.getStatus(), statusByRequestType);
	}

	@Test
	public void ReportJobStatusNotFound() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("requestId", "potato");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/reportjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 404
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void ReportJobStatusWhenIdNotSent() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/reportjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void ReportJobStatusWhenIdEmpty() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("requestId", "");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/reportjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

}
