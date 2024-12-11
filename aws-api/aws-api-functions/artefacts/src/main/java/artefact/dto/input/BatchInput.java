
package artefact.dto.input;

import com.fasterxml.jackson.annotation.*;
import artefact.dto.LastModUser;
import artefact.dto.LockedBy;
import artefact.dto.Operator;
import artefact.util.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "batch_sequence",
        "lockedDate",
        "creationDate",
        "lastModificationDate",
        "status",
        "operator",
        "lockedBy",
        "lastModUser",
        "artefacts"
})

public class BatchInput implements Serializable {

    private final static long serialVersionUID = -5141988849834997044L;
    @JsonProperty("id")
    private long id;
    @JsonProperty("batch_sequence")
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
    @JsonProperty("operator")
    private Operator operator;
    @JsonProperty("lockedBy")
    private LockedBy lockedBy;
    @JsonProperty("lastModUser")
    private LastModUser lastModUser;
    @JsonProperty("artefacts")
    private List<ArtefactInput> artefacts = new ArrayList<ArtefactInput>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public BatchInput() {
        this.creationDate = DateUtils.getCurrentUtcDateTimeStr();
        this.lastModificationDate = DateUtils.getCurrentUtcDateTimeStr();
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
    public BatchInput(long id, String batchSequence, String lockedDate, String creationDate, String lastModificationDate, String status, Operator operator, LockedBy lockedBy, LastModUser lastModUser, List<ArtefactInput> artefacts) {
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
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public BatchInput withId(long id) {
        this.id = id;
        return this;
    }

    @JsonProperty("batch_sequence")
    public String getBatchSequence() {
        return batchSequence;
    }

    @JsonProperty("batch_sequence")
    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public BatchInput withBatchSequence(String batchSequence) {
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

    public BatchInput withLockedDate(String lockedDate) {
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

    public BatchInput withCreationDate(String creationDate) {
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

    public BatchInput withLastModificationDate(String lastModificationDate) {
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

    public BatchInput withStatus(String status) {
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

    public BatchInput withOperator(Operator operator) {
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

    public BatchInput withLockedBy(LockedBy lockedBy) {
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

    public BatchInput withLastModUser(LastModUser lastModUser) {
        this.lastModUser = lastModUser;
        return this;
    }

    @JsonProperty("artefacts")
    public List<ArtefactInput> getArtefacts() {
        return artefacts;
    }

    @JsonProperty("artefacts")
    public void setArtefacts(List<ArtefactInput> artefacts) {
        this.artefacts = artefacts;
    }

    public BatchInput withArtefacts(List<ArtefactInput> artefacts) {
        this.artefacts = artefacts;
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

    public BatchInput withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BatchInput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        result = ((result * 31) + ((int) (this.id ^ (this.id >>> 32))));
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
        if ((other instanceof BatchInput) == false) {
            return false;
        }
        BatchInput rhs = ((BatchInput) other);
        return ((((((((((((this.lastModificationDate == rhs.lastModificationDate) || ((this.lastModificationDate != null) && this.lastModificationDate.equals(rhs.lastModificationDate))) && ((this.lockedBy == rhs.lockedBy) || ((this.lockedBy != null) && this.lockedBy.equals(rhs.lockedBy)))) && ((this.lockedDate == rhs.lockedDate) || ((this.lockedDate != null) && this.lockedDate.equals(rhs.lockedDate)))) && (this.id == rhs.id)) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.batchSequence == rhs.batchSequence) || ((this.batchSequence != null) && this.batchSequence.equals(rhs.batchSequence)))) && ((this.creationDate == rhs.creationDate) || ((this.creationDate != null) && this.creationDate.equals(rhs.creationDate)))) && ((this.operator == rhs.operator) || ((this.operator != null) && this.operator.equals(rhs.operator)))) && ((this.artefacts == rhs.artefacts) || ((this.artefacts != null) && this.artefacts.equals(rhs.artefacts)))) && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status)))) && ((this.lastModUser == rhs.lastModUser) || ((this.lastModUser != null) && this.lastModUser.equals(rhs.lastModUser))));
    }

}
