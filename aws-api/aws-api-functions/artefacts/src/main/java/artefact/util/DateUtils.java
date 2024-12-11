package artefact.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
public final class DateUtils {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";

	public static final String UTC_DATETIME_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

	private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

	public static SimpleDateFormat getSimpleDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(SHORT_DATE_FORMAT);
		sdf.setTimeZone(utcTimeZone);
		return sdf;
	}

	public static SimpleDateFormat getSimpleDateTimeFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		sdf.setTimeZone(utcTimeZone);
		return sdf;
	}

	public static boolean isValidDate(String date, SimpleDateFormat dateFormat) {
		try {
			dateFormat.setLenient(false);
			dateFormat.parse(date);
			return true;
		}
		catch (ParseException e) {
			return false;
		}
	}

	public static ZonedDateTime parseToZonedDateTime(String dateString) {
		ZonedDateTime zonedDateTime;
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(UTC_DATETIME_FORMAT);
		if (dateString == null || dateString.isBlank()) {
			return ZonedDateTime.now();
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
			zonedDateTime = ZonedDateTime.now();
		}
		return zonedDateTime;
	}

	public static SimpleDateFormat getSimpleUtcDateTimeFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		sdf.setTimeZone(utcTimeZone);
		return sdf;
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

	public static String getFullDateFromZonedDateTime(ZonedDateTime date) {
		if (date == null) {
			return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
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
				return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
			}
		}
	}

	public static String getShortDateFromZonedDateTime(ZonedDateTime date) {
		if (date == null) {
			return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
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
				return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
			}
		}
	}

	public static ObjectMapper createObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

}
