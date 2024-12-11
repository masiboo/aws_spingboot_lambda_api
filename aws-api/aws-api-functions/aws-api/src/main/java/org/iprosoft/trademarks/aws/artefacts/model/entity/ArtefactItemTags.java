package org.iprosoft.trademarks.aws.artefacts.model.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
public class ArtefactItemTags {

	private int id;

	private String value;

	private String key;

	private ZonedDateTime insertedDate;

	private String type;

	public ArtefactItemTags() {
		this.insertedDate = DateUtils.getCurrentDatetimeUtc();
	}

}
