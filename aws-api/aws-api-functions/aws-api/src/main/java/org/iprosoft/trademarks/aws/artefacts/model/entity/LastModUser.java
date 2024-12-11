
package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "aws", "username", "cognitoId" })

public class LastModUser implements Serializable {

	private final static long serialVersionUID = 5027535427525923245L;

	@JsonProperty("id")
	private long id;

	@JsonProperty("awsId")
	private String awsId;

	@JsonProperty("username")
	private String username;

	@JsonProperty("cognitoId")
	private String cognitoId;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 */
	public LastModUser() {
	}

	/**
	 * @param awsId
	 * @param cognitoId
	 * @param id
	 * @param username
	 */
	public LastModUser(long id, String awsId, String username, String cognitoId) {
		super();
		this.id = id;
		this.awsId = awsId;
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

	public LastModUser withId(long id) {
		this.id = id;
		return this;
	}

	@JsonProperty("awsId")
	public String getAwsId() {
		return awsId;
	}

	@JsonProperty("awsId")
	public void setAwsId(String awsId) {
		this.awsId = awsId;
	}

	public LastModUser withAwsId(String awsId) {
		this.awsId = awsId;
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

	public LastModUser withUsername(String username) {
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

	public LastModUser withCognitoId(String cognitoId) {
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

	public LastModUser withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(LastModUser.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("id");
		sb.append('=');
		sb.append(this.id);
		sb.append(',');
		sb.append("awsId");
		sb.append('=');
		sb.append(((this.awsId == null) ? "<null>" : this.awsId));
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
		}
		else {
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = ((result * 31) + ((this.awsId == null) ? 0 : this.awsId.hashCode()));
		result = ((result * 31) + ((this.cognitoId == null) ? 0 : this.cognitoId.hashCode()));
		result = ((result * 31) + ((int) (this.id ^ (this.id >>> 32))));
		result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
		result = ((result * 31) + ((this.username == null) ? 0 : this.username.hashCode()));
		return result;
	}

	// @Override
	// public boolean equals(Object other) {
	// if (other == this) {
	// return true;
	// }
	// if ((other instanceof LastModUser) == false) {
	// return false;
	// }
	// LastModUser rhs = ((LastModUser) other);
	// return ((((((this.wipoId == rhs.wipoId) || ((this.wipoId != null) &&
	// this.wipoId.equals(rhs.wipoId))) && ((this.cognitoId == rhs.cognitoId) ||
	// ((this.cognitoId != null) && this.cognitoId.equals(rhs.cognitoId)))) && (this.id ==
	// rhs.id)) && ((this.additionalProperties == rhs.additionalProperties) ||
	// ((this.additionalProperties != null) &&
	// this.additionalProperties.equals(rhs.additionalProperties)))) && ((this.username ==
	// rhs.username) || ((this.username != null) && this.username.equals(rhs.username))));
	// }

}
