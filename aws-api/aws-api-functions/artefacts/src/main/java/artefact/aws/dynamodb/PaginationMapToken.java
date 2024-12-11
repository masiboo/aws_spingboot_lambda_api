package artefact.aws.dynamodb;


import java.util.Map;

/** Pagination Token for Results. */
public class PaginationMapToken {

  /** {@link Object} {@link Map}. */
  private Map<String, Object> attributeMap;

  /**
   * constructor.
   *
   * @param map {@link Map}
   */
  public PaginationMapToken(final Map<String, Object> map) {
    this.attributeMap = map;
  }

  /**
   * Get Attribute Map.
   *
   * @return {@link Map}
   */
  public Map<String, Object> getAttributeMap() {
    return this.attributeMap;
  }

  @Override
  public String toString() {
    return this.attributeMap.toString();
  }
}
