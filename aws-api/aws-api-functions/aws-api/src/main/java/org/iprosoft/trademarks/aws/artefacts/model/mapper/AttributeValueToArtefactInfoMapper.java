package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactMetadata;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AttributeValueToArtefactInfoMapper implements Function<Map<String, AttributeValue>, ArtefactMetadata> {

	@Override
	public ArtefactMetadata apply(Map<String, AttributeValue> attributeValueMap) {
		if (attributeValueMap == null) {
			throw new IllegalArgumentException("attributeValueMap cannot be null");
		}
		log.info("AttributeValueToArtefactInfoMapper input attributeValueMap: {}", attributeValueMap);
		ArtefactMetadata info = new ArtefactMetadata();

		if (attributeValueMap.containsKey("artefactId")) {
			AttributeValue artefactIdValue = attributeValueMap.get("artefactId");
			if (artefactIdValue != null && artefactIdValue.s() != null) {
				info.setArtefactId(artefactIdValue.s());
			}
		}

		if (attributeValueMap.containsKey("fileType")) {
			AttributeValue fileTypeValue = attributeValueMap.get("fileType");
			if (fileTypeValue != null && fileTypeValue.s() != null) {
				info.setFileType(fileTypeValue.s());
			}
		}

		if (attributeValueMap.containsKey("bitDepth")) {
			AttributeValue bitDepthValue = attributeValueMap.get("bitDepth");
			if (bitDepthValue != null && bitDepthValue.s() != null) {
				info.setBitDepth(bitDepthValue.s());
			}
		}

		if (attributeValueMap.containsKey("contentLength")) {
			AttributeValue contentLengthValue = attributeValueMap.get("contentLength");
			if (contentLengthValue != null && contentLengthValue.n() != null) {
				info.setSize(contentLengthValue.n());
			}
		}

		if (attributeValueMap.containsKey("mediaType")) {
			AttributeValue mediaTypeValue = attributeValueMap.get("mediaType");
			if (mediaTypeValue != null && mediaTypeValue.s() != null) {
				info.setMediaType(mediaTypeValue.s());
			}
		}

		if (attributeValueMap.containsKey("samplingFrequency")) {
			AttributeValue samplingFrequencyValue = attributeValueMap.get("samplingFrequency");
			if (samplingFrequencyValue != null && samplingFrequencyValue.s() != null) {
				info.setSamplingFrequency(samplingFrequencyValue.s());
			}
		}

		if (attributeValueMap.containsKey("resolutionInDpi")) {
			AttributeValue resolutionInDpiValue = attributeValueMap.get("resolutionInDpi");
			if (resolutionInDpiValue != null && resolutionInDpiValue.s() != null) {
				info.setResolutionInDpi(resolutionInDpiValue.s());
			}
		}

		if (attributeValueMap.containsKey("sizeWarning")) {
			AttributeValue sizeWarningValue = attributeValueMap.get("sizeWarning");
			if (sizeWarningValue != null && sizeWarningValue.bool() != null) {
				info.setSizeWarning(sizeWarningValue.bool());
			}
		}

		if (attributeValueMap.containsKey("artefactClassType")) {
			AttributeValue artefactClassTypeValue = attributeValueMap.get("artefactClassType");
			if (artefactClassTypeValue != null && artefactClassTypeValue.s() != null) {
				info.setClassType(artefactClassTypeValue.s());
			}
		}

		log.info("ArtefactMetadata info: {}", info);
		return info;
	}

}
