package org.wipo.trademarks.Aws.artefacts.service.batch;

import org.easymock.EasyMockExtension;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EasyMockExtension.class)
class BatchServiceImplTest {

	private DynamoDbClient dynamoDbClient;

	private ArtefactService artefactService;

	private BatchServiceImpl batchService;

	@BeforeEach
	void setUp() {
		dynamoDbClient = createMock(DynamoDbClient.class);
		artefactService = createMock(ArtefactService.class);
		batchService = new BatchServiceImpl(dynamoDbClient, artefactService);
	}

	@Test
	public void testSaveBatchSequenceWithChildren_Success() {
		// Arrange
		BatchInputDynamoDb batch = TestData.getBatchInputDynamoDb();
		PutItemResponse putItemResponse = PutItemResponse.builder().build();
		expect(dynamoDbClient.putItem((PutItemRequest) anyObject())).andReturn(putItemResponse).anyTimes();
		replay(dynamoDbClient);
		// Act
		batchService.saveBatchSequenceWithChildren(batch);
		// Assert and verify
		verify(dynamoDbClient);
	}

	@Test
	public void testSaveBatchSequenceWithChildren_WhenChildrenNull_Success() {
		// Arrange
		BatchInputDynamoDb batch = TestData.getBatchInputDynamoDb();
		batch.setArtefacts(null);
		PutItemResponse putItemResponse = PutItemResponse.builder().build();
		expect(dynamoDbClient.putItem((PutItemRequest) anyObject())).andReturn(putItemResponse).anyTimes();
		replay(dynamoDbClient);
		// Act
		batchService.saveBatchSequenceWithChildren(batch);
		// Assert and verify
		verify(dynamoDbClient);
	}

	@Test
	public void testSaveBatchSequenceWithChildren_Failure() {
		// Arrange
		BatchInputDynamoDb batch = TestData.getBatchInputDynamoDb();
		expect(dynamoDbClient.putItem(anyObject(PutItemRequest.class)))
			.andThrow(DynamoDbException.builder().message("DynamoDB error").build());
		replay(dynamoDbClient);

		// Act and Assert
		assertThrows(DynamoDbException.class, () -> {
			batchService.saveBatchSequenceWithChildren(batch);
		});
		// Assert and verify
		verify(dynamoDbClient);
	}

}