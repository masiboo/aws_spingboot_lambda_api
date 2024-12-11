package org.iprosoft.trademarks.aws.artefacts.model.entity;


import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;

import java.time.ZonedDateTime;

public class ArtefactItemDynamoDB {

	private String artefactItemId;

	private ZonedDateTime insertedDate;

	private String artefactTraceId;

	private String path;

	private String userId;

	private String contentType;

	private String checksum;

	private Long contentLength;

	private String timeToLive;

	public String getPK() {
		return "ITEM#" + artefactItemId;
	}

	public ArtefactItemDynamoDB() {
		this.insertedDate = DateUtils.getCurrentDatetimeUtc();
	}

}
