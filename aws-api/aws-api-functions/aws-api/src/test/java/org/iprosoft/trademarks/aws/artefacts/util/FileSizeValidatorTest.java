package org.wipo.trademarks.Aws.artefacts.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wipo.trademarks.Aws.artefacts.model.dto.ArtefactInput;

public class FileSizeValidatorTest {

	private static FileSizeValidator validator;

	private static final Long SOUND_FILE_SIZE_LIMIT_MB = 5L;

	private static final Long MULTIMEDIA_FILE_SIZE_LIMIT_MB = 20L;

	private static final Long BYTE_CONVERSION_FACTOR = (long) (1024 * 1024);

	@BeforeAll
	static void setUp() {
		validator = new FileSizeValidator();
	}

	@Test
	public void testValidate_When_ContentLength_Null() {
		Long contentLength = null;
		String classType = ArtefactInput.classType.MULTIMEDIA.toString();

		NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
				() -> validator.validate(contentLength, classType));
		Assertions.assertEquals("Cannot invoke \"java.lang.Long.longValue()\" because \"contentLength\" is null",
				exception.getMessage());

	}

	@Test
	public void testValidate_When_BWLOGO_File() {
		Long contentLength = 20132L;
		String classType = ArtefactInput.classType.BWLOGO.toString();
		Assertions.assertNull(validator.validate(contentLength, classType));
	}

	@Test
	public void testValidate_When_COLOURLOGO_File() {
		Long contentLength = 20132L;
		String classType = ArtefactInput.classType.COLOURLOGO.toString();
		Assertions.assertNull(validator.validate(contentLength, classType));
	}

	@Test
	public void testValidate_When_SOUND_File_AcceptableSize() {
		Long contentLength = SOUND_FILE_SIZE_LIMIT_MB * BYTE_CONVERSION_FACTOR;
		String classType = ArtefactInput.classType.SOUND.toString();
		Assertions.assertNull(validator.validate(contentLength, classType));
	}

	@Test
	public void testValidate_When_SOUND_File_BeyondSizeLimit() {
		String expectedWarningMsg = "ArtefactItem ContentLength should be less than or equal to 5MB";
		Long contentLength = (long) ((SOUND_FILE_SIZE_LIMIT_MB + 0.1) * BYTE_CONVERSION_FACTOR);
		String classType = ArtefactInput.classType.SOUND.toString();
		Assertions.assertEquals(expectedWarningMsg, validator.validate(contentLength, classType));
	}

	@Test
	public void testValidate_When_MULTIMEDIA_File_AcceptableSize() {
		Long contentLength = MULTIMEDIA_FILE_SIZE_LIMIT_MB * BYTE_CONVERSION_FACTOR;
		String classType = ArtefactInput.classType.MULTIMEDIA.toString();
		Assertions.assertNull(validator.validate(contentLength, classType));
	}

	@Test
	public void testValidate_When_MULTIMEDIA_File_BeyondSizeLimit() {
		String expectedWarningMsg = "ArtefactItem ContentLength should be less than or equal to 20MB";
		Long contentLength = (long) ((MULTIMEDIA_FILE_SIZE_LIMIT_MB + 0.1) * BYTE_CONVERSION_FACTOR);
		String classType = ArtefactInput.classType.MULTIMEDIA.toString();
		Assertions.assertEquals(expectedWarningMsg, validator.validate(contentLength, classType));
	}

}
