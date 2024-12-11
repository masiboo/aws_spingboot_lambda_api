package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ConvertImageResponse {

	Map<String, String> metaData;

	String signedS3Url;

	String errorMessage;

	String httpStatus;

}
