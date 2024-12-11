package artefact.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.fasterxml.jackson.annotation.JsonFormat;
import artefact.util.DateUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class ArtefactDynamoDbV2 {

    @DynamoDBAttribute(attributeName = "ArtefactId")
    private String artefactId;

    @DynamoDBHashKey(attributeName = "PK")
    public String getPK() {
        return "ARTEFACT#" + artefactId;
    }
    private String artefactName;
    private String artefactClassType;

    private String status;
    private String error;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime indexationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.DATETIME_FORMAT)
    private ZonedDateTime archiveDate;

    private List<ArtefactItemDynamoV2> artefactItems;

    public ArtefactDynamoDbV2(){
        this.indexationDate = ZonedDateTime.now();
    }

}
