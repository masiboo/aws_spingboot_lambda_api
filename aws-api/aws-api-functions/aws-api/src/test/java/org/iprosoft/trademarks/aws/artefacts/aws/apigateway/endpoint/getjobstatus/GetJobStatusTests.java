package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getjobstatus;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class GetJobStatusTests {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private ArtefactJobService artefactJobService;

	private GetJobStatus getJobStatus;

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
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.commonSetup();
		getJobStatus = TestSetupUtils.createGetJobStatus();
	}

	@Test
	@Disabled
	public void GetJobStatusSuccessfully() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobId", "dc12d620-7b1f-4a44-839f-9e4017823a39");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetJobStatusNotFound() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobId", "potato");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 404
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.NOT_FOUND, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetJobStatusWhenIdNotSent() throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetJobStatusWhenIdEmpty() throws Exception {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobId", "");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/getjobstatus")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	// @Test
	@ParameterizedTest
	@ValueSource(strings = { "jobid", "JOBID", "jobId", "JOB_ID", "job-id" })
	void testApplySuccess2(String jobId) {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put(jobId, "dc12d620-7b1f-4a44-839f-9e4017823a39");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getJobStatus.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestSetupUtils.job.getId()));
		assertTrue(response.getBody().contains(TestSetupUtils.job.getArtefactId()));
		assertTrue(response.getBody().contains(TestSetupUtils.job.getStatus()));
	}

	@Test
	void testApplyFailedJobIdEmpty() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobId", "  ");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// act
		APIGatewayV2HTTPResponse response = getJobStatus.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'jobId' parameter in path"));
	}

	@Test
	void testApplyFailedJobNotFound() throws MalformedURLException {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobId", "dc12d620-7b1f-4a44-839f-9e4017823a39");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		getJobStatus = TestSetupUtils.createGetJobStatus();
		// act
		APIGatewayV2HTTPResponse response = getJobStatus.apply(event);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("No job found"));
	}

}
