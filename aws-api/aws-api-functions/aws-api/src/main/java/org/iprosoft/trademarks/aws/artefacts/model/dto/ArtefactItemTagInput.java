
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "value", "key", "type" })
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactItemTagInput implements Serializable {

	@JsonProperty("value")
	private String value;

	@JsonProperty("key")
	private String key;

	@JsonProperty("type")
	private String type;

	@JsonIgnore
	@Valid
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	private final static long serialVersionUID = -1026720546767039912L;

	/**
	 * No args constructor for use in serialization
	 */
	public ArtefactItemTagInput() {
	}

	/**
	 * @param type
	 * @param value
	 * @param key
	 */
	public ArtefactItemTagInput(String value, String key, String type) {
		super();
		this.value = value;
		this.key = key;
		this.type = type;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}

	public ArtefactItemTagInput withValue(String value) {
		this.value = value;
		return this;
	}

	@JsonProperty("key")
	public String getKey() {
		return key;
	}

	@JsonProperty("key")
	public void setKey(String key) {
		this.key = key;
	}

	public ArtefactItemTagInput withKey(String key) {
		this.key = key;
		return this;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	public ArtefactItemTagInput withType(String type) {
		this.type = type;
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

	public ArtefactItemTagInput withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactItemTagInput.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("value");
		sb.append('=');
		sb.append(((this.value == null) ? "<null>" : this.value));
		sb.append(',');
		sb.append("key");
		sb.append('=');
		sb.append(((this.key == null) ? "<null>" : this.key));
		sb.append(',');
		sb.append("type");
		sb.append('=');
		sb.append(((this.type == null) ? "<null>" : this.type));
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

}
