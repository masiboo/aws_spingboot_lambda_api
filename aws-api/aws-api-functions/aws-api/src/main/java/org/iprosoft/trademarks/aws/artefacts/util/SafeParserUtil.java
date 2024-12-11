package org.iprosoft.trademarks.aws.artefacts.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class SafeParserUtil {

	private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

	private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static Long safeParseLong(String value) {
		try {
			return Long.valueOf(value);
		}
		catch (NumberFormatException e) {
			// ignore. logging it and return 0
			log.warn("Invalid number format: {}", value);
			return 0L;
		}
	}

	public static String safeLongToString(Long value) {
		if (value == null) {
			return "0";
		}
		try {
			return String.valueOf(value);
		}
		catch (Exception e) {
			// ignore. logging it and return 0
			log.warn("Invalid number format: {}", value);
			return "0";
		}
	}

	public static int safeParseInteger(String input) {
		int output = 0;
		try {
			output = Integer.parseInt(input);
		}
		catch (Exception e) {
			// ignore it
			log.warn("input {} error msg: {}", input, e.getMessage());
		}
		return output;
	}

	public static ZonedDateTime safeParseZonedDateTime(String input) {
		ZonedDateTime zonedDateTime = DateUtils.getCurrentDatetimeUtc();
		if (input == null || input.isBlank()) {
			return zonedDateTime;
		}
		try {
			return ZonedDateTime.parse(input, ISO_FORMATTER);
		}
		catch (DateTimeParseException e) {
			try {
				LocalDate localDate = LocalDate.parse(input, SHORT_DATE_FORMATTER);
				return localDate.atStartOfDay(ZoneId.systemDefault());
			}
			catch (DateTimeParseException ex) {
				return DateUtils.getCurrentDatetimeUtc();
			}
		}
	}

}
