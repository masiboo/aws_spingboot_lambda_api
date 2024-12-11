
package artefact.dto.output;

import com.fasterxml.jackson.annotation.*;
import artefact.dto.LastModUser;
import artefact.dto.LockedBy;
import artefact.dto.Operator;
import artefact.util.ScannedAppType;
import artefact.util.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "batchSequence",
        "lockedDate",
        "creationDate",
        "lastModificationDate",
        "status",
        "requestType",
        "operator",
        "lockedBy",
        "lastModUser",
        "artefacts"
})

public class BatchOutput implements Serializable {

    private final static long serialVersionUID = -5141988849834997044L;
    @JsonProperty("id")
    private String id;
    @JsonProperty("batchSequence")
    private String batchSequence;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    @JsonProperty("lockedDate")
    private String lockedDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    @JsonProperty("creationDate")
    private String creationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    @JsonProperty("lastModificationDate")
    private String lastModificationDate;
    @JsonProperty("status")
    private String status;
    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("operator")
    private Operator operator;
    @JsonProperty("lockedBy")
    private LockedBy lockedBy;
    @JsonProperty("lastModUser")
    private LastModUser lastModUser;

    public ScannedAppType getScanType() {
        return scanType;
    }

    public void setScanType(ScannedAppType scanType) {
        this.scanType = scanType;
    }

    @JsonProperty("scanType")
    private ScannedAppType scanType;

    @JsonProperty("artefacts")
    private List<ArtefactOutput> artefacts = new ArrayList<ArtefactOutput>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();
    private boolean locked;

    /**
     * No args constructor for use in serialization
     */
    public BatchOutput() {
        this.creationDate = DateUtils.getCurrentUtcDateTimeStr();
        this.lastModificationDate = DateUtils.getCurrentDateShortStr();
    }

    /**
     * @param lastModificationDate
     * @param lockedBy
     * @param lockedDate
     * @param id
     * @param batchSequence
     * @param creationDate
     * @param operator
     * @param artefacts
     * @param status
     * @param lastModUser
     */
    public BatchOutput(String id, String batchSequence, String lockedDate, String creationDate, String lastModificationDate, String status, Operator operator, LockedBy lockedBy, LastModUser lastModUser, List<ArtefactOutput> artefacts) {
        super();
        this.id = id;
        this.batchSequence = batchSequence;
        this.lockedDate = lockedDate;
        this.creationDate = creationDate;
        this.lastModificationDate = lastModificationDate;
        this.status = status;
        this.operator = operator;
        this.lockedBy = lockedBy;
        this.lastModUser = lastModUser;
        this.artefacts = artefacts;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public BatchOutput withId(String id) {
        this.id = id;
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

    public BatchOutput withBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
        return this;
    }

    @JsonProperty("lockedDate")
    public String getLockedDate() {
        return lockedDate;
    }

    @JsonProperty("lockedDate")
    public void setLockedDate(String lockedDate) {
        this.lockedDate = lockedDate;
    }

    public BatchOutput withLockedDate(String lockedDate) {
        this.lockedDate = lockedDate;
        return this;
    }

    @JsonProperty("creationDate")
    public String getCreationDate() {
        return creationDate;
    }

    @JsonProperty("creationDate")
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public BatchOutput withCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    @JsonProperty("lastModificationDate")
    public String getLastModificationDate() {
        return lastModificationDate;
    }

    @JsonProperty("lastModificationDate")
    public void setLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public BatchOutput withLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public BatchOutput withStatus(String status) {
        this.status = status;
        return this;
    }

    @JsonProperty("operator")
    public Operator getOperator() {
        return operator;
    }

    @JsonProperty("operator")
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public BatchOutput withOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    @JsonProperty("lockedBy")
    public LockedBy getLockedBy() {
        return lockedBy;
    }

    @JsonProperty("lockedBy")
    public void setLockedBy(LockedBy lockedBy) {
        this.lockedBy = lockedBy;
    }

    public BatchOutput withLockedBy(LockedBy lockedBy) {
        this.lockedBy = lockedBy;
        return this;
    }

    @JsonProperty("lastModUser")
    public LastModUser getLastModUser() {
        return lastModUser;
    }

    @JsonProperty("lastModUser")
    public void setLastModUser(LastModUser lastModUser) {
        this.lastModUser = lastModUser;
    }

    public BatchOutput withLastModUser(LastModUser lastModUser) {
        this.lastModUser = lastModUser;
        return this;
    }

    @JsonProperty("artefacts")
    public List<ArtefactOutput> getArtefacts() {
        return artefacts;
    }

    @JsonProperty("artefacts")
    public void setArtefacts(List<ArtefactOutput> artefacts) {
        this.artefacts = artefacts;
    }

    public BatchOutput withArtefacts(List<ArtefactOutput> artefacts) {
        this.artefacts = artefacts;
        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public BatchOutput withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BatchOutput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(this.id);
        sb.append(',');
        sb.append("batchSequence");
        sb.append('=');
        sb.append(((this.batchSequence == null) ? "<null>" : this.batchSequence));
        sb.append(',');
        sb.append("lockedDate");
        sb.append('=');
        sb.append(((this.lockedDate == null) ? "<null>" : this.lockedDate));
        sb.append(',');
        sb.append("creationDate");
        sb.append('=');
        sb.append(((this.creationDate == null) ? "<null>" : this.creationDate));
        sb.append(',');
        sb.append("lastModificationDate");
        sb.append('=');
        sb.append(((this.lastModificationDate == null) ? "<null>" : this.lastModificationDate));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("operator");
        sb.append('=');
        sb.append(((this.operator == null) ? "<null>" : this.operator));
        sb.append(',');
        sb.append("lockedBy");
        sb.append('=');
        sb.append(((this.lockedBy == null) ? "<null>" : this.lockedBy));
        sb.append(',');
        sb.append("lastModUser");
        sb.append('=');
        sb.append(((this.lastModUser == null) ? "<null>" : this.lastModUser));
        sb.append(',');
        sb.append("artefacts");
        sb.append('=');
        sb.append(((this.artefacts == null) ? "<null>" : this.artefacts));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.lastModificationDate == null) ? 0 : this.lastModificationDate.hashCode()));
        result = ((result * 31) + ((this.lockedBy == null) ? 0 : this.lockedBy.hashCode()));
        result = ((result * 31) + ((this.lockedDate == null) ? 0 : this.lockedDate.hashCode()));
