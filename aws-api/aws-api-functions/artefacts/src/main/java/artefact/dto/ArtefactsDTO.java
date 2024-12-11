package artefact.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactsDTO {
	private String mirisDocId;
	private String artefactClass;
	private ZonedDateTime indexationDate;
	private String artefactName;
	private String status;
	private String s3Bucket;
	private ZonedDateTime archiveDate;
	private ZonedDateTime lastModificationDate;
	private String lastModificationUser;
	private String dmapsVersion;
	private String importedImapsError;
	private String importedImapsDocId;
	private String artefactUUID;
	private int activeArtefactItem;
	private String activeJobId;
	private String lastError;
	private ZonedDateTime errorDate;
	private BatchDTO batch;
	private List<ArtefactItemDTO> artefactItems;
	private List<ArtefactTagDTO> artefactTags;
	private ArtefactNoteDTO artefactNote;
	private int id;


}
