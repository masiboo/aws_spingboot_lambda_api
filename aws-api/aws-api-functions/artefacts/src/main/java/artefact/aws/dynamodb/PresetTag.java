package artefact.aws.dynamodb;

import com.formkiq.graalvm.annotations.Reflectable;

import java.time.ZonedDateTime;

/**
 * {@link Preset} with Tags.
 *
 */
@Reflectable
public class PresetTag {

  /** Preset Key. */
  @Reflectable
  private String key;
  /** User Id. */
  @Reflectable
  private String userId;
  /** Content Type. */
  @Reflectable
  private ZonedDateTime insertedDate;

  /**
   * constructor.
   */
  public PresetTag() {

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
   * Get Key.
   * 
   * @return {@link String}
   */
  public String getKey() {
    return this.key;
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
   * Set Inserted Date.
   * 
   * @param date {@link ZonedDateTime}
   */
  public void setInsertedDate(final ZonedDateTime date) {
    this.insertedDate = date;
  }

  /**
   * Set Key.
   * 
   * @param s {@link String}
   */
  public void setKey(final String s) {
    this.key = s;
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
