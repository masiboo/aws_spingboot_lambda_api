package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

@Getter
@Setter
@ToString
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactBatch extends Artefact {

	private String type;

	private String filename;

	private String path;

	private String contentType;

	private String batchSequence;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
	private String creationDate;

	private String user;

	private String jobId;

	private String artefactId;

	private String s3Url;

	private String requestType;

	private String validationStatus;

	private String page;

	private String artefactItemFileName;

	@JsonProperty("mirisDocId")
	@Pattern(regexp = "^[0-9]{5,8}$", message = "mirisDocId must be between 5 to 8 digits and no space allowed")
	private String mirisDocId;

	@JsonProperty("artefactName")
	private String artefactMergeId;

	public ArtefactBatch() {
		this.creationDate = DateUtils.getCurrentDatetimeUtcStr();
	}

}
