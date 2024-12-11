package artefact.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import artefact.util.DateUtils;
import artefact.util.ScannedAppType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Holder class for Artefact(s).
 */
public class ArtefactDynamoDb implements IArtefact {

    /** Artefact Id. */
    private String artefactiTemId; // artefactId
    /** Artefact Inserted Date. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime insertedDate;
    /** Artefact Path. */
    private String path;
    /** User Id. */
    private String userId;
    /** Content Type. */
    private String contentType;
    /** Entity tag. */
    private String checksum;
    /** {@link Long}. */
    private Long contentLength;
    /** {@link List} {@link ArtefactItemDynamoDB}. */
    private List<ArtefactItemDynamoDB> artefactItems;
    /** Belongs To Artefact Id. */
    private String belongsToArtefactId;
    /** Time to Live. */
    private String timeToLive;

    private String bucket = null;
    private String key = null;

    private String mirisDocId;

    private String status;

    private String fileName;

    private String containerId; // artefact-id

    private boolean isPart; //Part or page of document

    private String pageNumber;

    private String totalPages;

    private String artefactMergeId;

    private String artefactItemFileName;

    public ScannedAppType getScannedType() {
        return scannedType;
    }

    public void setScannedType(ScannedAppType scannedType) {
        this.scannedType = scannedType;
    }

    private ScannedAppType scannedType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public String getArtefactContainerName() {
        return artefactContainerName;
    }

    @Override
    public void setArtefactContainerName(String artefactContainerName) {
        this.artefactContainerName = artefactContainerName;
    }

    @Override
    public String getBatchSequenceId() {
        return batchSequenceId;
    }

    @Override
    public void setBatchSequenceId(String batchSequenceId) {
        this.batchSequenceId = batchSequenceId;
    }

    private String artefactContainerName; // artefact-name

    private String batchSequenceId; // batch sequuence

    private String artefactClassType;

    private Boolean sizeWarning;

    /**
     * constructor.
     */
    public ArtefactDynamoDb() {
        this.insertedDate = ZonedDateTime.now();
    }

    /**
     * constructor.
     *
     * @param artefactItemId    {@link String}
     * @param date     {@link ZonedDateTime}
     * @param username {@link String}
     */
    public ArtefactDynamoDb(final String artefactItemId, final ZonedDateTime date, final String username) {
        this();
        setArtefactiTemId(artefactItemId);
        setInsertedDate(date);
        setUserId(username);
    }

    @Override
    public String getBelongsToArtefactId() {
        return this.belongsToArtefactId;
    }

    @Override
    public String getChecksum() {
        return this.checksum;
    }

    @Override
    public Long getContentLength() {
        return this.contentLength;
    }

    @Override
    public void setContentLength(final Long cl) {
        this.contentLength = cl;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setContentType(final String ct) {
        this.contentType = ct;
    }

    @Override
    public String getArtefactiTemId() {
        return this.artefactiTemId;
    }

    @Override
    public void setArtefactiTemId(final String id) {
        this.artefactiTemId = id;
    }

    @Override
    public String getTimeToLive() {
        return this.timeToLive;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public void setBelongsToArtefactId(final String id) {
        this.belongsToArtefactId = id;
    }

    @Override
    public void setChecksum(final String etag) {
        this.checksum = etag;
    }

    public List<ArtefactItemDynamoDB> getArtefactItems() {
        return this.artefactItems;
    }

    public void setArtefactItems(final List<ArtefactItemDynamoDB> objects) {
        this.artefactItems = objects;
    }

    @Override
    public ZonedDateTime getInsertedDate() {
        return this.insertedDate != null ? this.insertedDate : null;
    }

    @Override
    public void setInsertedDate(final ZonedDateTime date) {
        this.insertedDate = date;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(final String filepath) {
        this.path = filepath;
    }

    @Override
    public void setTimeToLive(final String ttl) {
        this.timeToLive = ttl;
    }

    @Override
    public void setUserId(final String username) {
        this.userId = username;
    }

    @Override
    public String toString() {
        return "ArtefactId=" + this.artefactiTemId + ",inserteddate=" + this.insertedDate;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public String getMirisDocId() {
        return mirisDocId;
    }

    public void setMirisDocId(String mirisDocId) {
        this.mirisDocId = mirisDocId;
    }

    public String getArtefactClassType() {
        return artefactClassType;
    }

    public void setArtefactClassType(String artefactClassType) {
        this.artefactClassType = artefactClassType;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getSizeWarning() {
        return sizeWarning;
    }

    public void setSizeWarning(Boolean sizeWarning) {
        this.sizeWarning = sizeWarning;
    }

    @Override
    public boolean isPart() {
        return this.isPart;
    }

    public void setPart(boolean part) {
        this.isPart = part;
    }

    @Override
    public String getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public String getTotalPages() {
        return this.totalPages;
    }

    @Override
    public void setTotalPages(String totalPages) {
        this.totalPages = totalPages;
    }

    public String getArtefactMergeId() {
        return artefactMergeId;
    }

    public void setArtefactMergeId(String artefactMergeId) {
        this.artefactMergeId = artefactMergeId;
    }

    public String getArtefactItemFileName() {
        return artefactItemFileName;
    }

    public void setArtefactItemFileName(String artefactItemFileName) {
        this.artefactItemFileName = artefactItemFileName;
    }
}
