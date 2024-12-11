
package artefact.dto;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "wipoId",
        "username",
        "cognitoId"
})

public class Operator implements Serializable {

    private final static long serialVersionUID = -9065132365041685938L;
    @JsonProperty("id")
    private long id;
    @JsonProperty("wipoId")
    private String wipoId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("cognitoId")
    private String cognitoId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public Operator() {
    }

    /**
     * @param wipoId
     * @param cognitoId
     * @param id
     * @param username
     */
    public Operator(long id, String wipoId, String username, String cognitoId) {
        super();
        this.id = id;
        this.wipoId = wipoId;
        this.username = username;
        this.cognitoId = cognitoId;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public Operator withId(long id) {
        this.id = id;
        return this;
    }

    @JsonProperty("wipoId")
    public String getWipoId() {
        return wipoId;
    }

    @JsonProperty("wipoId")
    public void setWipoId(String wipoId) {
        this.wipoId = wipoId;
    }

    public Operator withWipoId(String wipoId) {
        this.wipoId = wipoId;
        return this;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    public Operator withUsername(String username) {
        this.username = username;
        return this;
    }

    @JsonProperty("cognitoId")
    public String getCognitoId() {
        return cognitoId;
    }

    @JsonProperty("cognitoId")
    public void setCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
    }

    public Operator withCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
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

    public Operator withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Operator.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(this.id);
        sb.append(',');
        sb.append("wipoId");
        sb.append('=');
        sb.append(((this.wipoId == null) ? "<null>" : this.wipoId));
        sb.append(',');
        sb.append("username");
        sb.append('=');
        sb.append(((this.username == null) ? "<null>" : this.username));
        sb.append(',');
        sb.append("cognitoId");
        sb.append('=');
        sb.append(((this.cognitoId == null) ? "<null>" : this.cognitoId));
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
        result = ((result * 31) + ((this.wipoId == null) ? 0 : this.wipoId.hashCode()));
        result = ((result * 31) + ((this.cognitoId == null) ? 0 : this.cognitoId.hashCode()));
        result = ((result * 31) + ((int) (this.id ^ (this.id >>> 32))));
        result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
        result = ((result * 31) + ((this.username == null) ? 0 : this.username.hashCode()));
        return result;
    }

//    @Override
//    public boolean equals(Object other) {
//        if (other == this) {
//            return true;
//        }
//        if ((other instanceof Operator) == false) {
//            return false;
//        }
//        Operator rhs = ((Operator) other);
//        return ((((((this.wipoId == rhs.wipoId) || ((this.wipoId != null) && this.wipoId.equals(rhs.wipoId))) && ((this.cognitoId == rhs.cognitoId) || ((this.cognitoId != null) && this.cognitoId.equals(rhs.cognitoId)))) && (this.id == rhs.id)) && ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null) && this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.username == rhs.username) || ((this.username != null) && this.username.equals(rhs.username))));
//    }

}
