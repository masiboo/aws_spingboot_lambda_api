package artefact.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import artefact.util.DateUtils;

public class ArtefactBatch extends Artefact {

    @JsonProperty("type")
    private String type;
    @JsonProperty("filename")
    private String filename;
    @JsonProperty("path")
    private String path;
    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("batchSequence")
    private String batchSequence;

    @JsonProperty("artefactName")
    private String artefactMergeId;

    @JsonProperty("artefactItemFileName")
    private String artefactItemFileName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    @JsonProperty("creationDate")
    private String creationDate;
    @JsonProperty("user")
    private String user;
    @JsonProperty("jobId")
    private String jobId;
    @JsonProperty("artefactId")
    private String artefactId;
    @JsonProperty("s3Url")
    private String s3Url;

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("validationStatus")
    private String validationStatus;

    private String page;

    public ArtefactBatch(){
        this.creationDate = DateUtils.getCurrentDateShortStr();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBatchSequence() {
        return batchSequence;
    }

    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getArtefactId() {
        return artefactId;
    }

    public void setArtefactId(String artefactId) {
        this.artefactId = artefactId;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "ArtefactBatch{" +
                "type='" + type + '\'' +
                ", filename='" + filename + '\'' +
                ", path='" + path + '\'' +
                ", contentType='" + contentType + '\'' +
                ", batchSequence='" + batchSequence + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", user='" + user + '\'' +
                ", jobId='" + jobId + '\'' +
                ", artefactId='" + artefactId + '\'' +
                ", s3Url='" + s3Url + '\'' +
                ", requestType='" + requestType + '\'' +
                ", validationStatus='" + validationStatus + '\'' +
                '}';
    }

    public String getArtefactItemFileName() {
        return artefactItemFileName;
    }

    public void setArtefactItemFileName(String artefactItemFileName) {
        this.artefactItemFileName = artefactItemFileName;
    }

    public String getArtefactMergeId() {
        return artefactMergeId;
    }

    public void setArtefactMergeId(String artefactMergeId) {
        this.artefactMergeId = artefactMergeId;
    }
}
