package org.iprosoft.trademarks.aws.artefacts.aws.dynamodb;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class DynamoDbHelper {

	public static UpdateItemResponse updateTableItem(DynamoDbClient ddb, String tableName,
			Map<String, AttributeValue> itemKey, String columnName, String updateVal) {
		HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
		updatedValues.put(columnName, buildAttrValueUpdate(updateVal, AttributeAction.PUT));
		return updateAttributes(ddb, tableName, itemKey, updatedValues);
	}

	public static UpdateItemResponse updateAttributes(DynamoDbClient ddb, String tableName,
			Map<String, AttributeValue> itemKey, Map<String, AttributeValueUpdate> updatedValues) {
		UpdateItemRequest request = UpdateItemRequest.builder()
			.tableName(tableName)
			.key(itemKey)
			.attributeUpdates(updatedValues)
			.build();
		log.info("UpdateItemRequest request {}", request);
		try {
			UpdateItemResponse response = ddb.updateItem(request);
			log.info("UpdateItemResponse response {}", response.toString());
			if (response.sdkHttpResponse() != null) {
				if (response.sdkHttpResponse().isSuccessful()) {
					log.info("Status updated successfully for request: {}", request);
				}
				else {
					log.warn("Failed to update status for request: {}", request);
				}
			}
			else {
				log.warn("response.sdkHttpResponse() is null  for request: {}", request);
			}
			return response;
		}
		catch (DynamoDbException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static AttributeValueUpdate buildAttrValueUpdate(String updateVal, AttributeAction action) {
		return buildAttrValueUpdate(AttributeValue.builder().s(updateVal).build(), action);
	}

	public static AttributeValueUpdate buildAttrValueUpdate(AttributeValue attributeValue, AttributeAction action) {
		AttributeValueUpdate attributeValueUpdate = AttributeValueUpdate.builder()
			.value(attributeValue)
			.action(action)
			.build();
		log.info("buildAttrValueUpdate {}", attributeValueUpdate.toString());
		return attributeValueUpdate;
	}

}