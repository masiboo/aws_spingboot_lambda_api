package org.wipo.trademarks.Aws.artefacts.model.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.wipo.trademarks.Aws.artefacts.TestData;
import org.wipo.trademarks.Aws.artefacts.model.entity.Artefact;
import org.wipo.trademarks.Aws.artefacts.model.entity.ArtefactItemTags;
import org.wipo.trademarks.Aws.artefacts.model.entity.Items;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueToArtefactMapperTest {

	@InjectMocks
	private AttributeValueToArtefactMapper mapper;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testApplyWithNullValueMap() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			mapper.apply(null);
		});
		assertEquals("valueMap cannot be null", exception.getMessage());
	}

	@Test
	public void testApplyWithAllArtefactAttributes() {
		// Arrange
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("artefactId", AttributeValue.fromS("12345"));
		valueMap.put("type", AttributeValue.fromS("CERTIFICATE"));
		valueMap.put("fileName", AttributeValue.fromS("testFile.txt"));
		valueMap.put("s3Bucket", AttributeValue.fromS("testBucket"));
		valueMap.put("s3Key", AttributeValue.fromS("testKey"));
		valueMap.put("status", AttributeValue.fromS("active"));
		valueMap.put("mirisDocId", AttributeValue.fromS("87654321"));
		valueMap.put("contentLength", AttributeValue.fromN("1024"));
		valueMap.put("sizeWarning", AttributeValue.fromBool(true));
		valueMap.put("MERGED_ARTEFACT_ID", AttributeValue.fromS("merged123"));

		// Act
		Artefact artefact = mapper.apply(valueMap);
		// Assert
		assertNotNull(artefact);
		assertEquals("12345", artefact.getId());
		assertEquals("CERTIFICATE", artefact.getArtefactClassType());
		assertEquals("testFile.txt", artefact.getArtefactName());
		assertEquals("testBucket", artefact.getS3Bucket());
		assertEquals("testKey", artefact.getS3Key());
		assertEquals("active", artefact.getStatus());
		assertEquals("87654321", artefact.getMirisDocId());
		assertEquals("1024", artefact.getContentLength());
		assertTrue(artefact.getSizeWarning());

	}

	@Test
	public void testApplyWithNullAttributeValues() {
		// Arrange
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("artefactId", null);
		valueMap.put("type", AttributeValue.fromS(null));
		valueMap.put("fileName", null);
		valueMap.put("s3Bucket", null);
		valueMap.put("s3Key", null);
		valueMap.put("status", null);
		valueMap.put("mirisDocId", null);
		valueMap.put("contentLength", null);
		valueMap.put("sizeWarning", null);
		valueMap.put("MERGED_ARTEFACT_ID", null);
		valueMap.put("artefactItemTags", null);
		valueMap.put("artefactItem", null);

		// Act
		Artefact artefact = mapper.apply(valueMap);
		// Assert
		assertNotNull(artefact);
		assertNull(artefact.getId());
		assertNull(artefact.getArtefactClassType());
		assertNull(artefact.getArtefactName());
		assertNull(artefact.getS3Bucket());
		assertNull(artefact.getS3Key());
		assertNull(artefact.getStatus());
		assertNull(artefact.getMirisDocId());
		assertNull(artefact.getContentLength());
		assertNull(artefact.getSizeWarning());
		assertNull(artefact.getArtefactItemTags());
		assertNull(artefact.getItems());
	}

}