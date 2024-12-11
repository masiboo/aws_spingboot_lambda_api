package org.iprosoft.trademarks.aws.artefacts.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbHelper;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbPartiQ;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.ArtefactToArtefactOutputMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueDB2ToArtefactMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueDB2ToBatchMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToBatchOutputMapper;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;
import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.KEY_LOCKED;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchServiceImpl implements BatchService, DbKeys {

	private final DynamoDbClient dynamoDbClient;

	private final ArtefactService artefactService;

	private void save(final Map<String, AttributeValue> values) {
		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.item(values)
			.build();
		this.dynamoDbClient.putItem(put);
	}

	// @Override
	// public void updateBatchSequence(BatchInputDynamoDb batch) {
	// log.info("batch with children: a");
	// Map<String, AttributeValue> keysWithChild =
	// keysBatchWithArtefact(batch.getBatchSequence(),
	// batch.getArtefactId());
	// saveBatchWithValues(keysWithChild, batch, false, null);
	//
	// // workaround for single batch SK
	// log.info("single batch: a");
	// Map<String, AttributeValue> keysNoChild =
	// keysBatchWithArtefact(batch.getBatchSequence(),
	// batch.getArtefactId());
	// saveBatchWithValues(keysNoChild, batch, true, null);
	// }

	@Override
	public Map<String, AttributeValue> keysDocument(String batchSequence) {
		return keysDocument(batchSequence, Optional.empty());
	}

	public Map<String, AttributeValue> keysBatchWithArtefact(String batchSequence, String artefactId) {
		// 1. A
		return keysDocument(batchSequence, Optional.ofNullable(artefactId));
	}

	@Override
	public Map<String, AttributeValue> keysDocument(String batchSequence, Optional<String> childDocument) {
		// 1. B
		return childDocument.isPresent() ? keysGeneric(PREFIX_BATCH + batchSequence, PREFIX_DOCS + childDocument.get())
				: keysGeneric(PREFIX_BATCH + batchSequence, "batch");
	}

	private void saveBatchWithValues(final Map<String, AttributeValue> keys, final BatchInputDynamoDb batch,
			final boolean saveGsi1, final String timeToLive) {
		// 2.
		saveBatchWithKeys(keys, batch, saveGsi1, timeToLive, null);
	}

	private void saveBatchWithKeys(final Map<String, AttributeValue> keys, final BatchInputDynamoDb batch,
			final boolean saveGsi1, final String timeToLive, String childEdge) {
		// 3.
		ZonedDateTime insertedDate = batch.getCreationDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();

		String batchSequence = batch.getBatchSequence();
		Map<String, AttributeValue> pkValues = new HashMap<>(keys);
		addS(pkValues, "batchSequence", batchSequence);

		if (childEdge != null) {
			addS(pkValues, "artefactId", childEdge);
		}

		if (saveGsi1) {

			if (batch.getArtefacts() != null && !batch.getArtefacts().isEmpty()) {
				// addS(pkValues, "artefactId",
				// batch.getArtefacts().get(0).getArtefactItemId());
				addStringSet(pkValues, "artefacts",
						batch.getArtefacts()
							.stream()
							.map(artefact -> artefact.getArtefactItemId()) // Extracting
																			// IDs
							.collect(Collectors.toList())); // Collecting as a Set
			}

			addS(pkValues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
			addS(pkValues, GSI1_SK, fullDate + TAG_DELIMINATOR + batch.getBatchSequence());

			addS(pkValues, "batchStatus", batch.getStatus());
			addS(pkValues, "requestId", batch.getRequestId());
			if (batch.getOperator() != null) {
				addS(pkValues, "operator", batch.getOperator().getUsername());
			}

			if (batch.getRequestType() != null) {
				addS(pkValues, "requestType", batch.getRequestType());
			}

			addS(pkValues, "type", "batch");

			if (batch.getScannedType() != null) {
				addEnum(pkValues, "scanType", batch.getScannedType().toString());
			}

			if (batch.getJobs() != null) {
				addStringSet(pkValues, "jobs", batch.getJobsIds());
			}
		}

		// Persistence
		log.info("write pk values: " + pkValues);
		save(pkValues);
	}

	@Override
	public void saveBatchSequence(BatchInputDynamoDb batch) {
		log.info("single batch: a");
		Map<String, AttributeValue> keysNoChild = keysDocument(batch.getBatchSequence());
		saveBatchWithValues(keysNoChild, batch, true, null);
	}

	@Override
	public void saveBatchSequenceWithChildren(BatchInputDynamoDb batch) {
		if (batch.getArtefacts() == null) {
			return;
		}
		batch.getArtefacts()
			.stream()
			.map(ArtefactDynamoDb::getArtefactItemId) // Extracting IDs
			.forEach(artefactId -> {
				log.info("batch with children: {}", artefactId);
				Map<String, AttributeValue> keysWithChild = keysBatchWithArtefact(batch.getBatchSequence(), artefactId);
				saveBatchWithKeys(keysWithChild, batch, false, null, artefactId);
			});
	}

	@Override
	public void saveBatchSequenceAtomic(Collection<BatchInputDynamoDb> batch) {

	}

	@Override
	public void saveBatchSequenceWithChildrenAtomic(Collection<BatchInputDynamoDb> batch) {

	}

	@Override
	public void updateBatchWithStatus(String batchSequence, String status) {
		List<ArtefactOutput> artefacts = getAllArtefactsForBatch(batchSequence, "artefact");
		if (artefacts != null && !artefacts.isEmpty()) {
			artefacts.forEach(artefact -> DynamoDbHelper.updateTableItem(this.dynamoDbClient,
					SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
					keysGeneric(PREFIX_DOCS + artefact.getId(), "document"), "status", status));
		}
		DynamoDbHelper.updateTableItem(this.dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				keysDocument(batchSequence), "batchStatus", status);
	}

	@Override
	public BatchOutput getBatchDetail(String batchSequence) {
		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(batchSequence))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();
		Map<String, AttributeValue> result = this.dynamoDbClient.getItem(r).item();
		if (!result.isEmpty()) {
			return new AttributeValueToBatchOutputMapper().apply(result);
		}
		return null;
	}

	// @Override
	// public List<BatchOutput> getAllArtefactsForBatch() {
	// return null;
	// }

	@Override
	public List<ArtefactOutput> getAllArtefactsForBatch(String batchSequenceId, String secondary) {
		try {
			// Create QueryRequest
			QueryRequest queryRequest = createQueryRequest(SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
					PREFIX_BATCH + batchSequenceId, secondary);
			QueryResponse queryResponse = this.dynamoDbClient.query(queryRequest);
			log.info(" getAllArtefactsForBatch Query successful.");
			log.info(queryResponse.toString());
			// Handle QueryResponse
			return queryResponse.items()
				.stream()
				.map(m -> new AttributeValueDB2ToArtefactMapper().apply(m))
				.collect(Collectors.toList());
		}
		catch (Exception e) {
			handleQueryErrors(e);
		}
		return null;
	}

	@Override
	public List<BatchOutput> getAllBatchByStatus(String status) {
		try {
			QueryRequest queryRequest = createQueryRequestWithIndex(SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
					status);
			log.info("Table Name: {}, Index Name: {}, Key Condition Expression: {} Expression Attribute Names: {} ",
					queryRequest.tableName(), queryRequest.indexName(), queryRequest.keyConditionExpression(),
					queryRequest.expressionAttributeNames());
			QueryResponse queryResult = this.dynamoDbClient.query(queryRequest);
			log.info("getAllBatchByStatus query was successful: " + queryResult.toString());
			// Handle queryResult
			return queryResult.items()
				.stream()
				.map(m -> new AttributeValueDB2ToBatchMapper().apply(m))
				.collect(Collectors.toList());
		}
		catch (Exception e) {

			handleQueryErrors(e);
		}
		// FIXME: Do not return null (return an appropriate error object
		return null; //
	}

	@Override
	public void updateLockState(String batchSeq, boolean isLocked) {
		Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
		updatedValues.put(KEY_LOCKED, DynamoDbHelper
			.buildAttrValueUpdate(AttributeValue.builder().bool(isLocked).build(), AttributeAction.PUT));
		DynamoDbHelper.updateAttributes(this.dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				keysDocument(batchSeq), updatedValues);
	}

	@Override
	public String findStatusByRequestType(String batchSeq) {
		BatchOutput batchOutput = getBatchDetail(batchSeq);
		if (RequestType.ADDENDUM.name().equalsIgnoreCase(batchOutput.getRequestType())) {
			return BatchStatus.INDEXED.getStatus();
		}
		else {
			return BatchStatus.INSERTED.getStatus();
		}
	}

	@Override
	public void updateStatus(String batchSeq, String status) {
		DynamoDbHelper.updateTableItem(this.dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				keysDocument(batchSeq), "batchStatus", status);
	}

	@Override
	public void updateBatchIfAllIndexed(String batchSeq) {
		List<ArtefactOutput> allArtefacts = getAllArtefactsForBatch(batchSeq, "artefact");
		// filter the Artefact which are eligible for indexation
		Predicate<Artefact> eligibleForIndexation = artefact -> ArtefactStatus.INSERTED.getStatus()
			.equalsIgnoreCase(artefact.getStatus())
				&& !ArtefactClassType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType());
		List<ArtefactOutput> indexEligibleArtefacts = allArtefacts.stream()
			.map(x -> artefactService.getArtefactById(x.getId()))
			.filter(eligibleForIndexation)
			.map(new ArtefactToArtefactOutputMapper())
			.collect(Collectors.toList());

		// if there are no artefacts eligible for indexation
		// INDEX and Unblock the batch
		if (CollectionUtils.isEmpty(indexEligibleArtefacts)) {
			updateBatchWithStatus(batchSeq, BatchStatus.INDEXED.getStatus());
			updateLockState(batchSeq, false);
		}

	}

	@Override
	public void updateBatchIfAllInserted(String batchSeq) {
		log.info("updateBatchIfAllInserted, {}", batchSeq);
		// List<ArtefactOutput> allArtefacts = getAllArtefactsForBatch(batchSeq,
		// "artefact");

		// get batch from batch sequence
		BatchOutput out = getBatchDetail(batchSeq);
		String requestId = out.getRequestId();
		log.info("requestId, {}", requestId);

		String targetJobStatus = "UPLOADED";

		// find and set values
		Map<String, Object> jobStatusResponseMap = getAllBatchesForRequestId(requestId);
		// filter for jobStatusResponseMap.get("batchStatus").toString() == value
		// {
		// "requestId": "0c6280fa-fe86-448a-8169-c561a5235e11",
		// "batches": [
		// {
		// "jobs": [
		// {
		// "jobId": "c0708bb7-9e71-449e-85fe-5cbf7e1b6327",
		// "jobStatus": "INIT"
		// },
		// {
		// "jobId": "8dc2ab38-85b4-4380-a60c-c89f757ac67f",
		// "jobStatus": "INIT"
		// }
		// ],
		// "batchSequence": "0221123.7000",
		// "batchStatus": "INIT"
		// },
		// {
		// "jobs": [
		// {
		// "jobId": "3e474a7a-6192-4b82-b533-785eefeb10e0",
		// "jobStatus": "INIT"
		// },
		// {
		// "jobId": "75941f69-c32f-4a1b-aa73-f388d97959b6",
		// "jobStatus": "INIT"
		// }
		// ],
		// "batchSequence": "0221123.7001",
		// "batchStatus": "INIT"
		// }
		// ]
		// }
		log.info("jobStatusResponseMap {} ", jobStatusResponseMap);

		if (software.amazon.awssdk.utils.CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
			log.info("jobStatusResponseMap is empty", jobStatusResponseMap);
			return;
		}

		// Filter the batches based on batchSequence
		// Filter the batches based on batchSequence and all jobs having the target status
		List<Map<String, Object>> filteredBatches = ((List<Map<String, Object>>) jobStatusResponseMap.get("batches"))
			.stream()
			.filter(batch -> batchSeq.equals(batch.get("batchSequence")))
			.filter(batch -> {
				List<Map<String, Object>> jobs = (List<Map<String, Object>>) batch.get("jobs");
				return jobs.stream().allMatch(job -> targetJobStatus.equals(job.get("jobStatus")));
			})
			.collect(Collectors.toList());

		// Create a new map with the filtered batches
		Map<String, Object> filteredMap = new HashMap<>(jobStatusResponseMap);
		filteredMap.put("batches", filteredBatches);

		log.info("Filtered jobStatusResponseMap: {}", filteredMap);

		if (filteredBatches.isEmpty()) {
			log.warn("filteredMap is empty so return without any staus update");
			return;
		}
		else {
			String statusByRequestType = findStatusByRequestType(batchSeq);
			updateStatus(batchSeq, statusByRequestType);
		}

		// if
		// (ArtefactStatus.UPLOADED.getStatus().equalsIgnoreCase(filteredMap.get("batchStatus").toString()))
		// {
		//
		// String batchSequence = jobStatusResponseMap.get("batchSequence").toString();
		// String statusByRequestType = findStatusByRequestType(batchSequence);
		// updateStatus(batchSequence, statusByRequestType);
		//
		// }
	}

	@Override
	public Map<String, Object> getAllBatchesForRequestId(String requestId) {
		List<Map<String, Object>> jobStatusMapList = new ArrayList<>();

		List<BatchOutput> batches = DynamoDbPartiQ.getAllBatchesForRequestId(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, requestId);
		if (batches == null || batches.isEmpty()) {
			return null;
		}
		log.info("DynamoDbPartiQ.getAllBatchesForRequestId returns batches {}, size {}", batches, batches.size());

		List<ArtefactJob> artefactJobs = DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, requestId);
		if (artefactJobs == null || artefactJobs.isEmpty()) {
			return null;
		}
		log.info("DynamoDbPartiQ.getAllArtefactJobsByRequestId returns artefactJobs {}, size {}", artefactJobs,
				artefactJobs.size());

		List<String> jobStatusList = new ArrayList<>();
		for (ArtefactJob job : artefactJobs) {
			Map<String, Object> jobStatusMap = new HashMap<>();
			// log.info("Found Job {}", job);
			jobStatusMap.put("jobId", job.getId());
			jobStatusMap.put("jobStatus", job.getStatus());
			jobStatusMapList.add(jobStatusMap);
			jobStatusList.add(job.getStatus());
		}

		log.info("All batch {}, size {}", batches, batches.size());
		List<Map<String, Object>> listOfBatches = new ArrayList<>();
		for (BatchOutput batch : batches) {
			Map<String, Object> batchStatusMap = new HashMap<>();
			List<Map<String, Object>> jobs = mapReduceList(batch.getJobIds(), jobStatusMapList);
			batchStatusMap.put("jobs", jobs);
			batchStatusMap.put("batchSequence", batch.getBatchSequence());
			batchStatusMap.put("batchStatus", batch.getStatus());
			listOfBatches.add(batchStatusMap);
		}
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("requestId", requestId);
		responseMap.put("batches", listOfBatches);

		responseMap.forEach((key, value) -> log
			.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
		return responseMap;
	}

	public Map<String, Object> getPagedBatchesForRequestId(String requestId) {
		int PAGE_SIZE = 300;
		List<Map<String, Object>> jobStatusMapList = new ArrayList<>();
		List<String> jobStatusList = new ArrayList<>();

		List<BatchOutput> batches = DynamoDbPartiQ.getAllBatchesForRequestId(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, requestId);
		if (batches == null || batches.isEmpty()) {
			return null;
		}
		log.info("DynamoDbPartiQ.getAllBatchesForRequestId returns batches {}, size {}", batches, batches.size());
		Set<ArtefactJob> artefactJobs = new HashSet<>();
		PagedArtefactJobs pagedJobs = new PagedArtefactJobs();
		fetchJobsPaginated(requestId, PAGE_SIZE, null, artefactJobs, pagedJobs);
		log.info("fetchJobsPaginated  pagedJobs {}", pagedJobs);
		log.info("pagedJobs.getNextToken() {}", pagedJobs.getNextToken());
		log.info("artefactJobs size {}", artefactJobs.size());
		log.info("Final pagedArtefactJobs nextToken {}", pagedJobs.getNextToken());
		if (artefactJobs.isEmpty()) {
			return null;
		}
		log.info("Found artefactJobs {}, size {}", artefactJobs, artefactJobs.size());
		for (ArtefactJob job : artefactJobs) {
			Map<String, Object> jobStatusMap = new HashMap<>();
			jobStatusMap.put("jobId", job.getId());
			jobStatusMap.put("jobStatus", job.getStatus());
			jobStatusMapList.add(jobStatusMap);
			jobStatusList.add(job.getStatus());
		}

		log.info("All batch {}, size {}", batches, batches.size());
		List<Map<String, Object>> listOfBatches = new ArrayList<>();
		for (BatchOutput batch : batches) {
			Map<String, Object> batchStatusMap = new HashMap<>();
			List<Map<String, Object>> jobs = mapReduceList(batch.getJobIds(), jobStatusMapList);
			batchStatusMap.put("jobs", jobs);
			batchStatusMap.put("batchSequence", batch.getBatchSequence());
			batchStatusMap.put("batchStatus", batch.getStatus());
			listOfBatches.add(batchStatusMap);
		}
		Map<String, Object> responseMap = new LinkedHashMap<>();
		responseMap.put("requestId", requestId);
		responseMap.put("batches", listOfBatches);
		responseMap.put("jobCount", artefactJobs.size());
		// paging is done at the query level, not at the API level for now
		// responseMap.put("hasMorePages", pagedArtefactJobs.isHasMorePages());
		// responseMap.put("lastEvaluatedKey", pagedArtefactJobs.getLastEvaluatedKey());

		responseMap.forEach((key, value) -> log
			.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
		return responseMap;
	}

	private Set<ArtefactJob> fetchAllJobs(String requestId, String pageSize, String nextToken) {
		int PAGE_SIZE = 300;
		Set<ArtefactJob> artefactJobs = new HashSet<>();
		PagedArtefactJobs pagedJobs = new PagedArtefactJobs();
		fetchJobsPaginated(requestId, PAGE_SIZE, nextToken, artefactJobs, pagedJobs);

		int pageCount = 1;
		while (pagedJobs.isHasMorePages() && pageCount < 10) { // Limit to 4 pages as in
																// the original code
			log.info("artefactJobs size {} {}", pageCount, artefactJobs.size());
			log.info("pagedArtefactJobs size {} {}", pageCount, pagedJobs.getNextToken());

			fetchJobsPaginated(requestId, PAGE_SIZE, pagedJobs.getNextToken(), artefactJobs, pagedJobs);
			pageCount++;
		}

		// Log the final state
		log.info("Final artefactJobs size {}", artefactJobs.size());
		log.info("Final pagedArtefactJobs nextToken {}", pagedJobs.getNextToken());

		return artefactJobs;
	}

	private void fetchJobsPaginated(String requestId, int pageSize, String nextToken, Set<ArtefactJob> artefactJobs,
			PagedArtefactJobs pagedArtefactJobsPages) {
		do {
			log.info("Inside do loop in fetchJobsPaginated");
			DynamoDbPartiQ.getAllArtefactJobsByRequestId(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
					requestId, pageSize, nextToken, pagedArtefactJobsPages);

			artefactJobs.addAll(pagedArtefactJobsPages.getItems());
			log.info("Current page retrieved: artefactJobs size {}", artefactJobs.size());
			log.info("pagedArtefactJobsPages.getItems(): {}", pagedArtefactJobsPages.getItems().size());
			nextToken = pagedArtefactJobsPages.getNextToken();
		}
		while (pagedArtefactJobsPages.isHasMorePages());
		log.info(
				"fetchJobsPaginated while loop finished pagedArtefactJobsPages.getItems().size() {}, artefactJobs {}, size {}",
				pagedArtefactJobsPages.getItems().size(), artefactJobs, artefactJobs.size());
	}

	private QueryRequest createQueryRequest(String tableName, String primary, String secondary) {
		return QueryRequest.builder()
			.tableName(tableName)
			.keyConditionExpression("#cd420 = :cd420 And begins_with(#cd421, :cd421)")
			.consistentRead(false)
			.scanIndexForward(true)
			.expressionAttributeNames(getExpressionAttributeNames())
			.expressionAttributeValues(getExpressionAttributeValues(primary, secondary))
			.build();
	}

	private Map<String, String> getExpressionAttributeNames() {
		Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("#cd420", "PK");
		expressionAttributeNames.put("#cd421", "SK");
		return expressionAttributeNames;
	}

	private Map<String, AttributeValue> getExpressionAttributeValues(String primaryAttribute,
			String secondaryExpression) {
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":cd420", AttributeValue.builder().s(primaryAttribute).build());
		expressionAttributeValues.put(":cd421", AttributeValue.builder().s(secondaryExpression).build());
		return expressionAttributeValues;
	}

	private QueryRequest createQueryRequestWithIndex(String tableName, String status) {
		return QueryRequest.builder()
			.tableName(tableName)
			.indexName("GSI-Artefact-4")
			.keyConditionExpression("#e4150 = :e4150 And #e4151 = :e4151")
			.scanIndexForward(true)
			.expressionAttributeNames(getExpressionAttributeNamesWithIndexKeys())
			.expressionAttributeValues(getExpressionAttributeIndexValues(status))
			.build();
	}

	private Map<String, String> getExpressionAttributeNamesWithIndexKeys() {
		Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("#e4150", "type");
		expressionAttributeNames.put("#e4151", "batchStatus");
		return expressionAttributeNames;
	}

	private Map<String, AttributeValue> getExpressionAttributeIndexValues(String status) {
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":e4150", AttributeValue.builder().s("batch").build());
		expressionAttributeValues.put(":e4151", AttributeValue.builder().s(status).build());
		return expressionAttributeValues;
	}

	private void handleQueryErrors(Exception exception) {
		try {
			throw exception;
		}
		catch (Exception e) {
			// There are no API specific errors to handle for Query, common DynamoDB API
			// errors are handled below
			handleCommonErrors(e);
		}
	}

	private void handleCommonErrors(Exception exception) {
		try {
			throw exception;
		}
		catch (InternalServerErrorException e) {
			log.error("Internal Server Error, generally safe to retry with exponential back-off. Error: "
					+ e.getMessage());
		}
		catch (RequestLimitExceededException e) {
			log.error(
					"Throughput exceeds the current throughput limit for your account, increase account level throughput before "
							+ "retrying. Error: " + e.getMessage());
		}
		catch (ProvisionedThroughputExceededException e) {
			log.error(
					"Request rate is too high. If you're using a custom retry strategy make sure to retry with exponential back-off. "
							+ "Otherwise consider reducing frequency of requests or increasing provisioned capacity for your table or secondary index. Error: "
							+ e.getMessage());
		}
		catch (ResourceNotFoundException e) {
			log.error("One of the tables was not found, verify table exists before retrying. Error: " + e.getMessage());
		}
		catch (Exception e) {
			log.info("An exception occurred, investigate and configure retry strategy. Error: " + e.getMessage());
		}
	}

	private List<Map<String, Object>> mapReduceList(ArrayList<String> idList, List<Map<String, Object>> listOfMaps) {

		// Filter the list of maps to include only those with "id" in the list of IDs
		List<Map<String, Object>> filteredList = listOfMaps.stream()
			.filter(map -> idList.contains(map.get("jobId")))
			.collect(Collectors.toList());

		return filteredList;
	}

	private Map<String, Object> mapReduce(ArrayList<String> idList, Map<String, Object> idMap) {
		// Filter the map to include only entries with keys in the list
		Map<String, Object> filteredMap = idMap.entrySet()
			.stream()
			.filter(entry -> idList.contains(entry.getKey()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return filteredMap;
	}

}