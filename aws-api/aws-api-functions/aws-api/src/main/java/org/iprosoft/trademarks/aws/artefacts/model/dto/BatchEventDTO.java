
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchEventDTO implements Serializable {

	private String version;

	private String id;

	private String detailType;

	private String source;

	private String account;

	private String time;

	private String region;

	private List<java.lang.Object> resources;

	private BatchEventDetail detail;

	private final static long serialVersionUID = -7311372937512073627L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public BatchEventDTO() {
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
	public BatchEventDTO(String version, String id, String detailType, String source, String account, String time,
			String region, List<java.lang.Object> resources, BatchEventDetail detail) {
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

	public List<java.lang.Object> getResources() {
		return resources;
	}

	public void setResources(List<java.lang.Object> resources) {
		this.resources = resources;
	}

	public BatchEventDetail getDetail() {
		return detail;
	}

	public void setDetail(BatchEventDetail detail) {
		this.detail = detail;
	}

}
