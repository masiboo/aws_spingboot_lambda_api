package artefact.aws.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.entity.ArtefactJob;
import artefact.entity.IArtefact;
import artefact.mapper.AttributeValueToArtefactJobMapper;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.util.AppConstants;
import artefact.util.ArtefactStatus;
import artefact.util.DateUtils;
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

import static artefact.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;

/**
 * Implementation of the {@link ArtefactJobServiceInterface}.
 */
public class ArtefactJobServiceImpl implements ArtefactJobServiceInterface, DbKeys {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactJobServiceImpl.class);

    /**
     * {@link DynamoDbClient}.
     */
    private final DynamoDbClient dynamoDB;
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
    public ArtefactJobServiceImpl(final DynamoDbConnectionBuilder builder, final String documentsTable) {
        if (documentsTable == null) {
            throw new IllegalArgumentException("Table name is null");
        }

        this.dynamoDB = builder.build();
        this.documentTableName = documentsTable;

        this.yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.df = new SimpleDateFormat(AppConstants.DATETIME_FORMAT);

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

    @Override
    public void saveJob(final ArtefactJob job) {
        Map<String, AttributeValue> keys = keysDocument(job.getId());

        saveJobWithValues(keys, job, false, null);

    }

    @Override
    public Map<String, AttributeValue> keysDocument(String jobId) {
        return keysDocument(jobId, Optional.empty());
    }

    @Override
    public Map<String, AttributeValue> keysDocument( String jobId,
                                                      Optional<String> childdocument) {
        return childdocument.isPresent()
                ? keysGeneric(PREFIX_JOBS + jobId,
                "job" + TAG_DELIMINATOR + childdocument.get())
                : keysGeneric(PREFIX_JOBS + jobId, "job");
    }

    private void saveJobWithValues(final Map<String, AttributeValue> keys, final ArtefactJob job,
                                   final boolean saveGsi1, final String timeToLive) {
// TODO save Document/Tags inside transaction.
        saveJobWithKeys(keys, job, saveGsi1, timeToLive);
    }

    private void saveJobWithKeys(final Map<String, AttributeValue> keys,
                                 final ArtefactJob job, final boolean saveGsi1, final String timeToLive) {

        ZonedDateTime insertedDate = job.getCreationDate();
        String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentDateShortStr();
        String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentDatetimeUtcStr();
        String batchSequence = job.getBatchSequence();

        Map<String, AttributeValue> pkvalues = new HashMap<>(keys);

        if (saveGsi1) {
            addS(pkvalues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
            addS(pkvalues, GSI1_SK, fullDate + TAG_DELIMINATOR + job.getId());
        }


        addS(pkvalues, "artefactId", job.getArtefactId());
        addS(pkvalues, "jobId", job.getId());
        addS(pkvalues, "jobStatus", job.getStatus());
        addS(pkvalues, "s3_signed_url", job.getS3SignedUrl());
        addS(pkvalues, "updatedDate", shortDate);
        addS(pkvalues,"requestId",job.getRequestId());

        addS(pkvalues, "type", "job");

        if (job.getFilename() != null){
            addS(pkvalues, "filename", job.getFilename());
        }

        if ( job.getPath() != null) {
            addS(pkvalues, "path",  job.getPath());
        }

        if (fullDate != null) {
            addS(pkvalues, "insertedDate", fullDate);
        }

        if (timeToLive != null) {
            addN(pkvalues, "TimeToLive", timeToLive);
        }

        if(batchSequence != null && !batchSequence.isBlank()){
            addS(pkvalues,"batchSequence", batchSequence);
        }

        // Persistence
        save(pkvalues);
    }

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

    @Override
    public void updateJob(final ArtefactJob job) {

    }

    @Override
    public void updateJobWithStatus(String jobId, String statusValue) {

        // fetch with jobId (make sure it exists)
        GetItemRequest r = GetItemRequest.builder().key(keysDocument(jobId))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();

        DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysDocument(jobId),  "jobStatus", statusValue);

    }

    @Override
    public ArtefactJob getJobStatus(String jobId) {

        GetItemRequest r = GetItemRequest.builder().key(keysDocument(jobId))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();

        if (!result.isEmpty()) {
            ArtefactJob item = new AttributeValueToArtefactJobMapper().apply(result);
            return item;
        }

        return null;
    }

    /**
     *  To Pull all the job status report by requestId after you do bulk upload
     *  refer MPD-270 for more details
     * @param requestId
     * @return
     */
    @Override
    public Map<String,Object> getAllJobStatusByRequestId(String requestId) {
        List<Map<String,Object>> jobStatusMapList = new ArrayList<>();
        List<String> jobStatusList = new ArrayList<>();
        String batchSequence = null;

        List<ArtefactJob> artefactJobs = DynamoDbPartiQ.getAllArtefactJobsByRequestId(this.dynamoDB,this.documentTableName,requestId);

        if(artefactJobs == null || artefactJobs.isEmpty()){
            return null;
        }

        for(ArtefactJob job : artefactJobs){
            if(batchSequence == null){
                batchSequence = job.getBatchSequence();
                logger.info("batchSequence : {} ",batchSequence);
            }
            Map<String,Object> jobStatusMap = new HashMap<>();
            jobStatusMap.put("jobId",job.getId());
            jobStatusMap.put("jobStatus",job.getStatus());
            jobStatusMap.put("artefactId",job.getArtefactId());
            jobStatusMapList.add(jobStatusMap);
            jobStatusList.add(job.getStatus());
        }
        Map<String,Object> responseMap = new LinkedHashMap<>();
        responseMap.put("requestId",requestId);
        responseMap.put("batchSequence",batchSequence);
        responseMap.put("jobs",jobStatusMapList);
        responseMap.put("batchStatus", getOverallJobStatus(jobStatusList));
        responseMap.forEach((key, value) -> logger.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
        return responseMap;
    }

    @Override
    public List<ArtefactJob> getAllJobs(String date, String status) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("date", date);
        conditions.put("status", status);
        return DynamoDbPartiQ.getAllJobs(this.dynamoDB,this.documentTableName, conditions);
    }

    private String getOverallJobStatus(List<String> jobStatusList) {
        Map<String, Long> statusCountMap = jobStatusList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (statusCountMap.containsKey(ArtefactStatus.DELETED.getStatus()))
            return ArtefactStatus.DELETED.getStatus();

        if (statusCountMap.containsKey(ArtefactStatus.INIT.getStatus()))
            return ArtefactStatus.INIT.getStatus();

        if (statusCountMap.size() == 1)
            return statusCountMap.keySet().stream().findFirst().get();

        return ArtefactStatus.INIT.getStatus();
    }


}
