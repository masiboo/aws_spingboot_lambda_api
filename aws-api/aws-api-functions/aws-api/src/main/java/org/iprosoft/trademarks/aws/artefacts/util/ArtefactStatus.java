package org.iprosoft.trademarks.aws.artefacts.util;

public enum ArtefactStatus {

	INDEXED("INDEXED"), DELETED("DELETED"),

	INSERTED("INSERTED"), UPLOADED("UPLOADED"), ERROR("ERROR"),

	INIT("INIT"), CANCELED("CANCELED");

	private String status;

	ArtefactStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return this.status;
	}

}
