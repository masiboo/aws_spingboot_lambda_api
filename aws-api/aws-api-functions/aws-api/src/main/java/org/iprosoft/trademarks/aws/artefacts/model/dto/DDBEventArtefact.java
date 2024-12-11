
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "GSI1PK", "GSI1SK", "PK", "SK", "artefactId", "insertedDate", "mirisDocId", "s3Bucket", "s3Key",
		"status", "type", "userId" })

public class DDBEventArtefact implements Serializable {

	@JsonProperty("GSI1PK")
	private String gsi1pk;

	@JsonProperty("GSI1SK")
	private String gsi1sk;

	@JsonProperty("PK")
	private String pk;

	@JsonProperty("SK")
	private String sk;

	@JsonProperty("artefactId")
	private String artefactId;

	@JsonProperty("insertedDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String insertedDate;

	@JsonProperty("mirisDocId")
	private String mirisDocId;

	@JsonProperty("s3Bucket")
	private String s3Bucket;

	@JsonProperty("s3Key")
	private String s3Key;

	@JsonProperty("status")
	private String status;

	@JsonProperty("type")
	private String type;

	@JsonProperty("userId")
	private String userId;

	private final static long serialVersionUID = 5137990582851939037L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public DDBEventArtefact() {
		this.insertedDate = DateUtils.getCurrentDateShortStr();
	}

	/**
	 * @param s3Key
	 * @param gsi1sk
	 * @param insertedDate
	 * @param gsi1pk
	 * @param artefactId
	 * @param sk
	 * @param mirisDocId
	 * @param pk
	 * @param type
	 * @param userId
	 * @param s3Bucket
	 * @param status
	 */
	public DDBEventArtefact(String gsi1pk, String gsi1sk, String pk, String sk, String artefactId, String insertedDate,
			String mirisDocId, String s3Bucket, String s3Key, String status, String type, String userId) {
		super();
		this.gsi1pk = gsi1pk;
		this.gsi1sk = gsi1sk;
		this.pk = pk;
		this.sk = sk;
		this.artefactId = artefactId;
		this.insertedDate = insertedDate;
		this.mirisDocId = mirisDocId;
		this.s3Bucket = s3Bucket;
		this.s3Key = s3Key;
		this.status = status;
		this.type = type;
		this.userId = userId;
	}

	@JsonProperty("GSI1PK")
	public String getGsi1pk() {
		return gsi1pk;
	}

	@JsonProperty("GSI1PK")
	public void setGsi1pk(String gsi1pk) {
		this.gsi1pk = gsi1pk;
	}

	@JsonProperty("GSI1SK")
	public String getGsi1sk() {
		return gsi1sk;
	}

	@JsonProperty("GSI1SK")
	public void setGsi1sk(String gsi1sk) {
		this.gsi1sk = gsi1sk;
	}

	@JsonProperty("PK")
	public String getPk() {
		return pk;
	}

	@JsonProperty("PK")
	public void setPk(String pk) {
		this.pk = pk;
	}

	@JsonProperty("SK")
	public String getSk() {
		return sk;
	}

	@JsonProperty("SK")
	public void setSk(String sk) {
		this.sk = sk;
	}

	@JsonProperty("artefactId")
	public String getArtefactId() {
		return artefactId;
	}

	@JsonProperty("artefactId")
	public void setArtefactId(String artefactId) {
		this.artefactId = artefactId;
	}

	@JsonProperty("insertedDate")
	public String getInsertedDate() {
		return insertedDate;
	}

	@JsonProperty("insertedDate")
	public void setInsertedDate(String insertedDate) {
		this.insertedDate = insertedDate;
	}

	@JsonProperty("mirisDocId")
	public String getMirisDocId() {
		return mirisDocId;
	}

	@JsonProperty("mirisDocId")
	public void setMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
	}

	@JsonProperty("s3Bucket")
	public String getS3Bucket() {
		return s3Bucket;
	}

	@JsonProperty("s3Bucket")
	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	@JsonProperty("s3Key")
	public String getS3Key() {
		return s3Key;
	}

	@JsonProperty("s3Key")
	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("userId")
	public String getUserId() {
		return userId;
	}

	@JsonProperty("userId")
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(DDBEventArtefact.class.getName())
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(this)))
			.append('[');
		sb.append("gsi1pk");
		sb.append('=');
		sb.append(((this.gsi1pk == null) ? "<null>" : this.gsi1pk));
		sb.append(',');
		sb.append("gsi1sk");
		sb.append('=');
		sb.append(((this.gsi1sk == null) ? "<null>" : this.gsi1sk));
		sb.append(',');
		sb.append("pk");
		sb.append('=');
		sb.append(((this.pk == null) ? "<null>" : this.pk));
		sb.append(',');
		sb.append("sk");
		sb.append('=');
		sb.append(((this.sk == null) ? "<null>" : this.sk));
		sb.append(',');
		sb.append("artefactId");
		sb.append('=');
		sb.append(((this.artefactId == null) ? "<null>" : this.artefactId));
		sb.append(',');
		sb.append("insertedDate");
		sb.append('=');
		sb.append(((this.insertedDate == null) ? "<null>" : this.insertedDate));
		sb.append(',');
		sb.append("mirisDocId");
		sb.append('=');
		sb.append(((this.mirisDocId == null) ? "<null>" : this.mirisDocId));
		sb.append(',');
		sb.append("s3Bucket");
		sb.append('=');
		sb.append(((this.s3Bucket == null) ? "<null>" : this.s3Bucket));
		sb.append(',');
		sb.append("s3Key");
		sb.append('=');
		sb.append(((this.s3Key == null) ? "<null>" : this.s3Key));
		sb.append(',');
		sb.append("status");
		sb.append('=');
		sb.append(((this.status == null) ? "<null>" : this.status));
		sb.append(',');
		sb.append("type");
		sb.append('=');
		sb.append(((this.type == null) ? "<null>" : this.type));
		sb.append(',');
		sb.append("userId");
		sb.append('=');
		sb.append(((this.userId == null) ? "<null>" : this.userId));
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