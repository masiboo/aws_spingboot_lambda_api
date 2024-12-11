package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Setter
@Getter
public class ArtefactsEntityDTO {

	// Getters and setters
	private Long id;

	private String mirisDocId;

	private String artefactClass;

	private ZonedDateTime indexationDate;

	private String artefactName;

	private String status;

	private String s3Bucket;

	private ZonedDateTime archiveDate;

	private ZonedDateTime lastModificationDate;

	private String lastModificationUser;

	private String dmapsVersion;

	private String importedImapsError;

	private String importedImapsDocId;

	private Integer activeArtefactItem;

	private String activeJobId;

	private String lastError;

	private ZonedDateTime errorDate;

	private BatchesEntityDTO batch;

	private String artefactUUID;

	private List<ArtefactItemsEntityDTO> artefactItems;

	private List<ArtefactTagsEntityDTO> artefactTags;

	private ArtefactNotesEntityDTO artefactNote;

	private ArtefactsEntityDTO() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ArtefactsEntityDTO artefactsEntity;

		private Builder() {
			artefactsEntity = new ArtefactsEntityDTO();
		}

		public Builder id(Long id) {
			artefactsEntity.id = id;
			return this;
		}

		public Builder mirisDocId(String mirisDocId) {
			artefactsEntity.mirisDocId = mirisDocId;
			return this;
		}

		public Builder artefactClass(String artefactClass) {
			artefactsEntity.artefactClass = artefactClass;
			return this;
		}

		public Builder indexationDate(ZonedDateTime indexationDate) {
			artefactsEntity.indexationDate = indexationDate;
			return this;
		}

		public Builder artefactName(String artefactName) {
			artefactsEntity.artefactName = artefactName;
			return this;
		}

		public Builder status(String status) {
			artefactsEntity.status = status;
			return this;
		}

		public Builder s3Bucket(String s3Bucket) {
			artefactsEntity.s3Bucket = s3Bucket;
			return this;
		}

		public Builder archiveDate(ZonedDateTime archiveDate) {
			artefactsEntity.archiveDate = archiveDate;
			return this;
		}

		public Builder lastModificationDate(ZonedDateTime lastModificationDate) {
			artefactsEntity.lastModificationDate = lastModificationDate;
			return this;
		}

		public Builder lastModificationUser(String lastModificationUser) {
			artefactsEntity.lastModificationUser = lastModificationUser;
			return this;
		}

		public Builder dmapsVersion(String dmapsVersion) {
			artefactsEntity.dmapsVersion = dmapsVersion;
			return this;
		}

		public Builder importedImapsError(String importedImapsError) {
			artefactsEntity.importedImapsError = importedImapsError;
			return this;
		}

		public Builder importedImapsDocId(String importedImapsDocId) {
			artefactsEntity.importedImapsDocId = importedImapsDocId;
			return this;
		}

		public Builder activeArtefactItem(Integer activeArtefactItem) {
			artefactsEntity.activeArtefactItem = activeArtefactItem;
			return this;
		}

		public Builder activeJobId(String activeJobId) {
			artefactsEntity.activeJobId = activeJobId;
			return this;
		}

		public Builder lastError(String lastError) {
			artefactsEntity.lastError = lastError;
			return this;
		}

		public Builder artefactUUID(String artefactUUID) {
			artefactsEntity.artefactUUID = artefactUUID;
			return this;
		}

		public Builder errorDate(ZonedDateTime errorDate) {
			artefactsEntity.errorDate = errorDate;
			return this;
		}

		public Builder batch(BatchesEntityDTO batch) {
			artefactsEntity.batch = batch;
			return this;
		}

		public Builder artefactItems(List<ArtefactItemsEntityDTO> artefactItems) {
			artefactsEntity.artefactItems = artefactItems;
			return this;
		}

		public Builder artefactTags(List<ArtefactTagsEntityDTO> artefactTags) {
			artefactsEntity.artefactTags = artefactTags;
			return this;
		}

		public Builder artefactNote(ArtefactNotesEntityDTO artefactNote) {
			artefactsEntity.artefactNote = artefactNote;
			return this;
		}

		public ArtefactsEntityDTO build() {
			return artefactsEntity;
		}

	}

}
