package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getallartefacts;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactServiceImpl;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.JsonConverterUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ContextConfiguration(classes = { AwsServicesTestConfig.class })
// @ExtendWith(LocalstackDockerExtension.class)
// @LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetAllArtefactsTests {

	private final ObjectMapper objectMapper;

	private final ArtefactServiceImpl artefactService;

	private ArtefactServiceImpl mockArtefactService;

	private final TestRestTemplate testRestTemplate;

	private GetAllArtefacts getAllArtefacts;

	/*
	 * @BeforeAll static void setUp() { AwsServicesSetup.prepareDynamoDB();
	 * AwsServicesSetup.prepareS3(); AwsServicesSetup.populateDynamoDB(); }
	 */

	@BeforeEach
	void init() throws MalformedURLException {
		/*
		 * getAllArtefacts = new GetAllArtefacts(objectMapper, artefactService);
		 * mockArtefactService = createNiceMock(ArtefactServiceImpl.class);
		 */
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.commonSetup();
		getAllArtefacts = TestSetupUtils.createGetAllArtefacts();

	}

	@Test
	public void GetAllArtefactsSuccessfullyFoundArtefactByDuration() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("date", "2024-06-30");
		pathParameters.put("status", "INDEXED");
		pathParameters.put("fromDate", "2024-08-10");
		pathParameters.put("untilDate", "2024-08-13");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.OK);
		assertNotNull(response.getBody());
		List<Artefact> actualArtefacts = JsonConverterUtil.getObjectListFromJson(response.getBody(), Artefact.class);
		assertEquals(TestData.getMixedArtefactList().size(), actualArtefacts.size());
	}

	@Test
	public void GetAllArtefactsSuccessfullyFoundArtefactByDate() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("date", "2024-06-30");
		pathParameters.put("status", "INDEXED");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.OK);
		assertNotNull(response.getBody());
		List<Artefact> actualArtefacts = JsonConverterUtil.getObjectListFromJson(response.getBody(), Artefact.class);
		assertEquals(TestData.getMixedArtefactList().size(), actualArtefacts.size());
	}

	@Test
	public void getAllArtefactsSuccessfullyNotFoundWithAllQueryParameters() {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withQueryStringParameters(new HashMap<>())
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.CREATED);
		assertTrue(response.getBody().contains("No artefacts found"));
	}

	@Test
	public void GetAllArtefactsSuccessfullyNotFoundWithOnlyStatus() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("status", "INDEXED");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.CREATED);
		assertTrue(response.getBody().contains("No artefacts found"));
	}

	@Test
	public void getAllArtefactsWithOnlyDate() {
		// arrange
		System.out.println(getAllArtefacts);
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("date", "2024-08-13");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.BAD_REQUEST);
		assertTrue(response.getBody().contains("Status is null/empty"));
	}

	@Test
	public void getAllArtefactsWithOnlyInterval() {
		// arrange
		System.out.println(getAllArtefacts);
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("fromDate", "2024-08-10");
		pathParameters.put("untilDate", "2024-08-13");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.BAD_REQUEST);
		assertTrue(response.getBody().contains("Status is null/empty"));
	}

	@Test
	public void getAllArtefactsWithoutStatus() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("fromDate", "2024-08-10");
		pathParameters.put("untilDate", "2024-08-13");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.BAD_REQUEST);
		assertTrue(response.getBody().contains("Status is null/empty"));
	}

	@Test
	public void getAllArtefactsSuccessfullyNotFoundWithOnlyDateStatus() {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("date", "2024-08-13");
		pathParameters.put("status", "INDEXED");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withQueryStringParameters(pathParameters).build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(response.getStatusCode(), HttpStatusCode.OK);
		assertTrue(response.getBody().contains("artefactName"));
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
			.exchange(RequestEntity.post(new URI("/getallartefacts")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void testInvalidDateParam() throws Exception {
		// Arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("date", "13-11-2023"))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);

		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid dateFormat and valid format is yyyy-MM-dd"));
	}

	@Test
	public void testInvalidStatusParam() throws Exception {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("status", "COMPLETED"))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody()
			.contains(
					"Invalid status given allowed values are [INDEXED, DELETED, INSERTED, UPLOADED, ERROR, INIT, CANCELED]"));
	}

	@Test
	public void testValidParams() throws Exception {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withQueryStringParameters(Map.of("status", "INIT", "date", "2023-07-11"))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getAllArtefacts.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertNotNull(response.getBody().contains("artefactName"));
	}

}
