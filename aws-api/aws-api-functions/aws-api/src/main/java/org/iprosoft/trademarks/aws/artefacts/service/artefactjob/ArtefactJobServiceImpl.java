package org.iprosoft.trademarks.aws.artefacts.service.artefactjob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbHelper;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbPartiQ;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.model.entity.IArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToArtefactJobMapper;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtefactJobServiceImpl implements ArtefactJobService, DbKeys {

	private final DynamoDbClient dynamoDbClient;

	private Map<String, AttributeValue> save(final Map<String, AttributeValue> values) {
		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.item(values)
			.build();
		Map<String, AttributeValue> output = dynamoDbClient.putItem(put).attributes();
		return output;
	}

	@Override
	public void saveJob(final ArtefactJob job) {
		Map<String, AttributeValue> keys = keysDocument(job.getId());
		saveJobWithValues(keys, job, false, null);
	}

	public Map<String, AttributeValue> createJob(final ArtefactJob job) {
		Map<String, AttributeValue> keys = keysDocument(job.getId());
		return createJobWithValues(keys, job, false, null);
	}

	@Override
	public void saveJobAtomic(Collection<ArtefactJob> jobs) {
		saveArtefactsWithTransactions(jobs);
	}

	private WriteRequest createWriteRequestItem(final Map<String, AttributeValue> values) {
		PutRequest put = PutRequest.builder().item(values).build();
		return WriteRequest.builder().putRequest(put).build();
	}

	public void saveArtefactsWithTransactions(Collection<ArtefactJob> artefactJobs) {
		final int BATCH_SIZE = 25;
		List<List<ArtefactJob>> batches = splitIntoBatches(artefactJobs, BATCH_SIZE);
		List<WriteRequest> allUnprocessedItems = new ArrayList<>();

		for (List<ArtefactJob> batch : batches) {
			List<WriteRequest> writeRequests = batch.stream()
				.map(item -> createWriteRequestItem(createJob(item)))
				.collect(Collectors.toList());

			Map<String, List<WriteRequest>> requestItems = new HashMap<>();
			requestItems.put(SystemEnvironmentVariables.REGISTRY_TABLE_NAME, writeRequests);

			BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
				.requestItems(requestItems)
				.build();

			try {
				BatchWriteItemResponse response;
				do {
					response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);

					Map<String, List<WriteRequest>> unprocessedItems = response.unprocessedItems();

					if (!unprocessedItems.isEmpty()) {
						log.info("There were unprocessed items in the batch. Retrying...");
						List<WriteRequest> unprocessedWriteRequests = unprocessedItems
							.get(SystemEnvironmentVariables.REGISTRY_TABLE_NAME);
						allUnprocessedItems.addAll(unprocessedWriteRequests);
						requestItems = new HashMap<>();
						requestItems.put(SystemEnvironmentVariables.REGISTRY_TABLE_NAME, unprocessedWriteRequests);
						batchWriteItemRequest = BatchWriteItemRequest.builder().requestItems(requestItems).build();
					}
					else {
						log.info("Batch write succeeded");
						break;
					}
				}
				while (true);
			}
			catch (DynamoDbException e) {
				log.error("DynamoDbException: {}", e.getMessage());
			}
		}

		// Process any remaining unprocessed items
		if (!allUnprocessedItems.isEmpty()) {
			log.info("Processing remaining unprocessed items...");
			processUnprocessedItems(allUnprocessedItems);
		}
	}

	private List<List<ArtefactJob>> splitIntoBatches(Collection<ArtefactJob> jobs, int batchSize) {
		List<List<ArtefactJob>> batches = new ArrayList<>();
		List<ArtefactJob> currentBatch = new ArrayList<>();

		for (ArtefactJob job : jobs) {
			if (currentBatch.size() == batchSize) {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
			}
			currentBatch.add(job);
		}

		if (!currentBatch.isEmpty()) {
			batches.add(currentBatch);
		}

		return batches;
	}

	private void processUnprocessedItems(List<WriteRequest> unprocessedItems) {
		Map<String, List<WriteRequest>> requestItems = new HashMap<>();
		requestItems.put(SystemEnvironmentVariables.REGISTRY_TABLE_NAME, unprocessedItems);

		BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
			.requestItems(requestItems)
			.build();

		try {
			BatchWriteItemResponse response;
			do {
				response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);

				Map<String, List<WriteRequest>> remainingUnprocessedItems = response.unprocessedItems();

				if (!remainingUnprocessedItems.isEmpty()) {
					log.info("There are still unprocessed items. Retrying...");
					requestItems = remainingUnprocessedItems;
					batchWriteItemRequest = BatchWriteItemRequest.builder().requestItems(requestItems).build();
				}
				else {
					log.info("All remaining unprocessed items have been processed");
					break;
				}
			}
			while (true);
		}
		catch (DynamoDbException e) {
			log.error("DynamoDbException while processing remaining unprocessed items: {}", e.getMessage());
		}
	}

	@Override
	public Map<String, AttributeValue> keysDocument(String jobId) {
		return keysDocument(jobId, Optional.empty());
	}

	@Override
	public Map<String, AttributeValue> keysDocument(String jobId, Optional<String> childdocument) {
		return childdocument.isPresent()
				? keysGeneric(PREFIX_JOBS + jobId, "job" + TAG_DELIMINATOR + childdocument.get())
				: keysGeneric(PREFIX_JOBS + jobId, "job");
	}

	private void saveJobWithValues(final Map<String, AttributeValue> keys, final ArtefactJob job,
			final boolean saveGsi1, final String timeToLive) {
		// TODO save Document/Tags inside transaction.
		saveJobWithKeys(keys, job, saveGsi1, timeToLive);
	}

	private Map<String, AttributeValue> createJobWithValues(final Map<String, AttributeValue> keys,
			final ArtefactJob job, final boolean saveGsi1, final String timeToLive) {
		// TODO save Document/Tags inside transaction.
		return createJobWithKeys(keys, job, saveGsi1, timeToLive);
	}

	private void saveJobWithKeys(final Map<String, AttributeValue> keys, final ArtefactJob job, final boolean saveGsi1,
			final String timeToLive) {
		ZonedDateTime insertedDate = job.getCreationDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();
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
		addS(pkvalues, "requestId", job.getRequestId());

		addS(pkvalues, "type", "job");

		if (job.getFilename() != null) {
			addS(pkvalues, "filename", job.getFilename());
		}

		if (job.getPath() != null) {
			addS(pkvalues, "path", job.getPath());
		}

		if (fullDate != null) {
			addS(pkvalues, "insertedDate", fullDate);
		}

		if (timeToLive != null) {
			addN(pkvalues, "TimeToLive", timeToLive);
		}

		if (batchSequence != null && !batchSequence.isBlank()) {
			addS(pkvalues, "batchSequence", batchSequence);
		}

		// Persistence
		save(pkvalues);
	}

	private Map<String, AttributeValue> createJobWithKeys(final Map<String, AttributeValue> keys, final ArtefactJob job,
			final boolean saveGsi1, final String timeToLive) {
		ZonedDateTime insertedDate = job.getCreationDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();
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
		addS(pkvalues, "requestId", job.getRequestId());

		addS(pkvalues, "type", "job");

		if (job.getFilename() != null) {
			addS(pkvalues, "filename", job.getFilename());
		}

		if (job.getPath() != null) {
			addS(pkvalues, "path", job.getPath());
		}

		if (fullDate != null) {
			addS(pkvalues, "insertedDate", fullDate);
		}

		if (timeToLive != null) {
			addN(pkvalues, "TimeToLive", timeToLive);
		}

		if (batchSequence != null && !batchSequence.isBlank()) {
			addS(pkvalues, "batchSequence", batchSequence);
		}

		return pkvalues;
	}

	private void saveDocumentDate(final IArtefact document) {
		ZonedDateTime insertedDate = document.getInsertedDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		Map<String, AttributeValue> values = Map.of(PK, AttributeValue.builder().s(PREFIX_DOCUMENT_DATE).build(), SK,
				AttributeValue.builder().s(shortDate).build());
		String conditionExpression = "attribute_not_exists(" + PK + ")";
		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.conditionExpression(conditionExpression)
			.item(values)
			.build();

		try {
			dynamoDbClient.putItem(put).attributes();
		}
		catch (ConditionalCheckFailedException e) {
			// Conditional Check Fails on second insert attempt
		}
	}

	@Override
	public void updateJob(final ArtefactJob job) {

	}

	@Override
	public UpdateItemResponse updateJobWithStatus(String jobId, String statusValue) {

		log.warn("UpdateItemResponse: jobId {} and status {}", jobId, statusValue);

		// fetch with jobId (make sure it exists)
		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(jobId))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();

		Map<String, AttributeValue> result = dynamoDbClient.getItem(r).item();
		log.warn("dynamoDbClient.getItem(r).item() result {}", result);

		UpdateItemResponse response = DynamoDbHelper.updateTableItem(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, keysDocument(jobId), "jobStatus", statusValue);
		log.warn("Not a real warning,  jobStatus set to statusValue {}", statusValue);
		log.warn("UpdateItemResponse response {}", response);
		return response;
	}

	@Override
	public ArtefactJob getJobStatus(String jobId) {

		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(jobId))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();

		Map<String, AttributeValue> result = dynamoDbClient.getItem(r).item();

		if (!result.isEmpty()) {
			return new AttributeValueToArtefactJobMapper().apply(result);
		}
		return null;
	}

	@Override
	public Map<String, Object> getAllJobStatusByRequestId(String requestId) {
		List<Map<String, Object>> jobStatusMapList = new ArrayList<>();
		List<String> jobStatusList = new ArrayList<>();
		String batchSequence = null;
		List<ArtefactJob> artefactJobs = DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, requestId);
		if (artefactJobs == null || artefactJobs.isEmpty()) {
			return null;
		}
		log.info("All artefactJobs {}", artefactJobs);
		for (ArtefactJob job : artefactJobs) {
			log.info("Found Job {}", job);
			if (batchSequence == null) {
				batchSequence = job.getBatchSequence();
				log.info("batchSequence : {} ", batchSequence);
			}
			Map<String, Object> jobStatusMap = new HashMap<>();
			jobStatusMap.put("jobId", job.getId());
			jobStatusMap.put("jobStatus", job.getStatus());
			jobStatusMap.put("artefactId", job.getArtefactId());
			jobStatusMapList.add(jobStatusMap);
			jobStatusList.add(job.getStatus());
		}
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("requestId", requestId);
		responseMap.put("batchSequence", batchSequence);
		responseMap.put("jobs", jobStatusMapList);
		responseMap.put("batchStatus", getOverallJobStatus(jobStatusList));
		responseMap.forEach((key, value) -> log
			.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
		return responseMap;
	}

	@Override
	public Map<String, Object> getAllBatchStatusByRequestId(String requestId) {
		return null;
	}

	@Override
	public Map<String, Object> getAllBulkJobStatusByRequestId(String requestId) {
		List<Map<String, Object>> jobStatusMapList = new ArrayList<>();
		List<String> jobStatusList = new ArrayList<>();
		List<ArtefactJob> artefactJobs = DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, requestId);
		if (artefactJobs == null || artefactJobs.isEmpty()) {
			return null;
		}
		log.info("All artefactJobs {}", artefactJobs);
		for (ArtefactJob job : artefactJobs) {
			log.info("Found Job {}", job);
			Map<String, Object> jobStatusMap = new HashMap<>();
			jobStatusMap.put("jobId", job.getId());
			jobStatusMap.put("jobStatus", job.getStatus());
			jobStatusMap.put("artefactId", job.getArtefactId());
			jobStatusMapList.add(jobStatusMap);
			jobStatusList.add(job.getStatus());
		}
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("requestId", requestId);
		responseMap.put("jobs", jobStatusMapList);
		responseMap.forEach((key, value) -> log
			.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
		return responseMap;
	}

	@Override
	public List<ArtefactJob> getAllJobs(String date, String status) {
		Map<String, String> conditions = new HashMap<>();
		conditions.put("date", date);
		conditions.put("status", status);
		return DynamoDbPartiQ.getAllJobs(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME, conditions);
	}

	private String getOverallJobStatus(List<String> jobStatusList) {
		log.info("getOverallJobStatus(List<String> jobStatusList) with param jobStatusList {}", jobStatusList);
		if (jobStatusList == null) {
			return ArtefactStatus.INIT.getStatus();
		}
		Map<String, Long> statusCountMap = jobStatusList.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		log.info("After collection of all non null in statusCountMap {}", statusCountMap);
		if (statusCountMap.containsKey(ArtefactStatus.DELETED.getStatus())) {
			return ArtefactStatus.DELETED.getStatus();
		}
		else if (statusCountMap.containsKey(ArtefactStatus.INIT.getStatus())) {
			return ArtefactStatus.INIT.getStatus();
		}
		else if (statusCountMap.size() == 1) {
			return statusCountMap.keySet().stream().findFirst().orElse(ArtefactStatus.INIT.getStatus());
		}
		else {
			return ArtefactStatus.INIT.getStatus();
		}
	}

}