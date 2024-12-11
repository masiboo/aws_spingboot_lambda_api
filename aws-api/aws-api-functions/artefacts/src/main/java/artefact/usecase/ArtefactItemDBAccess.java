package artefact.usecase;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArtefactItemDBAccess {
    private int id;
    private String s3Key;
    private String fileName;
    private Integer totalPages;
    private String contentType;
    private Long contentLength;
    private String artefactItemType;
    private String fragmentType;
    private String mergedArtefactId;
    private String scanType;
    private String createdDate;
    private String lastModificationDate;

    // Getters and Setters
    // Add toString() method for easier logging
}
