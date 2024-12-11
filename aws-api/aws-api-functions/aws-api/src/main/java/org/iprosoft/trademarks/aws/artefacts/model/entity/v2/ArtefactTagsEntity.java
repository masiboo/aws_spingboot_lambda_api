package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
public class ArtefactTagsEntity {

	private Long id;

	@NotBlank
	@Size(max = 50)
	private String key;

	@NotBlank
	@Size(max = 50)
	private String value;

	@NotNull
	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime insertedDate;

	@ToString.Exclude
	private ArtefactsEntity artefact;

	public ArtefactTagsEntity() {
		this.insertedDate = DateUtils.getCurrentDatetimeUtc();
	}

}
