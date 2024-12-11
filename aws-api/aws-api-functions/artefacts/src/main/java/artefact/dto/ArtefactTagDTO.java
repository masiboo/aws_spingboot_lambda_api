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
public class ArtefactTagDTO {
    private int id;
    private String key;
    private String value;
    private String type;
    private ZonedDateTime insertedDate;
    private String artefact;
}
