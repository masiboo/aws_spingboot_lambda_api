package artefact.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import artefact.util.DateUtils;

import java.time.ZonedDateTime;


public class ArtefactItemDynamoDB {

    private String artefactItemId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime insertedDate;
    private String artefactTraceId;
    private String path;
    private String userId;
    private String contentType;
    private String checksum;
    private Long contentLength;
    private String timeToLive;

    public String getPK() {
        return "ITEM#" + artefactItemId;
    }

    public ArtefactItemDynamoDB(){
        this.insertedDate = ZonedDateTime.now();
    }

}
