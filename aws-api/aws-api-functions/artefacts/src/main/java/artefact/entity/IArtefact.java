package artefact.entity;

import artefact.util.ScannedAppType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Holder class for Artefact(s).
 */
public interface IArtefact {

  void setFileName(String fileName);
  String getFileName();

    /**
     * Gets Belongs To ArtefactId.
     *
     * @return {@link String}
     */
    String getBelongsToArtefactId();

    /**
     * Get Entity Checksum.
   * 
   * @return {@link String}
   */
  String getChecksum();

  /**
   * Get Content Length.
   *
   * @return {@link Long}
   */
  Long getContentLength();

  /**
   * Get Content Type.
   *
   * @return {@link String}
   */
  String getContentType();

  /**
   * Get Artefact Id.
   *
   * @return {@link String}
   */
  String getArtefactiTemId();

    /**
     * Get Artefacts.
     *
     * @return {@link List} {@link IArtefact}
     */
//  List<IArtefact> getArtefacts();
    List<ArtefactItemDynamoDB> getArtefactItems();

  /**
   * Get Inserteddate.
   *
   * @return {@link Date}
   */
  ZonedDateTime getInsertedDate();

  /**
   * Get Path.
   *
   * @return {@link String}
   */
  String getPath();

  /**
   * Get Time To Live.
   *
   * @return {@link String}
   */
  String getTimeToLive();

  /**
   * Get User Id.
   *
   * @return {@link String}
   */
  String getUserId();

  /**
   * Sets Belongs To ArtefactId.
   * 
   * @param ArtefactId {@link String}
   */
  void setBelongsToArtefactId(String ArtefactId);

  /**
   * Set Entity Checksum.
   * 
   * @param checksum {@link String}
   */
  void setChecksum(String checksum);

  /**
   * Set Content Length.
   *
   * @param cl {@link Long}
   */
  void setContentLength(Long cl);

  /**
   * Set Content Type.
   *
   * @param ct {@link String}
   */
  void setContentType(String ct);

  /**
   * Set Artefact ID.
   *
   * @param id {@link String}
   */
  void setArtefactiTemId(String id);

//  void setArtefacts(List<IArtefact> ids);

  /**
   * Set Inserted Date.
   *
   * @param date {@link ZonedDateTime}
   */
  void setInsertedDate(ZonedDateTime date);

  /**
   * Set Path.
   *
   * @param filepath {@link String}
   */
  void setPath(String filepath);
  
  /**
   * Set Time To Live.
   *
   * @param ttl {@link String}
   */
  void setTimeToLive(String ttl);
  
  /**
   * Set User Id.
   *
   * @param username {@link String}
   */
  void setUserId(String username);


  String getBucket();

  void setBucket(String bucket);

  String getKey() ;

  void setKey(String key) ;

  String getMirisDocId();

  void setMirisDocId(String mirisDocId);

  String getArtefactClassType();

  void setArtefactClassType(String artefactClassType);

  String getContainerId() ;

  void setContainerId(String containerId);

  String getArtefactContainerName();

  void setArtefactContainerName(String artefactContainerName);

  String getBatchSequenceId();

  void setBatchSequenceId(String batchSequenceId);
  
  String getStatus();

  void setStatus(String status);

  Boolean getSizeWarning();

  void setSizeWarning(Boolean sizeWarning);

  boolean isPart();

  void setPart(boolean isPart);

  String getPageNumber();

  void setPageNumber(String pageNumber);

  String getTotalPages();

  void setTotalPages(String totalPages);


  public String getArtefactMergeId();

  public void setArtefactMergeId(String artefactMergeId) ;

  public String getArtefactItemFileName() ;

  public void setArtefactItemFileName(String artefactItemFileName);


  public ScannedAppType getScannedType();

  public void setScannedType(ScannedAppType scannedType);
}
