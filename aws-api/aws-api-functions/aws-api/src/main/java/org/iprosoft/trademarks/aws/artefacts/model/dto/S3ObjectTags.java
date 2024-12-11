package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class S3ObjectTags {

	private String bitDepth;

	private String samplingFrequency;

	private String format;

	private String codec;

	private String frameRate;

	private String resolutionInDpi;

	private String totalDuration;

}