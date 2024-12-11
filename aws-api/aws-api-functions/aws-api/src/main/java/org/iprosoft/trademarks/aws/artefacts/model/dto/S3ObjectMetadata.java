package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class S3ObjectMetadata {

	private String etag;

	private String contentType;

	private Map<String, String> metadata;

	private boolean objectExists;

	private Long contentLength;

}
