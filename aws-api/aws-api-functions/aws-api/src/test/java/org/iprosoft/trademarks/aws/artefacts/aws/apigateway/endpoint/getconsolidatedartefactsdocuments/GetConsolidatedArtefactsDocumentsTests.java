package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getconsolidatedartefactsdocuments;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.model.dto.MergeFilesRequest;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class GetConsolidatedArtefactsDocumentsTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private WireMockServer wireMockServer;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
		AwsServicesSetup.populateDynamoDB();
	}

	@BeforeEach
	public void setup() {
		// Start the WireMockServer on a random port
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
	}

	@AfterEach
	public void tearDown() {
		// Stop the WireMockServer after each test
		wireMockServer.stop();
	}

	@Test
	public void GetConsolidatedArtefactsDocumentsSuccessfully() throws Exception {
		String jsonString = "{\"objects\":[{\"bucket\":\"test-bucket\",\"key\":\"Merged_document.tiff\"},{\"bucket\":\"test-bucket\",\"key\":\"Merged_document_2.tiff\"}]}";
		MergeFilesRequest request = objectMapper.readValue(jsonString, MergeFilesRequest.class);
		wireMockServer.stubFor(post(urlMatching("/media-processing-svc/api/v1/merge-files-to-pdf"))
			.withRequestBody(containing(objectMapper.writeValueAsString(request)))
			.willReturn(aResponse().withHeader("Content-Type", "text/plain")
				.withBody(
						"http://127.0.0.1:4566/sapuhcdtbyz5itiwvlcc/merged_artefact_documents.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230801T110956Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Credential=localstack%2F20230801%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=fdff259cd61811ed48f2e20c57aae78fa13eaebd858f7a3d23f9b08d1c955726")));

		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("artefactIds",
				"4b917a69-c1e5-420e-8eb8-78195bdfa174,8ab7ed96-0799-4c4f-8a3b-4739fa0ec738");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getconsolidatedartefactsdocuments")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void GetConsolidatedArtefactsDocumentsNotFound() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("artefactIds", "122321111111");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getconsolidatedartefactsdocuments")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void GetArtefactsByMirisDocIdWithEmptyQueryParams() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getconsolidatedartefactsdocuments")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

}
