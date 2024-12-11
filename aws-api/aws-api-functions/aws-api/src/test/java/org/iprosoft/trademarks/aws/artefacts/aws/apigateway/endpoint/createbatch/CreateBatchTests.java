package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createbatch;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
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
import org.springframework.web.client.RestClientException;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class CreateBatchTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private final BatchService batchService;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
	}

	@Test
	@Disabled
	public void createBatchSuccessfully() throws Exception {
		// Create path parameters map
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("mirisDocId", "123456789");
		pathParameters.put("scannedApp", "NEW_REQUEST");
		// Create body which is a List of ArtefactBatch, even if it is a single
		// ArtefactBatch
		List<ArtefactBatch> artefactBatchList = new ArrayList<>();
		ArtefactBatch artefactBatch = new ArtefactBatch();
		artefactBatch.setArtefactName("unit-test-artefactbatch");
		artefactBatch.setType("Image");
		artefactBatch.setFilename("trademark_picture");
		artefactBatch.setPath("/local/path/to/folder");
		artefactBatch.setContentType("PNG");
		artefactBatch.setBatchSequence("4354562353");
		artefactBatch.setUser("alogothetis");
		artefactBatch.setJobId("6534322");
		artefactBatch.setArtefactId("45930238486735f0a49s65d4f345");
		artefactBatch.setS3Url("");
		artefactBatch.setRequestType("hello-world");
		artefactBatchList.add(artefactBatch);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.withBody(objectMapper.writeValueAsString(artefactBatchList))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = null;
		try {
			result = testRestTemplate.exchange(RequestEntity.post(new URI("/createbatch")).body(event),
					APIGatewayV2HTTPResponse.class);
		}
		catch (RestClientException rce) {
			log.error(rce.getMessage());
		}

		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.CREATED, (Objects.requireNonNull(result.getBody())).getStatusCode());

		// Ensure the RequestType attached to Batch
		APIGatewayV2HTTPResponse response = result.getBody();
		JsonNode jsonNodeBody = objectMapper.readTree(response.getBody());
		String batchSequenceNum = jsonNodeBody.get(0).get("batchSequence").asText();
		BatchOutput output = batchService.getBatchDetail(batchSequenceNum);
		Assertions.assertNotNull(output);
		Assertions.assertEquals(pathParameters.get("scannedApp"), output.getRequestType());

	}

}
