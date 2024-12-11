
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "storage", "path", "filename", "contentType" })
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactItemInput implements Serializable {

	@JsonProperty("storage")
	private String storage;

	@JsonProperty("path")
	private String path;

	@JsonProperty("filename")
	private String filename;

	@JsonProperty("contentType")
	private String contentType;

	@JsonProperty("contentLength")
	private String contentLength;

	@JsonIgnore
	@Valid
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	private final static long serialVersionUID = -2812069656080434221L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ArtefactItemInput() {
	}

	/**
	 * @param path
	 * @param filename
	 * @param storage
	 * @param contentType
	 */
	public ArtefactItemInput(String storage, String path, String filename, String contentType, String contentLength) {
		super();
		this.storage = storage;
		this.path = path;
		this.filename = filename;
		this.contentType = contentType;
		this.contentLength = contentLength;
	}

	@JsonProperty("storage")
	public String getStorage() {
		return storage;
	}

	@JsonProperty("storage")
	public void setStorage(String storage) {
		this.storage = storage;
	}

	public ArtefactItemInput withStorage(String storage) {
		this.storage = storage;
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

	public ArtefactItemInput withPath(String path) {
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

	public ArtefactItemInput withFilename(String filename) {
		this.filename = filename;
		return this;
	}

	@JsonProperty("contentType")
	public String getContentType() {
		return contentType;
	}

	@JsonProperty("contentType")
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public ArtefactItemInput withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@JsonProperty("contentLength")
	public String getContentLength() {
		return contentLength;
	}

	@JsonProperty("contentLength")
	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public ArtefactItemInput withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactItemInput.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("storage");
		sb.append('=');
		sb.append(((this.storage == null) ? "<null>" : this.storage));
		sb.append(',');
		sb.append("path");
		sb.append('=');
		sb.append(((this.path == null) ? "<null>" : this.path));
		sb.append(',');
		sb.append("filename");
		sb.append('=');
		sb.append(((this.filename == null) ? "<null>" : this.filename));
		sb.append(',');
		sb.append("contentType");
		sb.append('=');
		sb.append(((this.contentType == null) ? "<null>" : this.contentType));
		sb.append(',');
		sb.append("contentLength");
		sb.append('=');
		sb.append(((this.contentLength == null) ? "<null>" : this.contentLength));
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
