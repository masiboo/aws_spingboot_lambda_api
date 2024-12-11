
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactItem;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactItemTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "artefactName", "artefactClassType", "status", "error", "indexationDate", "archiveDate",
		"gets3Bucket", "mirisDocId", "artefactItemTags", "items" })
@EqualsAndHashCode
public class ArtefactOutput implements Serializable {

	@JsonProperty("id")
	private String id;

	@JsonProperty("artefactName")
	private String artefactName;

	@JsonProperty("artefactClassType")
	private String artefactClassType;

	@JsonProperty("status")
	private String status;

	@JsonProperty("error")
	private String error;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	@JsonProperty("indexationDate")
	private String indexationDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	@JsonProperty("archiveDate")
	private String archiveDate;

	@JsonProperty("gets3Bucket")
	private String gets3Bucket;

	@JsonProperty("mirisDocId")
	private String mirisDocId;

	public String getBatchSequence() {
		return batchSequence;
	}

	public void setBatchSequence(String batchSequence) {
		this.batchSequence = batchSequence;
	}

	public String getArtefactContainer() {
		return artefactContainer;
	}

	public void setArtefactContainer(String artefactContainer) {
		this.artefactContainer = artefactContainer;
	}

	public List<ArtefactItem> getArtefactItems() {
		return artefactItems;
	}

	public void setArtefactItems(List<ArtefactItem> artefactItems) {
		this.artefactItems = artefactItems;
	}

	private String batchSequence;

	private String artefactContainer;

	@JsonProperty("artefactItemTags")
	@Valid
	private List<ArtefactItemTag> artefactItemTags = new ArrayList<ArtefactItemTag>();

	@JsonProperty("items")
	@Valid
	private List<ArtefactItem> artefactItems = new ArrayList<ArtefactItem>();

	@JsonIgnore
	@Valid
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	private final static long serialVersionUID = -8399083458956934595L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ArtefactOutput() {
	}

	/**
	 * @param artefactName
	 * @param gets3Bucket
	 * @param archiveDate
	 * @param artefactItemTags
	 * @param mirisDocId
	 * @param id
	 * @param error
	 * @param indexationDate
	 * @param artefactClassType
	 * @param artefactItems
	 * @param status
	 */
	public ArtefactOutput(String id, String artefactName, String artefactClassType, String status, String error,
			String indexationDate, String archiveDate, String gets3Bucket, String mirisDocId,
			List<ArtefactItemTag> artefactItemTags, List<ArtefactItem> artefactItems) {
		super();
		this.id = id;
		this.artefactName = artefactName;
		this.artefactClassType = artefactClassType;
		this.status = status;
		this.error = error;
		this.indexationDate = indexationDate;
		this.archiveDate = archiveDate;
		this.gets3Bucket = gets3Bucket;
		this.mirisDocId = mirisDocId;
		this.artefactItemTags = artefactItemTags;
		this.artefactItems = artefactItems;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	public ArtefactOutput withId(String id) {
		this.id = id;
		return this;
	}

	@JsonProperty("artefactName")
	public String getArtefactName() {
		return artefactName;
	}

	@JsonProperty("artefactName")
	public void setArtefactName(String artefactName) {
		this.artefactName = artefactName;
	}

	public ArtefactOutput withArtefactName(String artefactName) {
		this.artefactName = artefactName;
		return this;
	}

	@JsonProperty("artefactClassType")
	public String getArtefactClassType() {
		return artefactClassType;
	}

	@JsonProperty("artefactClassType")
	public void setArtefactClassType(String artefactClassType) {
		this.artefactClassType = artefactClassType;
	}

	public ArtefactOutput withArtefactClassType(String artefactClassType) {
		this.artefactClassType = artefactClassType;
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

	public ArtefactOutput withStatus(String status) {
		this.status = status;
		return this;
	}

	@JsonProperty("error")
	public String getError() {
		return error;
	}

	@JsonProperty("error")
	public void setError(String error) {
		this.error = error;
	}

	public ArtefactOutput withError(String error) {
		this.error = error;
		return this;
	}

	@JsonProperty("indexationDate")
	public String getIndexationDate() {
		return indexationDate;
	}

	@JsonProperty("indexationDate")
	public void setIndexationDate(String indexationDate) {
		this.indexationDate = indexationDate;
	}

	public ArtefactOutput withIndexationDate(String indexationDate) {
		this.indexationDate = indexationDate;
		return this;
	}

	@JsonProperty("archiveDate")
	public String getArchiveDate() {
		return archiveDate;
	}

	@JsonProperty("archiveDate")
	public void setArchiveDate(String archiveDate) {
		this.archiveDate = archiveDate;
	}

	public ArtefactOutput withArchiveDate(String archiveDate) {
		this.archiveDate = archiveDate;
		return this;
	}

	@JsonProperty("gets3Bucket")
	public String getGets3Bucket() {
		return gets3Bucket;
	}

	@JsonProperty("gets3Bucket")
	public void setGets3Bucket(String gets3Bucket) {
		this.gets3Bucket = gets3Bucket;
	}

	public ArtefactOutput withGets3Bucket(String gets3Bucket) {
		this.gets3Bucket = gets3Bucket;
		return this;
	}

	@JsonProperty("mirisDocId")
	public String getMirisDocId() {
		return mirisDocId;
	}

	@JsonProperty("mirisDocId")
	public void setMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
	}

	public ArtefactOutput withMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
		return this;
	}

