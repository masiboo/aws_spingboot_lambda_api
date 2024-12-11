package org.wipo.trademarks.Aws.artefacts.model.mapper;

import org.junit.jupiter.api.Test;
import org.wipo.trademarks.Aws.artefacts.model.entity.ArtefactBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueToBatchedArtefactMapperTest {

	private final AttributeValueToBatchedArtefactMapper mapper = new AttributeValueToBatchedArtefactMapper();

	@Test
	public void testApplyWithAllFields() {
		Map<String, AttributeValue> valueMap = new HashMap<>();

		valueMap.put("id", AttributeValue.fromS("id1"));
		valueMap.put("artefactId", AttributeValue.fromS("artefactId1"));
		valueMap.put("type", AttributeValue.fromS("type1"));
		valueMap.put("fileName", AttributeValue.fromS("fileName1"));
		valueMap.put("path", AttributeValue.fromS("path1"));
		valueMap.put("contentType", AttributeValue.fromS("contentType1"));
		valueMap.put("creationDate", AttributeValue.fromS("2023-01-01T00:00:00Z"));
		valueMap.put("userId", AttributeValue.fromS("user1"));
		valueMap.put("jobId", AttributeValue.fromS("job1"));
		valueMap.put("artefactName", AttributeValue.fromS("artefactName1"));
		valueMap.put("s3Bucket", AttributeValue.fromS("bucket1"));
		valueMap.put("s3Key", AttributeValue.fromS("key1"));
		valueMap.put("s3Url", AttributeValue.fromS("s3Url"));
		valueMap.put("status", AttributeValue.fromS("status1"));
		valueMap.put("mirisDocId", AttributeValue.fromS("doc1"));
		valueMap.put("contentLength", AttributeValue.fromS("12345"));
		valueMap.put("sizeWarning", AttributeValue.fromBool(true));
		valueMap.put("MERGED_ARTEFACT_ID", AttributeValue.fromS("mergedId1"));
		valueMap.put("pageNumber", AttributeValue.fromS("1"));
		valueMap.put("artefactMergeId", AttributeValue.fromS("mergeId1"));
		valueMap.put("artefaactItemFileName", AttributeValue.fromS("itemFileName1"));
		valueMap.put("BatchSequenceId", AttributeValue.fromS("1326564.999"));
		valueMap.put("requestType", AttributeValue.fromS("requestType"));
		valueMap.put("validationStatus", AttributeValue.fromS("OK"));

		ArtefactBatch artefactBatch = mapper.apply(valueMap);

		assertNotNull(artefactBatch);
		assertEquals("artefactId1", artefactBatch.getArtefactId());
		assertEquals("path1", artefactBatch.getPath());
		assertEquals("contentType1", artefactBatch.getContentType());
		assertEquals("2023-01-01T00:00:00Z", artefactBatch.getCreationDate());
		assertEquals("user1", artefactBatch.getUser());
		assertEquals("job1", artefactBatch.getJobId());
		assertEquals("artefactName1", artefactBatch.getArtefactName());
		assertEquals("bucket1", artefactBatch.getS3Bucket());
		assertEquals("key1", artefactBatch.getS3Key());
		assertEquals("status1", artefactBatch.getStatus());
		assertEquals("doc1", artefactBatch.getMirisDocId());
		assertEquals("12345", artefactBatch.getContentLength());
		assertTrue(artefactBatch.getSizeWarning());
		assertEquals("mergeId1", artefactBatch.getArtefactMergeId());
		assertEquals("1", artefactBatch.getPage());
		assertEquals("itemFileName1", artefactBatch.getArtefactItemFileName());
		assertEquals("1326564.999", artefactBatch.getBatchSequence());
	}

	@Test
	public void testApplyWithMissingFields() {
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("id", AttributeValue.fromS("id1"));

		ArtefactBatch artefactBatch = mapper.apply(valueMap);

		assertNotNull(artefactBatch);
		assertNull(artefactBatch.getArtefactClassType());
		assertNull(artefactBatch.getFilename());
		assertNull(artefactBatch.getPath());
		assertNull(artefactBatch.getContentType());
		assertNotNull(artefactBatch.getCreationDate());
		assertNull(artefactBatch.getUser());
		assertNull(artefactBatch.getJobId());
		assertNull(artefactBatch.getArtefactName());
		assertNull(artefactBatch.getS3Bucket());
		assertNull(artefactBatch.getS3Key());
		assertNull(artefactBatch.getStatus());
		assertNull(artefactBatch.getMirisDocId());
		assertNull(artefactBatch.getContentLength());
		assertNull(artefactBatch.getSizeWarning());
		assertNull(artefactBatch.getArtefactMergeId());
		assertNull(artefactBatch.getPage());
		assertNull(artefactBatch.getArtefactItemFileName());
		assertNull(artefactBatch.getBatchSequence());
	}

	@Test
	public void testApplyNullValueMap() {
		assertThrows(IllegalArgumentException.class, () -> {
			mapper.apply(null);
		});
	}

}