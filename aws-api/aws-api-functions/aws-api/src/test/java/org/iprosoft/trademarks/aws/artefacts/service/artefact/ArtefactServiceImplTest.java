package org.iprosoft.trademarks.aws.artefacts.service.artefact;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.easymock.Mock;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.model.entity.IArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;
import org.iprosoft.trademarks.aws.artefacts.service.miris.MirisService;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.QueryRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.createMock;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import org.mockito.Mockito;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(classes = { AwsServicesTestConfig.class })
//@Order(1)
//@ExtendWith(LocalstackDockerExtension.class)
//@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
//@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
class ArtefactServiceImplTest {

	@Mock
	private DynamoDbClient dynamoDbClient;

	@Mock
	private QueryRequestBuilder queryRequestBuilder;

	@Mock
	private MirisService mirisService;

	private ArtefactServiceImpl artefactServiceImpl;

	private ValidatorFactory mockValidatorFactory;

	private Validator mockValidator;

	/*
	 * @Mock private ArtefactApiClient artefactApiClient;
	 */

	@BeforeEach
	void setUp() {
		dynamoDbClient = createMock(DynamoDbClient.class);
		queryRequestBuilder = createMock(QueryRequestBuilder.class);
		mirisService = createMock(MirisService.class);
		// artefactApiClient = createMock(ArtefactApiClient.class);
		mockValidatorFactory = createMock(ValidatorFactory.class);
		mockValidator = createMock(Validator.class);
		artefactServiceImpl = partialMockBuilder(ArtefactServiceImpl.class)
			.addMockedMethods("getArtefactbyMirisDocId", "updateArtefactWithStatus", "isDocIdValid")
			.withConstructor(dynamoDbClient, queryRequestBuilder, mirisService/*
																				 * ,
																				 * artefactApiClient
																				 */)
			.createMock();
	}

	@Test
	void testHasLogoWithSameDocIdForNonLogo() {
		// arrange
		String mirisDocId = "123456";
		String classType = ArtefactClassType.MULTIMEDIA.name();
		List<Artefact> existingArtefacts = TestData.getMultimediaArtefactList();
		expect(artefactServiceImpl.getArtefactbyMirisDocId(mirisDocId)).andReturn(existingArtefacts);
		artefactServiceImpl.updateArtefactWithStatus(anyString(), eq(ArtefactStatus.DELETED.toString()));
		expectLastCall().anyTimes();
		replay(artefactServiceImpl);

		// act
		boolean result = artefactServiceImpl.hasFileWithSameDocId(mirisDocId, classType);

		// assert
		assertTrue(result);
	}

	@Test
	void testHasLogoWithSameDocId() {
		// Arrange
		String mirisDocId = "123456";
		String classType = ArtefactClassType.BWLOGO.name();
		List<Artefact> existingArtefacts = TestData.getAllTypeArtefactList();
		expect(artefactServiceImpl.getArtefactbyMirisDocId(mirisDocId)).andReturn(existingArtefacts);
		artefactServiceImpl.updateArtefactWithStatus(anyString(), eq(ArtefactStatus.DELETED.toString()));
		expectLastCall().anyTimes();
		replay(artefactServiceImpl);
		// Act
		boolean result = artefactServiceImpl.hasFileWithSameDocId(mirisDocId, classType);
		// Assert
		verify(artefactServiceImpl);
		assertTrue(result);
	}

	@Test
	void testMultimediaMustNotReplaceLogo() {
		// Arrange
		String mirisDocId = "123456";
		String classType = ArtefactClassType.BWLOGO.name();
		List<Artefact> existingArtefacts = TestData.getMultimediaArtefactList();
		expect(artefactServiceImpl.getArtefactbyMirisDocId(mirisDocId)).andReturn(existingArtefacts);
		artefactServiceImpl.updateArtefactWithStatus(anyString(), eq(ArtefactStatus.DELETED.toString()));
		expectLastCall().anyTimes();
		replay(artefactServiceImpl);
		// Act
		boolean result = artefactServiceImpl.hasFileWithSameDocId(mirisDocId, classType);
		// Assert
		verify(artefactServiceImpl);
		assertFalse(result);
	}

	@Test
	void testLogoMustNotReplaceMultimedia() {
		// Arrange
		String mirisDocId = "123456";
		String classType = ArtefactClassType.SOUND.name();
		List<Artefact> existingArtefacts = TestData.getLogoArtefactList();
		expect(artefactServiceImpl.getArtefactbyMirisDocId(mirisDocId)).andReturn(existingArtefacts);
		artefactServiceImpl.updateArtefactWithStatus(anyString(), eq(ArtefactStatus.DELETED.toString()));
		expectLastCall().anyTimes();
		replay(artefactServiceImpl);
		// Act
		boolean result = artefactServiceImpl.hasFileWithSameDocId(mirisDocId, classType);
		// Assert
		verify(artefactServiceImpl);
		assertFalse(result);
	}

