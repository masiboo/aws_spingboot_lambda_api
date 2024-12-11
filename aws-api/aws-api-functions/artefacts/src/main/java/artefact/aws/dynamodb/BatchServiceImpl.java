package artefact.aws.dynamodb;

import artefact.mapper.AttributeValueDB2ToArtefactMapper;
import artefact.mapper.AttributeValueDB2ToBatchMapper;
import artefact.mapper.AttributeValueToBatchMapper;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.RequestLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.dto.input.BatchInputDynamoDb;
import artefact.dto.output.ArtefactOutput;
import artefact.dto.output.BatchOutput;
import artefact.entity.IArtefact;
import artefact.usecase.BatchServiceInterface;
import artefact.util.BatchStatus;
import artefact.util.DateUtils;
import artefact.util.ScannedAppType;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static artefact.aws.dynamodb.DDBHelper.buildAttrValueUpdate;
import static artefact.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;
import static artefact.util.AppConstants.DATETIME_FORMAT;
import static artefact.util.AppConstants.KEY_LOCKED;


/**
 * Implementation of the {@link BatchServiceInterface}.
 */
public class BatchServiceImpl implements BatchServiceInterface, DbKeys {

    private static final Logger logger = LoggerFactory.getLogger(BatchServiceImpl.class);

    /**
     * {@link DynamoDbClient}.
     */
    private final DynamoDbClient dynamoDB;

    private final  AmazonDynamoDB dynamoDB2;
    /**
     * {@link DateTimeFormatter}.
     */
    private final DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * {@link SimpleDateFormat} YYYY-mm-dd format.
     */
    private final SimpleDateFormat yyyymmddFormat;
    /**
     * {@link SimpleDateFormat} in ISO Standard format.
     */
    private final SimpleDateFormat df;
    /**
     * Documents Table Name.
     */
    private final String documentTableName;

    /**
     * constructor.
     *
     * @param builder        {@link DynamoDbConnectionBuilder}
     * @param documentsTable {@link String}
     */
    public BatchServiceImpl(final DynamoDbConnectionBuilder builder, final String documentsTable) {
        if (documentsTable == null) {
            throw new IllegalArgumentException("Table name is null");
        }

        this.dynamoDB = builder.build();
        this.documentTableName = documentsTable;

        this.dynamoDB2 = AmazonDynamoDBClientBuilder.standard().build();

        this.yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.df = new SimpleDateFormat(DATETIME_FORMAT);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        this.yyyymmddFormat.setTimeZone(tz);
        this.df.setTimeZone(tz);
    }


    /**
     * Save Record to DB.
     *
     * @param values {@link Map} {@link AttributeValue}
     * @return {@link Map} {@link AttributeValue}
     */
    private Map<String, AttributeValue> save(final Map<String, AttributeValue> values) {

        PutItemRequest put =
                PutItemRequest.builder().tableName(this.documentTableName).item(values).build();
        Map<String, AttributeValue> output = this.dynamoDB.putItem(put).attributes();
        return output;
    }

    /**
     * @param batch
     */
    @Override
    public void updateBatchSequence(BatchInputDynamoDb batch) {

        logger.info("batch with children: a");
        Map<String, AttributeValue> keysWithChild = keysBatchWithArtefact(batch.getBatchSequence(), batch.getArtefactId());
        saveBatchWithValues(keysWithChild, batch, false, null);

        // workaround for single batch SK
        logger.info("single batch: a");
        Map<String, AttributeValue> keysNoChild = keysBatchWithArtefact(batch.getBatchSequence(), batch.getArtefactId());
        saveBatchWithValues(keysNoChild, batch, true, null);

    }

    @Override
    public Map<String, AttributeValue> keysDocument(String batchSequence) {
        return keysDocument(batchSequence, Optional.empty());
    }

    public Map<String, AttributeValue> keysBatchWithArtefact(String batchSequence, String artefactId) {
        return keysDocument(batchSequence, Optional.ofNullable(artefactId));
    }

