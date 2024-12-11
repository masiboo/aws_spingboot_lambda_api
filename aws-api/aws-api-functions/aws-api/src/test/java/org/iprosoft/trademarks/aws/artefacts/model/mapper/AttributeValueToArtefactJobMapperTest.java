package org.wipo.trademarks.Aws.artefacts.model.mapper;

import org.junit.jupiter.api.Test;
import org.wipo.trademarks.Aws.artefacts.model.entity.ArtefactJob;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueToArtefactJobMapperTest {

	private final AttributeValueToArtefactJobMapper mapper = new AttributeValueToArtefactJobMapper();

	@Test
	public void testApply_withAllFields() {
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("jobId", AttributeValue.fromS("12345"));
		map.put("path", AttributeValue.fromS("test/path"));
		map.put("filename", AttributeValue.fromS("testFile.txt"));
		map.put("status", AttributeValue.fromS("ACTIVE"));
		map.put("s3_signed_url", AttributeValue.fromS("http://example.com/signedurl"));
		map.put("creationDate", AttributeValue.fromS(String.valueOf(new Date().getTime())));
		map.put("updatedDate", AttributeValue.fromS(String.valueOf(new Date().getTime())));
		map.put("artefactId", AttributeValue.fromS("67890"));
		map.put("requestId", AttributeValue.fromS("abcde"));
		map.put("batchSequence", AttributeValue.fromS("batch123"));

		Map<String, AttributeValue> additionalProperties = new HashMap<>();
		additionalProperties.put("property1", AttributeValue.fromS("value1"));
		additionalProperties.put("property2", AttributeValue.fromS("123"));
		map.put("additionalProperties", AttributeValue.fromM(additionalProperties));

		ArtefactJob artefactJob = mapper.apply(map);

		assertNotNull(artefactJob);
		assertEquals("12345", artefactJob.getId());
		assertEquals("test/path", artefactJob.getPath());
		assertEquals("testFile.txt", artefactJob.getFilename());
		assertEquals("ACTIVE", artefactJob.getStatus());
		assertEquals("http://example.com/signedurl", artefactJob.getS3SignedUrl());
		assertNotNull(artefactJob.getCreationDate());
		assertNotNull(artefactJob.getUpdatedDate());
		assertEquals("67890", artefactJob.getArtefactId());
		assertEquals("abcde", artefactJob.getRequestId());
		assertEquals("batch123", artefactJob.getBatchSequence());
	}

	@Test
	public void testApplyNullMap() {
		AttributeValueToArtefactJobMapper mapper = new AttributeValueToArtefactJobMapper();
		assertThrows(IllegalArgumentException.class, () -> mapper.apply(null));
	}

	@Test
	public void testApplyMissingKeys() {
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("jobId", AttributeValue.fromS("job123"));

		AttributeValueToArtefactJobMapper mapper = new AttributeValueToArtefactJobMapper();
		ArtefactJob result = mapper.apply(map);

		assertEquals("job123", result.getId());
		assertNull(result.getArtefactId());
		assertNull(result.getS3SignedUrl());
		assertNull(result.getStatus());
		assertNull(result.getBatchSequence());
	}

	@Test
	public void testApplyNullValues() {
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("jobId", AttributeValue.fromS("job123"));
		map.put("artefactId", null);
		map.put("s3_signed_url", null);
		map.put("jobStatus", null);
		map.put("batchSequence", null);

		AttributeValueToArtefactJobMapper mapper = new AttributeValueToArtefactJobMapper();
		ArtefactJob result = mapper.apply(map);

		assertEquals("job123", result.getId());
		assertNull(result.getArtefactId());
		assertNull(result.getS3SignedUrl());
		assertNull(result.getStatus());
		assertNull(result.getBatchSequence());
	}

}