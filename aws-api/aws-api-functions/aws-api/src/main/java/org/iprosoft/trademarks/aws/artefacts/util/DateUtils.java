package org.iprosoft.trademarks.aws.artefacts.util;

import com.amazonaws.lambda.thirdparty.org.joda.time.DateTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public final class DateUtils {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final String UTC_DATETIME_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

	private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";

	private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

	private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

	private static final List<DateTimeFormatter> inputFormatters = Arrays.asList(
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH"),
			DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ssXXX"),
			DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm"),
			DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ssXXX"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"), DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ssXXX"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss"), DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH"), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"), DateTimeFormatter.ofPattern("dd/MM/yyyy HH"),
			DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"));

	public static String ensureCorrectFormat(String input) {
		OffsetDateTime dateTime = null;
		if (input == null) {
			input = getCurrentDatetimeUtcStr();
		}
		log.info("Input datetime: " + input);
		for (DateTimeFormatter formatter : inputFormatters) {
			try {
				LocalDateTime localDateTime = LocalDateTime.parse(input, formatter);
				dateTime = localDateTime.atOffset(ZoneOffset.UTC);
				break;
			}
			catch (DateTimeParseException e) {
				// ignore exception and continue next format
			}
		}
		// If no valid date was parsed, use the current datetime
		if (dateTime == null) {
			log.info("Invalid date format for input: " + input + ". Using current datetime.");
			dateTime = OffsetDateTime.now(ZoneOffset.UTC);
		}
		return dateTime.format(outputFormatter);
	}

	private static String replaceSpaceWithPlusOrMinus(String dateTime) {
		// Regular expression to match the time and offset parts
		String regex = "(\\d{2}:\\d{2}:\\d{2}) ?([+-]?\\d{4})";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(dateTime);

		// Replace the space with "+" if there is no existing sign, or preserve the
		// existing sign
		if (matcher.find()) {
			String offsetPart = matcher.group(2);
			if (offsetPart.startsWith("+") || offsetPart.startsWith("-")) {
				dateTime = matcher.replaceAll("$1$2");
			}
			else {
				dateTime = matcher.replaceAll("$1+$2");
			}
		}
		return dateTime;
	}

	public static LocalDateTime parseToLocalDateTime(String dateString) {
		LocalDateTime localDateTime;
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(UTC_DATETIME_FORMAT);

		try {
			if (Character.isDigit(dateString.charAt(0))) {
				ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, formatter1);
				localDateTime = zonedDateTime.toLocalDateTime();
			}
			else {
				ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, formatter2);
				localDateTime = zonedDateTime.toLocalDateTime();
			}
		}
		catch (DateTimeParseException e) {
			localDateTime = LocalDateTime.now();
		}
		return localDateTime;
	}

	public static ZonedDateTime parseToZonedDateTime(String dateString) {
		ZonedDateTime zonedDateTime;
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(UTC_DATETIME_FORMAT);
		if (dateString == null || dateString.isBlank()) {
			return DateUtils.getCurrentDatetimeUtc();
		}
		try {
			if (Character.isDigit(dateString.charAt(0))) {
				zonedDateTime = ZonedDateTime.parse(dateString, formatter1);
			}
			else {
				zonedDateTime = ZonedDateTime.parse(dateString, formatter2);
			}
		}
		catch (DateTimeParseException e) {
			zonedDateTime = DateUtils.getCurrentDatetimeUtc();
		}
		return zonedDateTime;
	}

	public static boolean isSameDay(String dateStr1, String dateStr2) {
		ZonedDateTime dateTime1 = parseToZonedDateTime(dateStr1);
		ZonedDateTime dateTime2 = parseToZonedDateTime(dateStr2);
		return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
	}

	public static boolean isPastDay(String dateStr1, String dateStr2) {
		ZonedDateTime dateTime1 = parseToZonedDateTime(dateStr1);
		ZonedDateTime dateTime2 = parseToZonedDateTime(dateStr2);
		return dateTime1.isAfter(dateTime2);
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(SHORT_DATE_FORMAT);
		sdf.setTimeZone(utcTimeZone);
		return sdf;
	}

	public static SimpleDateFormat getSimpleUtcDateTimeFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		sdf.setTimeZone(utcTimeZone);
		return sdf;
	}

	public static boolean isValidDate(String date, SimpleDateFormat dateFormat) {
		if (Strings.isBlank(date)) {
			return false;
		}
		try {
			dateFormat.setLenient(false);
			dateFormat.parse(date);
			return true;
		}
		catch (ParseException e) {
			return false;
		}
	}

	public static Date parseDateFromUTC(String dateString) throws ParseException {
		SimpleDateFormat sdf = getSimpleUtcDateTimeFormat();
		return sdf.parse(dateString);
	}

	/*
	 * return format 2024-06-20
	 */
	public static String getCurrentDateShortStr() {
		return getSimpleDateFormat().format(new Date());
	}

	/*
	 * return format 2024-06-20T07:20:32+0000
	 */
	public static String getCurrentDatetimeUtcStr() {
		return getSimpleUtcDateTimeFormat().format(new Date());
	}

	public static String getCurrentUtcDateTimeStr() {
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		return now.format(formatter);
	}

	public static String getCurrentTimeStr() {
		LocalTime currentTime = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		return currentTime.format(formatter);
	}

	/*
	 * returns format 2024-06-20T07:22:43Z
	 */
	public static ZonedDateTime getCurrentDatetimeUtc() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		try {
			return ZonedDateTime.parse(getCurrentUtcDateTimeStr(), formatter.withZone(ZoneOffset.UTC));
		}
		catch (Exception e) {
			// Handle the exception
			log.error("Error parsing date", e);
			// Return the current date-time as a fallback
			return ZonedDateTime.now(ZoneOffset.UTC);
		}
	}

	public static boolean isValidShortDateFormat(String input) {
		// regex pattern to check correct short date format i.g 2024-06-21
		String regex = "^\\d{4}-\\d{2}-\\d{2}$";
		return input != null && input.matches(regex);
	}

	public static String getShortDateStringFromDateTime(DateTime date) {
		if (date != null) {
			if (isValidShortDateFormat(date.toString())) {
				return date.toString();
			}
			else {
				return new Date().toString();
			}
		}
		else {
			return new Date().toString();
		}
	}

	public static String getFullDateFromZonedDateTime(ZonedDateTime date) {
		if (date == null) {
			return getCurrentDatetimeUtc().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
		}
		try {
			return date.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
		}
		catch (Exception e) {
			try {
				int year = date.getYear();
				int month = date.getMonthValue();
				int day = date.getDayOfMonth();
				int hour = date.getHour();
				int minute = date.getMinute();
				int second = date.getSecond();
				String zoneOffset = date.getOffset().getId().replace(":", "");
				return String.format("%04d-%02d-%02dT%02d:%02d:%02d%s", year, month, day, hour, minute, second,
						zoneOffset);
			}
			catch (Exception ex) {
				return getCurrentDatetimeUtc().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
			}
		}
	}

	public static String getShortDateFromZonedDateTime(ZonedDateTime date) {
		if (date == null) {
			return getCurrentDatetimeUtc().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
		}
		try {
			return date.format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
		}
		catch (Exception e) {
			try {
				int year = date.getYear();
				int month = date.getMonthValue();
				int day = date.getDayOfMonth();
				return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
			}
			catch (Exception ex) {
				return getCurrentDatetimeUtc().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
			}
		}
	}

}