	@JsonProperty("ArtefactItemTags")
	public List<ArtefactItemTag> getArtefactItemTags() {
		return artefactItemTags;
	}

	@JsonProperty("ArtefactItemTags")
	public void setArtefactItemTags(List<ArtefactItemTag> artefactItemTags) {
		this.artefactItemTags = artefactItemTags;
	}

	public ArtefactOutput withArtefactItemTags(List<ArtefactItemTag> artefactItemTags) {
		this.artefactItemTags = artefactItemTags;
		return this;
	}

	@JsonProperty("items")
	public List<ArtefactItem> getItems() {
		return artefactItems;
	}

	@JsonProperty("items")
	public void setItems(List<ArtefactItem> artefactItems) {
		this.artefactItems = artefactItems;
	}

	public ArtefactOutput withItems(List<ArtefactItem> artefactItems) {
		this.artefactItems = artefactItems;
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

	public ArtefactOutput withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactOutput.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("id");
		sb.append('=');
		sb.append(((this.id == null) ? "<null>" : this.id));
		sb.append(',');
		sb.append("artefactName");
		sb.append('=');
		sb.append(((this.artefactName == null) ? "<null>" : this.artefactName));
		sb.append(',');
		sb.append("artefactClassType");
		sb.append('=');
		sb.append(((this.artefactClassType == null) ? "<null>" : this.artefactClassType));
		sb.append(',');
		sb.append("status");
		sb.append('=');
		sb.append(((this.status == null) ? "<null>" : this.status));
		sb.append(',');
		sb.append("error");
		sb.append('=');
		sb.append(((this.error == null) ? "<null>" : this.error));
		sb.append(',');
		sb.append("indexationDate");
		sb.append('=');
		sb.append(((this.indexationDate == null) ? "<null>" : this.indexationDate));
		sb.append(',');
		sb.append("archiveDate");
		sb.append('=');
		sb.append(((this.archiveDate == null) ? "<null>" : this.archiveDate));
		sb.append(',');
		sb.append("gets3Bucket");
		sb.append('=');
		sb.append(((this.gets3Bucket == null) ? "<null>" : this.gets3Bucket));
		sb.append(',');
		sb.append("mirisDocId");
		sb.append('=');
		sb.append(((this.mirisDocId == null) ? "<null>" : this.mirisDocId));
		sb.append(',');
		sb.append("artefactItemTags");
		sb.append('=');
		sb.append(((this.artefactItemTags == null) ? "<null>" : this.artefactItemTags));
		sb.append(',');
		sb.append("items");
		sb.append('=');
		sb.append(((this.artefactItems == null) ? "<null>" : this.artefactItems));
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
