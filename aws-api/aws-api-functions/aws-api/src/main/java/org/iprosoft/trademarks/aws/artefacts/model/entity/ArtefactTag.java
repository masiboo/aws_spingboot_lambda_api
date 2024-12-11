package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ArtefactTag {

	private String key;

	private String documentId;

	private String value;

	private String userId;

	@DateTimeFormat(pattern = DateUtils.DATETIME_FORMAT, iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
	private ZonedDateTime insertedDate;

	private DocumentTagType documentTagType;

	private List<String> values;

	public ArtefactTag() {
		if (this.insertedDate == null) {
			insertedDate = DateUtils.getCurrentDatetimeUtc();
		}
	}

	/**
	 * constructor.
	 * @param docid {@link String}
	 * @param tagKey {@link String}
	 * @param tagValue {@link String}
	 * @param date {@link ZonedDateTime}
	 * @param user {@link String}
	 * @param tagType {@link DocumentTagType}
	 */
	public ArtefactTag(final String docid, final String tagKey, final String tagValue, final ZonedDateTime date,
			final String user, final DocumentTagType tagType) {
		this();
		setDocumentId(docid);
		setKey(tagKey);
		setValue(tagValue);
		setInsertedDate(date);
		setUserId(user);
		setDocumentTagType(tagType);
	}

	/**
	 * constructor.
	 * @param docid {@link String}
	 * @param tagKey {@link String}
	 * @param tagValue {@link String}
	 * @param date {@link ZonedDateTime}
	 * @param user {@link String}
	 */
	public ArtefactTag(final String docid, final String tagKey, final String tagValue, final ZonedDateTime date,
			final String user) {
		this(docid, tagKey, tagValue, date, user, DocumentTagType.USERDEFINED);
	}

	public String getFormattedInsertedDate() {
		if (this.insertedDate == null) {
			insertedDate = DateUtils.getCurrentDatetimeUtc();
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(insertedDate.toInstant(),
				TimeZone.getTimeZone("UTC").toZoneId());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		return zonedDateTime.format(formatter);
	}

}