	@Test
	public void testValidateArtefactInputArtefactNameIsNull() {
		// Arrange
		ArtefactInput artefactInput = new ArtefactInput();
		artefactInput.setArtefactClassType("DOCUMENT");
		artefactInput.setMirisDocId("12345");

		ArtefactItemInput item = new ArtefactItemInput();
		item.setContentType("application/pdf");
		item.setFilename("test.pdf");
		artefactInput.setItems(Arrays.asList(item));

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefact(artefactInput);

		// Assert
		assertTrue(errors.get("artefactName").equalsIgnoreCase("Name cannot be null"));
	}

	@Test
	public void testValidateArtefactInputArtefactClassTypeInvalid() {
		// Arrange
		ArtefactInput artefactInput = new ArtefactInput();
		artefactInput.setArtefactClassType("Invalid");
		artefactInput.setMirisDocId("12345");

		ArtefactItemInput item = new ArtefactItemInput();
		item.setContentType("application/pdf");
		item.setFilename("test.pdf");
		artefactInput.setItems(Arrays.asList(item));

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefact(artefactInput);

		// Assert
		assertTrue(errors.get("classType").contains("Artefact class type is not valid."));
		assertTrue(errors.get("artefactName").contains("Name cannot be null"));
	}

	@Test
	public void testValidateArtefactBatchNewRequestNoMirisDocIdValidation() {
		// Arrange
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.batchSequence("231654.5656")
			.user("test-user")
			.filename("filename")
			.artefactName("20221110.7000-00000000.TIF")
			.artefactClassType(ArtefactClassType.DOCUMENT.name())
			.type(ScannedAppType.NEW_REQUEST.name())
			.contentType("TIFF")
			.build();

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.NEW_REQUEST.name());

