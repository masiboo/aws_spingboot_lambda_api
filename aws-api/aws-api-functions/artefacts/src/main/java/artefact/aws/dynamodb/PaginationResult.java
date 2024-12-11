package artefact.aws.dynamodb;

import java.util.List;
import java.util.Map;

/**
 * Pagination Result for a DynamoDB Query.
 *
 * @param <T> Type of Result.
 */
public class PaginationResult<T> {

  /** Object. */
  private T result;
  /** Last Evaluated DynamoDB Key. */
  private PaginationMapToken token;

  /**
   * constructor.
   *
   * @param obj {@link Object}
   * @param pagination {@link PaginationMapToken}
   */
  public PaginationResult(final T obj, final PaginationMapToken pagination) {
    this.result = obj;
    this.token = pagination;
  }

  /**
   * Get Result.
   *
   * @return {@link List}
   */
  public T getResult() {
    return this.result;
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
