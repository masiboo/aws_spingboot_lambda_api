package artefact.aws.dynamodb;


import java.time.ZonedDateTime;

/**
 * Preset.
 *
 */
public class Preset {

  /** Id. */
  private String id;
  /** Name. */
  private String name;
  /** Type. */
  private String type;
  /** User Id. */
  private String userId;
  /** Content Type. */
  private ZonedDateTime insertedDate;

  /**
   * constructor.
   */
  public Preset() {}

  /**
   * Get Id.
   * 
   * @return {@link String}
   */
  public String getId() {
    return this.id;
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
   * Get Name.
   * 
   * @return {@link String}
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get Type.
   * 
   * @return {@link String}
   */
  public String getType() {
    return this.type;
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
   * Set Id.
   * 
   * @param presetId {@link String}
   */
  public void setId(final String presetId) {
    this.id = presetId;
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
   * Set Name.
   * 
   * @param s {@link String}
   */
  public void setName(final String s) {
    this.name = s;
  }

  /**
   * Set Type.
   * 
   * @param presetType {@link String}
   */
  public void setType(final String presetType) {
    this.type = presetType;
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
