package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class DDBEventArtefactToArtefactEntityMapper implements Function<DDBEventArtefact, ArtefactsDTO> {

	public ArtefactOutput apply(Artefact artefact) {
		if (artefact == null) {
			return null;
		}
		log.info("DDBEventArtefactToArtefactEntityMapper input artefact: {}", artefact);
		ArtefactOutput artefactOutput = new ArtefactOutput();
		artefactOutput.setId(artefact.getId());
		artefactOutput.setArtefactClassType(artefact.getArtefactClassType());
		artefactOutput.setStatus(artefact.getStatus());
		artefactOutput.setArtefactName(artefact.getArtefactName());
		log.info("Output artefactOutput {}", artefactOutput);
		return artefactOutput;
	}

	@Override
	public ArtefactsDTO apply(DDBEventArtefact ddbEventArtefact) {
		if (ddbEventArtefact == null) {
			return null;
		}
		log.info("Input ddbEventArtefact: {}", ddbEventArtefact);
		ArtefactsDTO artefactsEntity = new ArtefactsDTO();

		String status = ddbEventArtefact.getStatus();
		if (status != null) {
			artefactsEntity.setStatus(ArtefactStatusEnum.valueOf(status));
		}

		String insertedDate = ddbEventArtefact.getInsertedDate();
		ZonedDateTime date;
		if (insertedDate != null) {
			date = SafeParserUtil.safeParseZonedDateTime(insertedDate);
		}
		else {
			date = DateUtils.getCurrentDatetimeUtc();
		}

		artefactsEntity.setIndexationDate(date);
		artefactsEntity.setArchiveDate(date);
		artefactsEntity.setS3Bucket(ddbEventArtefact.getS3Bucket());
		artefactsEntity.setMirisDocId(ddbEventArtefact.getMirisDocId());
		artefactsEntity.setArtefactUUID(ddbEventArtefact.getArtefactId());
		artefactsEntity.setLastModificationUser(
				ddbEventArtefact.getUserId() != null ? ddbEventArtefact.getUserId() : "AnonymousUser");

		ArtefactItemsDTO item = new ArtefactItemsDTO();
		item.setS3Key(ddbEventArtefact.getS3Key());

		// #fixme
		item.setContentType("contentType");
		item.setTotalPages(1L);
		item.setFileName("fileName");

		ArrayList<ArtefactItemsDTO> items = new ArrayList<>();
		item.setCreatedDate(date);
		items.add(item);

		artefactsEntity.setArtefactItems(items);
		log.info("Output ArtefactItemsDTO artefactsEntity: {}", artefactsEntity);
		return artefactsEntity;
	}

}
