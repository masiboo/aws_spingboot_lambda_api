package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artefact {

	private String id;

	private String artefactName;

	@NotNull(message = "ClassType cannot be null")
	@JsonProperty("artefactClassType")
	private String artefactClassType;

	private String status;

	private String error;

	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime indexationDate;

	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime archiveDate;

	private String s3Bucket;

	private String s3Key;

	private List<ArtefactItemTags> artefactItemTags;

	@JsonProperty("mirisDocId")
	@Pattern(regexp = "^[0-9]{5,8}$", message = "mirisDocId must be between 5 to 8 digits and no space allowed")
	private String mirisDocId;

	@JsonProperty("items")
	private List<Items> items = null;

	private Boolean sizeWarning;

	private String contentLength;

	private String artefactMergeId;

	public Artefact() {
		this.indexationDate = DateUtils.getCurrentDatetimeUtc();
		this.archiveDate = DateUtils.getCurrentDatetimeUtc();
		this.artefactName = (this.artefactName != null) ? this.artefactName : this.artefactMergeId;
	}

}
