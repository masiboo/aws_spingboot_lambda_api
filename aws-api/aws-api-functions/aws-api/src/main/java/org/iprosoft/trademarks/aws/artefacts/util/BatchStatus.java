package org.iprosoft.trademarks.aws.artefacts.util;

public enum BatchStatus {

	INIT("INIT"), COMPLETED("COMPLETED"), DELETED("DELETED"), UPLOADED("UPLOADED"), INSERTED("INSERTED"),
	INDEXED("INDEXED"), ERROR("ERROR");

	private String status;

	BatchStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return this.status;
	}

}
