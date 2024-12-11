package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.getalljobs;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.wipo.trademarks.Aws.artefacts.TestData;
import org.wipo.trademarks.Aws.artefacts.TestSetupUtils;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.model.entity.ArtefactJob;
import org.wipo.trademarks.Aws.artefacts.service.artefactjob.ArtefactJobService;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
import org.wipo.trademarks.Aws.artefacts.util.ArtefactStatus;
import org.wipo.trademarks.Aws.artefacts.util.DateUtils;
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
@LocalstackDockerProperties(services = { ServiceName.DYNAMO })
@AllArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class GetAllJobsTest {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private GetAllJobs getAllJobs;

	private ArtefactJobService artefactJobService;

	@BeforeAll
	static void setUp() {
		// AwsServicesSetup.prepareDynamoDB();
		// AwsServicesSetup.populateDynamoDB();
	}

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		getAllJobs = TestSetupUtils.createGetAllJobs();
	}

	@Test
	@Disabled
	public void GetAllArtefactsSuccessfully() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getalljobs")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void testInvalidDateParam() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("date", "13-11-2023"))
			.build();

		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getalljobs")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void testInvalidStatusParam() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("status", "COMPLETED"))
			.build();

		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getalljobs")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void testValidParams() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("status", "INIT", "date", "2023-12-14"))
			.build();

		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getalljobs")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	void testApplySuccess() {
		// arrange
		Map<String, String> queryParameters = new HashMap<>();
		String date = DateUtils.getCurrentDateShortStr();
		String status = ArtefactStatus.INSERTED.name();
		queryParameters.put("date", date);
		queryParameters.put("status", status);
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllJobs.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestData.getArtefactJobsNotNull().get(0).getBatchSequence()));
	}

	@Test
	void testApplyFailedInvalidStatus() {
		// arrange
		Map<String, String> queryParameters = new HashMap<>();
		String date = DateUtils.getCurrentDateShortStr();
		queryParameters.put("date", date);
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllJobs.apply(event);

		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid status"));
	}

	@Test
	void testApplyFailedInvalidDate() throws JsonProcessingException {
		// arrange
		Map<String, String> queryParameters = new HashMap<>();
		String status = ArtefactStatus.INSERTED.name();
		queryParameters.put("status", status);
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(queryParameters)
			.build();
		/*
		 * List<ArtefactJob> artefactJobList = TestData.getArtefactJobsNotNull();
		 * expect(artefactJobService.getAllJobs(anyString(),
		 * anyString())).andReturn(artefactJobList).anyTimes();
		 * replay(artefactJobService);
		 */

		// act
		APIGatewayV2HTTPResponse response = getAllJobs.apply(event);

		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid dateFormat"));
	}

}
