package artefact.entity;

import com.fasterxml.jackson.annotation.*;
import artefact.util.DateUtils;

import javax.validation.Valid;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "path",
        "filename",
        "status",
        "s3_signed_url",
        "creationDate",
        "updatedDate",
        "artefactId",
        "requestId",
        "batchSequence"
})

public class ArtefactJob implements Serializable
{

    @JsonProperty("id")
    private String id;
    @JsonProperty("path")
    private String path;
    @JsonProperty("filename")
    private String filename;
    @JsonProperty("status")
    private String status;
    @JsonProperty("s3_signed_url")
    private String s3SignedUrl;
    @JsonProperty("creationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime creationDate;
    @JsonProperty("updatedDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime updatedDate;
    @JsonProperty("artefactId")
    private String artefactId;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("batchSequence")
    private String batchSequence;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 3603522964983482884L;

    /**
     * No args constructor for use in serialization
     *
     */
    public ArtefactJob() {
        this.creationDate = ZonedDateTime.now();
        this.updatedDate = ZonedDateTime.now();
    }

    /**
     *
     * @param path
     * @param filename
     * @param artefactId
     * @param id
     * @param s3SignedUrl
     * @param status
     */
    public ArtefactJob(String id, String path, String filename, String status, String s3SignedUrl, String artefactId) {
        super();
        this.id = id;
        this.path = path;
        this.filename = filename;
        this.status = status;
        this.s3SignedUrl = s3SignedUrl;

        this.artefactId = artefactId;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public ArtefactJob withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    public ArtefactJob withPath(String path) {
        this.path = path;
        return this;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ArtefactJob withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public ArtefactJob withStatus(String status) {
        this.status = status;
        return this;
    }

    @JsonProperty("s3_signed_url")
    public String getS3SignedUrl() {
        return s3SignedUrl;
    }

    @JsonProperty("s3_signed_url")
    public void setS3SignedUrl(String s3SignedUrl) {
        this.s3SignedUrl = s3SignedUrl;
    }

    public ArtefactJob withS3SignedUrl(String s3SignedUrl) {
        this.s3SignedUrl = s3SignedUrl;
        return this;
    }

    @JsonProperty("creationDate")
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    @JsonProperty("creationDate")
    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ArtefactJob withCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    @JsonProperty("updatedDate")
    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    @JsonProperty("updatedDate")
    public void setUpdatedDate(ZonedDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public ArtefactJob withUpdatedDate(ZonedDateTime updatedDate) {
        this.updatedDate = updatedDate;
        return this;
    }

    @JsonProperty("artefactId")
    public String getArtefactId() {
        return artefactId;
    }

    @JsonProperty("artefactId")
    public void setArtefactId(String artefactId) {
        this.artefactId = artefactId;
    }

    public ArtefactJob withArtefactId(String artefactId) {
        this.artefactId = artefactId;
        return this;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ArtefactJob withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @JsonProperty("batchSequence")
    public String getBatchSequence() {
        return batchSequence;
    }

    @JsonProperty("batchSequence")
    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public ArtefactJob withBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ArtefactJob withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ArtefactJob.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("path");
        sb.append('=');
        sb.append(((this.path == null)?"<null>":this.path));
        sb.append(',');
        sb.append("filename");
        sb.append('=');
        sb.append(((this.filename == null)?"<null>":this.filename));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("s3SignedUrl");
        sb.append('=');
        sb.append(((this.s3SignedUrl == null)?"<null>":this.s3SignedUrl));
        sb.append(',');
        sb.append("creationDate");
        sb.append('=');
        sb.append(((this.creationDate == null)?"<null>":this.creationDate));
        sb.append(',');
        sb.append("updatedDate");
        sb.append('=');
        sb.append(((this.updatedDate == null)?"<null>":this.updatedDate));
        sb.append(',');
        sb.append("artefactId");
        sb.append('=');
        sb.append(((this.artefactId == null)?"<null>":this.artefactId));
        sb.append(',');
        sb.append("requestId");
        sb.append('=');
        sb.append(((this.requestId == null)?"<null>":this.requestId));
        sb.append("batchSequence");
        sb.append('=');
        sb.append(((this.batchSequence == null)?"<null>":this.batchSequence));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.path == null)? 0 :this.path.hashCode()));
        result = ((result* 31)+((this.filename == null)? 0 :this.filename.hashCode()));
        result = ((result* 31)+((this.artefactId == null)? 0 :this.artefactId.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.updatedDate == null)? 0 :this.updatedDate.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.creationDate == null)? 0 :this.creationDate.hashCode()));
        result = ((result* 31)+((this.s3SignedUrl == null)? 0 :this.s3SignedUrl.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ArtefactJob) == false) {
            return false;
        }
        ArtefactJob rhs = ((ArtefactJob) other);
        return ((((((((((this.path == rhs.path)||((this.path!= null)&&this.path.equals(rhs.path)))&&((this.filename == rhs.filename)||((this.filename!= null)&&this.filename.equals(rhs.filename))))&&((this.artefactId == rhs.artefactId)||((this.artefactId!= null)&&this.artefactId.equals(rhs.artefactId))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.updatedDate == rhs.updatedDate)||((this.updatedDate!= null)&&this.updatedDate.equals(rhs.updatedDate))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.creationDate == rhs.creationDate)||((this.creationDate!= null)&&this.creationDate.equals(rhs.creationDate))))&&((this.s3SignedUrl == rhs.s3SignedUrl)||((this.s3SignedUrl!= null)&&this.s3SignedUrl.equals(rhs.s3SignedUrl))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

}