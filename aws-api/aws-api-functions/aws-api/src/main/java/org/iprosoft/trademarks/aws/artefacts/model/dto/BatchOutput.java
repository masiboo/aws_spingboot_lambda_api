
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.iprosoft.trademarks.aws.artefacts.model.entity.LastModUser;
import org.iprosoft.trademarks.aws.artefacts.model.entity.LockedBy;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Operator;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "batch_sequence", "lockedDate", "creationDate", "lastModificationDate", "requestType",
		"user", "status", "operator", "lockedBy", "lastModUser", "artefacts", "reportDate", "reportUrl", "s3Bucket",
		"s3Key" })
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@SuperBuilder
public class BatchOutput {

	@JsonProperty("id")
	private String id;

	@JsonProperty("batch_sequence")
	private String batchSequence;

	@JsonProperty("lockedDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String lockedDate;

	@JsonProperty("creationDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String creationDate;

	@JsonProperty("lastModificationDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String lastModificationDate;

	@JsonProperty("status")
	private String status;

	@JsonProperty("operator")
	private Operator operator;

	@JsonProperty("lockedBy")
	private LockedBy lockedBy;

	@JsonProperty("lastModUser")
	private LastModUser lastModUser;

	@JsonProperty("requestType")
	private String requestType;

	@JsonProperty("user")
	private String user;

	@JsonProperty("artefacts")
	private List<ArtefactOutput> artefacts = new ArrayList<>();

	@JsonProperty("reportDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String reportDate;

	@JsonProperty("reportUrl")
	@DynamoDBIgnore
	private String reportUrl;

	@JsonProperty("s3Bucket")
	private String s3Bucket;

	@JsonProperty("s3Key")
	private String s3Key;

	@JsonProperty("requestId")
	private String requestId;

	@JsonIgnore
	private ArrayList<String> jobIds;

	@Builder.Default
	@JsonIgnore
	private Map<String, Object> additionalProperties = new LinkedHashMap<>();

	private boolean locked;

	public BatchOutput withId(String id) {
		this.id = id;
		return this;
	}

	public BatchOutput withBatchSequence(String batchSequence) {
		this.batchSequence = batchSequence;
		return this;
	}

	public BatchOutput() {
		// Set the current date and time for creationDate and lastModificationDate if null
		if (this.creationDate == null || this.creationDate.isEmpty()) {
			this.creationDate = DateUtils.getCurrentDatetimeUtcStr();
		}
		if (this.lastModificationDate == null || this.lastModificationDate.isEmpty()) {
			this.lastModificationDate = DateUtils.getCurrentDatetimeUtcStr();
		}
	}

	@JsonProperty("reportUrl")
	@DynamoDBIgnore
	public String getReportUrl() {
		return reportUrl;
	}

	@JsonSetter("reportUrl")
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

}
