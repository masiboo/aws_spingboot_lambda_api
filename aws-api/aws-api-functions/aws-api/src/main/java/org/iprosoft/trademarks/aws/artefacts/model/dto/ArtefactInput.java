
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "artefactName", "artefactClassType", "artefactItemTags", "items", "mirisDocId" })
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactInput implements Serializable {

	@JsonProperty("artefactName")
	@NotNull(message = "Name cannot be null")
	private String artefactName;

	@NotNull(message = "Name cannot be null")
	@JsonProperty("artefactClassType")
	private String artefactClassType;

	@JsonProperty("artefactItemTags")
	@Valid
	private List<org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemTagInput> artefactItemTagInputs = new ArrayList<>();

	@JsonProperty("items")
	@Valid
	private List<org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput> artefactItemInputs = new ArrayList<>();

	@JsonProperty("mirisDocId")
	@Pattern(regexp = "^[0-9]{5,8}$", message = "mirisDocId must be between 5 to 8 digits and no space allowed")
	@NotNull
	private String mirisDocId;

	@JsonIgnore
	@Valid
	private Map<String, Object> additionalProperties = new HashMap<>();

	public enum classType {

		CERTIFICATE, DOCUMENT, BWLOGO, COLOURLOGO, SOUND, MULTIMEDIA

	}

	@Serial
	private final static long serialVersionUID = -6005086773412589591L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ArtefactInput() {
	}

	/**
	 * @param artefactName
	 * @param artefactItemTagInputs
	 * @param mirisDocId
	 * @param artefactClassType
	 * @param artefactItemInputs
	 */
	public ArtefactInput(String artefactName, String artefactClassType,
			List<ArtefactItemTagInput> artefactItemTagInputs, List<ArtefactItemInput> artefactItemInputs,
			String mirisDocId) {
		super();
		this.artefactName = artefactName;
		this.artefactClassType = artefactClassType;
		this.artefactItemTagInputs = artefactItemTagInputs;
		this.artefactItemInputs = artefactItemInputs;
		this.mirisDocId = mirisDocId;
	}

	@JsonProperty("artefactName")
	public String getArtefactName() {
		return artefactName;
	}

	@JsonProperty("artefactName")
	public void setArtefactName(String artefactName) {
		this.artefactName = artefactName;
	}

	public ArtefactInput withArtefactName(String artefactName) {
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

	public ArtefactInput withArtefactClassType(String artefactClassType) {
		this.artefactClassType = artefactClassType;
		return this;
	}

	@JsonProperty("artefactItemTags")
	public List<ArtefactItemTagInput> getArtefactItemTags() {
		return artefactItemTagInputs;
	}

	@JsonProperty("artefactItemTags")
	public void setArtefactItemTags(List<ArtefactItemTagInput> artefactItemTagInputs) {
		this.artefactItemTagInputs = artefactItemTagInputs;
	}

	public ArtefactInput withArtefactItemTags(List<ArtefactItemTagInput> artefactItemTagInputs) {
		this.artefactItemTagInputs = artefactItemTagInputs;
		return this;
	}

	@JsonProperty("items")
	public List<ArtefactItemInput> getItems() {
		return artefactItemInputs;
	}

	@JsonProperty("items")
	public void setItems(List<ArtefactItemInput> artefactItemInputs) {
		this.artefactItemInputs = artefactItemInputs;
	}

	public ArtefactInput withItems(List<ArtefactItemInput> artefactItemInputs) {
		this.artefactItemInputs = artefactItemInputs;
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

	public ArtefactInput withMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
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

	public ArtefactInput withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ArtefactInput.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("artefactName");
		sb.append('=');
		sb.append(((this.artefactName == null) ? "<null>" : this.artefactName));
		sb.append(',');
		sb.append("artefactClassType");
		sb.append('=');
		sb.append(((this.artefactClassType == null) ? "<null>" : this.artefactClassType));
		sb.append(',');
		sb.append("artefactItemTags");
		sb.append('=');
		sb.append(((this.artefactItemTagInputs == null) ? "<null>" : this.artefactItemTagInputs));
		sb.append(',');
		sb.append("artefactItemInputs");
		sb.append('=');
		sb.append(((this.artefactItemInputs == null) ? "<null>" : this.artefactItemInputs));
		sb.append(',');
		sb.append("mirisDocId");
		sb.append('=');
		sb.append(((this.mirisDocId == null) ? "<null>" : this.mirisDocId));
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
