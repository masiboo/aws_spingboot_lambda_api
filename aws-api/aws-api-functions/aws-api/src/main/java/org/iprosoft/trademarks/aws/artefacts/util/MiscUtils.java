package org.iprosoft.trademarks.aws.artefacts.util;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MiscUtils {

	public static Map<String, String> validateQueryParams(String date, String status, String fromDate,
			String untilDate) {
		Map<String, String> validationErrors = new HashMap<>();
		if (StringUtils.hasText(date)) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				dateFormat.setLenient(false);
				dateFormat.parse(date);
			}
			catch (ParseException e) {
				validationErrors.put("date", "Invalid dateFormat and valid format is yyyy-MM-dd");
			}
		}
		if (StringUtils.hasText(status)) {
			try {
				ArtefactStatus.valueOf(status);
			}
			catch (IllegalArgumentException e) {
				validationErrors.put("status",
						"Invalid status given allowed values are " + Arrays.toString(ArtefactStatus.values()));
			}
		}
		else {
			validationErrors.put("status",
					"Status is null/empty. Status should have a value: " + Arrays.toString(ArtefactStatus.values()));
		}
		if (StringUtils.hasText(fromDate)) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				dateFormat.setLenient(false);
				dateFormat.parse(fromDate);
			}
			catch (ParseException e) {
				validationErrors.put("fromDate", "Invalid dateFormat and valid format is yyyy-MM-dd");
			}
		}
		if (StringUtils.hasText(untilDate)) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				dateFormat.setLenient(false);
				dateFormat.parse(untilDate);
			}
			catch (ParseException e) {
				validationErrors.put("untilDate", "Invalid dateFormat and valid format is yyyy-MM-dd");
			}
		}
		if (DateUtils.isPastDay(fromDate, untilDate)) {
			validationErrors.put("untilDate", "untilDate must be before fromDate");
		}
		return validationErrors;
	}

}
