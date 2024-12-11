package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ArtefactInputRequestValidationDTO {

	private String artefactId;

	private Map<String, String> validation;

}
