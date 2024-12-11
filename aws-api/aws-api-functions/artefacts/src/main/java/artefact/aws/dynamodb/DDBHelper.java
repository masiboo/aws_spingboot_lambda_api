package artefact.aws.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class DDBHelper {
    static final Logger logger = LoggerFactory.getLogger(ArtefactJobServiceImpl.class);

    static void updateTableItem(DynamoDbClient ddb, String tableName, Map<String, AttributeValue> itemKey, String columnName, String updateVal){

        HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(columnName, buildAttrValueUpdate(updateVal,AttributeAction.PUT));

        updateAttributes(ddb,tableName,itemKey,updatedValues);
    }

    public static void updateAttributes(DynamoDbClient ddb, String tableName, Map<String, AttributeValue> itemKey, Map<String,AttributeValueUpdate> updatedValues){

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
        } catch (DynamoDbException e) {
            logger.error(e.getMessage());

        }
        System.out.println("The Amazon DynamoDB table was updated!");
    }

    public static AttributeValueUpdate buildAttrValueUpdate(String updateVal,AttributeAction action){
        return buildAttrValueUpdate(AttributeValue.builder().s(updateVal).build(),action);
    }

    public static AttributeValueUpdate buildAttrValueUpdate(AttributeValue attributeValue,AttributeAction action){
        return AttributeValueUpdate.builder()
                .value(attributeValue)
                .action(action)
                .build();
    }

}
