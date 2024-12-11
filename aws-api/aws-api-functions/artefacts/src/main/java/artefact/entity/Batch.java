
package artefact.entity;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "batchSequence",
    "creationDate"
})
public class Batch implements Serializable
{

    @JsonProperty("batchSequence")
    private String batchSequence;
    @JsonProperty("creationDate")
    private String creationDate;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -8711180690470139588L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Batch() {
    }

    /**
     * 
     * @param batchSequence
     * @param creationDate
     */
    public Batch(String batchSequence, String creationDate) {
        super();
        this.batchSequence = batchSequence;
        this.creationDate = creationDate;
    }

    @JsonProperty("batchSequence")
    public String getBatchSequence() {
        return batchSequence;
    }

    @JsonProperty("batchSequence")
    public void setBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
    }

    public Batch withBatchSequence(String batchSequence) {
        this.batchSequence = batchSequence;
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

    public Batch withCreationDate(String creationDate) {
        this.creationDate = creationDate;
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

    public Batch withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Batch.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("batchSequence");
        sb.append('=');
        sb.append(((this.batchSequence == null)?"<null>":this.batchSequence));
        sb.append(',');
        sb.append("creationDate");
        sb.append('=');
        sb.append(((this.creationDate == null)?"<null>":this.creationDate));
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
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.batchSequence == null)? 0 :this.batchSequence.hashCode()));
        result = ((result* 31)+((this.creationDate == null)? 0 :this.creationDate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Batch) == false) {
            return false;
        }
        Batch rhs = ((Batch) other);
        return ((((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties)))&&((this.batchSequence == rhs.batchSequence)||((this.batchSequence!= null)&&this.batchSequence.equals(rhs.batchSequence))))&&((this.creationDate == rhs.creationDate)||((this.creationDate!= null)&&this.creationDate.equals(rhs.creationDate))));
    }

}
