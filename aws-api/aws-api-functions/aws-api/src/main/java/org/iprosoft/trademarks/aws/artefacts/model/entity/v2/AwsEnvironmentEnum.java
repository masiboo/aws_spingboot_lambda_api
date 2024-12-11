package org.iprosoft.trademarks.aws.artefacts.model.entity.v2;

import java.util.Arrays;

public enum AwsEnvironmentEnum {

	ACC, DEV, PROD;

	public static boolean isAllowedType(String inputType) {
		return Arrays.stream(values()).anyMatch(val -> val.toString().equalsIgnoreCase(inputType));
	}

}
