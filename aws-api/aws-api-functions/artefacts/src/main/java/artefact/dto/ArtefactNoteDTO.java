package artefact.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtefactNoteDTO {
    private int id;
    private String content;
    private ZonedDateTime createdDate;
    private String author;
    private ZonedDateTime modifiedDate;
    private String artefact;
}
