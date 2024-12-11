package org.iprosoft.trademarks.aws.artefacts.util;

import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactClassEnum;

public class ArtefactClassTypeDetector {

	public static ArtefactClassEnum detectByType(String type) {
		String artefactType = getClasstype(type);
		if (StringUtils.hasText(artefactType)) {
			return ArtefactClassEnum.valueOf(artefactType);
		}

		throw new RuntimeException("Unable to find  ArtefactClassType from the given type: " + type);
	}

	private static String getClasstype(String type) {
		int index = type.indexOf("#");
		if (index > 0)
			return type.substring(index + 1);
		throw new IllegalArgumentException("Invalid ArtefactClassType found: " + type);
	}

}
