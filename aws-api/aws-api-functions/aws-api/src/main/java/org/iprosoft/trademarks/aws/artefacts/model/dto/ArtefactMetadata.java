package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ArtefactMetadata {

	String artefactId;

	String mediaType;

	String fileType;

	String size;

	String bitDepth;

	String samplingFrequency;

	String resolutionInDpi;

	boolean sizeWarning;

	String classType;

}
