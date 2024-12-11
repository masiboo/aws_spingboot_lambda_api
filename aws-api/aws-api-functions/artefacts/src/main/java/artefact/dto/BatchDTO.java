package artefact.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchDTO {
    private int id;
    private String batchSequence;
    private String operator;
    private String status;
    private String lockedBy;
    private ZonedDateTime lockedDate;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastModificationDate;
    private String lastModificationUser;
    private String requestType;
    private String requestId;
    private String scanType;
    private String type;
    private List<String> artefacts;
}
