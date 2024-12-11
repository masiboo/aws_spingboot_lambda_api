package artefact.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ArtefactTagsDTO {

	private Long id;

	private String key;

	private String value;

	private String type;

	private ZonedDateTime insertedDate;

	private Long artefactId;

}
