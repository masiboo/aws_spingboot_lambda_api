package org.wipo.trademarks.Aws.artefacts.util;

import com.amazonaws.services.dynamodbv2.xspec.S;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

	@BeforeEach
	void setUp() {
	}

	@Test
	void isSameDay() {
		// Arrange
		String date1 = "2010-05-20T08:30:00+0000";
		String date2 = "2010-05-20T00:30:00+0000";

		// Act
		boolean sameDay = DateUtils.isSameDay(date1, date2);

		// Assert
		assertTrue(sameDay);
	}

	@Test
	void isNotSameDay() {
		// Arrange
		String date1 = "2010-05-21T08:30:00+0000";
		String date2 = "2010-05-20T08:30:00+0000";

		// Act
		boolean sameDay = DateUtils.isSameDay(date1, date2);

		// Assert
		assertFalse(sameDay);
	}

	@Test
	void isSameDayWithDifferentDateFormat() {
		// Arrange
		String date1 = "2024-06-01T09:26:45+0000";
		String date2 = "Sat Jun 01 16:41:52 UTC 2024";

		// Act
		boolean sameDay = DateUtils.isSameDay(date1, date2);

		// Assert
		assertTrue(sameDay);
	}

	@Test
	void isNotSameDayWithDifferentDateFormat() {
		// Arrange
		String date1 = "2024-06-01T09:26:45+0000";
		String date2 = "Sat Jun 02 16:41:52 UTC 2024";

		// Act
		boolean sameDay = DateUtils.isSameDay(date1, date2);

		// Assert
		assertFalse(sameDay);
	}

	@Test
	void testGetShortDateFromZonedDateTimeWithNull() {
		String dateTime = DateUtils.getShortDateFromZonedDateTime(null);
		assertNotNull(dateTime);
	}

	@Test
	void testGetShortDateFromZonedDateTimeWithInvalidDate() {
		ZonedDateTime date = ZonedDateTime.parse("2024-06-21T17:59:47.239310816+02:00[Europe/Berlin]");
		String dateTime = DateUtils.getShortDateFromZonedDateTime(date);
		assertNotNull(dateTime);
	}

	@Test
	void testGetFullDateFromZonedDateTimeWithValidInput() {
		String ret = DateUtils.getFullDateFromZonedDateTime(DateUtils.getCurrentDatetimeUtc());
		assertNotNull(ret);
	}

	@Test
	void testGetFullDateFromZonedDateTimeWithNull() {
		String ret = DateUtils.getFullDateFromZonedDateTime(null);
		assertNotNull(ret);
	}

	@Test
	void testIsPastDay() {
		// Arrange
		String date1 = "2010-08-13T08:30:00+0000";
		String date2 = "2010-08-10T00:30:00+0000";

		// Act
		boolean sameDay = DateUtils.isPastDay(date1, date2);

		// Assert
		assertTrue(sameDay);
	}

}