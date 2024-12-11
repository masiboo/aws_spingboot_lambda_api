package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class BatchesEntity {

	private Long id;

	@NotNull
	private Long sequenceNumber;

	@Size(max = 50)
	private String operator;

	@Size(max = 50)
	@NotBlank
	private String stack;

	@NotBlank
	private String lockedBy;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime lockedDate;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime creationDate;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime lastModDate;

	@ToString.Exclude
	private List<ArtefactsEntity> artefacts;

	private BatchesEntity() {
		this.creationDate = DateUtils.getCurrentDatetimeUtc();
		this.lastModDate = DateUtils.getCurrentDatetimeUtc();
	}

}