//        result = ((result * 31) + ((int) (this.id ^ (this.id >>> 32))));
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.batchSequence == null) ? 0 : this.batchSequence.hashCode()));
        result = ((result * 31) + ((this.creationDate == null) ? 0 : this.creationDate.hashCode()));
        result = ((result * 31) + ((this.operator == null) ? 0 : this.operator.hashCode()));
        result = ((result * 31) + ((this.artefacts == null) ? 0 : this.artefacts.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        result = ((result * 31) + ((this.lastModUser == null) ? 0 : this.lastModUser.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BatchOutput) == false) {
            return false;
        }
        BatchOutput rhs = ((BatchOutput) other);
        return ((((((((((((this.lastModificationDate == rhs.lastModificationDate) || ((this.lastModificationDate != null) && this.lastModificationDate.equals(rhs.lastModificationDate))) && ((this.lockedBy == rhs.lockedBy) || ((this.lockedBy != null) && this.lockedBy.equals(rhs.lockedBy)))) && ((this.lockedDate == rhs.lockedDate) || ((this.lockedDate != null) && this.lockedDate.equals(rhs.lockedDate)))) && (this.id == rhs.id)) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.batchSequence == rhs.batchSequence) || ((this.batchSequence != null) && this.batchSequence.equals(rhs.batchSequence)))) && ((this.creationDate == rhs.creationDate) || ((this.creationDate != null) && this.creationDate.equals(rhs.creationDate)))) && ((this.operator == rhs.operator) || ((this.operator != null) && this.operator.equals(rhs.operator)))) && ((this.artefacts == rhs.artefacts) || ((this.artefacts != null) && this.artefacts.equals(rhs.artefacts)))) && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status)))) && ((this.lastModUser == rhs.lastModUser) || ((this.lastModUser != null) && this.lastModUser.equals(rhs.lastModUser))));
    }

}
