package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DDBEventArtefactToArtefactEntityMapperTest {

	private final DDBEventArtefactToArtefactEntityMapper mapper = new DDBEventArtefactToArtefactEntityMapper();

	@Test
	public void testApplySuccess() {
		DDBEventArtefact ddbEventArtefact = new DDBEventArtefact();
		ddbEventArtefact.setStatus("INDEXED");
		ddbEventArtefact.setInsertedDate("2024-06-18T15:30:00Z");
		ddbEventArtefact.setS3Bucket("my-bucket");
		ddbEventArtefact.setMirisDocId("miris123");
		ddbEventArtefact.setS3Key("my-key");

		ArtefactsDTO result = mapper.apply(ddbEventArtefact);

		assertNotNull(result);
		assertEquals(ArtefactStatusEnum.INDEXED, result.getStatus());

		ZonedDateTime expectedDate = SafeParserUtil.safeParseZonedDateTime("2024-06-18T15:30:00Z");

		assertEquals(expectedDate, result.getIndexationDate());
		assertEquals(expectedDate, result.getArchiveDate());
		assertEquals("my-bucket", result.getS3Bucket());
		assertEquals("miris123", result.getMirisDocId());

		List<ArtefactItemsDTO> items = result.getArtefactItems();
		assertNotNull(items);
		assertEquals(1, items.size());

		ArtefactItemsDTO item = items.get(0);
		assertEquals("my-key", item.getS3Key());
		assertEquals("contentType", item.getContentType());
		assertEquals(1L, item.getTotalPages());
		assertEquals("fileName", item.getFileName());
		assertEquals(expectedDate, item.getCreatedDate());
	}

	@Test
	public void testApplyNullDate() {
		DDBEventArtefact ddbEventArtefact = new DDBEventArtefact();
		ddbEventArtefact.setStatus("INDEXED");
		ddbEventArtefact.setS3Bucket("my-bucket");
		ddbEventArtefact.setMirisDocId("miris123");
		ddbEventArtefact.setS3Key("my-key");

		ArtefactsDTO result = mapper.apply(ddbEventArtefact);

		assertNotNull(result);
		assertEquals(ArtefactStatusEnum.INDEXED, result.getStatus());
		assertNotNull(result.getIndexationDate());
		assertNotNull(result.getArchiveDate());
		assertEquals("my-bucket", result.getS3Bucket());
		assertEquals("miris123", result.getMirisDocId());

		List<ArtefactItemsDTO> items = result.getArtefactItems();
		assertNotNull(items);
		assertEquals(1, items.size());

		ArtefactItemsDTO item = items.get(0);
		assertEquals("my-key", item.getS3Key());
		assertEquals("contentType", item.getContentType());
		assertEquals(1L, item.getTotalPages());
		assertEquals("fileName", item.getFileName());
		assertNotNull(item.getCreatedDate());
	}

}