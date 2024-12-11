package org.iprosoft.trademarks.aws.artefacts.service.artefactjob;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbPartiQ;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.ZonedDateTime;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

// set env REGISTRY_TABLE_NAME
@ExtendWith(EasyMockExtension.class)
public class ArtefactJobServiceImplTest {

	private DynamoDbClient dynamoDbClient;

	private ArtefactJobService artefactJobService;

	@BeforeEach
	void setUp() {
		dynamoDbClient = createMock(DynamoDbClient.class);
		artefactJobService = new ArtefactJobServiceImpl(dynamoDbClient);
	}

	@Test
	void testSaveJob() {
		// Arrange
		ArtefactJob job = new ArtefactJob();
		job.setId("jobId");
		job.setCreationDate(ZonedDateTime.parse("2024-06-21T17:59:47.239310816+02:00[Europe/Zurich]"));
		job.setArtefactId("artefactId");
		job.setStatus("status");
		job.setS3SignedUrl("url");
		job.setRequestId("requestId");
		job.setFilename("filename");
		job.setPath("path");
		job.setBatchSequence("123654.999");

		Map<String, AttributeValue> expectedValues = new HashMap<>();
		expectedValues.put("artefactId", AttributeValue.builder().s("artefactId").build());
		expectedValues.put("jobId", AttributeValue.builder().s("jobId").build());
		expectedValues.put("jobStatus", AttributeValue.builder().s("status").build());
		expectedValues.put("s3_signed_url", AttributeValue.builder().s("url").build());
		expectedValues.put("updatedDate", AttributeValue.builder().s("2024-06-21").build());
		expectedValues.put("requestId", AttributeValue.builder().s("requestId").build());
		expectedValues.put("type", AttributeValue.builder().s("job").build());
		expectedValues.put("filename", AttributeValue.builder().s("filename").build());
		expectedValues.put("path", AttributeValue.builder().s("path").build());
		expectedValues.put("insertedDate", AttributeValue.builder().s("2024-06-21T15:59:47.239310816Z").build()); // Note:
																													// Time
																													// in
																													// UTC
		expectedValues.put("batchSequence", AttributeValue.builder().s("123654.999").build());

		// Capture expected PutItemRequest
		Capture<PutItemRequest> capture = Capture.newInstance();

		// Define expected PutItemRequest and response
		expect(dynamoDbClient.putItem(capture(capture))).andReturn(PutItemResponse.builder().build()).once();

		// Activate mock behavior
		replay(dynamoDbClient);

		// Act
		artefactJobService.saveJob(job);

		// Assert: Verify that putItem was called exactly once with the expected request
		verify(dynamoDbClient);

		// Assert the captured PutItemRequest if necessary
		PutItemRequest capturedRequest = capture.getValue();

		// Assertions on capturedRequest
		assertEquals(SystemEnvironmentVariables.REGISTRY_TABLE_NAME, capturedRequest.tableName());
		assertEquals(expectedValues.get("artefactId"), capturedRequest.item().get("artefactId"));
		assertEquals(expectedValues.get("jobId"), capturedRequest.item().get("jobId"));
		assertEquals(expectedValues.get("jobStatus"), capturedRequest.item().get("jobStatus"));
		assertEquals(expectedValues.get("s3_signed_url"), capturedRequest.item().get("s3_signed_url"));
		assertEquals(expectedValues.get("updatedDate"), capturedRequest.item().get("updatedDate"));
		assertEquals(expectedValues.get("requestId"), capturedRequest.item().get("requestId"));
		assertEquals(expectedValues.get("type"), capturedRequest.item().get("type"));
		assertEquals(expectedValues.get("filename"), capturedRequest.item().get("filename"));
		assertEquals(expectedValues.get("path"), capturedRequest.item().get("path"));
		assertEquals(expectedValues.get("batchSequence"), capturedRequest.item().get("batchSequence"));
	}

	@Test
	void testGetJobStatus() {
		// Arrange
		String jobId = "123456";
		String status = "UPLOADED";
		String filename = "filename";
		String requestId = "987654";

		Map<String, AttributeValue> result = new HashMap<>();
		result.put("jobId", AttributeValue.builder().s(jobId).build());
		result.put("status", AttributeValue.builder().s(status).build());
		result.put("filename", AttributeValue.builder().s(filename).build());
		result.put("requestId", AttributeValue.builder().s(requestId).build());

		expect(dynamoDbClient.getItem(anyObject(GetItemRequest.class)))
			.andReturn(GetItemResponse.builder().item(result).build())
			.once();

		replay(dynamoDbClient);

		// Act
		ArtefactJob artefactJob = artefactJobService.getJobStatus(jobId);

		// Assert
		assertNotNull(artefactJob);
		assertEquals(jobId, artefactJob.getId());
		assertEquals(status, artefactJob.getStatus());
		assertEquals(filename, artefactJob.getFilename());
		assertEquals(requestId, artefactJob.getRequestId());
		verify(dynamoDbClient);
	}

