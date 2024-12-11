package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
public class ArtefactItemsEntity {

	private Long id;

	private ZonedDateTime indexationDate;

	@ToString.Exclude
	private ArtefactsEntity artefact;

	private ArtefactItemTypeEnum artefactItemType;

	private FragmentTypeEnum fragmentType;

	@NotNull
	private String contentType;

	@NotNull
	private String s3Key;

	private Long totalPages;

	private Long contentLength;

	@NotNull
	private String fileName;

	private ZonedDateTime createdDate;

	private ZonedDateTime modifiedDate;

	public ArtefactItemsEntity() {
		createdDate = DateUtils.getCurrentDatetimeUtc();
		modifiedDate = DateUtils.getCurrentDatetimeUtc();
	}

}
