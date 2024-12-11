package org.iprosoft.trademarks.aws.artefacts.util;

import java.util.Arrays;

public enum RequestType {

	ADDENDUM, NEW_REQUEST;

	public static boolean isAllowedType(String inputType) {
		return Arrays.stream(values()).anyMatch(val -> val.toString().equalsIgnoreCase(inputType));
	}

}