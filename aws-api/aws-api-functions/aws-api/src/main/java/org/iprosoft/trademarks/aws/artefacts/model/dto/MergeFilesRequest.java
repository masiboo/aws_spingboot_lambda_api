package org.iprosoft.trademarks.aws.artefacts.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MergeFilesRequest {

	@NotNull
	private List<KeyValuePair> objects;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class KeyValuePair {

		@NotBlank
		private String bucket;

		@NotBlank
		private String key;

	}

}
