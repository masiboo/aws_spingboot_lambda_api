package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class ArtefactsEntity {

	private Long id;

	private ArtefactStatusEnum status;

	private ArtefactClassEnum artefactClass;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime indexationDate;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime archiveDate;

	@NotBlank
	private String s3Bucket;

	@NotBlank
	private String mirisDocId;

	@ToString.Exclude
	private List<ArtefactItemsEntity> artefactItems;

	@ToString.Exclude
	private List<ArtefactTagsEntity> artefactTags;

	@ToString.Exclude
	private BatchesEntity batch;

	private String artefactName;

	private String artefactUUID;

	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime lastModDate;

	private String lastModUser;

	private String imapsGenId;

	private String imapsDocName;

	private BigInteger activeArtefactItem;

	private String activeJobId;

	private String lastError;

	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime errorDate;

	@ToString.Exclude
	private ArtefactNotesEntity artefactNote;

	public ArtefactsEntity() {
		this.indexationDate = DateUtils.getCurrentDatetimeUtc();
		this.lastModDate = DateUtils.getCurrentDatetimeUtc();
	}

}