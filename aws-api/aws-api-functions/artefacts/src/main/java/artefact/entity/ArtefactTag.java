package artefact.entity;


import java.time.ZonedDateTime;
import java.util.List;

/** Document Tag. */
public class ArtefactTag {

  /** Document Tag Key. */
  private String key;
  /** Document Id. */
  private String documentId;
  /** Document String Tag Value. */
  private String value;
  /** User Id. */
  private String userId;
  /** Document Inserted Date. */
  private ZonedDateTime insertedDate;
  /** {@link DocumentTagType}. */
  private DocumentTagType type;

  /** constructor. */
  public ArtefactTag() {}

  /**
   * constructor.
   *
   * @param docid {@link String}
   * @param tagKey {@link String}
   * @param tagValue {@link String}
   * @param date {@link ZonedDateTime}
   * @param user {@link String}
   */
  public ArtefactTag(final String docid, final String tagKey, final String tagValue,
                     final ZonedDateTime date, final String user) {
    this(docid, tagKey, tagValue, date, user, DocumentTagType.USERDEFINED);
  }

  /**
   * constructor.
   *
   * @param docid {@link String}
   * @param tagKey {@link String}
   * @param tagValue {@link String}
   * @param date {@link ZonedDateTime}
   * @param user {@link String}
   * @param tagType {@link DocumentTagType}
   */
  public ArtefactTag(final String docid, final String tagKey, final String tagValue,
                     final ZonedDateTime date, final String user, final DocumentTagType tagType) {
    this();
    setDocumentId(docid);
    setKey(tagKey);
    setValue(tagValue);
    setInsertedDate(date);
    setUserId(user);
    setType(tagType);
  }

  /**
   * get Document Id.
   *
   * @return {@link String}
   */
  public String getDocumentId() {
    return this.documentId;
  }

  /**
   * Set Document ID.
   *
   * @param id {@link String}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setDocumentId(final String id) {
    this.documentId = id;
    return this;
  }

  /**
   * Get Document Tag Key.
   * 
   * @return {@link String}
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Set Tag Key.
   * 
   * @param tagkey {@link String}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setKey(final String tagkey) {
    this.key = tagkey;
    return this;
  }

  /**
   * Get Value.
   * 
   * @return {@link String}
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Set Value.
   * 
   * @param s {@link String}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setValue(final String s) {
    this.value = s;
    return this;
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
   * Set User Id.
   *
   * @param user {@link String}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setUserId(final String user) {
    this.userId = user;
    return this;
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
   * Set Inserted Date.
   *
   * @param date {@link ZonedDateTime}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setInsertedDate(final ZonedDateTime date) {
    this.insertedDate = date;
    return this;
  }

  /**
   * Get {@link DocumentTagType}.
   * 
   * @return {@link DocumentTagType}
   */
  public DocumentTagType getType() {
    return this.type;
  }

  /**
   * Set Document Type.
   * 
   * @param tagType {@link DocumentTagType}
   * @return {@link ArtefactTag}
   */
  public ArtefactTag setType(final DocumentTagType tagType) {
    this.type = tagType;
    return this;
  }

  private List<String> values;

  public List<String> getValues() {
    return this.values;
  }

  public ArtefactTag setValues(final List<String> list) {
    this.values = list;
    return this;
  }
}
