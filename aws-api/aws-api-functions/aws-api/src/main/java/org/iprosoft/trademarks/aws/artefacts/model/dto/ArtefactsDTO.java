package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactClassEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
@Slf4j
public class ArtefactsDTO {

	private Long id;

	private String mirisDocId;

	private ArtefactClassEnum artefactClass;

	private ZonedDateTime indexationDate;

	private String artefactName;

	private ArtefactStatusEnum status;

	private String s3Bucket;

	private ZonedDateTime archiveDate;

	private ZonedDateTime lastModificationDate;

	private String lastModificationUser;

	private String dmapsVersion;

	private String importedImapsError;

	private String importedImapsDocId;

	private BigInteger activeArtefactItem;

	private String activeJobId;

	private String lastError;

	private ZonedDateTime errorDate;

	private Long batchId;

	private List<ArtefactItemsDTO> artefactItems;

	private String artefactUUID;

	private List<ArtefactTagsDTO> artefactTags;

	private ArtefactNotesDTO artefactNote;

}
