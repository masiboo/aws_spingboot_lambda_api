package artefact.aws.dynamodb;

import java.util.List;
import java.util.Map;

/**
 * Pagination Results for a DynamoDB Query.
 *
 * @param <T> Type of Results.
 */
public class PaginationResults<T> {

  /** {@link List}. */
  private List<T> results;
  /** Last Evaluated DynamoDB Key. */
  private PaginationMapToken token;

  /**
   * constructor.
   *
   * @param list {@link List}
   * @param pagination {@link PaginationMapToken}
   */
  public PaginationResults(final List<T> list, final PaginationMapToken pagination) {
    this.results = list;
    this.token = pagination;
  }

  /**
   * Get Results.
   *
   * @return {@link List}
   */
  public List<T> getResults() {
    return this.results;
  }

  /**
   * Get Last Evaluated Key.
   *
   * @return {@link Map}
   */
  public PaginationMapToken getToken() {
    return this.token;
  }
}
