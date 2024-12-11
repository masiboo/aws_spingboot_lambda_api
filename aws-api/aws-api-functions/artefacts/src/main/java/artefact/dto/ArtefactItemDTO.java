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
public class ArtefactItemDTO {
    private int id;
    private String s3Key;
    private String fileName;
    private int totalPages;
    private String contentType;
    private long contentLength;
    private String artefactItemType;
    private String fragmentType;
    private String mergedArtefactId;
    private String scanType;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModificationDate;
    private String artefact;
    private Long resolutionInDpi;
}
