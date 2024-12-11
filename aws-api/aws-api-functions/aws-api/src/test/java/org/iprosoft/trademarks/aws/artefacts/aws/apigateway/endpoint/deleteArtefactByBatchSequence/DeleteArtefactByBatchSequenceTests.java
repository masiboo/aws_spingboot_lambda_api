package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.deleteArtefactByBatchSequence;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.deleteBatchByBatchSequence.DeleteArtefactByBatchSequence;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.util.Map;

/*
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
*/
public class DeleteArtefactByBatchSequenceTests {

	/*
	 *
	 * private final ObjectMapper objectMapper;
	 *
	 * private final TestRestTemplate testRestTemplate;
	 *
	 */

	private DeleteArtefactByBatchSequence deleteArtefactByBatchSequence;

	/*
	 * DeleteArtefactByBatchSequence
	 *
	 * @BeforeAll static void setUp() { AwsServicesSetup.prepareDynamoDB();
	 * AwsServicesSetup.prepareS3(); AwsServicesSetup.populateDynamoDB(); }
	 */

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		deleteArtefactByBatchSequence = TestSetupUtils.createDeleteArtefactByBatchSequence();
	}

	/*
	 * @Test public void DeleteBatchSuccessfully() throws Exception { Map<String, String>
	 * pathPDeleteBatchByBatchSequencearameters = new HashMap<>();
	 * pathParameters.put("batchSeq", "4354562353"); // Create API Gateway event
	 * APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withPathParameters(pathParameters) .build(); // Call the Spring Cloud Function
	 * endpoint ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
	 * .exchange(RequestEntity.post(new URI("/deletebatch")).body(event),
	 * APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 200 log.info("Response HTTP Status Code: " +
	 * (Objects.requireNonNull(result.getBody())).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.OK,
	 * (Objects.requireNonNull(result.getBody())).getStatusCode()); }
	 *
	 * @Test public void DeleteBatchNotFound() throws Exception { Map<String, String>
	 * pathParameters = new HashMap<>(); pathParameters.put("batchSeq",
	 * "435456235311111"); // Create API Gateway event APIGatewayV2HTTPEvent event =
	 * APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withPathParameters(pathParameters) .build(); // Call the Spring Cloud Function
	 * endpoint ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
	 * .exchange(RequestEntity.post(new URI("/deletebatch")).body(event),
	 * APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 404 log.info("Response HTTP Status Code: " +
	 * (Objects.requireNonNull(result.getBody())).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.NOT_FOUND,
	 * (Objects.requireNonNull(result.getBody())).getStatusCode()); }
	 *
	 * @Test public void DeleteBatchWhenIdNotSent() throws Exception { Map<String, String>
	 * pathParameters = new HashMap<>(); // Create API Gateway event APIGatewayV2HTTPEvent
	 * event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withPathParameters(pathParameters) .build(); // Call the Spring Cloud Function
	 * endpoint ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
	 * .exchange(RequestEntity.post(new URI("/deletebatch")).body(event),
	 * APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 400 log.info("Response HTTP Status Code: " +
	 * (Objects.requireNonNull(result.getBody())).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.BAD_REQUEST,
	 * (Objects.requireNonNull(result.getBody())).getStatusCode()); }
	 *
	 * @Test public void DeleteBatchWhenIdEmpty() throws Exception { Map<String, String>
	 * pathParameters = new HashMap<>(); pathParameters.put("batchSeq", ""); // Create API
	 * Gateway event APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders())
	 * .withPathParameters(pathParameters) .build(); // Call the Spring Cloud Function
	 * endpoint ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
	 * .exchange(RequestEntity.post(new URI("/deletebatch")).body(event),
	 * APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 400 log.info("Response HTTP Status Code: " +
	 * (Objects.requireNonNull(result.getBody())).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.BAD_REQUEST,
	 * (Objects.requireNonNull(result.getBody())).getStatusCode()); }
	 */

	@Test
	public void testDeleteBatch_Success() throws Exception {
		// Arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isBatchSequenceExist = true;
		TestSetupUtils.commonSetup();
		deleteArtefactByBatchSequence = TestSetupUtils.createDeleteArtefactByBatchSequence();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withPathParameters(Map.of("batchSeq", "020216.24"))
			.build();
		// Act
		APIGatewayV2HTTPResponse response = deleteArtefactByBatchSequence.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatusCode.OK, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains(TestData.getBatchOutput("INIT").getStatus()));
		Assertions.assertTrue(response.getBody().contains(TestData.getBatchOutput("INIT").getBatchSequence()));
	}

	@Test
	public void testDeleteBatch_BatchNotFound() throws MalformedURLException {
		// Arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isBatchSequenceExist = false;
		TestSetupUtils.commonSetup();
		deleteArtefactByBatchSequence = TestSetupUtils.createDeleteArtefactByBatchSequence();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withPathParameters(Map.of("batchSeq", "020216.24"))
			.build();
		// Act
		APIGatewayV2HTTPResponse response = deleteArtefactByBatchSequence.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("The Batch is not found"));
	}

	@Test
	public void testDeleteBatch_BatchAlreadyDeleted() throws MalformedURLException {
		// Arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		TestSetupUtils.setDetestedBatch();
		deleteArtefactByBatchSequence = TestSetupUtils.createDeleteArtefactByBatchSequence();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withPathParameters(Map.of("batchSeq", "020216.24"))
			.build();
		// Act
		APIGatewayV2HTTPResponse response = deleteArtefactByBatchSequence.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Batch is already in DELETED"));
	}

	@Test
	public void testDeleteBatch_BatchSeqParameterEmpty() throws MalformedURLException {
		// Arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withPathParameters(Map.of("batchSeq", ""))
			.build();
		// Act
		APIGatewayV2HTTPResponse response = deleteArtefactByBatchSequence.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("'batchSeq' parameter is empty"));
	}

	@Test
	public void testDeleteBatch_InternalServerError() {
		// Arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withPathParameters(Map.of("batchSeq", "020216.24"))
			.build();
		TestSetupUtils.configBatchRuntimeExceptionForGetBatchDetail();
		// Act
		APIGatewayV2HTTPResponse response = deleteArtefactByBatchSequence.apply(event);
		// Assert
		Assertions.assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Internal server error occurred"));
	}

}
