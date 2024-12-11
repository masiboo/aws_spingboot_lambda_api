package artefact.usecase;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArtefactDBAccess {
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
    private String activeArtefactItem;
    private String activeJobId;
    private String lastError;
    private String errorDate;
    private List<ArtefactItemDBAccess> artefactItems;
    private List<String> artefactTags;
    private String artefactNote;
    private String id;

    // Getters and Setters
    // Add toString() method for easier logging
}
