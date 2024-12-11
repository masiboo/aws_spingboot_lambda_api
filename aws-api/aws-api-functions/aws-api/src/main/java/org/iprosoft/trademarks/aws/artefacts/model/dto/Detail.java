package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class Detail implements Serializable {

	private final static long serialVersionUID = -2182960884441214969L;

	private String version;

	private Bucket bucket;

	private Object object;

	@JsonProperty("request-id")
	private String requestId;

	private String requester;

	@JsonProperty("source-ip-address")
	private String sourceIpAddress;

	private String reason;

	/**
	 * No args constructor for use in serialization
	 */
	public Detail() {
	}

	/**
	 * @param bucket
	 * @param requester
	 * @param reason
	 * @param requestId
	 * @param sourceIpAddress
	 * @param version
	 * @param object
	 */
	public Detail(String version, Bucket bucket, Object object, String requestId, String requester,
			String sourceIpAddress, String reason) {
		super();
		this.version = version;
		this.bucket = bucket;
		this.object = object;
		this.requestId = requestId;
		this.requester = requester;
		this.sourceIpAddress = sourceIpAddress;
		this.reason = reason;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequester() {
		return requester;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public String getSourceIpAddress() {
		return sourceIpAddress;
	}

	public void setSourceIpAddress(String sourceIpAddress) {
		this.sourceIpAddress = sourceIpAddress;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Detail.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("version");
		sb.append('=');
		sb.append(((this.version == null) ? "<null>" : this.version));
		sb.append(',');
		sb.append("bucket");
		sb.append('=');
		sb.append(((this.bucket == null) ? "<null>" : this.bucket));
		sb.append(',');
		sb.append("object");
		sb.append('=');
		sb.append(((this.object == null) ? "<null>" : this.object));
		sb.append(',');
		sb.append("requestId");
		sb.append('=');
		sb.append(((this.requestId == null) ? "<null>" : this.requestId));
		sb.append(',');
		sb.append("requester");
		sb.append('=');
		sb.append(((this.requester == null) ? "<null>" : this.requester));
		sb.append(',');
		sb.append("sourceIpAddress");
		sb.append('=');
		sb.append(((this.sourceIpAddress == null) ? "<null>" : this.sourceIpAddress));
		sb.append(',');
		sb.append("reason");
		sb.append('=');
		sb.append(((this.reason == null) ? "<null>" : this.reason));
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
