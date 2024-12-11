package artefact.aws.dynamodb;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public class DynamoDBPartiQLQuery {
    private final DynamoDbClient dynamoDbClient;
    private static final String ERROR_HELP_INTERNAL = "Internal Server Error, generally safe to retry with exponential back-off";
    private static final String ERROR_HELP_THROUGHPUT = "Request rate is too high. Consider reducing frequency of requests or increasing provisioned capacity";
    private static final String ERROR_HELP_RESOURCE = "One of the tables was not found, verify table exists before retrying";

    public DynamoDBPartiQLQuery(DynamoDbClient dynamoDbClient) {
        // Initialize DynamoDB client with profile and region
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Converts a DynamoDB AttributeValue to its corresponding Java type
     */
    private Object convertAttributeValue(AttributeValue attr) {
        if (attr == null) return null;

        if (attr.s() != null) return attr.s(); // String
        if (attr.n() != null) return attr.n(); // Number (as String)
        if (attr.bool() != null) return attr.bool(); // Boolean
        if (attr.m() != null) { // Map
            return attr.m().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            e -> convertAttributeValue(e.getValue())
                    ));
        }
        if (attr.l() != null) { // List
            return attr.l().stream()
                    .map(this::convertAttributeValue)
                    .collect(java.util.stream.Collectors.toList());
        }
        if (attr.nul() != null && attr.nul()) return null;
        if (attr.bs() != null) return attr.bs(); // Binary Set
        if (attr.ns() != null) return attr.ns(); // Number Set
        if (attr.ss() != null) return attr.ss(); // String Set

        return "Unknown type";
    }

    public List<Map<String, AttributeValue>> executePartiQLQueryWithPagination(String tableName, int pageSize) {
        List<Map<String, AttributeValue>> allItems = new ArrayList<>();
        String nextToken = null;

        do {
            try {
                // Create the PartiQL statement
                String statement = String.format(
                        "SELECT * FROM \"%s\".\"GSI-Artefact-4\" WHERE \"type\" = ? AND \"batchStatus\" = ? AND begins_with(\"scanType\", ?)",
                        tableName
                );

                // Create parameters for the query
                List<AttributeValue> parameters = Arrays.asList(
                        AttributeValue.builder().s("batch").build(),
                        AttributeValue.builder().s("INSERTED").build(),
                        AttributeValue.builder().s("NEW").build()
                );

                // Build the execute statement request
                ExecuteStatementRequest.Builder requestBuilder = ExecuteStatementRequest.builder()
                        .statement(statement)
                        .parameters(parameters)
                        .limit(pageSize);

                // Add next token if available
                if (nextToken != null) {
                    requestBuilder.nextToken(nextToken);
                }

                // Execute the query
                ExecuteStatementResponse response = dynamoDbClient.executeStatement(requestBuilder.build());

                // Process the results
//                List<Map<String, Object>> items = response.items().stream()
//                        .map(record -> record.entrySet().stream()
//                                .collect(java.util.stream.Collectors.toMap(
//                                        Map.Entry::getKey,
//                                        e -> convertAttributeValue(e.getValue())
//                                )))
//                        .toList();

                List<Map<String, AttributeValue>> items = response.items();
                allItems.addAll(items);

                // Print progress
                System.out.printf("Retrieved %d items. Total items so far: %d%n",
                        items.size(), allItems.size());

                // Get the next token
                nextToken = response.nextToken();

            } catch (DynamoDbException e) {
                handleError(e);
                break;
            } catch (Exception e) {
                System.out.println("Unknown error while querying: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        } while (nextToken != null);

        System.out.printf("Query completed successfully. Retrieved %d total items.%n", allItems.size());
        return allItems;
    }

    private void handleError(DynamoDbException e) {
        String errorCode = e.awsErrorDetails().errorCode();
        String errorMessage = e.awsErrorDetails().errorMessage();
        String helpString = switch (errorCode) {
            case "InternalServerError" -> ERROR_HELP_INTERNAL;
            case "ProvisionedThroughputExceededException" -> ERROR_HELP_THROUGHPUT;
            case "ResourceNotFoundException" -> ERROR_HELP_RESOURCE;
            default -> "An error occurred";
        };

        System.out.printf("[%s] %s. Error message: %s%n",
                errorCode, helpString, errorMessage);
    }

    void processItems(List<Map<String, Object>> items) {
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("\nItem %d:%n", i + 1);
            Map<String, Object> item = items.get(i);

            // Print each attribute
            item.forEach((key, value) -> {
                System.out.printf("  %s: %s%n", key, formatValue(value));
            });

            System.out.println("-".repeat(50));
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return map.entrySet().stream()
                    .map(e -> String.format("%s: %s", e.getKey(), formatValue(e.getValue())))
                    .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .map(this::formatValue)
                    .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
        }
        return value.toString();
    }


}