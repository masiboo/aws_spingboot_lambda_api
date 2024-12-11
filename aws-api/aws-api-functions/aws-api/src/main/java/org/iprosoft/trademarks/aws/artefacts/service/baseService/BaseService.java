package org.iprosoft.trademarks.aws.artefacts.service.baseService;

import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.IArtefact;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface BaseService {

	public static final DynamoDbClient dynamoDbClient = null;

	private WriteRequest createWriteRequestItem(final Map<String, AttributeValue> values) {

		PutRequest put = PutRequest.builder().item(values).build();

		return WriteRequest.builder().putRequest(put).build();
	}

	private <T> void saveItemsWithTransaction(Collection<T> items,
			Function<T, Map<String, AttributeValue>> itemMapper) {

		// 1. Prepare items to be written
		List<WriteRequest> writeRequests = new ArrayList<>();
		for (T item : items) {
			writeRequests.add(createWriteRequestItem(itemMapper.apply(item)));
		}

		// 2. bundle the request
		Map<String, List<WriteRequest>> requestItems = new HashMap<>();
		requestItems.put(SystemEnvironmentVariables.REGISTRY_TABLE_NAME, writeRequests);

		BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
			.requestItems(requestItems)
			.build();

		try {
			BatchWriteItemResponse response;
			do {
				response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);

				// Check for unprocessed items
				Map<String, List<WriteRequest>> unprocessedItems = response.unprocessedItems();

				if (!unprocessedItems.isEmpty()) {
					// log.info("There were unprocessed items. Retrying...");
					// Prepare the next batch request with unprocessed items
					requestItems = new HashMap<>();
					requestItems.put(SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
							unprocessedItems.get(SystemEnvironmentVariables.REGISTRY_TABLE_NAME));
					batchWriteItemRequest = BatchWriteItemRequest.builder().requestItems(requestItems).build();
				}
				else {
					// log.info("BatchWriteItem succeeded");
				}
			}
			while (!response.unprocessedItems().isEmpty());

		}
		catch (DynamoDbException e) {
			// log.error("DynamoDbException: {}", e.getMessage());
		}

	}

}
