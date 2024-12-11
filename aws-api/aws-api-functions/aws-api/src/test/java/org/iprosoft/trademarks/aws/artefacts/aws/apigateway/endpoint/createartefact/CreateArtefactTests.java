package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createartefact;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.jetbrains.annotations.NotNull;
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
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.wipo.trademarks.Aws.helper.FunctionInvokeHelper;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class CreateArtefactTests {

	public static final String SOUND_TYPE = ArtefactInput.classType.SOUND.toString();

	public static final String VIDEO_TYPE = ArtefactInput.classType.MULTIMEDIA.toString();

	public static final String WARNING = "warning";

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private final ArtefactService artefactService;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.prepareS3();
		AwsServicesSetup.populateDynamoDB();
	}

	@Test
	public void createArtefactSuccessfully() throws Exception {
		// Create an Artefact object
		ArtefactInput artefact = TestData.createArtefactInput();
		List<ArtefactItemInput> artefactItemInputList = List.of(TestData.createArtefactInputItem());
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/createartefact")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.CREATED, (Objects.requireNonNull(result.getBody())).getStatusCode());
		assertArtefactHasFileName(result.getBody());
	}

	@Test
	public void createArtefactTestValidationFailure() throws JsonProcessingException, URISyntaxException {
		// Create an Artefact object with Invalid classType
		ArtefactInput artefact = createArtefactInputWithType("TEXT");
		List<ArtefactItemInput> artefactItemInputList = List.of(TestData.createArtefactInputItem());
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/createartefact")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void createArtefactWithoutBodyValidationFailure() throws JsonProcessingException, URISyntaxException {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/createartefact")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	public void testCreateAudioArtefactSuccess() throws Exception {
		Long allowedSize = Long.parseLong(SystemEnvironmentVariables.SOUND_FILE_SIZE_LIMIT) - 1;
		APIGatewayV2HTTPResponse gatewayV2HTTPResponse = testArtefactCreationSuccess(SOUND_TYPE, allowedSize);
		assertNoWarnings(gatewayV2HTTPResponse);
	}

	@Test
	public void testCreateVideoArtefactSuccess() throws Exception {
		Long allowedSize = Long.parseLong(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT) - 1;
		APIGatewayV2HTTPResponse gatewayV2HTTPResponse = testArtefactCreationSuccess(VIDEO_TYPE, allowedSize);
		assertNoWarnings(gatewayV2HTTPResponse);
	}

	private void assertNoWarnings(APIGatewayV2HTTPResponse gatewayV2HTTPResponse) throws JsonProcessingException {
		JsonNode jsonNodeBody = objectMapper.readTree(gatewayV2HTTPResponse.getBody());
		Assertions.assertFalse(jsonNodeBody.has(WARNING));
		Assertions.assertNull(jsonNodeBody.get(WARNING));

		// Assert that the contentLength and sizeWarning has been persisted or not in DB
		Artefact artefact = artefactService.getArtefactById(jsonNodeBody.get("artefactId").asText());
		Assertions.assertNotNull(artefact.getContentLength());
		Assertions.assertNull(artefact.getSizeWarning());
	}

	private void assertArtefactHasFileName(APIGatewayV2HTTPResponse gatewayV2HTTPResponse)
			throws JsonProcessingException {
		JsonNode jsonNodeBody = objectMapper.readTree(gatewayV2HTTPResponse.getBody());

		// Assert that the fileName persisted in DB
		Artefact artefact = artefactService.getArtefactById(jsonNodeBody.get("artefactId").asText());
		Assertions.assertNotNull(artefact.getArtefactName());
	}

	@Test
	public void testCreateAudioArtefactWithSizeWarning() throws Exception {
		Long beyondSizeLimit = Long.parseLong(SystemEnvironmentVariables.SOUND_FILE_SIZE_LIMIT) + 10;
		testSizeWarning(SOUND_TYPE, beyondSizeLimit);
	}

	@Test
	public void testCreateVideoArtefactWithSizeWarning() throws Exception {
		Long beyondSizeLimit = Long.parseLong(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT) + 10;
		testSizeWarning(VIDEO_TYPE, beyondSizeLimit);
	}

	private void testSizeWarning(String type, Long beyondSizeLimit) throws Exception {
		APIGatewayV2HTTPResponse gatewayV2HTTPResponse = testArtefactCreationSuccess(type, beyondSizeLimit);
		JsonNode jsonNodeBody = objectMapper.readTree(gatewayV2HTTPResponse.getBody());
		log.info("warning message {}", jsonNodeBody.get(WARNING).asText());
		Assertions.assertTrue(jsonNodeBody.has(WARNING));
		Assertions.assertNotNull(jsonNodeBody.get(WARNING).asText());

		// Assert that the contentLength and sizeWarning has been persisted or not in DB
		Artefact artefact = artefactService.getArtefactById(jsonNodeBody.get("artefactId").asText());
		Assertions.assertNotNull(artefact.getContentLength());
		Assertions.assertTrue(artefact.getSizeWarning());
	}

	@Test
	public void testSingleMultimediaFileWithSameDocId() throws Exception {
		testSingleFileWithSameDocId("12235", ArtefactClassType.MULTIMEDIA.name());
	}

	@Test
	public void testSingleLOGOFileWithSameDocId() throws Exception {
		testSingleFileWithSameDocId("12236", ArtefactClassType.COLOURLOGO.name());
	}

	@Test
	public void testSingleBWLOGOFileWithSameDocId() throws Exception {
		testSingleFileWithSameDocId("12237", ArtefactClassType.BWLOGO.name());
	}

	@Test
	public void testSingleSOUNDFileWithSameDocId() throws Exception {
		testSingleFileWithSameDocId("12238", ArtefactClassType.SOUND.name());
	}

	@Test
	public void testMultipleCertFileWithSameDocId() throws Exception {
		testMultipleFileWithSameDocId("12239", ArtefactClassType.CERTIFICATE.name());
	}

	@Test
	public void testMultipleDocFileWithSameDocId() throws Exception {
		testMultipleFileWithSameDocId("12240", ArtefactClassType.DOCUMENT.name());
	}

	private void testMultipleFileWithSameDocId(String mirisDocId, String classType) throws Exception {
		// verify the existing artefact and its status
		List<Artefact> existingArtefactList = artefactService.getArtefactbyMirisDocIdAndType(mirisDocId,
				List.of(classType));
		Assertions.assertNotNull(existingArtefactList);
		Artefact existingArtefact = existingArtefactList.stream().findFirst().get();
		Assertions.assertEquals(ArtefactStatus.INDEXED.name(), existingArtefact.getStatus());

		Long allowedSize = Long.parseLong(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT) - 1;
		ArtefactInput artefact = createArtefact(classType, allowedSize, mirisDocId);
		APIGatewayV2HTTPResponse response = testArtefactCreactionSuccess(artefact);
		JsonNode jsonNodeBody = objectMapper.readTree(response.getBody());
		String newArtefactId = jsonNodeBody.get("artefactId").asText();

		// verify the new artefact is INDEXED and existing in DELETED
		Artefact newArtefact = artefactService.getArtefactById(newArtefactId);
		Artefact existingArtefactAfterNewUpload = artefactService.getArtefactById(existingArtefact.getId());
		Assertions.assertEquals(mirisDocId, newArtefact.getMirisDocId());
		Assertions.assertEquals(classType, newArtefact.getArtefactClassType());
		Assertions.assertEquals(ArtefactStatus.INIT.name(), newArtefact.getStatus());
		Assertions.assertEquals(ArtefactStatus.INDEXED.name(), existingArtefactAfterNewUpload.getStatus());
	}

	private void testSingleFileWithSameDocId(String mirisDocId, String classType) throws Exception {
		// verify the existing artefact and its status
		List<Artefact> existingArtefactList = artefactService.getArtefactbyMirisDocIdAndType(mirisDocId,
				List.of(classType));
		Assertions.assertNotNull(existingArtefactList);
		Artefact existingArtefact = existingArtefactList.stream().findFirst().get();
		Assertions.assertEquals(ArtefactStatus.INDEXED.name(), existingArtefact.getStatus());

		Long allowedSize = Long.parseLong(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT) - 1;
		ArtefactInput artefact = createArtefact(classType, allowedSize, mirisDocId);
		APIGatewayV2HTTPResponse response = testArtefactCreactionSuccess(artefact);
		JsonNode jsonNodeBody = objectMapper.readTree(response.getBody());
		String newArtefactId = jsonNodeBody.get("artefactId").asText();

		// verify the new artefact is INDEXED and existing in DELETED
		Artefact newArtefact = artefactService.getArtefactById(newArtefactId);
		Artefact existingArtefactAfterNewUpload = artefactService.getArtefactById(existingArtefact.getId());
		Assertions.assertEquals(mirisDocId, newArtefact.getMirisDocId());
		Assertions.assertEquals(classType, newArtefact.getArtefactClassType());
		Assertions.assertEquals(ArtefactStatus.INDEXED.name(), newArtefact.getStatus());
		Assertions.assertEquals(ArtefactStatus.DELETED.name(), existingArtefactAfterNewUpload.getStatus());

	}

	private APIGatewayV2HTTPResponse testArtefactCreactionSuccess(ArtefactInput artefact) throws Exception {
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/createartefact")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getBody()));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.CREATED, (Objects.requireNonNull(result.getBody())).getStatusCode());
		return result.getBody();
	}

	@NotNull
	private APIGatewayV2HTTPResponse testArtefactCreationSuccess(String type, Long beyondSizeLimit) throws Exception {
		// Create an Artefact object
		ArtefactInput artefact = createArtefactInputWithType(type);
		ArtefactItemInput artefactItemInput = createArtefactInputWithSize(beyondSizeLimit);
		List<ArtefactItemInput> artefactItemInputList = List.of(artefactItemInput);
		artefact.setItems(artefactItemInputList);
		return testArtefactCreactionSuccess(artefact);
	}

	private ArtefactInput createArtefact(String type, Long size, String mirisDocId) {
		ArtefactInput input = createArtefactInputWithType(type);
		ArtefactItemInput artefactItemInput = createArtefactInputWithSize(size);
		List<ArtefactItemInput> artefactItemInputList = List.of(artefactItemInput);
		input.setItems(artefactItemInputList);
		input.setMirisDocId(mirisDocId);
		return input;
	}

	private ArtefactInput createArtefactInputWithType(String type) {
		ArtefactInput input = TestData.createArtefactInput();
		input.setArtefactClassType(type);
		return input;
	}

	private ArtefactItemInput createArtefactInputWithSize(Long size) {
		long byteConversionFactor = 1024 * 1024;
		ArtefactItemInput input = TestData.createArtefactInputItem();
		input.setContentLength(String.valueOf(size * byteConversionFactor));
		return input;
	}

	@Test
	public void createArtefactThroughFunctionInvoker() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "createArtefact");

		// Create an Artefact object
		ArtefactInput artefact = TestData.createArtefactInput();
		List<ArtefactItemInput> artefactItemInputList = List.of(TestData.createArtefactInputItem());
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();

		FunctionInvokeHelper invokeHelper = new FunctionInvokeHelper();
		APIGatewayV2HTTPResponse result = invokeHelper.invoke(event, APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	}

}
