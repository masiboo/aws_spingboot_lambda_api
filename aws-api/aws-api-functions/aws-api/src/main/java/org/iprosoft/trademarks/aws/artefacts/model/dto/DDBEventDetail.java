package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "artefactId", "artefact", "artefactType", "eventType", "eventId", "status" })
public class DDBEventDetail implements Serializable {

	@JsonProperty("artefactId")
	private String artefactId;

	@JsonProperty("artefact")
	private DDBEventArtefact artefact;

	@JsonProperty("artefactType")
	private String artefactType;

	@JsonProperty("eventType")
	private String eventType;

	@JsonProperty("eventId")
	private String eventId;

	@JsonProperty("status")
	private String status;

	private final static long serialVersionUID = 542568591859415864L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public DDBEventDetail() {
	}

	/**
	 * @param eventId
	 * @param artefactType
	 * @param artefactId
	 * @param artefact
	 * @param eventType
	 * @param status
	 */
	public DDBEventDetail(String artefactId, DDBEventArtefact artefact, String artefactType, String eventType,
			String eventId, String status) {
		super();
		this.artefactId = artefactId;
		this.artefact = artefact;
		this.artefactType = artefactType;
		this.eventType = eventType;
		this.eventId = eventId;
		this.status = status;
	}

	@JsonProperty("artefactId")
	public String getArtefactId() {
		return artefactId;
	}

	@JsonProperty("artefactId")
	public void setArtefactId(String artefactId) {
		this.artefactId = artefactId;
	}

	@JsonProperty("artefact")
	public DDBEventArtefact getArtefact() {
		return artefact;
	}

	@JsonProperty("artefact")
	public void setArtefact(DDBEventArtefact artefact) {
		this.artefact = artefact;
	}

	@JsonProperty("artefactType")
	public String getArtefactType() {
		return artefactType;
	}

	@JsonProperty("artefactType")
	public void setArtefactType(String artefactType) {
		this.artefactType = artefactType;
	}

	@JsonProperty("eventType")
	public String getEventType() {
		return eventType;
	}

	@JsonProperty("eventType")
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@JsonProperty("eventId")
	public String getEventId() {
		return eventId;
	}

	@JsonProperty("eventId")
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(DDBEventDetail.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("artefactId");
		sb.append('=');
		sb.append(((this.artefactId == null) ? "<null>" : this.artefactId));
		sb.append(',');
		sb.append("artefact");
		sb.append('=');
		sb.append(((this.artefact == null) ? "<null>" : this.artefact));
		sb.append(',');
		sb.append("artefactType");
		sb.append('=');
		sb.append(((this.artefactType == null) ? "<null>" : this.artefactType));
		sb.append(',');
		sb.append("eventType");
		sb.append('=');
		sb.append(((this.eventType == null) ? "<null>" : this.eventType));
		sb.append(',');
		sb.append("eventId");
		sb.append('=');
		sb.append(((this.eventId == null) ? "<null>" : this.eventId));
		sb.append(',');
		sb.append("status");
		sb.append('=');
		sb.append(((this.status == null) ? "<null>" : this.status));
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