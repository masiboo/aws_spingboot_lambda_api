package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getbatch;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
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
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class GetBatchTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
		AwsServicesSetup.populateDynamoDB("Aws-table-batch-item-filter-test-data.json");
	}

	@Test
	public void GetBatchSuccessfully() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "4354562353");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getbatch")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void testGetBatchFetchOnlyIndexEligibleItems() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "4354562353");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getbatch")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());

		// Assert only Indexation eligible artefacts are returning
		BatchOutput batchOutput = objectMapper.readValue(result.getBody().getBody(), BatchOutput.class);
		List<ArtefactOutput> artefactOutputList = batchOutput.getArtefacts();
		Assertions.assertNotNull(artefactOutputList);
		Predicate<ArtefactOutput> indexEligibleArtefact = artefact -> ArtefactStatus.INSERTED.toString()
			.equalsIgnoreCase(artefact.getStatus())
				&& !ArtefactClassType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType());
		Assertions.assertTrue(artefactOutputList.stream().allMatch(indexEligibleArtefact));

		// Assert fileName present in the response
		Predicate<ArtefactOutput> fileNamePresent = artefact -> StringUtils.hasText(artefact.getArtefactName());
		Assertions.assertTrue(artefactOutputList.stream().allMatch(fileNamePresent));

	}

	@Test
	public void GetBatchNotFound() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "435456235311111");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getbatch")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 404
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void GetBatchWhenIdNotSent() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getbatch")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void GetBatchWhenIdEmpty() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("batchSeq", "");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getbatch")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

}
