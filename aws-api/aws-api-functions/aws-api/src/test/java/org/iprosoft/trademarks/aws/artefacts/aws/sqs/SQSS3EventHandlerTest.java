package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.dto.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.service.metadata.MetadataService;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.S3, ServiceName.SQS, ServiceName.DYNAMO })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class SQSS3EventHandlerTest {

	private ArtefactService artefactService;

	private ArtefactJobService jobService;

	private BatchService batchService;

	private S3Service s3Service;

	private ObjectMapper objectMapper;

	private MetadataService metadataService;

	private MediaProcessingService mediaProcessingService;

	private SQSS3EventHandler sqss3EventHandler;

	private DynamoDbClient dynamoDbClient;

	private static WireMockServer wireMockServer;

	private final String S3_KEY_PH = "S3_KEY";

	// @BeforeAll
	static void setUp() {
		/*
		 * AwsServicesSetup.prepareDynamoDB(); AwsServicesSetup.prepareS3();
		 * AwsServicesSetup.putObject("1001/merged_artefact_documents.pdf",
		 * Map.of(AppConstants.METADATA_KEY_IS_MERGED_FILE, Boolean.toString(true),
		 * AppConstants.METADATA_KEY_ARTEFACT_ID, "4b917a69-c1e5-420e-8eb8-78195bdfa174",
		 * AppConstants.METADATA_KEY_BATCH_SEQ, "0221123.052"));
		 * AwsServicesSetup.putObject("1002/merged_artefact_documents.pdf",
		 * Map.of(AppConstants.METADATA_KEY_IS_MERGED_FILE, Boolean.toString(false),
		 * AppConstants.METADATA_KEY_ARTEFACT_ID, "4b917a69-c1e5-420e-8eb8-78195bdfa174",
		 * AppConstants.METADATA_KEY_BATCH_SEQ, "1001"));
		 * AwsServicesSetup.putObject("1003/verify_jobstatus_documents.pdf",
		 * Map.of(AppConstants.METADATA_KEY_IS_MERGED_FILE, Boolean.toString(false),
		 * AppConstants.METADATA_KEY_ARTEFACT_ID, "4b917a69-c1e5-420e-8eb8-78195bdmpd688",
		 * AppConstants.METADATA_KEY_BATCH_SEQ, "1001",
		 * AppConstants.METADATA_KEY_TRACE_ID, "4d37ad36-883b-4c64-b17f-mpd688"));
		 *
		 * AwsServicesSetup.populateDynamoDB(); AwsServicesSetup.populateDynamoDB(
		 * "Aws-table-batch-item-job-status-Awscore-down.json");
		 * AwsServicesSetup.populateDynamoDB("Aws-table-batch-status-event.json");
		 *
		 * // Start the WireMockServer on a random port wireMockServer = new
		 * WireMockServer(); wireMockServer.start(); WireMock.configureFor("localhost",
		 * wireMockServer.port())
		 */;
	}

	@BeforeEach
	void init() {
		metadataService = createMock(MetadataService.class);
		mediaProcessingService = createMock(MediaProcessingService.class);
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		s3Service = createMock(S3Service.class);
		artefactService = createMock(ArtefactService.class);
		jobService = createMock(ArtefactJobService.class);
		batchService = createMock(BatchService.class);
		dynamoDbClient = createMock(DynamoDbClient.class);
		sqss3EventHandler = new SQSS3EventHandler(metadataService, mediaProcessingService, objectMapper, s3Service,
				artefactService, jobService, batchService, dynamoDbClient);
	}

	// @AfterAll
	static void tearDown() {
		// Stop the WireMockServer after each test
		wireMockServer.stop();
	}

	@Test
	@Disabled
	public void testS3EventForMergedDocument() throws Exception {

		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSS3EventHandler");

		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:sqs-event/SQS_S3MergedFileEvent.json").toPath());

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);
		// Ensure a new entry added for the merged document in Dynamo DB
		List<ArtefactOutput> artefactList = batchService.getAllArtefactsForBatch("0221123.052", "artefact");
		Map<String, List<ArtefactBatch>> artefactMap = artefactList.stream()
			.map(artefactOutput -> artefactService.getArtefactById(artefactOutput.getId()))
			.filter(artefact -> artefact instanceof ArtefactBatch)
			.map(artefact -> (ArtefactBatch) artefact)
			.collect(Collectors.groupingBy(ArtefactBatch::getArtefactClassType));

		// verify parentId updated in child
		Assertions.assertNotNull(artefactMap);
		String parentId = artefactMap.get("DOCUMENT").get(0).getId();
		boolean childHasParentId = artefactMap.get("PART")
			.stream()
			.allMatch(artefact -> artefact.getArtefactMergeId().equalsIgnoreCase(parentId));
		Assertions.assertTrue(childHasParentId);
	}

	@Test
	@Disabled
	public void testMetadataServiceCall() throws Exception {

		Map<String, String> reqMap = Map.of("bucket", "unit-test-bucket", "key", "1002/merged_artefact_documents.pdf");
		String requestBody = objectMapper.writeValueAsString(reqMap);

		MultimediaFileResponse expectedResp = new MultimediaFileResponse();
		S3ObjectTags s3Tags = new S3ObjectTags();
		s3Tags.setFormat("MP4");
		s3Tags.setCodec("H264");
		s3Tags.setFrameRate("25.0");
		expectedResp.setS3ObjectTags(s3Tags);

		// stub creation metadata service
		wireMockServer.stubFor(post(urlMatching("/api/v1/multimedia/metadata"))
			.withHeader("Content-Type", matching("application/json"))
			.withRequestBody(containing(requestBody))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(objectMapper.writeValueAsString(expectedResp))));

		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSS3EventHandler");

		String jsonEventPayload = getEventPayload(reqMap.get("key"));

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);

		GetObjectTaggingResponse taggingResponse = s3Service.getObjectTags(reqMap.get("bucket"), reqMap.get("key"));
		Map<String, String> s3TagMap = taggingResponse.tagSet()
			.stream()
			.collect(Collectors.toMap(Tag::key, Tag::value));

		Assertions.assertNotNull(s3TagMap);
		assertEquals("MP4", s3TagMap.get("format"));
		assertEquals("H264", s3TagMap.get("codec"));
		assertEquals("25.0", s3TagMap.get("frameRate"));

	}

	private String getEventPayload(String key) throws IOException {
		String jsonContent = Files.readString(ResourceUtils.getFile("classpath:sqs-event/SQS_S3Event.json").toPath());
		return jsonContent.replaceAll(S3_KEY_PH, key);
	}

	@Test
	@Disabled
	public void verifyArtefactAndJobStatus_When_mediaProcessor_down() throws Exception {
		Map<String, String> reqMap = Map.of("bucket", "unit-test-bucket", "key", "1003/verify_jobstatus_documents.pdf");
		String requestBody = objectMapper.writeValueAsString(reqMap);
		// stub creation metadata service
		wireMockServer.stubFor(post(urlMatching("/api/v1/multimedia/metadata"))
			.withHeader("Content-Type", matching("application/json"))
			.withRequestBody(containing(requestBody))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
				.withBody("503 Service Temporarily unavailable")));

		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSS3EventHandler");

		String jsonEventPayload = getEventPayload(reqMap.get("key"));

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);

		GetObjectTaggingResponse taggingResponse = s3Service.getObjectTags(reqMap.get("bucket"), reqMap.get("key"));
		Map<String, String> s3TagMap = taggingResponse.tagSet()
			.stream()
			.collect(Collectors.toMap(Tag::key, Tag::value));

		// verify no tag has been added
		Assertions.assertTrue(s3TagMap.isEmpty());

		S3ObjectMetadata md = s3Service.getObjectMetadata(reqMap.get("bucket"), reqMap.get("key"));
		String artefactId = md.getMetadata().get(AppConstants.METADATA_KEY_ARTEFACT_ID);
		String jobId = md.getMetadata().get(AppConstants.METADATA_KEY_TRACE_ID);
		String artefactStatus = artefactService.getArtefactById(artefactId).getStatus();

		// Verify the ArtefactStatus retained to INIT and JobStatus marked as ERROR
		Assertions.assertEquals(ArtefactStatus.INIT.getStatus(), artefactStatus);
		Assertions.assertEquals(ArtefactStatus.ERROR.getStatus(), jobService.getJobStatus(jobId).getStatus());

	}

	@Test
	public void testApplySuccess() throws Exception {
		// Arrange
		SQSEvent sqsEvent = new SQSEvent();
		SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
		message.setBody("{\"detail\": {\"bucket\": {\"name\": \"testBucket\"}, \"object\": {\"key\": \"testKey\"}}}");
		sqsEvent.setRecords(List.of(message));

		S3EventDTO mockS3EventDTO = new S3EventDTO();
		Detail mockDetail = new Detail();
		Bucket mockBucket = new Bucket();
		org.iprosoft.trademarks.aws.artefacts.model.dto.Object mockObject = new org.iprosoft.trademarks.aws.artefacts.model.dto.Object();

		mockBucket.setName("testBucket");
		mockObject.setKey("testKey");
		mockDetail.setBucket(mockBucket);
		mockDetail.setObject(mockObject);
		mockS3EventDTO.setDetail(mockDetail);

		S3ObjectMetadata mockMetadata = new S3ObjectMetadata();
		Map<String, String> mockMetadataMap = new HashMap<>();
		mockMetadataMap.put(AppConstants.METADATA_KEY_IS_MERGED_FILE, "false");
		mockMetadataMap.put(AppConstants.METADATA_KEY_ARTEFACT_ID, "artefactId");
		mockMetadata.setMetadata(mockMetadataMap);
		mockMetadata.setObjectExists(true);
		mockMetadata.setContentLength(100L);

		Artefact mockArtefact = new Artefact();
		mockArtefact.setArtefactClassType("SOUND");

		expect(s3Service.getObjectMetadata("testBucket", "testKey")).andReturn(mockMetadata);
		expect(metadataService.isMetadataValid(mockMetadataMap)).andReturn(true);
		expect(artefactService.getArtefactById(anyString())).andReturn(mockArtefact);
		ArtefactBatch artefactBatch = TestData.getArtefactBatch();
		expect(artefactService.getArtectBatchById(anyString())).andReturn(artefactBatch);

		expect(mediaProcessingService.insertMultimediaMetadata(anyObject(Map.class)))
			.andReturn(new MultimediaFileResponse());
		s3Service.setObjectTag(anyString(), anyString(), anyObject(Map.class));
		expectLastCall().anyTimes();
		artefactService.updateArtefact(anyString(), anyObject(Map.class));
		expectLastCall().andVoid().anyTimes();
		artefactService.updateArtefactWithStatus(anyString(), anyString());
		expectLastCall().andVoid().anyTimes();
		UpdateItemResponse updateItemResponse = UpdateItemResponse.builder().build();
		expect(jobService.updateJobWithStatus(anyString(), anyString())).andReturn(updateItemResponse);
		replay(s3Service, metadataService, mediaProcessingService, artefactService, jobService);

		// Act
		String result = sqss3EventHandler.apply(sqsEvent);

		// Assert
		assertEquals("", result);
	}

}