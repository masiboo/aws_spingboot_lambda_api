package org.iprosoft.trademarks.aws.artefacts.aws.audit;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class AuditEventService {

	/** Partition Key of Table. */
	static String PK = "PK";

	/** Sort Key of Table. */
	static String SK = "SK";

	private final DynamoDbClient dynamoDbClient;

	public void saveAuditEvent(final String event_type, final String values, String event_id) {

		// Structure [event-type]-[today's-date-time-event-id]-[event-detail]
		String pk_now = DateUtils.getCurrentDatetimeUtcStr();
		Map<String, AttributeValue> pkValues = new HashMap<>(auditKeys(event_type, pk_now + "-" + event_id));

		addValue(pkValues, "event-id", event_id);
		addValue(pkValues, "event-details", values);

		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.AUDIT_EVENT_TABLE_NAME)
			.item(pkValues)
			.build();
		this.dynamoDbClient.putItem(put);
	}

	private static Map<String, AttributeValue> auditKeys(String pk, String sk) {
		return auditKeysGeneric(PK, pk, SK, sk);
	}

	private static Map<String, AttributeValue> auditKeysGeneric(String pkKey, String pk, String skKey, String sk) {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put(pkKey, AttributeValue.builder().s((pk)).build());

		if (sk != null) {
			key.put(skKey, AttributeValue.builder().s(sk).build());
		}

		return key;
	}

	private static void addValue(final Map<String, AttributeValue> map, final String key, final String valueDetail) {
		if (valueDetail != null) {
			map.put(key, AttributeValue.builder().s(valueDetail).build());
		}
	}

}
