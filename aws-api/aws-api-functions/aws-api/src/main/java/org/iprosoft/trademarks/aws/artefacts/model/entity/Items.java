package org.iprosoft.trademarks.aws.artefacts.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Items {

	private int id;

	private String storage;

	private String path;

	private String filename;

	private String artefactType;

	private String contentType;

	private int totalPages;

	private String jobId;

	private String jobStatus;

}
