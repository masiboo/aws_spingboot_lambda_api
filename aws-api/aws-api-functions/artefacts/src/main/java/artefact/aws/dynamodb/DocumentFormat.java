package artefact.aws.dynamodb;

import java.time.ZonedDateTime;

/**
 * Document Formats.
 *
 */
public class DocumentFormat {

  /** Document Id. */
  private String documentId;
  /** Document Inserted Date. */
  private ZonedDateTime insertedDate;
  /** User Id. */
  private String userId;
  /** Content Type. */
  private String contentType;

  /**
   * constructor.
   */
  public DocumentFormat() {}

  /**
   * Get Content-Type.
   * 
   * @return {@link String}
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * Get Document Id.
   * 
   * @return {@link String}
   */
  public String getDocumentId() {
    return this.documentId;
  }

  /**
   * Get Inserted Date.
   * 
   * @return {@link ZonedDateTime}
   */
  public ZonedDateTime getInsertedDate() {
    return this.insertedDate != null ? this.insertedDate : null;
  }

  /**
   * Get UserId.
   * 
   * @return {@link String}
   */
  public String getUserId() {
    return this.userId;
  }

  /**
   * Set Content-Type.
   * 
   * @param ct {@link String}
   */
  public void setContentType(final String ct) {
    this.contentType = ct;
  }

  /**
   * Set Document Id.
   * 
   * @param id {@link String}
   */
  public void setDocumentId(final String id) {
    this.documentId = id;
  }

  /**
   * Set Inserted Date.
   * 
   * @param date {@link ZonedDateTime}
   */
  public void setInsertedDate(final ZonedDateTime date) {
    this.insertedDate = date;
  }

  /**
   * Set User Id.
   * 
   * @param user {@link String}
   */
  public void setUserId(final String user) {
    this.userId = user;
  }
}
