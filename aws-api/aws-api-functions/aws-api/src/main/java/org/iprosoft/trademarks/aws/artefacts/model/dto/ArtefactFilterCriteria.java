package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtefactFilterCriteria {

	private String mirisDocId;

	private String docType;

	private String insertedDate;

	private String dateFrom;

	private String batchStatus;

	private String reportDate;

	private String dateTo;

}
