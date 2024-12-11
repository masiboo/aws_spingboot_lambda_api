
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(value = { "id", "batch_sequence", "lockedDate", "creationDate", "lastModificationDate", "status",
		"operator", "lockedBy", "lastModUser", "artefacts", "reportDate", "reportUrl", "s3Bucket", "s3Key", "jobs" })
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@ToString
public class BatchInputDynamoDb implements Serializable {

	private final static long serialVersionUID = -5141988849834997044L;

	@JsonProperty("id")
	private long id;

	@JsonProperty("batch_sequence")
	private String batchSequence;

	@JsonProperty("lockedDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private ZonedDateTime lockedDate;

	@JsonProperty("creationDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private ZonedDateTime creationDate;

	@JsonProperty("lastModificationDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private ZonedDateTime lastModificationDate;

	@JsonProperty("status")
	private String status;

	@JsonProperty("operator")
	private Operator operator;

	@JsonProperty("lockedBy")
	private LockedBy lockedBy;

	@JsonProperty("lastModUser")
	private LastModUser lastModUser;

	@JsonProperty("reportDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private ZonedDateTime reportDate;

	@JsonProperty("reportUrl")
	@DynamoDBIgnore
	private String reportUrl;

	@JsonProperty("s3Bucket")
	private String s3Bucket;

	@JsonProperty("s3Key")
	private String s3Key;

	private String requestType;

	@Builder.Default
	@JsonProperty("artefacts")
	private List<ArtefactDynamoDb> artefacts = new ArrayList<>();

	@Builder.Default
	@JsonIgnore
	private Map<String, Object> additionalProperties = new LinkedHashMap<>();

	private String requestId;

	@Builder.Default
	@JsonProperty("jobs")
	private List<ArtefactJob> jobs = new ArrayList<>();

	@JsonProperty("unmergedArtefacts")
	private List<String> unmergedArtefacts;

	@JsonProperty("scannedType")
	private ScannedAppType scannedType;

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	/**
	 * No args constructor for use in serialization
	 */
	public BatchInputDynamoDb() {
		// Set the current date and time for creationDate and lastModificationDate if null
		if (this.creationDate == null) {
			this.creationDate = DateUtils.getCurrentDatetimeUtc();
		}
		if (this.lastModificationDate == null) {
			this.lastModificationDate = DateUtils.getCurrentDatetimeUtc();
		}
	}

	/**
	 * @param lastModificationDate
	 * @param lockedBy
	 * @param lockedDate
	 * @param id
	 * @param batchSequence
	 * @param creationDate
	 * @param operator
	 * @param artefacts
	 * @param status
	 * @param lastModUser
	 * @param reportDate
	 * @param s3Key
	 * @param s3Bucket
	 */
	public BatchInputDynamoDb(long id, String batchSequence, ZonedDateTime lockedDate, ZonedDateTime creationDate,
			ZonedDateTime lastModificationDate, String status, Operator operator, LockedBy lockedBy,
			LastModUser lastModUser, List<ArtefactDynamoDb> artefacts, ZonedDateTime reportDate, String reportUrl,
			String s3Key, String s3Bucket) {
		super();
		this.id = id;
		this.batchSequence = batchSequence;
		this.lockedDate = lockedDate;
		this.creationDate = creationDate;
		this.lastModificationDate = lastModificationDate;
		this.status = status;
		this.operator = operator;
		this.lockedBy = lockedBy;
		this.lastModUser = lastModUser;
		this.artefacts = artefacts;
		this.reportDate = reportDate;
		this.reportUrl = reportUrl;
		this.s3Key = s3Key;
		this.s3Bucket = s3Bucket;
	}

	@JsonProperty("id")
	public long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(long id) {
		this.id = id;
	}

	public BatchInputDynamoDb withId(long id) {
		this.id = id;
		return this;
	}

	@JsonProperty("batch_sequence")
	public String getBatchSequence() {
		return batchSequence;
	}

	@JsonProperty("batch_sequence")
	public void setBatchSequence(String batchSequence) {
		this.batchSequence = batchSequence;
	}

	public BatchInputDynamoDb withBatchSequence(String batchSequence) {
		this.batchSequence = batchSequence;
		return this;
	}

	@JsonProperty("lockedDate")
	public ZonedDateTime getLockedDate() {
		return lockedDate;
	}

	@JsonProperty("lockedDate")
	public void setLockedDate(ZonedDateTime lockedDate) {
		this.lockedDate = lockedDate;
	}

	public BatchInputDynamoDb withLockedDate(ZonedDateTime lockedDate) {
		this.lockedDate = lockedDate;
		return this;
	}

	@JsonProperty("creationDate")
	public ZonedDateTime getCreationDate() {
		return creationDate;
	}

	@JsonProperty("creationDate")
	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public BatchInputDynamoDb withCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
		return this;
	}

	@JsonProperty("lastModificationDate")
	public ZonedDateTime getLastModificationDate() {
		return lastModificationDate;
	}

	@JsonProperty("lastModificationDate")
	public void setLastModificationDate(ZonedDateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public BatchInputDynamoDb withLastModificationDate(ZonedDateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
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

	public BatchInputDynamoDb withStatus(String status) {
		this.status = status;
		return this;
	}

	@JsonProperty("operator")
	public Operator getOperator() {
		return operator;
	}

	@JsonProperty("operator")
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public BatchInputDynamoDb withOperator(Operator operator) {
		this.operator = operator;
		return this;
	}

	@JsonProperty("lockedBy")
	public LockedBy getLockedBy() {
		return lockedBy;
	}

	@JsonProperty("lockedBy")
	public void setLockedBy(LockedBy lockedBy) {
		this.lockedBy = lockedBy;
	}

	public BatchInputDynamoDb withLockedBy(LockedBy lockedBy) {
		this.lockedBy = lockedBy;
		return this;
	}

	@JsonProperty("lastModUser")
	public LastModUser getLastModUser() {
		return lastModUser;
	}

	@JsonProperty("lastModUser")
	public void setLastModUser(LastModUser lastModUser) {
		this.lastModUser = lastModUser;
	}

	public BatchInputDynamoDb withLastModUser(LastModUser lastModUser) {
		this.lastModUser = lastModUser;
		return this;
	}

	@JsonProperty("artefacts")
	public List<ArtefactDynamoDb> getArtefacts() {
		return artefacts;
	}

	@JsonProperty("artefacts")
	public void setArtefacts(List<ArtefactDynamoDb> artefacts) {
		this.artefacts = artefacts;
	}

	public BatchInputDynamoDb withArtefacts(List<ArtefactDynamoDb> artefacts) {
		this.artefacts = artefacts;
		return this;
	}

	@JsonProperty("reportDate")
	public ZonedDateTime getReportDate() {
		return reportDate;
	}

	@JsonProperty("reportDate")
	public void setReportDate(ZonedDateTime reportDate) {
		this.reportDate = reportDate;
	}

	@JsonProperty("reportUrl")
	@DynamoDBIgnore
	public String getReportUrl() {
		return reportUrl;
	}

	@JsonProperty("reportUrl")
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
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

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public BatchInputDynamoDb withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@JsonProperty("unmergedArtefacts")
	public List<String> getUnmergedArtefacts() {
		return unmergedArtefacts;
	}

	@JsonProperty("unmergedArtefacts")
	public void setUnmergedArtefacts(List<String> unmergedArtefacts) {
		this.unmergedArtefacts = unmergedArtefacts;
	}

	@JsonProperty("jobs")
	public List<ArtefactJob> getJobs() {
		return jobs;
	}

	public List<String> getJobsIds() {
		return jobs.stream().map(job -> {
			return job.getId();
		}).toList();
	}

	@JsonProperty("jobs")
	public void setJobs(List<ArtefactJob> jobs) {
		this.jobs = jobs;
	}

	@JsonProperty("scannedType")
	public ScannedAppType getScannedType() {
		return scannedType;
	}

	@JsonProperty("scannedType")
	public void setScannedType(ScannedAppType scannedType) {
		this.scannedType = scannedType;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public void addArtefact(ArtefactDynamoDb artefact) {
		if (this.artefacts == null) {
			this.artefacts = new ArrayList<>();
		}
		this.artefacts.add(artefact);
	}

	public void addJob(ArtefactJob artefactJob) {
		if (this.jobs == null) {
			this.jobs = new ArrayList<>();
		}
		this.jobs.add(artefactJob);
	}

}
