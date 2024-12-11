
package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "value", "key", "insertedDate", "type" })
public class ArtefactItemTag implements Serializable {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("value")
	private String value;

	@JsonProperty("key")
	private String key;

	@JsonProperty("insertedDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String insertedDate;

	@JsonProperty("type")
	private String type;

	@JsonIgnore
	@Valid
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	private final static long serialVersionUID = 4010579139943320954L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ArtefactItemTag() {
		this.insertedDate = DateUtils.getCurrentDatetimeUtcStr();
	}

	/**
	 * @param insertedDate
	 * @param id
	 * @param type
	 * @param value
	 * @param key
	 */
	public ArtefactItemTag(Long id, String value, String key, String insertedDate, String type) {
		super();
		this.id = id;
		this.value = value;
		this.key = key;
		this.insertedDate = insertedDate;
		this.type = type;
	}

	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Long id) {
		this.id = id;
	}

	public ArtefactItemTag withId(Long id) {
		this.id = id;
		return this;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}

	public ArtefactItemTag withValue(String value) {
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

	public ArtefactItemTag withKey(String key) {
		this.key = key;
		return this;
	}

	@JsonProperty("insertedDate")
	public String getInsertedDate() {
		return insertedDate;
	}

	@JsonProperty("insertedDate")
	public void setInsertedDate(String insertedDate) {
		this.insertedDate = insertedDate;
	}

	public ArtefactItemTag withInsertedDate(String insertedDate) {
		this.insertedDate = insertedDate;
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

	public ArtefactItemTag withType(String type) {
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

	public ArtefactItemTag withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactItemTag.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("id");
		sb.append('=');
		sb.append(((this.id == null) ? "<null>" : this.id));
		sb.append(',');
		sb.append("value");
		sb.append('=');
		sb.append(((this.value == null) ? "<null>" : this.value));
		sb.append(',');
		sb.append("key");
		sb.append('=');
		sb.append(((this.key == null) ? "<null>" : this.key));
		sb.append(',');
		sb.append("insertedDate");
		sb.append('=');
		sb.append(((this.insertedDate == null) ? "<null>" : this.insertedDate));
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

	@Override
	public int hashCode() {
		int result = 1;
		result = ((result * 31) + ((this.insertedDate == null) ? 0 : this.insertedDate.hashCode()));
		result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
		result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
		result = ((result * 31) + ((this.type == null) ? 0 : this.type.hashCode()));
		result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
		result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof ArtefactItemTag) == false) {
			return false;
		}
		ArtefactItemTag rhs = ((ArtefactItemTag) other);
		return (((((((this.insertedDate == rhs.insertedDate)
				|| ((this.insertedDate != null) && this.insertedDate.equals(rhs.insertedDate)))
				&& ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
				&& ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null)
						&& this.additionalProperties.equals(rhs.additionalProperties))))
				&& ((this.type == rhs.type) || ((this.type != null) && this.type.equals(rhs.type))))
				&& ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))))
				&& ((this.key == rhs.key) || ((this.key != null) && this.key.equals(rhs.key))));
	}

}
