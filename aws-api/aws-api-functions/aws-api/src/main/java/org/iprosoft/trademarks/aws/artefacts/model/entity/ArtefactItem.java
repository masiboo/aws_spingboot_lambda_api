
package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "storage", "path", "filename", "contentType", "totalPages", "pageIndex", "artefactType" })

public class ArtefactItem implements Serializable {

	private final static long serialVersionUID = -4503274556159749889L;

	@JsonProperty("storage")
	private String storage;

	@JsonProperty("path")
	private String path;

	@JsonProperty("filename")
	private String filename;

	@JsonProperty("contentType")
	private String contentType;

	@JsonProperty("id")
	private long id;

	@JsonProperty("totalPages")
	private long totalPages;

	@JsonProperty("pageIndex")
	private long pageIndex;

	@JsonProperty("artefactType")
	private String artefactType;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 */
	public ArtefactItem() {
	}

	/**
	 * @param path
	 * @param filename
	 * @param pageIndex
	 * @param artefactType
	 * @param totalPages
	 * @param id
	 * @param storage
	 * @param contentType
	 */
	public ArtefactItem(long id, String storage, String path, String filename, String contentType, long totalPages,
			long pageIndex, String artefactType) {
		super();
		this.id = id;
		this.storage = storage;
		this.path = path;
		this.filename = filename;
		this.contentType = contentType;
		this.totalPages = totalPages;
		this.pageIndex = pageIndex;
		this.artefactType = artefactType;
	}

	@JsonProperty("id")
	public long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(long id) {
		this.id = id;
	}

	public ArtefactItem withId(long id) {
		this.id = id;
		return this;
	}

	@JsonProperty("storage")
	public String getStorage() {
		return storage;
	}

	@JsonProperty("storage")
	public void setStorage(String storage) {
		this.storage = storage;
	}

	public ArtefactItem withStorage(String storage) {
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

	public ArtefactItem withPath(String path) {
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

	public ArtefactItem withFilename(String filename) {
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

	public ArtefactItem withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@JsonProperty("totalPages")
	public long getTotalPages() {
		return totalPages;
	}

	@JsonProperty("totalPages")
	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}

	public ArtefactItem withTotalPages(long totalPages) {
		this.totalPages = totalPages;
		return this;
	}

	@JsonProperty("pageIndex")
	public long getPageIndex() {
		return pageIndex;
	}

	@JsonProperty("pageIndex")
	public void setPageIndex(long pageIndex) {
		this.pageIndex = pageIndex;
	}

	public ArtefactItem withPageIndex(long pageIndex) {
		this.pageIndex = pageIndex;
		return this;
	}

	@JsonProperty("artefactType")
	public String getArtefactType() {
		return artefactType;
	}

	@JsonProperty("artefactType")
	public void setArtefactType(String artefactType) {
		this.artefactType = artefactType;
	}

	public ArtefactItem withArtefactType(String artefactType) {
		this.artefactType = artefactType;
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

	public ArtefactItem withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactItem.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("id");
		sb.append('=');
		sb.append(this.id);
		sb.append(',');
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
		sb.append("totalPages");
		sb.append('=');
		sb.append(this.totalPages);
		sb.append(',');
		sb.append("pageIndex");
		sb.append('=');
		sb.append(this.pageIndex);
		sb.append(',');
		sb.append("artefactType");
		sb.append('=');
		sb.append(((this.artefactType == null) ? "<null>" : this.artefactType));
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
		result = ((result * 31) + ((this.path == null) ? 0 : this.path.hashCode()));
		result = ((result * 31) + ((this.filename == null) ? 0 : this.filename.hashCode()));
		result = ((result * 31) + ((int) (this.pageIndex ^ (this.pageIndex >>> 32))));
		result = ((result * 31) + ((this.artefactType == null) ? 0 : this.artefactType.hashCode()));
		result = ((result * 31) + ((int) (this.totalPages ^ (this.totalPages >>> 32))));
		result = ((result * 31) + ((int) (this.id ^ (this.id >>> 32))));
		result = ((result * 31) + ((this.storage == null) ? 0 : this.storage.hashCode()));
		result = ((result * 31) + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
		result = ((result * 31) + ((this.contentType == null) ? 0 : this.contentType.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof ArtefactItem) == false) {
			return false;
		}
		ArtefactItem rhs = ((ArtefactItem) other);
		return ((((((((((this.path == rhs.path) || ((this.path != null) && this.path.equals(rhs.path)))
				&& ((this.filename == rhs.filename) || ((this.filename != null) && this.filename.equals(rhs.filename))))
				&& (this.pageIndex == rhs.pageIndex))
				&& ((this.artefactType == rhs.artefactType)
						|| ((this.artefactType != null) && this.artefactType.equals(rhs.artefactType))))
				&& (this.totalPages == rhs.totalPages)) && (this.id == rhs.id))
				&& ((this.storage == rhs.storage) || ((this.storage != null) && this.storage.equals(rhs.storage))))
				&& ((this.additionalProperties == rhs.additionalProperties) || ((this.additionalProperties != null)
						&& this.additionalProperties.equals(rhs.additionalProperties))))
				&& ((this.contentType == rhs.contentType)
						|| ((this.contentType != null) && this.contentType.equals(rhs.contentType))));
	}

}
