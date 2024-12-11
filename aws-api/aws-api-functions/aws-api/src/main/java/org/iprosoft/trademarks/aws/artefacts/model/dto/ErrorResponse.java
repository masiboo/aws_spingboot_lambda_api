package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ErrorResponse {

	private String code;

	private String message;

}
