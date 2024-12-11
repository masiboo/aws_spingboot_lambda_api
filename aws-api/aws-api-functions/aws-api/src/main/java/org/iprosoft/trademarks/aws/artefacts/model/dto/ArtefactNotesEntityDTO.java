package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.time.LocalDateTime;

public class ArtefactNotesEntityDTO {

	private Long id;

	private String content;

	private LocalDateTime createdDate;

	private String author;

	private LocalDateTime modifiedDate;

	private ArtefactsEntityDTO artefact;

}