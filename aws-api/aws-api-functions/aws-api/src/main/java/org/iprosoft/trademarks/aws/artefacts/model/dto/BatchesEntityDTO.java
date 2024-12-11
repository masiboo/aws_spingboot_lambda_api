package org.iprosoft.trademarks.aws.artefacts.model.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class BatchesEntityDTO {

	private Long id;

	private String batchSequence;

	private String operator;

	private String status;

	private String lockedBy;

	private ZonedDateTime lockedDate;

	private ZonedDateTime creationDate;

	private ZonedDateTime lastModificationDate;

	private String lastModificationUser;

	private String requestType;

	private String requestId;

	private String scanType;

	private String type;

	private List<ArtefactsEntityDTO> artefacts;

	private Long sequenceNumber;

	private String stack;

	private ZonedDateTime lastModDate;

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final BatchesEntityDTO batchesEntity;

		private Builder() {
			batchesEntity = new BatchesEntityDTO();
		}

		public Builder id(Long id) {
			batchesEntity.id = id;
			return this;
		}

		public Builder sequenceNumber(Long sequenceNumber) {
			batchesEntity.sequenceNumber = sequenceNumber;
			return this;
		}

		public Builder operator(String operator) {
			batchesEntity.operator = operator;
			return this;
		}

		public Builder stack(String stack) {
			batchesEntity.stack = stack;
			return this;
		}

		public Builder lockedBy(String lockedBy) {
			batchesEntity.lockedBy = lockedBy;
			return this;
		}

		public Builder lockedDate(ZonedDateTime lockedDate) {
			batchesEntity.lockedDate = lockedDate;
			return this;
		}

		public Builder creationDate(ZonedDateTime creationDate) {
			batchesEntity.creationDate = creationDate;
			return this;
		}

		public Builder lastModDate(ZonedDateTime lastModDate) {
			batchesEntity.lastModDate = lastModDate;
			return this;
		}

		public Builder artefacts(List<ArtefactsEntityDTO> artefacts) {
			batchesEntity.artefacts = artefacts;
			return this;
		}

		public BatchesEntityDTO build() {
			return batchesEntity;
		}

	}

}