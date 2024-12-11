package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration;

import cloud.localstack.Localstack;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class AwsServicesSetup {

	public static final Region REGION = Region.US_EAST_1;

	public static final String UNIT_TEST_BUCKET = "unit-test-bucket";

	public static void prepareDynamoDB() {
		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointDynamoDB());
			DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
				.endpointOverride(localstackEndpoint)
				.region(REGION)
				.build();
			DynamoDbWaiter dbWaiter = dynamoDbClient.waiter();
			// Create te Global Secondary Indexes of the DynamoDB table
			Collection<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
			// Create GlobalSecondaryIndexes for Aws-table
			globalSecondaryIndices.add(createGlobalSecondaryIndex("GSI-Artefact-1", "type", "status"));
			globalSecondaryIndices.add(createGlobalSecondaryIndex("GSI-Artefact-2", "mirisDocId", "type"));
			globalSecondaryIndices.add(createGlobalSecondaryIndex("GSI-Artefact-3", "requestId", "type"));
			globalSecondaryIndices.add(createGlobalSecondaryIndex("GSI-Artefact-4", "type", "batchStatus"));
			globalSecondaryIndices.add(createGlobalSecondaryIndex("GSI-Artefact-5", "insertedDate", "mirisDocId"));
			// PartitionKey and SortKey for table
			Collection<KeySchemaElement> keySchemaElementsAwsTable = new ArrayList<>();
			KeySchemaElement partitionKey = KeySchemaElement.builder()
				.attributeName("PK")
				.keyType(KeyType.HASH)
				.build();
			KeySchemaElement sortKey = KeySchemaElement.builder().attributeName("SK").keyType(KeyType.RANGE).build();
			keySchemaElementsAwsTable.add(partitionKey);
			keySchemaElementsAwsTable.add(sortKey);
			// Attribute definition for keys
			Collection<AttributeDefinition> attributeDefinitions = new ArrayList<>();
			AttributeDefinition partitionKeyAttributeDefinition = AttributeDefinition.builder()
				.attributeName("PK")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition sortKeyAttributeDefinition = AttributeDefinition.builder()
				.attributeName("SK")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition typeAttributeDefinition = AttributeDefinition.builder()
				.attributeName("type")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition statusAttributeDefinition = AttributeDefinition.builder()
				.attributeName("status")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition requestIdAttributeDefinition = AttributeDefinition.builder()
				.attributeName("requestId")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition mirisDocIdAttributeDefinition = AttributeDefinition.builder()
				.attributeName("mirisDocId")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition batchStatusAttributeDefinition = AttributeDefinition.builder()
				.attributeName("batchStatus")
				.attributeType(ScalarAttributeType.S)
				.build();
			AttributeDefinition insertedDateAttributeDefinition = AttributeDefinition.builder()
				.attributeName("insertedDate")
				.attributeType(ScalarAttributeType.S)
				.build();
			attributeDefinitions.add(partitionKeyAttributeDefinition);
			attributeDefinitions.add(sortKeyAttributeDefinition);
			attributeDefinitions.add(typeAttributeDefinition);
			attributeDefinitions.add(statusAttributeDefinition);
			attributeDefinitions.add(requestIdAttributeDefinition);
			attributeDefinitions.add(mirisDocIdAttributeDefinition);
			attributeDefinitions.add(batchStatusAttributeDefinition);
			attributeDefinitions.add(insertedDateAttributeDefinition);
			// Create the table
			CreateTableRequest createTableRequest = CreateTableRequest.builder()
				.tableName("Aws-table")
				.keySchema(keySchemaElementsAwsTable)
				.attributeDefinitions(attributeDefinitions)
				.globalSecondaryIndexes(globalSecondaryIndices)
				.billingMode(BillingMode.PAY_PER_REQUEST)
				.build();
			CreateTableResponse response = dynamoDbClient.createTable(createTableRequest);
			log.info("CreateTableResponse: " + response.toString());
			DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName("Aws-table").build();
			// Wait until the Amazon DynamoDB table is created.
			WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
			log.info("DescribeTableResponse: " + waiterResponse.toString());
		}
		catch (URISyntaxException e) {
			log.error("URISyntaxException: " + e.getMessage());
		}
	}

	public static void prepareS3() {
		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointS3());
			S3Client s3client = S3Client.builder().endpointOverride(localstackEndpoint).region(REGION).build();
			// Create Bucket unit-test-bucket
			CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(UNIT_TEST_BUCKET).build();
			s3client.createBucket(createBucketRequest);
		}
		catch (URISyntaxException e) {
			log.error("URISyntaxException: " + e.getMessage());
		}
	}

	public static void deleteAllFiles() {

		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointS3());
			S3Client s3client = S3Client.builder().endpointOverride(localstackEndpoint).region(REGION).build();
			boolean isDone = false;

			while (!isDone) {

				ListObjectsRequest req = ListObjectsRequest.builder().bucket(UNIT_TEST_BUCKET).build();
				ListObjectsResponse resp = s3client.listObjects(req);

				for (S3Object s3Object : resp.contents()) {
					s3client.deleteObject(
							DeleteObjectRequest.builder().bucket(UNIT_TEST_BUCKET).key(s3Object.key()).build());
				}

				isDone = !resp.isTruncated();
			}
		}
		catch (URISyntaxException e) {
			log.error("URISyntaxException: " + e.getMessage());
		}
	}

	public static void populateDynamoDB() {
		populateDynamoDB("Aws-table-batch-write-items.json");
	}

	public static void populateDynamoDB(String testData) {
		String testdataLocation = "classpath:dynamodb/items/" + testData;
		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointDynamoDB());
			DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
				.endpointOverride(localstackEndpoint)
				.region(REGION)
				.build();
			// Read file from test resources folder and create request items
			ObjectMapper objectMapper = new ObjectMapper();
			// Read file with DynamoDB table items
			String json = Files.readString(ResourceUtils.getFile(testdataLocation).toPath());
			// Write the data to DynamoDB
			BatchWriteItemRequest batchWriteItemRequest = objectMapper
				.readValue(json, BatchWriteItemRequest.serializableBuilderClass())
				.build();
			dynamoDbClient.batchWriteItem(batchWriteItemRequest);
		}
		catch (Exception e) {
			log.error("URISyntaxException: " + e.getMessage());
		}
	}

	public static void prepareSSMParamStore() {
		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointSSM());
			SsmClient ssmClient = SsmClient.builder().endpointOverride(localstackEndpoint).region(REGION).build();

			PutParameterRequest putParamRequest = PutParameterRequest.builder()
				.name(AppConstants.JWT_SECRET_KEY)
				.value("dummy_token")
				.type(ParameterType.STRING)
				.build();
			ssmClient.putParameter(putParamRequest);
		}
		catch (URISyntaxException e) {
			log.error("URISyntaxException: " + e.getMessage());
		}
	}

	private static GlobalSecondaryIndex createGlobalSecondaryIndex(String indexName, String partitionKeyName,
			String sortKeyName) {
		Collection<KeySchemaElement> keySchemaElementsGsiArtefact1 = new ArrayList<>();
		KeySchemaElement partitionKey = KeySchemaElement.builder()
			.attributeName(partitionKeyName)
			.keyType(KeyType.HASH)
			.build();
		KeySchemaElement sortKey = KeySchemaElement.builder().attributeName(sortKeyName).keyType(KeyType.RANGE).build();
		keySchemaElementsGsiArtefact1.add(partitionKey);
		keySchemaElementsGsiArtefact1.add(sortKey);
		return GlobalSecondaryIndex.builder()
			.indexName(indexName)
			.keySchema(keySchemaElementsGsiArtefact1)
			.projection(Projection.builder().projectionType(ProjectionType.ALL).build())
			.build();
	}

	public static void putObject(String objectKey, Map<String, String> metadata) {
		URI localstackEndpoint;
		try {
			localstackEndpoint = new URI(Localstack.INSTANCE.getEndpointS3());
			S3Client s3Client = S3Client.builder().endpointOverride(localstackEndpoint).region(REGION).build();
			InputStream inputStream = new FileInputStream(
					ResourceUtils.getFile("classpath:files/merged_artefact_documents.pdf"));

			byte[] data = IoUtils.toByteArray(inputStream);
			PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
				.bucket(UNIT_TEST_BUCKET)
				.key(objectKey)
				.contentType(MediaType.APPLICATION_PDF_VALUE)
				.metadata(metadata)
				.contentLength((long) data.length)
				.build(), RequestBody.fromBytes(data));
			log.info(putObjectResponse.eTag());
		}
		catch (Exception e) {
			log.error("Exception while uploading files to S3", e);
		}
	}

}
