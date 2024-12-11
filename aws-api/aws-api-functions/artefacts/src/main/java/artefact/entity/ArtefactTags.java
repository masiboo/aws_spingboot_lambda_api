package artefact.entity;

import java.util.List;

/**
 * {@link List} of {@link ArtefactDynamoDb} tags.
 */
public class ArtefactTags {

  /** {@link List} of {@link ArtefactTag}. */
  private List<ArtefactTag> tags;

  /** constructor. */
  public ArtefactTags() {}

  /**
   * Get {@link ArtefactTag}.
   *
   * @return {@link List} {@link ArtefactTag}
   */
  public List<ArtefactTag> getTags() {
    return this.tags;
  }

  /**
   * Set {@link ArtefactTag}.
   *
   * @param list {@link List} {@link ArtefactTag}
   */
  public void setTags(final List<ArtefactTag> list) {
    this.tags = list;
  }
}
