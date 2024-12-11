package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.convertgiftojpg;

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
import org.easymock.EasyMock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.wipo.trademarks.Aws.artefacts.TestData;
import org.wipo.trademarks.Aws.artefacts.TestSetupUtils;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.model.dto.ConvertImageRequest;
import org.wipo.trademarks.Aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.easymock.EasyMock.createMock;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wipo.trademarks.Aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.wipo.trademarks.Aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_OK;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/*
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.S3 })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
*/
@Slf4j
public class ConvertGIFToJPGTests {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private WireMockServer wireMockServer;

	private MediaProcessingService mediaProcessingService;

	private ConvertGIFToJPG convertGIFToJPG;

	@BeforeAll
	static void setUp() {
		// AwsServicesSetup.prepareS3();
	}

	@BeforeEach
	public void setup() throws MalformedURLException {
		// Start the WireMockServer on a random port
		/*
		 * wireMockServer = new WireMockServer(); wireMockServer.start();
		 * WireMock.configureFor("localhost", wireMockServer.port());
		 */
		/*
		 * mediaProcessingService = createMock(MediaProcessingService.class);
		 * convertGIFToJPG = new ConvertGIFToJPG(mediaProcessingService);
		 */
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.commonSetup();
		convertGIFToJPG = TestSetupUtils.createConvertGIFToJPG();
	}

	@AfterEach
	public void tearDown() {
		// Stop the WireMockServer after each test
		// wireMockServer.stop();
	}

	@Test
	@Disabled
	public void ConvertGIFToJPGSuccessfully() throws Exception {
		String jsonString = "{\"bucket\":\"test-bucket\",\"key\":\"intranet.gif\"}";
		ConvertImageRequest request = objectMapper.readValue(jsonString, ConvertImageRequest.class);
		wireMockServer.stubFor(post(urlMatching("/media-processing-svc/api/v1/convert-gif-to-jpg"))
			.withRequestBody(containing(objectMapper.writeValueAsString(request)))
			.willReturn(aResponse().withHeader("Content-Type", "text/plain")
				.withBody(
						"http://127.0.0.1:4566/test-bucket/intranet.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230911T140334Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Credential=localstack%2F20230911%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=0c85d4177c39aa34cbd96cabe437c8855f441ca1b18b7b269ac3ebc69530af2b")));

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("bucket", "test-bucket");
		pathParameters.put("key", "intranet.gif");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/media-processing-svc/api/v1/convert-gif-to-jpg")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void ConvertGIFToJPGWithPathParams() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/convertgiftojpg")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void testApplySuccess() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("bucket", "test-bucket");
		pathParameters.put("key", "intranet.gif");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse res = convertGIFToJPG.apply(event);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_OK.getStatusCode(), res.getStatusCode());
	}

	@Test
	public void testApplyFailed() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("bucket", "test-bucket");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse res = convertGIFToJPG.apply(event);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_BAD_REQUEST.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains("Both 'key' and 'bucket' must be present"));
	}

}
