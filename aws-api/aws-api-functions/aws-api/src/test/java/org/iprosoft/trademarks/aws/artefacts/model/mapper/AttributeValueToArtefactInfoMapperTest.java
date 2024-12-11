package org.wipo.trademarks.Aws.artefacts.model.mapper;

import org.junit.jupiter.api.Test;
import org.wipo.trademarks.Aws.artefacts.model.dto.ArtefactMetadata;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueToArtefactInfoMapperTest {

	private final AttributeValueToArtefactInfoMapper mapper = new AttributeValueToArtefactInfoMapper();

	@Test
	void testApply_withValidAttributes() {
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("artefactId", AttributeValue.fromS("12345"));
		valueMap.put("fileType", AttributeValue.fromS("PDF"));
		valueMap.put("bitDepth", AttributeValue.fromS("24"));
		valueMap.put("contentLength", AttributeValue.fromN("1024"));
		valueMap.put("mediaType", AttributeValue.fromS("image/jpeg"));
		valueMap.put("samplingFrequency", AttributeValue.fromS("44100"));
		valueMap.put("resolutionInDpi", AttributeValue.fromS("300"));
		valueMap.put("sizeWarning", AttributeValue.fromBool(true));

		ArtefactMetadata artefactMetadata = mapper.apply(valueMap);

		assertNotNull(artefactMetadata);
		assertEquals("12345", artefactMetadata.getArtefactId());
		assertEquals("PDF", artefactMetadata.getFileType());
		assertEquals("24", artefactMetadata.getBitDepth());
		assertEquals("1024", artefactMetadata.getSize());
		assertEquals("image/jpeg", artefactMetadata.getMediaType());
		assertEquals("44100", artefactMetadata.getSamplingFrequency());
		assertEquals("300", artefactMetadata.getResolutionInDpi());
		assertTrue(artefactMetadata.isSizeWarning());
	}

	@Test
	void testApply_withMissingOptionalAttributes() {
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("artefactId", AttributeValue.fromS("12345"));

		ArtefactMetadata result = mapper.apply(valueMap);

		assertNotNull(result);
		assertEquals("12345", result.getArtefactId());
		assertNull(result.getFileType());
		assertNull(result.getBitDepth());
		assertNull(result.getSize());
		assertNull(result.getMediaType());
		assertNull(result.getSamplingFrequency());
		assertNull(result.getResolutionInDpi());
		assertFalse(result.isSizeWarning());
	}

	@Test
	void testApply_withNullMap() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.apply(null));
		assertEquals("attributeValueMap cannot be null", exception.getMessage());
	}

	@Test
	void testApply_withNullValuesInMap() {
		Map<String, AttributeValue> valueMap = new HashMap<>();
		valueMap.put("artefactId", null);
		valueMap.put("fileType", null);

		ArtefactMetadata result = mapper.apply(valueMap);

		assertNotNull(result);
		assertNull(result.getArtefactId());
		assertNull(result.getFileType());
	}

}