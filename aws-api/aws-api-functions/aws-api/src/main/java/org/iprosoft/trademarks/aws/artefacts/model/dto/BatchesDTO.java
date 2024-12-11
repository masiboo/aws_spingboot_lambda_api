package org.iprosoft.trademarks.aws.artefacts.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BatchesDTO implements Serializable {

	private Long id;

	@NotNull
	@Size(max = 50)
	@NotBlank
	private String batchSequence;

	private String operator;

	private BatchStatusEnum status;

	private String lockedBy;

	private ZonedDateTime lockedDate;

	@NotNull
	private ZonedDateTime creationDate;

	@NotNull
	private ZonedDateTime lastModificationDate;

	@NotNull
	@Size(max = 50)
	private String lastModificationUser;

	@Size(max = 50)
	private String requestType;

	@Size(max = 255)
	private String requestId;

	@Size(max = 255)
	private String scanType;

	@Size(max = 255)
	private String type;

	private List<ArtefactsDTO> artefacts;

}