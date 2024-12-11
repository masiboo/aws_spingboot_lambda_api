package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefacturl;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class GetArtefactURLTests {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private S3Service s3Service;

	private ArtefactService artefactService;

	private GetArtefactURL getArtefactURL;

	@BeforeAll
	static void setUp() {
		/*
		 * AwsServicesSetup.prepareDynamoDB(); AwsServicesSetup.prepareS3();
		 * AwsServicesSetup.populateDynamoDB();
		 */
	}

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		getArtefactURL = TestSetupUtils.createGetArtefactURL();
	}

	@Test
	@Disabled
	public void GetArtefactURLSuccessfully() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("artefactId", "4b917a69-c1e5-420e-8eb8-78195bdfa174");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getartefacturl")).body(event), APIGatewayV2HTTPResponse.class);
		Map<String, String> map = objectMapper.readValue(Objects.requireNonNull(result.getBody()).getBody(),
				new TypeReference<>() {
				});
		// Should be 200 and signedS3Url should not be empty
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		log.info("signedS3Url: " + map.get("signedS3Url"));
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertNotNull(map.get("signedS3Url"));
	}

	@Test
	@Disabled
	public void GetArtefactURLMissingArtefactId() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getartefacturl")).body(event), APIGatewayV2HTTPResponse.class);
		Map<String, String> map = objectMapper.readValue(Objects.requireNonNull(result.getBody()).getBody(),
				new TypeReference<>() {
				});
		// Should be 200 and signedS3Url should not be empty
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetArtefactURLEmptyArtefactId() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("artefactId", "");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getartefacturl")).body(event), APIGatewayV2HTTPResponse.class);
		Map<String, String> map = objectMapper.readValue(Objects.requireNonNull(result.getBody()).getBody(),
				new TypeReference<>() {
				});
		// Should be 200 and signedS3Url should not be empty
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	void testApplySuccess() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("artefactId", "4b917a69-c1e5-420e-8eb8-78195bdfa174");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactURL.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestData.getArtefact(ArtefactStatus.INDEXED.getStatus()).getId()));
		assertTrue(response.getBody().contains(TestSetupUtils.expectedSignedS3Url));

	}

	@Test
	void testApplyFailedMissingArtefactId() {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(null)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactURL.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'artefactId' parameter in path"));
	}

	@Test
	void testApplyFailedEmptyArtefactId() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("artefactId", "");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactURL.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("'artefactId' parameter is empty"));
	}

	@Test
	void testNoObjectInS3Object() throws MalformedURLException {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("artefactId", "4b917a69-c1e5-420e-8eb8-78195bdfa174");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();

		TestSetupUtils.resetBooleans();
		TestSetupUtils.setNoS3Object = true;
		TestSetupUtils.setNoS3Bucket = true;
		TestSetupUtils.commonSetup();
		getArtefactURL = TestSetupUtils.createGetArtefactURL();

		// act
		APIGatewayV2HTTPResponse response = getArtefactURL.apply(event);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("S3 object does not exist in bucket"));
	}

	@Test
	void testNoS3Bucket() throws MalformedURLException {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("artefactId", "4b917a69-c1e5-420e-8eb8-78195bdfa174");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();

		TestSetupUtils.resetBooleans();
		TestSetupUtils.setNoS3Object = false;
		TestSetupUtils.setNoS3Bucket = true;
		TestSetupUtils.commonSetup();
		getArtefactURL = TestSetupUtils.createGetArtefactURL();

		// act
		APIGatewayV2HTTPResponse response = getArtefactURL.apply(event);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("S3 bucket does not exist"));
	}

}
