package org.iprosoft.trademarks.aws.artefacts.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConvertImageRequest {

	@NotBlank
	private String bucket;

	@NotBlank
	private String key;

}