		// Assert
		assertFalse(errors.isEmpty());
		assertTrue(errors.get("filename")
			.contains("must be 8 digits followed by a dot after dot any accepted file extension"));
	}

	@Test
	public void testValidateArtefactBatchNewRequestWithParameterScannedAppAddendum() {
		// Arrange
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.batchSequence("231654.5656")
			.user("test-user")
			.filename("11223344.tif")
			.artefactName("20221110.7000-00000000.TIF")
			.artefactClassType(ArtefactClassType.DOCUMENT.name())
			.type(ScannedAppType.NEW_REQUEST.name())
			.contentType("TIFF")
			.build();

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.ADDENDUM.name());

		// Assert
		assertFalse(errors.isEmpty());
		// assertTrue(errors.get("scanedApp").contains("batchRequestType and scanedApp
		// must be same"));
	}

	@Test
	public void testValidateArtefactBatchAddendumMirisDocIdValidation() {
		// Arrange ADDENDUM
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.batchSequence("231654.5656")
			.user("test-user")
			.filename("filename")
			.artefactName("20221110.7000-00000000.TIF")
			.artefactClassType(ArtefactClassType.DOCUMENT.name())
			.type(ScannedAppType.ADDENDUM.name())
			.contentType("TIFF")
			.mirisDocId("123654")
			.build();

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.ADDENDUM.name());

		// Assert
		assertTrue(!errors.isEmpty());
		assertTrue(errors.get("filename")
			.contains("must be 8 digits followed by a dot after dot any accepted file extension"));
	}

	@Test
	public void testValidateArtefactBatchAddendumNoMirisDocIdValidationError() {
		// Arrange ADDENDUM
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.batchSequence("231654.5656")
			.user("test-user")
			.filename("filename")
			.artefactName("20221110.7000-00000000.TIF")
			.artefactClassType(ArtefactClassType.DOCUMENT.name())
			.type(ScannedAppType.ADDENDUM.name())
			.contentType("TIFF")
			.build();

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.ADDENDUM.name());

		// Assert
		assertTrue(errors.get("mirisDocId").equalsIgnoreCase("must be present"));
	}

	@Test
	void testValidateInputDocumentWhenInvalidContentType() {
		// arrange
		ArtefactInput artefactInput = TestData.createArtefactInput();
		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		ArtefactBatch artefactBatch = TestData.getArtefactBatch();
		artefactBatch.setContentType("application/xxx");
		// act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch, artefactBatch.getType());

		assertTrue(errors.get("classType").contains("ArtefactBatch class type is not valid"));
		assertTrue(errors.get("contentType").contains("not valid type"));
	}

	@Test
	void testValidateInputDocumentWhenContentTypeAndArtefactClassTypeMismatched() {
		// arrange
		ArtefactInput artefactInput = TestData.createInvalidArtefactInput();
		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());
		// act
		Map<String, String> errors = artefactServiceImpl.validateArtefact(artefactInput);
		// assert
		assertTrue(errors.get("MULTIMEDIA").contains("Supported content types are mp4, application/mp4, video/mp4."));
		assertTrue(errors.get("mirisDocId").contains("mirisDocId must be between 5 to 8 digits and no space allowed"));

	}

	@Test
	public void testValidateArtefactBatch() {
		// Arrange
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.batchSequence("")
			.user("")
			.filename("000000000.TIF")
			.artefactName("")
			.artefactClassType("XX-Class")
			.type("test")
			.contentType("")
			.mirisDocId("")
			.build();

		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.ADDENDUM.name());

		// Assert
		assertFalse(errors.isEmpty());
		assertTrue(errors.get("scannedAppType").contains("scannedAppType/type should be Addendum or New_Request."));
		assertTrue(errors.get("artefactName").contains("artefactName cannot be null or empty"));
		assertTrue(errors.get("filename")
			.contains("must be 8 digits followed by a dot after dot any accepted file extension"));
		assertTrue(errors.get("batchsequence").contains("must be present"));
		assertTrue(errors.get("mirisDocId").contains("mirisDocId must be between 5 to 8 digits and no space allowed"));
		assertTrue(errors.get("contentType").contains("must be present"));
		assertTrue(errors.get("classType").contains("ArtefactBatch class type is not valid"));
	}

	@Test
	public void testValidateArtefactInput() {
		// Arrange
		ArtefactInput artefactInput = TestData.createInvalidArtefactInput();
		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefact(artefactInput);

		// Assert
		assertFalse(errors.isEmpty());
		assertTrue(errors.get("item").contains("More than 1 item found. Only one item is allowed"));
		assertTrue(errors.get("mirisDocId").contains("mirisDocId must be between 5 to 8 digits and no space allowed"));
		assertTrue(errors.get("contentType").contains("must be present"));
		assertTrue(errors.get("filename").contains("Filename is not valid"));
	}

	@Test
	public void testValidateArtefactInputWithXml() {
		// Arrange
		ArtefactInput artefactInput = TestData.createWithXmlArtefactInput();
		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactInput)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefact(artefactInput);

		// Assert
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testValidateArtefactBatchClassTypeSound() {
		// Arrange
		ArtefactBatch artefactBatch = TestData.getArtefactBatch();
		expect(mockValidatorFactory.getValidator()).andReturn(mockValidator);
		expect(artefactServiceImpl.isDocIdValid(anyString())).andReturn(true);
		expect(mockValidator.validate(artefactBatch)).andReturn(Set.of());

		replay(mockValidatorFactory, mockValidator, artefactServiceImpl);

		// Act
		Map<String, String> errors = artefactServiceImpl.validateArtefactBatch(artefactBatch,
				ScannedAppType.ADDENDUM.name());

		// Assert
		assertFalse(errors.isEmpty());
		assertTrue(errors.get("classType").contains("ArtefactBatch class type is not valid"));
	}

	@Test
	void saveArtefactsWithTransactions_SuccessfulBatchWrite() {
		// Arrange
		List<IArtefact> artefacts = TestData.createMockArtefacts(5);
		BatchWriteItemResponse batchWriteItemResponse = BatchWriteItemResponse.builder().build();

		// Expecting a call to batchWriteItem and returning a mock response
		expect(dynamoDbClient.batchWriteItem((BatchWriteItemRequest) anyObject())).andReturn(batchWriteItemResponse)
			.anyTimes();

		// Switch to replay mode to validate the mock behavior
		replay(dynamoDbClient);

		// Act
		artefactServiceImpl.saveArtefactsWithTransactions(artefacts);

		// Assert
		verify(dynamoDbClient);
	}

	@Test
	void saveArtefactsWithTransactions_WithUnprocessedItems() {
		// Arrange
		List<IArtefact> artefacts = TestData.createMockArtefacts(10);

		BatchWriteItemResponse successResponse = BatchWriteItemResponse.builder()
			.unprocessedItems(Collections.emptyMap())
			.build();

		expect(dynamoDbClient.batchWriteItem((BatchWriteItemRequest) anyObject())).andReturn(successResponse)
			.anyTimes();
		replay(dynamoDbClient);

		// Act
		artefactServiceImpl.saveArtefactsWithTransactions(artefacts);

		// Assert
		// For the void method this ensures that the method under test interacted with the
		// mocked dynamoDbClient as expected
		verify(dynamoDbClient);
	}

	@Test
	void saveArtefactsWithTransactions_WithDynamoDbException() {
		// Arrange
		List<IArtefact> artefacts = TestData.createMockArtefacts(300);
		expect(dynamoDbClient.batchWriteItem((BatchWriteItemRequest) anyObject()))
			.andThrow(DynamoDbException.builder().build())
			.anyTimes();

		replay(dynamoDbClient);

		// Act
		artefactServiceImpl.saveArtefactsWithTransactions(artefacts);

		// Assert
		// Verify that the mock DynamoDB client was called as expected
		verify(dynamoDbClient);

	}

}
