package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueToBatchOutputMapperTest {

	private final AttributeValueToBatchOutputMapper mapper = new AttributeValueToBatchOutputMapper();

	@Test
	public void testApplySuccess() {
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("batchSequence", AttributeValue.fromS("12345"));
		map.put("batchStatus", AttributeValue.fromS("INSERTED"));
		map.put("operator", AttributeValue.fromS("operatorUser"));
		map.put("requestType", AttributeValue.fromS("typeA"));
		map.put("locked", AttributeValue.fromBool(true));

		BatchOutput result = mapper.apply(map);

		assertNotNull(result);
		assertEquals("12345", result.getBatchSequence());
		assertEquals("INSERTED", result.getStatus());
		assertEquals("operatorUser", result.getOperator().getUsername());
		assertEquals("typeA", result.getRequestType());
		assertTrue(result.isLocked());
	}

	@Test
	public void testApplyMissingValues() {
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("batchSequence", AttributeValue.fromS("12345"));

		BatchOutput result = mapper.apply(map);

		assertNotNull(result);
		assertEquals("12345", result.getBatchSequence());
		assertNull(result.getStatus());
		assertNull(result.getOperator());
		assertNull(result.getRequestType());
		assertFalse(result.isLocked());
	}

	@Test
	public void testApply_nullMap() {
		assertThrows(IllegalArgumentException.class, () -> {
			mapper.apply(null);
		});
	}

	@Test
	public void testApply_emptyMap() {
		Map<String, AttributeValue> map = new HashMap<>();

		BatchOutput result = mapper.apply(map);

		assertNotNull(result);
		assertNull(result.getBatchSequence());
		assertNull(result.getStatus());
		assertNull(result.getOperator());
		assertNull(result.getRequestType());
		assertFalse(result.isLocked());
	}

}