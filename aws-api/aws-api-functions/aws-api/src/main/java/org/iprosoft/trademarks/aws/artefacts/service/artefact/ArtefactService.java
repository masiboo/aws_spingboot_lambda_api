package org.iprosoft.trademarks.aws.artefacts.service.artefact;

import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactIndexDto;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactMetadata;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactTag;
import org.iprosoft.trademarks.aws.artefacts.model.entity.IArtefact;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;

import java.time.ZonedDateTime;
import java.util.*;

public interface ArtefactService {

	Set<String> SYSTEM_DEFINED_TAGS = Set.of("untagged", "path", "CLAMAV_SCAN_STATUS", "CLAMAV_SCAN_TIMESTAMP",
			"userId");

	void addTags(String documentId, Collection<ArtefactTag> tags, String timeToLive);

	void saveDocument(IArtefact document, Collection<ArtefactTag> tags);

	void saveArtefactsAtomic(Collection<IArtefact> artefacts);

	Artefact getArtefactByTags(Collection<ArtefactTag> tags);

	List<List<ArtefactTag>> getAllArtefactItemTags();

	Map<String, String> validateArtefactBatch(ArtefactBatch artefactBatch, String scanedApp);

	Map<String, String> validateArtefact(ArtefactInput artefactInput);

	void updateArtefactWithStatus(String artefactId, String status);

	List<Artefact> getAllArtefacts(String date, String status);

	List<Artefact> getAllArtefactsByInterval(String fromDate, String untilDate, String status);

	Map<String, String> validateInputMirisDocId(ArtefactInput document);

	Artefact getArtefactById(String artefactId);

	List<ArtefactBatch> getArtefactBatchByBatchSequenceAndDate(String batchSequence, ZonedDateTime date);

	ArtefactBatch getArtectBatchById(String artefactId);

	List<Artefact> getArtefactbyMirisDocId(String mirisDocId);

	List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList);

	List<Artefact> getArtefactByFilterCritera(ArtefactFilterCriteria filterCriteria);

	void softDeleteArtefactById(String artefactId);

	void indexArtefact(String artefactId, ArtefactIndexDto artefactIndexDto, ArtefactStatus artefactStatus);

	boolean isValidClassType(String type);

	String getAllClassTypes();

	void updateArtefact(String artefactId, Map<String, String> attributes);

	ArtefactMetadata getArtefactInfoById(String artefactId);

	Artefact createArtefact(Artefact artefact);

	boolean hasFileWithSameDocId(String mirisDocId, String classType);

	void updateParentIdInBatchItems(String batchSequence, List<String> batchItemsIds);

	boolean isDocIdValid(String docId);

}
