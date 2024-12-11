package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
public class ArtefactNotesEntity {

	private Long id;

	@NotBlank
	@Size(max = 255)
	private String content;

	@NotNull
	private ZonedDateTime createdDate;

	@Size(max = 50)
	private String author;

	@NotNull
	private ZonedDateTime modifiedDate;

	private String mirisDocId;

	public ArtefactNotesEntity() {
		this.createdDate = DateUtils.getCurrentDatetimeUtc();
		this.modifiedDate = DateUtils.getCurrentDatetimeUtc();
	}

}
