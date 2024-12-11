
package org.iprosoft.trademarks.aws.artefacts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Batch implements Serializable {

	private String pk;

	private String sk;

	private String artefactId;

	private String batchStatus;

	private String batchSequence;

	private String requestId;

	private String mirisDocId;

	private String scanType;

	private final static long serialVersionUID = -4822388947592286956L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Batch() {
	}

	/**
	 * @param requestId
	 * @param artefactId
	 * @param sk
	 * @param pk
	 * @param batchSequence
	 * @param batchStatus
	 */
	public Batch(String pk, String sk, String artefactId, String batchStatus, String batchSequence, String requestId,
			String mirisDocId) {
		super();
		this.pk = pk;
		this.sk = sk;
		this.artefactId = artefactId;
		this.batchStatus = batchStatus;
		this.batchSequence = batchSequence;
		this.requestId = requestId;
		this.mirisDocId = mirisDocId;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public String getSk() {
		return sk;
	}

	public void setSk(String sk) {
		this.sk = sk;
	}

	public String getArtefactId() {
		return artefactId;
	}

	public void setArtefactId(String artefactId) {
		this.artefactId = artefactId;
	}

	public String getBatchStatus() {
		return batchStatus;
	}

	public void setBatchStatus(String batchStatus) {
		this.batchStatus = batchStatus;
	}

	public String getBatchSequence() {
		return batchSequence;
	}

	public void setBatchSequence(String batchSequence) {
		this.batchSequence = batchSequence;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMirisDocId() {
		return mirisDocId;
	}

	public void setMirisDocId(String mirisDocId) {
		this.mirisDocId = mirisDocId;
	}

	public String getScanType() {
		return scanType;
	}

	public void setScanType(String scanType) {
		this.scanType = scanType;
	}

}
