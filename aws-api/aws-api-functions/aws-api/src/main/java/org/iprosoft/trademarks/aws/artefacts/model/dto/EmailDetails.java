package org.iprosoft.trademarks.aws.artefacts.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EmailDetails {

	private String from;

	private List<String> to;

	private List<String> cc;

	private List<String> bcc;

	private String subject;

	private String body;

	private Attachment attachment;

	@Getter
	@Setter
	public static class Attachment {

		private String filename;

		private String contentType;

		private String base64Content;

	}

}