    @Override
    public Map<String, AttributeValue> keysDocument( String batchSequence,
                                                      Optional<String> childdocument) {
        return childdocument.isPresent()
                ? keysGeneric(PREFIX_BATCH + batchSequence,
                PREFIX_DOCS + childdocument.get())
                : keysGeneric(PREFIX_BATCH + batchSequence, "batch");
    }

    public Map<String, AttributeValue> keysBatchSearchPK( String batchSequence) {

        return keysGeneric(PREFIX_BATCH + batchSequence, null);
    }

    private void saveBatchWithValues(final Map<String, AttributeValue> keys, final BatchInputDynamoDb batch,
                                     final boolean saveGsi1, final String timeToLive) {
        // TODO save Document/Tags inside transaction.
        saveBatchWithKeys(keys, batch, saveGsi1, timeToLive);
    }

    private void saveBatchWithKeys(final Map<String, AttributeValue> keys,
                                   final BatchInputDynamoDb batch, final boolean saveGsi1, final String timeToLive) {

        ZonedDateTime insertedDate = batch.getCreationDate();
        String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentDateShortStr();
        String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentDatetimeUtcStr();
        String batchSequence = batch.getBatchSequence();
        Map<String, AttributeValue> pkvalues = new HashMap<>(keys);
        addS(pkvalues, "batchSequence", batchSequence);

        if (batch.getArtefactId() != null){
            addS(pkvalues, "artefactId", batch.getArtefactId());
        }


        if (saveGsi1) {
            addS(pkvalues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
            addS(pkvalues, GSI1_SK, fullDate + TAG_DELIMINATOR + batch.getBatchSequence());

            addS(pkvalues, "batchStatus", batch.getStatus());
            addS(pkvalues,"requestId",batch.getRequestId());
            if (batch.getOperator() != null){
                addS(pkvalues, "operator", batch.getOperator().getUsername());
            }

            if (batch.getRequestType() != null){
                addS(pkvalues, "requestType", batch.getRequestType());
            }

            if (batch.getScannedType() != null) {
                addEnum(pkvalues, "scanType", batch.getScannedType().toString());
            }

//            if (batch.getUnmergedArtefacts() != null) {
//                addStringSet(pkvalues, "unmergedArtefacts", batch.getUnmergedArtefacts());
//            }
//
//            if (batch.getJobs() != null) {
//                addStringSet(pkvalues, "jobs", batch.getJobs());
//            }

            addS(pkvalues, "type", "batch");
        }


        // Persistence
        logger.info("write pk values: " + pkvalues);
        save(pkvalues);
    }

//    private void saveBatch(final Map<String, AttributeValue> keys,
//                                   final BatchInputDynamoDb batch, final boolean saveGsi1, final String timeToLive) {
//
//        Date insertedDate = batch.getCreationDate();
//        String shortdate = insertedDate != null ? this.yyyymmddFormat.format(insertedDate) : null;
//        String fulldate = insertedDate != null ? this.df.format(insertedDate) : null;
//
//        String batchSequence = batch.getBatchSequence();
//        Map<String, AttributeValue> pkvalues = new HashMap<>(keys);
//        addS(pkvalues, "batchSequence", batchSequence);
//
////        if (batch.getArtefactId() != null){
////            addS(pkvalues, "artefactId", batch.getArtefactId());
////        }
//
//        if (batch.getScannedType() != null) {
//            addEnum(pkvalues, "scanType", batch.getScannedType().toString());
//        }
//
//        if (batch.getUnmergedArtefacts() != null) {
//            addStringSet(pkvalues, "unmergedArtefacts", batch.getUnmergedArtefacts());
//        }
//
//        if (batch.getJobs() != null) {
//            addStringSet(pkvalues, "jobs", batch.getJobs());
//        }
//
//
//        if (saveGsi1) {
//            addS(pkvalues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortdate));
//            addS(pkvalues, GSI1_SK, fulldate + TAG_DELIMINATOR + batch.getBatchSequence());
//        }
//
//        addS(pkvalues, "batchStatus", batch.getStatus());
//        addS(pkvalues,"requestId",batch.getRequestId());
//        if (batch.getOperator() != null){
//                addS(pkvalues, "operator", batch.getOperator().getUsername());
//        }
//
//        if (batch.getRequestType() != null){
//            addS(pkvalues, "requestType", batch.getRequestType());
//        }
//
//            addS(pkvalues, "type", "batch");
//        }
//
//
//        // Persistence
//        logger.info("write pk values: " + pkvalues);
//        save(pkvalues);
//    }

    private void saveDocumentDate(final IArtefact document) {
        ZonedDateTime insertedDate = document.getInsertedDate();
        String shortDate = DateUtils.getShortDateFromZonedDateTime(insertedDate);

        Map<String, AttributeValue> values =
                Map.of(PK, AttributeValue.builder().s(PREFIX_DOCUMENT_DATE).build(), SK,
                        AttributeValue.builder().s(shortDate).build());
        String conditionExpression = "attribute_not_exists(" + PK + ")";
        PutItemRequest put = PutItemRequest.builder().tableName(this.documentTableName)
                .conditionExpression(conditionExpression).item(values).build();

        try {
            this.dynamoDB.putItem(put).attributes();
        } catch (ConditionalCheckFailedException e) {
            // Conditional Check Fails on second insert attempt
        }
    }

    private String getOverallBatchStatus(List<String> jobStatusList) {
        Map<String, Long> statusCountMap = jobStatusList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (statusCountMap.containsKey("DELETED"))
            return "DELETED";

        if (statusCountMap.containsKey("INIT"))
            return "INIT";

        if (statusCountMap.size() == 1)
            return statusCountMap.keySet().stream().findFirst().get();

        return "INIT";
    }


    /**
     * @param batch
     */
    @Override
    public void saveBatchSequence(BatchInputDynamoDb batch) {
//        updateBatchSequence(batch);

        logger.info("single batch: a");
        Map<String, AttributeValue> keysNoChild = keysDocument(batch.getBatchSequence());
        saveBatchWithValues(keysNoChild, batch, true, null);


    }

    /**
     * @param batch
     */
    @Override
    public void saveBatchSequenceWithChildren(BatchInputDynamoDb batch) {

        logger.info("batch with children: a");
        Map<String, AttributeValue> keysWithChild = keysBatchWithArtefact(batch.getBatchSequence(), batch.getArtefactId());
        saveBatchWithValues(keysWithChild, batch, false, null);

    }


    /**
     * @param batchSequence
     * @param status
     */
    @Override
    public void updateBatchWithStatus(String batchSequence, String status) {
        List<ArtefactOutput> artefacts = getAllArtefactsForBatch(batchSequence, "artefact");
        if(artefacts != null && !artefacts.isEmpty()){
            artefacts.forEach(artefact -> DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysGeneric(PREFIX_DOCS + artefact.getId(), "document"),  "status", status));
        }
        DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysDocument(batchSequence),  "batchStatus", status);
    }

    /**
     * @param batchSequence
     * @return
     */
    @Override
    public BatchOutput getBatchDetail(String batchSequence) {

        GetItemRequest r = GetItemRequest.builder()
                .key(keysDocument(batchSequence))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();

        if (!result.isEmpty()) {
            BatchOutput item = new AttributeValueToBatchMapper().apply(result);
            return item;
        }

        return null;
    }

    /**
     * @param status 
     * @return
     */


    /**
     * @return 
     */
    @Override
    public List<BatchOutput> getAllArtefactsForBatch() {
        return null;
    }

    @Override
    public List<ArtefactOutput> getAllArtefactsForBatch(String batchSequenceId, String secondary) {

        try {
            // Create QueryRequest
            QueryRequest queryRequest = createQueryRequest(documentTableName, PREFIX_BATCH + batchSequenceId, secondary);
            QueryResult queryResult = dynamoDB2.query(queryRequest);
            logger.info(" getAllArtefactsForBatch Query successful.");
            logger.info(queryResult.toString());
            // Handle queryResult

            List<ArtefactOutput> out =  queryResult.getItems().stream()
                    .map(m -> new AttributeValueDB2ToArtefactMapper().apply(m)).collect(Collectors.toList());

            return out;
        } catch (Exception e) {
            handleQueryErrors(e);
        }

        return null;
    }

    /**
     * @param status
     * @return
     */
    @Override
    public List<BatchOutput> getAllBatchByStatus(String status) {
//        List<BatchOutput> out = getAllBatchByStatusWithFilter(status, ScannedAppType.NEW_REQUEST);
//        if (out != null) return out
        return getAllInsertedBatch();

    }

    private List<BatchOutput> getAllBatchByStatusWithFilter(String status, ScannedAppType type) {
        try {
            // Create QueryRequest
            QueryRequest queryRequest = createQueryRequestWithIndex(this.documentTableName, status);
            QueryResult queryResult = dynamoDB2.query(queryRequest);
            logger.info("Batches queryResult response {}", queryResult);


            // Handle queryResult
            List<BatchOutput> out =  queryResult.getItems().stream()
                    .map(m -> new AttributeValueDB2ToBatchMapper().apply(m)).toList();

            logger.info("BatchOutput BatchOutput response {}", out.get(0));

            // return only Addendum items, not reflected in API contract
            return out.stream().filter(item -> item.getScanType().equals(type))
                    .toList();

        } catch (Exception e) {

            handleQueryErrors(e);
        }
        return null;
    }

    private List<BatchOutput> getAllInsertedBatch() {

        List<BatchOutput> out = new ArrayList<>();

        try {

            // Create instance and execute query
            DynamoDBPartiQLQuery query = new DynamoDBPartiQLQuery(this.dynamoDB);
            System.out.println("\nStarting paginated query...");
            List<Map<String, AttributeValue>> allItems = query.executePartiQLQueryWithPagination(System.getenv("REGISTRY_TABLE_NAME"), 50);

            // Process results
            if (!allItems.isEmpty()) {
                System.out.println("\nProcessing retrieved items...");
                out =  allItems.stream()
                        .map(m -> new AttributeValueToBatchMapper().apply(m)).toList();

            } else {
                System.out.println("No items to process.");
            }

        } catch (Exception e) {
            System.out.println("No items to process.");

        }

        return out;
    }

    @Override
    public void updateLockState(String batchSeq, boolean isLocked) {
        Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(KEY_LOCKED,buildAttrValueUpdate(AttributeValue.builder().bool(isLocked).build()
                ,software.amazon.awssdk.services.dynamodb.model.AttributeAction.PUT.PUT));
        DDBHelper.updateAttributes(this.dynamoDB, this.documentTableName, keysDocument(batchSeq),updatedValues);
    }

    @Override
    public String findStatusByRequestType(String batchSeq) {
        BatchOutput batchOutput = getBatchDetail(batchSeq);
        if (ScannedAppType.ADDENDUM.name().equalsIgnoreCase(batchOutput.getRequestType())) {
            return BatchStatus.INDEXED.getStatus();
        }
        else {
            return BatchStatus.INSERTED.getStatus();
        }
    }

    @Override
    public void updateStatus(String batchSeq, String status) {
        DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysDocument(batchSeq), "batchStatus", status);
    }

    private  AmazonDynamoDB createDynamoDb2Client(String region) {
        return AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
    }

    private  QueryRequest createQueryRequest(String tablename, String primary, String secondary) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTableName(tablename);
        String keyConditionExpression = "#cd420 = :cd420 And begins_with(#cd421, :cd421)";
        queryRequest.setKeyConditionExpression(keyConditionExpression);
        queryRequest.setConsistentRead(false);
        queryRequest.setScanIndexForward(true);
        queryRequest.setExpressionAttributeNames(getExpressionAttributeNames());
        queryRequest.setExpressionAttributeValues(getExpressionAttributeValues(primary, secondary));
        return queryRequest;
    }

    private  Map<String, String> getExpressionAttributeNames() {
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#cd420", "PK");
        expressionAttributeNames.put("#cd421", "SK");
        return expressionAttributeNames;
    }

    private  Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> getExpressionAttributeValues(String primaryAttribute, String secondaryExpression ) {
        Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> expressionAttributeValues = new HashMap<String, com.amazonaws.services.dynamodbv2.model.AttributeValue>();
        expressionAttributeValues.put(":cd420", new com.amazonaws.services.dynamodbv2.model.AttributeValue(primaryAttribute));
        expressionAttributeValues.put(":cd421", new com.amazonaws.services.dynamodbv2.model.AttributeValue(secondaryExpression));
        return expressionAttributeValues;
    }
    
    private  QueryRequest createQueryRequestWithIndex(String tableName, String status) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTableName(tableName);
        queryRequest.setIndexName("GSI-Artefact-4");
        String keyConditionExpression = "#e4150 = :e4150 And #e4151 = :e4151";
        queryRequest.setKeyConditionExpression(keyConditionExpression);
        queryRequest.setScanIndexForward(true);
        queryRequest.setExpressionAttributeNames(getExpressionAttributeNamesWithIndexKeys());
        queryRequest.setExpressionAttributeValues(getExpressionAttributeIndexValues(status));
        return queryRequest;
    }

    private  Map<String, String> getExpressionAttributeNamesWithIndexKeys() {
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#e4150", "type");
        expressionAttributeNames.put("#e4151", "batchStatus");
        return expressionAttributeNames;
    }

    private  Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> getExpressionAttributeIndexValues(String status) {
        Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> expressionAttributeValues = new HashMap<String, com.amazonaws.services.dynamodbv2.model.AttributeValue>();
        expressionAttributeValues.put(":e4150", new com.amazonaws.services.dynamodbv2.model.AttributeValue("batch"));
        expressionAttributeValues.put(":e4151", new com.amazonaws.services.dynamodbv2.model.AttributeValue(status));
        return expressionAttributeValues;
    }
    
    // Handles errors during Query execution. Use recommendations in error messages below to add error handling specific to
    // your application use-case.
    private  void handleQueryErrors(Exception exception) {
        try {
            throw exception;
        } catch (Exception e) {
            // There are no API specific errors to handle for Query, common DynamoDB API errors are handled below
            handleCommonErrors(e);
        }
    }

    private  void handleCommonErrors(Exception exception) {
        try {
            throw exception;
        } catch (InternalServerErrorException isee) {
            logger.error("Internal Server Error, generally safe to retry with exponential back-off. Error: " + isee.getErrorMessage());
        } catch (RequestLimitExceededException rlee) {
            logger.error("Throughput exceeds the current throughput limit for your account, increase account level throughput before " +
                    "retrying. Error: " + rlee.getErrorMessage());
        } catch (ProvisionedThroughputExceededException ptee) {
            logger.error("Request rate is too high. If you're using a custom retry strategy make sure to retry with exponential back-off. " +
                    "Otherwise consider reducing frequency of requests or increasing provisioned capacity for your table or secondary index. Error: " +
                    ptee.getErrorMessage());
        } catch (ResourceNotFoundException rnfe) {
            logger.error("One of the tables was not found, verify table exists before retrying. Error: " + rnfe.getErrorMessage());
        } catch (AmazonServiceException ase) {
            logger.error("An AmazonServiceException occurred, indicates that the request was correctly transmitted to the DynamoDB " +
                    "service, but for some reason, the service was not able to process it, and returned an error response instead. Investigate and " +
                    "configure retry strategy. Error type: " + ase.getErrorType() + ". Error message: " + ase.getErrorMessage());
        } catch (AmazonClientException ace) {
            logger.error("An AmazonClientException occurred, indicates that the client was unable to get a response from DynamoDB " +
                    "service, or the client was unable to parse the response from the service. Investigate and configure retry strategy. "+
                    "Error: " + ace.getMessage());
        } catch (Exception e) {
            System.out.println("An exception occurred, investigate and configure retry strategy. Error: " + e.getMessage());
        }
    }

}
