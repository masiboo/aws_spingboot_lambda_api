package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "version", "id", "detail-type", "source", "account", "time", "region", "resources", "detail" })

public class DynamoDBEventDTO implements Serializable {

	@JsonProperty("version")
	private String version;

	@JsonProperty("id")
	private String id;

	@JsonProperty("detail-type")
	private String detailType;

	@JsonProperty("source")
	private String source;

	@JsonProperty("account")
	private String account;

	@JsonProperty("time")
	private String time;

	@JsonProperty("region")
	private String region;

	@JsonProperty("resources")
	private List<java.lang.Object> resources;

	@JsonProperty("detail")
	private DDBEventDetail detail;

	private final static long serialVersionUID = 6116850250651051645L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public DynamoDBEventDTO() {
	}

	/**
	 * @param detailType
	 * @param resources
	 * @param id
	 * @param source
	 * @param time
	 * @param detail
	 * @param region
	 * @param version
	 * @param account
	 */
	public DynamoDBEventDTO(String version, String id, String detailType, String source, String account, String time,
			String region, List<java.lang.Object> resources, DDBEventDetail detail) {
		super();
		this.version = version;
		this.id = id;
		this.detailType = detailType;
		this.source = source;
		this.account = account;
		this.time = time;
		this.region = region;
		this.resources = resources;
		this.detail = detail;
	}

	@JsonProperty("version")
	public String getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(String version) {
		this.version = version;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("detail-type")
	public String getDetailType() {
		return detailType;
	}

	@JsonProperty("detail-type")
	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}

	@JsonProperty("source")
	public String getSource() {
		return source;
	}

	@JsonProperty("source")
	public void setSource(String source) {
		this.source = source;
	}

	@JsonProperty("account")
	public String getAccount() {
		return account;
	}

	@JsonProperty("account")
	public void setAccount(String account) {
		this.account = account;
	}

	@JsonProperty("time")
	public String getTime() {
		return time;
	}

	@JsonProperty("time")
	public void setTime(String time) {
		this.time = time;
	}

	@JsonProperty("region")
	public String getRegion() {
		return region;
	}

	@JsonProperty("region")
	public void setRegion(String region) {
		this.region = region;
	}

	@JsonProperty("resources")
	public List<java.lang.Object> getResources() {
		return resources;
	}

	@JsonProperty("resources")
	public void setResources(List<java.lang.Object> resources) {
		this.resources = resources;
	}

	@JsonProperty("detail")
	public DDBEventDetail getDetail() {
		return detail;
	}

	@JsonProperty("detail")
	public void setDetail(DDBEventDetail detail) {
		this.detail = detail;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(DynamoDBEventDTO.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("version");
		sb.append('=');
		sb.append(((this.version == null) ? "<null>" : this.version));
		sb.append(',');
		sb.append("id");
		sb.append('=');
		sb.append(((this.id == null) ? "<null>" : this.id));
		sb.append(',');
		sb.append("detailType");
		sb.append('=');
		sb.append(((this.detailType == null) ? "<null>" : this.detailType));
		sb.append(',');
		sb.append("source");
		sb.append('=');
		sb.append(((this.source == null) ? "<null>" : this.source));
		sb.append(',');
		sb.append("account");
		sb.append('=');
		sb.append(((this.account == null) ? "<null>" : this.account));
		sb.append(',');
		sb.append("time");
		sb.append('=');
		sb.append(((this.time == null) ? "<null>" : this.time));
		sb.append(',');
		sb.append("region");
		sb.append('=');
		sb.append(((this.region == null) ? "<null>" : this.region));
		sb.append(',');
		sb.append("resources");
		sb.append('=');
		sb.append(((this.resources == null) ? "<null>" : this.resources));
		sb.append(',');
		sb.append("detail");
		sb.append('=');
		sb.append(((this.detail == null) ? "<null>" : this.detail));
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
