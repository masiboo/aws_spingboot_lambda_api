package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.KEY_LOCKED;
import static org.junit.jupiter.api.Assertions.*;

class AttributeValueDB2ToBatchMapperTest {

	private final AttributeValueDB2ToBatchMapper mapper = new AttributeValueDB2ToBatchMapper();

	@Test
	void testApply_withValidAttributes() {
		// Arrange
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("batchSequence", AttributeValue.fromS("12345"));
		valueMap.put("batchStatus", AttributeValue.fromS("active"));
		valueMap.put("requestType", AttributeValue.fromS("update"));
		valueMap.put("user", AttributeValue.fromS("testUser"));
		valueMap.put("operator", AttributeValue.fromS("testOperator"));
		valueMap.put("reportDate", AttributeValue.fromS("2024-06-18T10:15:30Z"));
		valueMap.put("reportUrl", AttributeValue.fromS("http://example.com/report"));
		valueMap.put("s3Bucket", AttributeValue.fromS("testBucket"));
		valueMap.put("s3Key", AttributeValue.fromS("testKey"));
		valueMap.put(KEY_LOCKED, AttributeValue.fromBool(true));

		// Act
		BatchOutput result = mapper.apply(valueMap);

		// Assert
		assertNotNull(result);
		assertEquals("12345", result.getId());
		assertEquals("12345", result.getBatchSequence());
		assertEquals("active", result.getStatus());
		assertEquals("update", result.getRequestType());
		assertEquals("testUser", result.getUser());
		assertNotNull(result.getOperator());
		assertEquals("testOperator", result.getOperator().getUsername());
		assertEquals("2024-06-18T10:15:30Z", result.getReportDate());
		assertEquals("http://example.com/report", result.getReportUrl());
		assertEquals("testBucket", result.getS3Bucket());
		assertEquals("testKey", result.getS3Key());
		assertTrue(result.isLocked());
	}

	@Test
	void testApply_withNullMap() {
		Executable executable = () -> mapper.apply(null);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
		assertEquals("stringAttributeValueMap cannot be null", exception.getMessage());
	}

	@Test
	void testApply_withNullValuesInMap() {
		// Arrange
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("batchSequence", null);

		// Act
		BatchOutput result = mapper.apply(valueMap);

		// Assert
		assertNotNull(result);
		assertEquals("", result.getId());
		assertEquals("", result.getBatchSequence());
		assertNull(result.getStatus());
		assertNull(result.getRequestType());
		assertNull(result.getUser());
		assertNull(result.getOperator());
		assertNull(result.getReportDate());
		assertNull(result.getReportUrl());
		assertNull(result.getS3Bucket());
		assertNull(result.getS3Key());
		assertFalse(result.isLocked());
	}

}