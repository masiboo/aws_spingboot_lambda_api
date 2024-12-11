
package artefact.aws;

import artefact.entity.Artefact;
import artefact.entity.ArtefactTag;
import artefact.entity.IArtefact;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Services for Querying, Updating Documents. */
public interface DocumentService {

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
  Map<String,String> validateInputDocument(Artefact document, String environment);

}