	@Test
	public void testUpdateJobWithStatus() {
		// Arrange
		String jobId = "123";
		String status = "INDEXED";
		Map<String, AttributeValue> keys = new HashMap<>();
		keys.put("jobId", AttributeValue.builder().s(jobId).build());
		keys.put("status", AttributeValue.builder().s(status).build());

		GetItemResponse getItemResponse = GetItemResponse.builder().item(keys).build();
		EasyMock.expect(dynamoDbClient.getItem(EasyMock.anyObject(GetItemRequest.class)))
			.andReturn(getItemResponse)
			.once();

		UpdateItemResponse updateItemResponse = UpdateItemResponse.builder().attributes(keys).build();
		EasyMock.expect(dynamoDbClient.updateItem(EasyMock.anyObject(UpdateItemRequest.class)))
			.andReturn(updateItemResponse)
			.once();

		EasyMock.replay(dynamoDbClient);

		// Act
		UpdateItemResponse response = artefactJobService.updateJobWithStatus(jobId, status);

		// Assert
		EasyMock.verify(dynamoDbClient);
		assertNotNull(response);
		assertEquals(jobId, keys.get("jobId").s());
		assertEquals(status, keys.get("status").s());
	}

	// We must set environment available REGISTRY_TABLE_NAME=REGISTRY_TABLE_NAME
	@Test
	public void testGetAllJobStatusByRequestId() {
		// Arrange
		String requestId = "d6621579-76bc-41be-874e-449ee975b83d";
		ArtefactJobService artefactJobService = new ArtefactJobServiceImpl(dynamoDbClient);
		try (MockedStatic<DynamoDbPartiQ> mockedStatic = mockStatic(DynamoDbPartiQ.class)) {
			List<ArtefactJob> artefactJobs = TestData.getArtefactJobsNotNull();
			mockedStatic.when(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(), anyString(), anyString()))
				.thenReturn(artefactJobs);

			// Act
			Map<String, Object> result = artefactJobService.getAllJobStatusByRequestId(requestId);

			// Assert
			assertEquals(4, result.size());
			assertEquals(requestId, result.get("requestId"));
			assertEquals("060624.999", result.get("batchSequence"));
			assertEquals("INIT", result.get("batchStatus"));
		}
	}

	// We must set environment available REGISTRY_TABLE_NAME=REGISTRY_TABLE_NAME
	@Test
	public void testGetAllJobStatusByRequestIdWithMixOfNullJobs() {
		// Arrange
		String requestId = "d6621579-76bc-41be-874e-449ee975b83d";
		ArtefactJobService artefactJobService = new ArtefactJobServiceImpl(dynamoDbClient);
		try (MockedStatic<DynamoDbPartiQ> mockedStatic = mockStatic(DynamoDbPartiQ.class)) {
			List<ArtefactJob> artefactJobs = TestData.getArtefactJobListMixOfNull();
			mockedStatic.when(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(), anyString(), anyString()))
				.thenReturn(artefactJobs);

			// Act
			Map<String, Object> result = artefactJobService.getAllJobStatusByRequestId(requestId);

			// Assert
			assertEquals(4, result.size());
			assertEquals(requestId, result.get("requestId"));
			assertNull(result.get("batchSequence"));
			assertEquals("INIT", result.get("batchStatus"));
		}
	}

	// We must set environment available REGISTRY_TABLE_NAME=REGISTRY_TABLE_NAME
	@Test
	public void testGetAllBulkJobStatusByRequestId() {
		// Arrange
		String requestId = "d6621579-76bc-41be-874e-449ee975b83d";
		ArtefactJobService artefactJobService = new ArtefactJobServiceImpl(dynamoDbClient);
		try (MockedStatic<DynamoDbPartiQ> mockedStatic = mockStatic(DynamoDbPartiQ.class)) {
			List<ArtefactJob> artefactJobs = TestData.getBulkArtefactJobsNotNull();
			mockedStatic.when(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(), anyString(), anyString()))
				.thenReturn(artefactJobs);

			// Act
			Map<String, Object> result = artefactJobService.getAllBulkJobStatusByRequestId(requestId);

			// Assert
			assertEquals(2, result.size());
			assertEquals(requestId, result.get("requestId"));
		}
	}

	// We must set environment available REGISTRY_TABLE_NAME=REGISTRY_TABLE_NAME
	@Test
	public void testGetAllBulkJobStatusByRequestIdWithMixOfNullJobs() {
		// Arrange
		String requestId = "d6621579-76bc-41be-874e-449ee975b83d";
		ArtefactJobService artefactJobService = new ArtefactJobServiceImpl(dynamoDbClient);
		try (MockedStatic<DynamoDbPartiQ> mockedStatic = mockStatic(DynamoDbPartiQ.class)) {
			List<ArtefactJob> artefactJobs = TestData.getBulkArtefactJobListMixOfNull();
			mockedStatic.when(() -> DynamoDbPartiQ.getAllArtefactJobsByRequestId(any(), anyString(), anyString()))
				.thenReturn(artefactJobs);

			// Act
			Map<String, Object> result = artefactJobService.getAllBulkJobStatusByRequestId(requestId);

			// Assert
			assertEquals(2, result.size());
			assertEquals(requestId, result.get("requestId"));
		}
	}

}
