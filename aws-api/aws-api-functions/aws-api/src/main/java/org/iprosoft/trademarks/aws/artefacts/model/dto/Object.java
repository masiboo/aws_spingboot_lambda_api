package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Object implements Serializable {

	private final static long serialVersionUID = -4820251252814187929L;

	private String key;

	private Long size;

	private String etag;

	@JsonProperty("version-id")
	private String versionId;

	private String sequencer;

	/**
	 * No args constructor for use in serialization
	 */
	public Object() {
	}

	/**
	 * @param versionId
	 * @param size
	 * @param etag
	 * @param key
	 * @param sequencer
	 */
	public Object(String key, Long size, String etag, String versionId, String sequencer) {
		super();
		this.key = key;
		this.size = size;
		this.etag = etag;
		this.versionId = versionId;
		this.sequencer = sequencer;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getSequencer() {
		return sequencer;
	}

	public void setSequencer(String sequencer) {
		this.sequencer = sequencer;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Object.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("key");
		sb.append('=');
		sb.append(((this.key == null) ? "<null>" : this.key));
		sb.append(',');
		sb.append("size");
		sb.append('=');
		sb.append(((this.size == null) ? "<null>" : this.size));
		sb.append(',');
		sb.append("etag");
		sb.append('=');
		sb.append(((this.etag == null) ? "<null>" : this.etag));
		sb.append(',');
		sb.append("versionId");
		sb.append('=');
		sb.append(((this.versionId == null) ? "<null>" : this.versionId));
		sb.append(',');
		sb.append("sequencer");
		sb.append('=');
		sb.append(((this.sequencer == null) ? "<null>" : this.sequencer));
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
		result = ((result * 31) + ((this.etag == null) ? 0 : this.etag.hashCode()));
		result = ((result * 31) + ((this.versionId == null) ? 0 : this.versionId.hashCode()));
		result = ((result * 31) + ((this.size == null) ? 0 : this.size.hashCode()));
		result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
		result = ((result * 31) + ((this.sequencer == null) ? 0 : this.sequencer.hashCode()));
		return result;
	}

	@Override
	public boolean equals(java.lang.Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Object)) {
			return false;
		}
		Object rhs = ((Object) other);
		return (((((Objects.equals(this.etag, rhs.etag)) && (Objects.equals(this.versionId, rhs.versionId)))
				&& (Objects.equals(this.size, rhs.size))) && (Objects.equals(this.key, rhs.key)))
				&& (Objects.equals(this.sequencer, rhs.sequencer)));
	}

}
