package org.iprosoft.trademarks.aws.artefacts.model.entity;

import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Holder class for Artefact(s).
 */
public interface IArtefact {

	void setFileName(String fileName);

	String getFileName();

	/**
	 * Gets Belongs To ArtefactId.
	 * @return {@link String}
	 */
	String getBelongsToArtefactId();

	/**
	 * Get Entity Checksum.
	 * @return {@link String}
	 */
	String getChecksum();

	/**
	 * Get Content Length.
	 * @return {@link Long}
	 */
	Long getContentLength();

	/**
	 * Get Content Type.
	 * @return {@link String}
	 */
	String getContentType();

	/**
	 * Get Artefact Id.
	 * @return {@link String}
	 */
	String getArtefactItemId();

	/**
	 * Get Artefacts.
	 * @return {@link List} {@link IArtefact}
	 */
	// List<IArtefact> getArtefacts();
	List<ArtefactItemDynamoDB> getArtefactItems();

	/**
	 * Get InsertedDate.
	 * @return {@link ZonedDateTime}
	 */
	ZonedDateTime getInsertedDate();

	/**
	 * Get Path.
	 * @return {@link String}
	 */
	String getPath();

	/**
	 * Get Time To Live.
	 * @return {@link String}
	 */
	String getTimeToLive();

	/**
	 * Get User Id.
	 * @return {@link String}
	 */
	String getUserId();

	/**
	 * Sets Belongs To ArtefactId.
	 * @param ArtefactId {@link String}
	 */
	void setBelongsToArtefactId(String ArtefactId);

	/**
	 * Set Entity Checksum.
	 * @param checksum {@link String}
	 */
	void setChecksum(String checksum);

	/**
	 * Set Content Length.
	 * @param cl {@link Long}
	 */
	void setContentLength(Long cl);

	/**
	 * Set Content Type.
	 * @param ct {@link String}
	 */
	void setContentType(String ct);

	/**
	 * Set Artefact ID.
	 * @param id {@link String}
	 */
	void setArtefactItemId(String id);

	// void setArtefacts(List<IArtefact> ids);

	/**
	 * Set Inserted ZonedDateTime.
	 * @param date {@link ZonedDateTime}
	 */
	void setInsertedDate(ZonedDateTime date);

	/**
	 * Set Path.
	 * @param filepath {@link String}
	 */
	void setPath(String filepath);

	/**
	 * Set Time To Live.
	 * @param ttl {@link String}
	 */
	void setTimeToLive(String ttl);

	/**
	 * Set User Id.
	 * @param username {@link String}
	 */
	void setUserId(String username);

	String getBucket();

	void setBucket(String bucket);

	String getKey();

	void setKey(String key);

	String getMirisDocId();

	void setMirisDocId(String mirisDocId);

	String getArtefactClassType();

	void setArtefactClassType(String artefactClassType);

	String getContainerId();

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

	String getArtefactMergeId();

	void setArtefactMergeId(String artefactMergeId);

	String getArtefactItemFileName();

	void setArtefactItemFileName(String artefactItemFileName);

	ScannedAppType getScannedType();

	void setScannedType(ScannedAppType scannedType);

}
