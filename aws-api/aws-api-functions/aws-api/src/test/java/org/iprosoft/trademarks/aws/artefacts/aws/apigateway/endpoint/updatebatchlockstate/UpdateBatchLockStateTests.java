package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.updatebatchlockstate;

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
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Temporarily disabled")
public class UpdateBatchLockStateTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
		AwsServicesSetup.populateDynamoDB();
	}

	@Test
	@Order(1)
	public void LockBatchSuccessfully() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "4354562353");
		pathParameters.put("lock", "true");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Order(2)
	public void LockBatchAlreadyLocked() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "4354562353");
		pathParameters.put("lock", "true");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatus.CONFLICT.value(),
				(Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Order(3)
	public void UnlockBatchSuccessfully() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "4354562353");
		pathParameters.put("lock", "false");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Order(4)
	public void LockBatchNotFound() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "435456235311111");
		pathParameters.put("lock", "false");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 404
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Order(5)
	public void LockBatchWhenPathParamsNotSent() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Order(6)
	public void LockBatchWhenBatchSeqEmpty() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/updatebatchlockstate")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

}
