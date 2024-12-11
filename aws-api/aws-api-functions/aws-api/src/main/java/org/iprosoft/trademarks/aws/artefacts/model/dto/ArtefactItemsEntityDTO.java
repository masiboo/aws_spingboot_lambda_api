package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class ArtefactItemsEntityDTO {

	private Long id;

	private String s3Key;

	private String fileName;

	private Integer totalPages;

	private String contentType;

	private Long contentLength;

	private String artefactItemType;

	private String fragmentType;

	private String mergedArtefactId;

	private String scanType;

	private ZonedDateTime createdDate;

	private ZonedDateTime lastModificationDate;

	private ArtefactsEntityDTO artefact;

	private ArtefactItemsEntityDTO() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ArtefactItemsEntityDTO artefactItemsEntity;

		public Builder() {
			artefactItemsEntity = new ArtefactItemsEntityDTO();
		}

		public Builder id(Long id) {
			artefactItemsEntity.id = id;
			return this;
		}

		public Builder s3Key(String s3Key) {
			artefactItemsEntity.s3Key = s3Key;
			return this;
		}

		public Builder fileName(String fileName) {
			artefactItemsEntity.fileName = fileName;
			return this;
		}

		public Builder totalPages(Integer totalPages) {
			artefactItemsEntity.totalPages = totalPages;
			return this;
		}

		public Builder contentType(String contentType) {
			artefactItemsEntity.contentType = contentType;
			return this;
		}

		public Builder contentLength(Long contentLength) {
			artefactItemsEntity.contentLength = contentLength;
			return this;
		}

		public Builder artefactItemType(String artefactItemType) {
			artefactItemsEntity.artefactItemType = artefactItemType;
			return this;
		}

		public Builder fragmentType(String fragmentType) {
			artefactItemsEntity.fragmentType = fragmentType;
			return this;
		}

		public Builder mergedArtefactId(String mergedArtefactId) {
			artefactItemsEntity.mergedArtefactId = mergedArtefactId;
			return this;
		}

		public Builder scanType(String scanType) {
			artefactItemsEntity.scanType = scanType;
			return this;
		}

		public Builder createdDate(ZonedDateTime createdDate) {
			artefactItemsEntity.createdDate = createdDate;
			return this;
		}

		public Builder lastModificationDate(ZonedDateTime lastModificationDate) {
			artefactItemsEntity.lastModificationDate = lastModificationDate;
			return this;
		}

		public Builder artefact(ArtefactsEntityDTO artefact) {
			artefactItemsEntity.artefact = artefact;
			return this;
		}

		public ArtefactItemsEntityDTO build() {
			return artefactItemsEntity;
		}

	}

	// Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	public String getArtefactItemType() {
		return artefactItemType;
	}

	public void setArtefactItemType(String artefactItemType) {
		this.artefactItemType = artefactItemType;
	}

	public String getFragmentType() {
		return fragmentType;
	}

	public void setFragmentType(String fragmentType) {
		this.fragmentType = fragmentType;
	}

	public String getMergedArtefactId() {
		return mergedArtefactId;
	}

	public void setMergedArtefactId(String mergedArtefactId) {
		this.mergedArtefactId = mergedArtefactId;
	}

	public String getScanType() {
		return scanType;
	}

	public void setScanType(String scanType) {
		this.scanType = scanType;
	}

	public ZonedDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(ZonedDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public ZonedDateTime getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(ZonedDateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public ArtefactsEntityDTO getArtefact() {
		return artefact;
	}

	public void setArtefact(ArtefactsEntityDTO artefact) {
		this.artefact = artefact;
	}

}
