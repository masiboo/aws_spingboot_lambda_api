package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactNotesDTO {

	private Long id;

	@NotBlank
	private String content;

	@NotNull
	private ZonedDateTime createdDate;

	private String author;

	@NotNull
	private ZonedDateTime modifiedDate;

	private Long artefactId;

}