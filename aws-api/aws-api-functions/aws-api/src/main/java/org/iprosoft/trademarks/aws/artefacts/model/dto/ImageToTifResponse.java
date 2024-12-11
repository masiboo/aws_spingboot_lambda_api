package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "signedS3Url", "artefactMetadata", "fileName" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class ImageToTifResponse {

	String signedS3Url;

	Map<String, String> metaData;

	String errorMessage;

	String statusCode;

}
