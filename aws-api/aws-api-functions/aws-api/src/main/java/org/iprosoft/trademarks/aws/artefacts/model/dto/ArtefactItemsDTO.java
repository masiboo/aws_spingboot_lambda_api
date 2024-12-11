package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactItemTypeEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.FragmentTypeEnum;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Slf4j
public class ArtefactItemsDTO {

	private Long id;

	private String s3Key;

	private String fileName;

	private Long totalPages;

	private String contentType;

	private Long contentLength;

	private ArtefactItemTypeEnum artefactItemType;

	private FragmentTypeEnum fragmentType;

	private String mergedArtefactId;

	private String scanType;

	private ZonedDateTime createdDate;

	private ZonedDateTime lastModificationDate;

	private Long artefactId;

}
