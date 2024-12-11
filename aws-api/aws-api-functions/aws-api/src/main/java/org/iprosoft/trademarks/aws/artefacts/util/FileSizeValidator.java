package org.iprosoft.trademarks.aws.artefacts.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileSizeValidator {

	private static final int BYTE_CONVERSION_FACTOR = (1024 * 1024);

	public String validate(Long contentLength, String classType) {
		String warningMessage = null;
		Double sizeLimitByType = getSizeLimitByType(classType);
		double contentLengthInMB = (double) contentLength / BYTE_CONVERSION_FACTOR;

		// sizeLimit will be 0 except SOUND and MULTIMEDIA
		if (sizeLimitByType != 0 && contentLengthInMB > sizeLimitByType) {
			warningMessage = constructWarningMsg(sizeLimitByType.longValue());
		}
		return warningMessage;
	}

	private double getSizeLimitByType(String classType) {
		double sizeLimitInMB = 0d;
		if (ArtefactInput.classType.MULTIMEDIA.toString().equalsIgnoreCase(classType)) {
			sizeLimitInMB = StringUtils.hasText(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT)
					? Long.parseLong(SystemEnvironmentVariables.MULTIMEDIA_FILE_SIZE_LIMIT) : 20d;
		}
		else if (ArtefactInput.classType.SOUND.toString().equalsIgnoreCase(classType)) {
			sizeLimitInMB = StringUtils.hasText(SystemEnvironmentVariables.SOUND_FILE_SIZE_LIMIT)
					? Long.parseLong(SystemEnvironmentVariables.SOUND_FILE_SIZE_LIMIT) : 5d;
		}
		return sizeLimitInMB;
	}

	private String constructWarningMsg(Long artefactSizeLimit) {
		return String.format("ArtefactItem ContentLength should be less than or equal to %dMB", artefactSizeLimit);
	}

}
