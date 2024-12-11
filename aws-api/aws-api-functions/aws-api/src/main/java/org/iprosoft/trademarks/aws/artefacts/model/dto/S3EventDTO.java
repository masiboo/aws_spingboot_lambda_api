package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode
public class S3EventDTO implements Serializable {

	private final static long serialVersionUID = 6406480655813156384L;

	private String version;

	private String id;

	@JsonProperty("detail-type")
	private String detailType;

	private String source;

	private String account;

	private String time;

	private String region;

	private List<String> resources;

	private Detail detail;

	/**
	 * No args constructor for use in serialization
	 */
	public S3EventDTO() {
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
	public S3EventDTO(String version, String id, String detailType, String source, String account, String time,
			String region, List<String> resources, Detail detail) {
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDetailType() {
		return detailType;
	}

	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public List<String> getResources() {
		return resources;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
	}

	public Detail getDetail() {
		return detail;
	}

	public void setDetail(Detail detail) {
		this.detail = detail;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(S3EventDTO.class.getName())
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
