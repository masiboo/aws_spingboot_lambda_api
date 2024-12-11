package artefact.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonFormat;
import artefact.util.DateUtils;

import java.time.ZonedDateTime;
import java.util.Date;

@DynamoDBTable(tableName="RUNTIMESPECIFIC")
public class ArtefactItemDynamoV2 {

    @DynamoDBAttribute(attributeName = "ArtefactItemId")
    private String artefactItemId;

    @DynamoDBHashKey(attributeName = "PK")
    public String getPK() {
        return "ITEM#" + artefactItemId;
    }
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime insertedDate;
    private String artefactTraceId;

    private String path;
    private String userId;
    private String contentType;
    private String checksum;
    private Long contentLength;

    private String timeToLive;

    public ArtefactItemDynamoV2(){
        this.insertedDate = ZonedDateTime.now();
    }

}
