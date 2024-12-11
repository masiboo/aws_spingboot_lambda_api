package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.getallbatches;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wipo.trademarks.Aws.artefacts.TestData;
import org.wipo.trademarks.Aws.artefacts.TestSetupUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
*/
//@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetAllBatchesTests {

	/*
	 * private final ObjectMapper objectMapper;
	 *
	 * private final TestRestTemplate testRestTemplate;
	 */

	private GetAllBatches getAllBatches;

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
		getAllBatches = TestSetupUtils.createGetAllBatches();
	}

	/*
	 * @Test
	 *
	 * @Disabled public void GetAllBatchesSuccessfully() throws Exception { // Create API
	 * Gateway event APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
	 * .withHeaders(ApiGatewayResponseUtil.createHeaders()) .build(); // Call the Spring
	 * Cloud Function endpoint ResponseEntity<APIGatewayV2HTTPResponse> result =
	 * testRestTemplate .exchange(RequestEntity.post(new
	 * URI("/getallbatches")).body(event), APIGatewayV2HTTPResponse.class);
	 * log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	 * // Should be 200 log.info("Response HTTP Status Code: " +
	 * (Objects.requireNonNull(result.getBody())).getStatusCode());
	 * Assertions.assertEquals(HttpStatusCode.OK,
	 * (Objects.requireNonNull(result.getBody())).getStatusCode()); }
	 */

	@Test
	public void testApply_BatchesFound() throws Exception {
		// act
		InputStream event = TestData.getInputStreamByClassPath("classpath:api-events/api-get-indexed-batches.json");
		var response = getAllBatches.apply(event);
		// assert
		Assertions.assertEquals(HttpStatusCode.OK, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains(TestData.getBatchOutputs().get(0).getBatchSequence()));
		Assertions.assertTrue(response.getBody().contains(TestData.getBatchOutputs().get(0).getStatus()));
	}

	@Test
	public void testApply_NoBatchesFound() throws Exception {
		// arrange
		TestSetupUtils.setEmptyBatch = true;
		TestSetupUtils.commonSetup();
		getAllBatches = TestSetupUtils.createGetAllBatches();
		InputStream event = TestData.getInputStreamByClassPath("classpath:api-events/api-get-indexed-batches.json");

		// act
		var response = getAllBatches.apply(event);
		// assert
		Assertions.assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("No batches found"));
	}

	@Test
	public void testApply_ExceptionThrown() throws IOException {
		// Arrange
		TestSetupUtils.configBatchRuntimeExceptionForGetAllBatchByStatus();
		getAllBatches = TestSetupUtils.createGetAllBatches();
		InputStream event = TestData.getInputStreamByClassPath("classpath:api-events/api-get-indexed-batches.json");
		// act
		var response = getAllBatches.apply(event);

		// assert
		Assertions.assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Assertions.assertTrue(response.getBody().contains("Unexpected exception occurred Service error"));
	}

}
