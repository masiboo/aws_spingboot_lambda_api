package org.iprosoft.trademarks.aws.artefacts.service.database;

import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;

import java.util.List;

public interface DatabaseService {

	List<ArtefactsEntity> filterArtefacts(ArtefactStatusEnum status, String mirisDocId);

	ArtefactsDTO createArtefact(ArtefactsDTO artefactsEntity);

	ArtefactNotesEntity createArtefactNote(ArtefactNotesEntity artefactNotesEntity);

	List<ArtefactNotesEntity> filterArtefactNotes(String mirisDocId);

}
