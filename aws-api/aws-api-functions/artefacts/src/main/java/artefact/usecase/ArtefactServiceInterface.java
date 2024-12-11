
package artefact.usecase;

import artefact.dto.ArtefactIndexDto;
import artefact.dto.input.ArtefactInput;
import artefact.dto.input.BatchInput;
import artefact.dto.output.ArtefactInfo;
import artefact.entity.Artefact;
import artefact.entity.ArtefactTag;
import artefact.entity.IArtefact;
import artefact.util.ArtefactStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Services for Querying, Updating Documents. */
public interface ArtefactServiceInterface {

  /** The Default maximum results returned. */
  int MAX_RESULTS = 10;
  /** System Defined Tags. */
  Set<String> SYSTEM_DEFINED_TAGS =
      Set.of("untagged", "path", "CLAMAV_SCAN_STATUS", "CLAMAV_SCAN_TIMESTAMP", "userId");

  /**
   * Add Tags to Document.
   *
   * @param documentId {@link String}
   * @param tags {@link Collection} {@link ArtefactTag}
   * @param timeToLive {@link String}
   */
  void addTags(String documentId, Collection<ArtefactTag> tags, String timeToLive);

  /**
   * Save Document and Tags.
   *
   * @param document {@link IArtefact}
   * @param tags     {@link Collection} {@link ArtefactTag}
   */
  void saveDocument(IArtefact document, Collection<ArtefactTag> tags);
  /**
   * Validate Document Input.
   *
   * @param document {@link Artefact}
   */

  /**
   * get Artefact by Tags.
   *
   * @param tags {@link ArtefactTag}
   */
  Artefact getArtefactByTags(Collection<ArtefactTag> tags);

  /**
   * Validate Artefact Input.
   *
   */
  List<List<ArtefactTag>> getAllArtefactItemTags();
 
  Map<String,String> validateInputDocument(ArtefactInput document);

  void updateArtefactWithStatus(String artefactId, String status);

  List<Artefact> getAllArtefacts(String date,String status);

  Map<String, String> validateInputMirisDocid(ArtefactInput document);

  Artefact getArtefactById(String artefactId);

  List<Artefact> getArtefactbyMirisDocId(String mirisDocId);

  List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId,List<String> typeList);

  void softDeleteArtefactById(String artefactId);

  void indexArtefact(String artefactId, ArtefactIndexDto artefactIndexDto, ArtefactStatus artefactStatus);

  void saveArtefactWithItemsAndTags(ArtefactInput artefactInput);

  void saveBatchUploads(BatchInput batchInput);

  boolean isValidClassType(String type);

  String getAllClassTypes();

  void updateArtefact(String artefactId,Map<String,String> attributess);

  ArtefactInfo getArtefactInfoById(String artefactId);

  boolean hasFileWithSameDocId(String mirisDocId, String artefactClassType);

  boolean isDocIdValid(String mirisDocId);

}
