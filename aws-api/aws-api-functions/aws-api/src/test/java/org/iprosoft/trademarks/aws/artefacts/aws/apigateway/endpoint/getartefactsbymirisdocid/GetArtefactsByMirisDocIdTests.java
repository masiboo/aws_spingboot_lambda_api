package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbymirisdocid;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
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
public class GetArtefactsByMirisDocIdTests {

	private TestRestTemplate testRestTemplate;

	private ObjectMapper objectMapper;

	private GetArtefactsByMirisDocId getArtefactsByMirisDocId;

	private DatabaseService databaseService;

	@BeforeAll
	static void setUp() throws MalformedURLException {
		/*
		 * AwsServicesSetup.prepareDynamoDB(); AwsServicesSetup.prepareS3();
		 * AwsServicesSetup.populateDynamoDB();
		 */
	}

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		getArtefactsByMirisDocId = TestSetupUtils.createGetArtefactsByMirisDocId();
	}

	@Test
	@Disabled
	public void GetArtefactsByMirisDocIdSuccessfully() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("mirisDocId", "DOC001");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbymirisdocid")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());

		// Assert that the contentLength and sizeWarning has been fetched from in DB
		JsonNode jsonNodeBody = objectMapper.readTree(result.getBody().getBody()).get(0);
		Assertions.assertEquals(String.valueOf(ArtefactStatusEnum.INDEXED), jsonNodeBody.get("status").asText());
		Assertions.assertEquals("DOC001", jsonNodeBody.get("mirisDocId").asText());
	}

	@Test
	@Disabled
	public void GetArtefactsByMirisDocIdNotFound() throws Exception {
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("mirisDocId", "122321111111");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryStringParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbymirisdocid")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetArtefactsByMirisDocIdWithEmptyQueryParams() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbymirisdocid")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	void testApplySuccess() {
		// arrange
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("mirisDocId", "123");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withQueryStringParameters(queryStringParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactsByMirisDocId.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestData.getArtefactsEntityList().get(0).getMirisDocId()));
	}

	@Test
	void testApplyFailed() {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(null).build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactsByMirisDocId.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'mirisDocId' parameter in path"));
	}

}
