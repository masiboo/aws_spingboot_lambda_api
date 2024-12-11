package org.iprosoft.trademarks.aws.artefacts.service.artefact;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbHelper;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbPartiQ;
import org.iprosoft.trademarks.aws.artefacts.model.dto.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.AwsEnvironmentEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToArtefactInfoMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToArtefactMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.AttributeValueToBatchedArtefactMapper;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.DocumentTagToAttributeValueMapper;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.iprosoft.trademarks.aws.artefacts.util.QueryRequestBuilder;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.service.miris.MirisService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.lang.Object;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DynamoDbHelper.buildAttrValueUpdate;
import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;
import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.KEY_MIRIS_DOCID;
import static org.iprosoft.trademarks.aws.artefacts.util.AppConstants.KEY_STATUS;
import static org.iprosoft.trademarks.aws.artefacts.util.ArtefactUploadRequestUtil.MIRIS_DOC_ID_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtefactServiceImpl implements ArtefactService, DbKeys {

	private final DynamoDbClient dynamoDbClient;

	private final QueryRequestBuilder queryRequestBuilder;

	private final MirisService mirisService;

	// private final ArtefactApiClient artefactApiClient;

	@Override
	public void addTags(final String documentId, final Collection<ArtefactTag> tags, final String timeToLive) {
		if (tags != null) {
			Predicate<ArtefactTag> predicate = tag -> DocumentTagType.SYSTEMDEFINED.equals(tag.getDocumentTagType())
					|| !SYSTEM_DEFINED_TAGS.contains(tag.getKey());
			DocumentTagToAttributeValueMapper mapper = new DocumentTagToAttributeValueMapper(PREFIX_DOCS,
					DateUtils.getSimpleUtcDateTimeFormat(), documentId);
			List<Map<String, AttributeValue>> valueList = tags.stream()
				.filter(predicate)
				.map(mapper)
				.flatMap(List::stream)
				.toList();
			if (timeToLive != null) {
				valueList.forEach(v -> addN(v, "TimeToLive", timeToLive));
			}
			List<Put> putItems = valueList.stream()
				.map(values -> Put.builder()
					.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
					.item(values)
					.build())
				.toList();
			List<TransactWriteItem> writes = putItems.stream()
				.map(i -> TransactWriteItem.builder().put(i).build())
				.collect(Collectors.toList());
			if (!writes.isEmpty()) {
				this.dynamoDbClient
					.transactWriteItems(TransactWriteItemsRequest.builder().transactItems(writes).build());
			}
		}
	}

	private void save(final Map<String, AttributeValue> values) {

		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.item(values)
			.build();

		dynamoDbClient.putItem(put);
	}

	public void saveArtefactsWithTransactions(Collection<IArtefact> artefacts) {
		final int BATCH_SIZE = 25;
		List<List<IArtefact>> batches = splitIntoBatches(artefacts, BATCH_SIZE);
		List<WriteRequest> allUnprocessedItems = new ArrayList<>();

		for (List<IArtefact> batch : batches) {
			List<WriteRequest> writeRequests = batch.stream()
				.map(item -> createWriteRequestItem(createArtefact(item)))
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

	private List<List<IArtefact>> splitIntoBatches(Collection<IArtefact> artefacts, int batchSize) {
		List<List<IArtefact>> batches = new ArrayList<>();
		List<IArtefact> currentBatch = new ArrayList<>();

		for (IArtefact artefact : artefacts) {
			if (currentBatch.size() == batchSize) {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
			}
			currentBatch.add(artefact);
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

	private WriteRequest createWriteRequestItem(final Map<String, AttributeValue> values) {
		PutRequest put = PutRequest.builder().item(values).build();
		return WriteRequest.builder().putRequest(put).build();
	}

	private Map<String, AttributeValue> createArtefact(IArtefact artefact) {
		Map<String, AttributeValue> item = new HashMap<>();

		// Add primary key
		item.put(PK, AttributeValue.builder().s("artefacts#" + artefact.getArtefactItemId()).build());
		item.put(SK, AttributeValue.builder().s("document").build());

		// Add GSI1 keys
		ZonedDateTime insertedDate = artefact.getInsertedDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();

		item.put(GSI1_PK, AttributeValue.builder().s(createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate)).build());
		item.put(GSI1_SK,
				AttributeValue.builder().s(fullDate + TAG_DELIMINATOR + artefact.getArtefactItemId()).build());

		// Add other attributes
		addAttributeIfNotNull(item, "artefactId", artefact.getArtefactItemId());
		addAttributeIfNotNull(item, "fileName", artefact.getFileName());
		addAttributeIfNotNull(item, "insertedDate", fullDate);
		addAttributeIfNotNull(item, "userId", artefact.getUserId());
		addAttributeIfNotNull(item, "path", artefact.getPath());
		addAttributeIfNotNull(item, "contentType", artefact.getContentType());
		addAttributeIfNotNull(item, "BatchSequenceId", artefact.getBatchSequenceId());
		addAttributeIfNotNull(item, "containerId", artefact.getContainerId());
		addAttributeIfNotNull(item, "artefactContainerName", artefact.getArtefactContainerName());
		addAttributeIfNotNull(item, "artefactMergeId", artefact.getArtefactMergeId());
		addAttributeIfNotNull(item, "contentLength", artefact.getContentLength(), true);
		addAttributeIfNotNull(item, "etag", artefact.getChecksum());
		addAttributeIfNotNull(item, "belongsToArtefactId", artefact.getBelongsToArtefactId());
		addAttributeIfNotNull(item, "s3Bucket", artefact.getBucket());
		addAttributeIfNotNull(item, "s3Key", artefact.getKey());
		addAttributeIfNotNull(item, "mirisDocId", artefact.getMirisDocId());

		if (Boolean.TRUE.equals(artefact.getSizeWarning())) {
			item.put("sizeWarning", AttributeValue.builder().bool(true).build());
		}

		String valueType = "ARTEFACT" + TAG_DELIMINATOR + artefact.getArtefactClassType();
		if (artefact.isPart()) {
			valueType = valueType + TAG_DELIMINATOR + "PART";
			addAttributeIfNotNull(item, "pageNumber", artefact.getPageNumber());
			addAttributeIfNotNull(item, "totalPages", artefact.getTotalPages());
		}

		String status = artefact.getStatus() == null ? ArtefactStatus.INIT.getStatus() : artefact.getStatus();

		item.put("type", AttributeValue.builder().s(valueType).build());
		item.put("status", AttributeValue.builder().s(status).build());

		return item;
	}

	private void addAttributeIfNotNull(Map<String, AttributeValue> item, String key, Object value) {
		addAttributeIfNotNull(item, key, value, false);
	}

	private void addAttributeIfNotNull(Map<String, AttributeValue> item, String key, Object value, boolean isNumber) {
		if (value != null) {
			if (isNumber) {
				item.put(key, AttributeValue.builder().n(value.toString()).build());
			}
			else {
				item.put(key, AttributeValue.builder().s(value.toString()).build());
			}
		}
	}

	// TODO: persist mirisDocId
	@Override
	public void saveDocument(final IArtefact document, final Collection<ArtefactTag> tags) {
		log.info("saving document....");
		Map<String, AttributeValue> keys = keysDocument(document.getArtefactItemId());
		saveDocumentWithTags(keys, document, tags, true, null);
	}

	public Map<String, AttributeValue> createArtefact(final IArtefact document, final Collection<ArtefactTag> tags) {
		log.info("saving artefact....");
		Map<String, AttributeValue> keys = keysDocument(document.getArtefactItemId());
		return createArtefactWithTags(keys, document, tags, true, null);

	}

	private void saveDocumentWithTags(final Map<String, AttributeValue> keys, final IArtefact document,
			final Collection<ArtefactTag> tags, final boolean saveGsi1, final String timeToLive) {
		saveDocumentWithKeys(keys, document, saveGsi1, timeToLive);
		if (saveGsi1) {
			saveDocumentDate(document);
		}
	}

	private Map<String, AttributeValue> createArtefactWithTags(final Map<String, AttributeValue> keys,
			final IArtefact document, final Collection<ArtefactTag> tags, final boolean saveGsi1,
			final String timeToLive) {

		Map<String, AttributeValue> attributeValueMap = createArtefactWithKeys(keys, document, saveGsi1, timeToLive);
		return attributeValueMap;
	}

	private void saveDocumentWithKeys(final Map<String, AttributeValue> keys, final IArtefact document,
			final boolean saveGsi1, final String timeToLive) {
		ZonedDateTime insertedDate = document.getInsertedDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();

		Map<String, AttributeValue> pkValues = new HashMap<>(keys);
		if (saveGsi1) {
			addS(pkValues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
			addS(pkValues, GSI1_SK, fullDate + TAG_DELIMINATOR + document.getArtefactItemId());
		}

		addS(pkValues, "artefactId", document.getArtefactItemId());

		if (document.getFileName() != null) {
			addS(pkValues, "fileName", document.getFileName());
		}

		if (fullDate != null) {
			addS(pkValues, "insertedDate", fullDate);
		}

		if (document.getUserId() != null) {
			addS(pkValues, "userId", document.getUserId());
		}

		if (document.getPath() != null) {
			addS(pkValues, "path", document.getPath());
		}

		if (document.getContentType() != null) {
			addS(pkValues, "contentType", document.getContentType());
		}

		if (document.getBatchSequenceId() != null) {
			addS(pkValues, "BatchSequenceId", document.getBatchSequenceId());
		}

		if (document.getContainerId() != null) {
			addS(pkValues, "containerId", document.getContainerId()); // artefact-id
		}

		if (document.getArtefactContainerName() != null) {
			addS(pkValues, "artefactContainerName", document.getArtefactContainerName());
		}

		if (document.getArtefactMergeId() != null) {
			addS(pkValues, "artefactMergeId", document.getArtefactMergeId());
		}

		if (document.getContentLength() != null) {
			addN(pkValues, "contentLength", SafeParserUtil.safeLongToString(document.getContentLength()));
		}

		if (document.getChecksum() != null) {
			String etag = document.getChecksum().replaceAll("^\"|\"$", "");
			addS(pkValues, "etag", etag);
		}

		if (document.getBelongsToArtefactId() != null) {
			addS(pkValues, "belongsToArtefactId", document.getBelongsToArtefactId());
		}

		if (document.getBucket() != null) {
			addS(pkValues, "s3Bucket", document.getBucket());
		}

		if (document.getKey() != null) {
			addS(pkValues, "s3Key", document.getKey());
		}

		if (timeToLive != null) {
			addN(pkValues, "TimeToLive", timeToLive);
		}

		if (document.getMirisDocId() != null) {
			addS(pkValues, "mirisDocId", document.getMirisDocId());
		}

		if (Boolean.TRUE.equals(document.getSizeWarning())) {
			pkValues.put("sizeWarning", AttributeValue.builder().bool(document.getSizeWarning()).build());
		}

		// TODO: change type to composite of ArtefactClassType artefact#document,
		// artefact#certificate, etc
		String valueType = "ARTEFACT" + TAG_DELIMINATOR + document.getArtefactClassType();
		if (document.isPart()) {
			valueType = valueType + TAG_DELIMINATOR + "PART";
			addS(pkValues, "pageNumber", document.getPageNumber());
			addS(pkValues, "totalPages", document.getTotalPages());
		}

		String status = document.getStatus() == null ? ArtefactStatus.INIT.getStatus() : document.getStatus();

		addS(pkValues, "type", valueType);
		addS(pkValues, "status", status);

		// Persistence
		log.debug("@@@@@@#####@@@@ Persistence items .. {}", pkValues);
		save(pkValues);
	}

	private Map<String, AttributeValue> createArtefactWithKeys(final Map<String, AttributeValue> keys,
			final IArtefact document, final boolean saveGsi1, final String timeToLive) {
		ZonedDateTime insertedDate = document.getInsertedDate();
		String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentDateShortStr();
		String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate)
				: DateUtils.getCurrentUtcDateTimeStr();

		Map<String, AttributeValue> pkValues = new HashMap<>(keys);
		if (saveGsi1) {
			addS(pkValues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
			addS(pkValues, GSI1_SK, fullDate + TAG_DELIMINATOR + document.getArtefactItemId());
		}

		addS(pkValues, "artefactId", document.getArtefactItemId());

		if (document.getFileName() != null) {
			addS(pkValues, "fileName", document.getFileName());
		}

		if (fullDate != null) {
			addS(pkValues, "insertedDate", fullDate);
		}

		if (document.getUserId() != null) {
			addS(pkValues, "userId", document.getUserId());
		}

		if (document.getPath() != null) {
			addS(pkValues, "path", document.getPath());
		}

		if (document.getContentType() != null) {
			addS(pkValues, "contentType", document.getContentType());
		}

		if (document.getBatchSequenceId() != null) {
			addS(pkValues, "BatchSequenceId", document.getBatchSequenceId());
		}

		if (document.getContainerId() != null) {
			addS(pkValues, "containerId", document.getContainerId()); // artefact-id
		}

		if (document.getArtefactContainerName() != null) {
			addS(pkValues, "artefactContainerName", document.getArtefactContainerName());
		}

		if (document.getArtefactMergeId() != null) {
			addS(pkValues, "artefactMergeId", document.getArtefactMergeId());
		}

		if (document.getContentLength() != null) {
			addN(pkValues, "contentLength", SafeParserUtil.safeLongToString(document.getContentLength()));
		}

		if (document.getChecksum() != null) {
			String etag = document.getChecksum().replaceAll("^\"|\"$", "");
			addS(pkValues, "etag", etag);
		}

		if (document.getBelongsToArtefactId() != null) {
			addS(pkValues, "belongsToArtefactId", document.getBelongsToArtefactId());
		}

		if (document.getBucket() != null) {
			addS(pkValues, "s3Bucket", document.getBucket());
		}

		if (document.getKey() != null) {
			addS(pkValues, "s3Key", document.getKey());
		}

		if (timeToLive != null) {
			addN(pkValues, "TimeToLive", timeToLive);
		}

		if (document.getMirisDocId() != null) {
			addS(pkValues, "mirisDocId", document.getMirisDocId());
		}

		if (Boolean.TRUE.equals(document.getSizeWarning())) {
			pkValues.put("sizeWarning", AttributeValue.builder().bool(document.getSizeWarning()).build());
		}

		// TODO: change type to composite of ArtefactClassType artefact#document,
		// artefact#certificate, etc
		String valueType = "ARTEFACT" + TAG_DELIMINATOR + document.getArtefactClassType();
		if (document.isPart()) {
			valueType = valueType + TAG_DELIMINATOR + "PART";
			addS(pkValues, "pageNumber", document.getPageNumber());
			addS(pkValues, "totalPages", document.getTotalPages());
		}

		String status = document.getStatus() == null ? ArtefactStatus.INIT.getStatus() : document.getStatus();

		addS(pkValues, "type", valueType);
		addS(pkValues, "status", status);

		// Persistence
		return pkValues;
	}

	public Map<String, String> validateArtefactBatch(ArtefactBatch artefactBatch, String scanedApp) {
		Map<String, String> errorMessage = new HashMap<>();
		// Validate type
		validateType(artefactBatch.getType(), errorMessage);
		// Validate artefact class type
		validateArtefactBatchClassType(artefactBatch.getArtefactClassType(), errorMessage);
		// Validate contentType
		validateContentType(artefactBatch.getContentType(), errorMessage);
		// Validate is classType allowed for the contentType
		if (!errorMessage.containsKey("classType") && !errorMessage.containsKey("contentType")) {
			validateArtefactBatchClassContentType(Objects.requireNonNull(artefactBatch.getArtefactClassType()),
					Objects.requireNonNull(artefactBatch.getContentType()), errorMessage);
		}

		if (artefactBatch.getFilename() == null || artefactBatch.getFilename().isBlank()) {
			errorMessage.put("filename", "must be present");
		}
		else if (!artefactBatch.getFilename().matches("\\d{8}\\..*")) {
			errorMessage.put("filename", "must be 8 digits followed by a dot after dot any accepted file extension");
		}

		if (artefactBatch.getArtefactName() == null || artefactBatch.getArtefactName().isBlank()) {
			if (artefactBatch.getArtefactMergeId() == null || artefactBatch.getArtefactMergeId().isBlank()) {
				errorMessage.put("artefactName", "must be present 1A");
			}
		}

		if (artefactBatch.getContentType() == null || artefactBatch.getContentType().isBlank()) {
			errorMessage.put("ContentType", "must be present");
		}

		if (artefactBatch.getBatchSequence() == null || artefactBatch.getBatchSequence().isBlank()) {
			errorMessage.put("batchsequence", "must be present");
		}

		if (ScannedAppType.ADDENDUM.toString().equalsIgnoreCase(scanedApp)) {
			if (artefactBatch.getMirisDocId() == null || artefactBatch.getMirisDocId().isBlank()) {
				errorMessage.put(MIRIS_DOC_ID_KEY, "must be present");
			}
			else {
				// Validate mirisDocId
				validateMirisDocId(artefactBatch.getMirisDocId(), errorMessage);
			}
		}

		if (ScannedAppType.NEW_REQUEST.toString().equalsIgnoreCase(scanedApp)) {
			if (artefactBatch.getUser() == null || artefactBatch.getUser().isBlank()) {
				errorMessage.put("Author/User", "must be present");
			}
		}
		validateInputDocument(artefactBatch, errorMessage, scanedApp);
		return errorMessage;
	}

	public Map<String, String> validateArtefact(ArtefactInput artefactInput) {
		Map<String, String> errorMessage = new HashMap<>();
		// Validate artefact class type
		validateArtefactClassType(artefactInput.getArtefactClassType(), errorMessage);

		if (artefactInput.getItems().size() > 1) {
			errorMessage.put("item", "More than 1 item found. Only one item is allowed");
		}
		// Validate each item in the artefact
		for (ArtefactItemInput item : artefactInput.getItems()) {
			validateContentTypeAndFilename(errorMessage, item);
			if (!errorMessage.containsKey("classType") && !errorMessage.containsKey("contentType")) {
				validateClassContentType(Objects.requireNonNull(artefactInput.getArtefactClassType()),
						Objects.requireNonNull(item.getContentType()), errorMessage);
			}
		}
		// Validate mirisDocId
		validateMirisDocId(artefactInput.getMirisDocId(), errorMessage);

		validateInputDocument(artefactInput, errorMessage, "");
		return errorMessage;
	}

	private void validateContentTypeAndFilename(Map<String, String> errorMessage, ArtefactItemInput artefactItemInput) {
		validateContentType(artefactItemInput.getContentType(), errorMessage);
		if (artefactItemInput.getFilename() == null || artefactItemInput.getFilename().isEmpty()) {
			errorMessage.put("filename", "Filename is not valid");
		}
	}

	private static void validateArtefactBatchClassContentType(String artefactClassType, String contentType,
			Map<String, String> errorMessage) {
		Map<String, Set<String>> validContentType = new HashMap<>();

		validContentType.put(ArtefactClassType.CERTIFICATE.name(), new HashSet<>(Arrays.asList(
				ContentType.APPLICATION_PDF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.PDF.getContentType(), ContentType.XML.getContentType(),
				ContentType.XLS.getContentType(), ContentType.APPLICATION_XLS.getContentType(),
				ContentType.APPLICATION_XML.getContentType())));

		validContentType.put(ArtefactClassType.DOCUMENT.name(), new HashSet<>(Arrays.asList(
				ContentType.APPLICATION_PDF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.PDF.getContentType(), ContentType.XML.getContentType(),
				ContentType.XLS.getContentType(), ContentType.APPLICATION_XLS.getContentType(),
				ContentType.APPLICATION_XML.getContentType())));

		Set<String> validContentTypes = validContentType.get(artefactClassType.toUpperCase());

		if (validContentTypes != null && !validContentTypes.contains(contentType.toLowerCase())) {
			errorMessage.put(artefactClassType, "Supported content types are " + String.join(", ", validContentTypes)
					+ ". But provided content type is: " + contentType);
		}
	}

	private static void validateClassContentType(String artefactClassType, String contentType,
			Map<String, String> errorMessage) {
		Map<String, Set<String>> validContentType = new HashMap<>();

		validContentType.put(ArtefactClassType.MULTIMEDIA.name(),
				new HashSet<>(Arrays.asList(ContentType.VIDEO_MP4.getContentType(),
						ContentType.APPLICATION_MP4.getContentType(), ContentType.MP4.getContentType())));

		validContentType.put(ArtefactClassType.SOUND.name(),
				new HashSet<>(Arrays.asList(ContentType.APPLICATION_WAV.getContentType(),
						ContentType.AUDIO_MP4.getContentType(), ContentType.AUDIO_MP3.getContentType(),
						ContentType.AUDIO_WAV.getContentType(), ContentType.AUDIO_MPEG.getContentType(),

						ContentType.APPLICATION_MPEG.getContentType(), ContentType.APPLICATION_WAV.getContentType(),
						ContentType.APPLICATION_MP4.getContentType(), ContentType.APPLICATION_MP3.getContentType(),

						ContentType.MPEG.getContentType(), ContentType.MP4.getContentType(),
						ContentType.MP3.getContentType(), ContentType.WAV.getContentType())));

		validContentType.put(ArtefactClassType.COLOURLOGO.name(),
				new HashSet<>(Arrays.asList(ContentType.IMAGE_BMP.getContentType(),
						ContentType.IMAGE_GIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
						ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
						ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
						ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
						ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

						ContentType.APPLICATION_BMP.getContentType(), ContentType.APPLICATION_GIF.getContentType(),
						ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
						ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
						ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
						ContentType.APPLICATION_JPG.getContentType(),

						ContentType.BMP.getContentType(), ContentType.GIF.getContentType(),
						ContentType.PNG.getContentType(), ContentType.JPG.getContentType(),
						ContentType.JPEG.getContentType(), ContentType.PJPEG.getContentType(),
						ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
						ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
						ContentType.X_TIF.getContentType())));

		validContentType.put(ArtefactClassType.BWLOGO.name(),
				new HashSet<>(Arrays.asList(ContentType.IMAGE_BMP.getContentType(),
						ContentType.IMAGE_GIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
						ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
						ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
						ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
						ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

						ContentType.APPLICATION_BMP.getContentType(), ContentType.APPLICATION_GIF.getContentType(),
						ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
						ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
						ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
						ContentType.APPLICATION_JPG.getContentType(),

						ContentType.BMP.getContentType(), ContentType.GIF.getContentType(),
						ContentType.PNG.getContentType(), ContentType.JPG.getContentType(),
						ContentType.JPEG.getContentType(), ContentType.PJPEG.getContentType(),
						ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
						ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
						ContentType.X_TIF.getContentType())));

		validContentType.put(ArtefactClassType.CERTIFICATE.name(), new HashSet<>(Arrays.asList(
				ContentType.APPLICATION_PDF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.PDF.getContentType(), ContentType.XML.getContentType(),
				ContentType.XLS.getContentType(), ContentType.APPLICATION_XLS.getContentType(),
				ContentType.APPLICATION_XML.getContentType())));

		validContentType.put(ArtefactClassType.DOCUMENT.name(), new HashSet<>(Arrays.asList(
				ContentType.APPLICATION_PDF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.IMAGE_PNG.getContentType(),
				ContentType.IMAGE_JPEG.getContentType(), ContentType.IMAGE_PJPEG.getContentType(),
				ContentType.IMAGE_JPG.getContentType(), ContentType.IMAGE_JPEG.getContentType(),
				ContentType.IMAGE_TIFF.getContentType(), ContentType.IMAGE_TIF.getContentType(),
				ContentType.IMAGE_X_TIFF.getContentType(), ContentType.IMAGE_X_TIF.getContentType(),

				ContentType.APPLICATION_PNG.getContentType(), ContentType.APPLICATION_TIF.getContentType(),
				ContentType.APPLICATION_X_TIF.getContentType(), ContentType.APPLICATION_TIFF.getContentType(),
				ContentType.APPLICATION_X_TIFF.getContentType(), ContentType.APPLICATION_JPEG.getContentType(),
				ContentType.APPLICATION_JPG.getContentType(),

				ContentType.PNG.getContentType(), ContentType.JPG.getContentType(), ContentType.JPEG.getContentType(),
				ContentType.PJPEG.getContentType(), ContentType.PNG.getContentType(), ContentType.TIF.getContentType(),
				ContentType.TIFF.getContentType(), ContentType.X_TIFF.getContentType(),
				ContentType.X_TIF.getContentType(), ContentType.PDF.getContentType(), ContentType.XML.getContentType(),
				ContentType.XLS.getContentType(), ContentType.APPLICATION_XLS.getContentType(),
				ContentType.APPLICATION_XML.getContentType())));

		Set<String> validContentTypes = validContentType.get(artefactClassType.toUpperCase());

		if (validContentTypes != null && !validContentTypes.contains(contentType.toLowerCase())) {
			errorMessage.put(artefactClassType, "Supported content types are " + String.join(", ", validContentTypes)
					+ ". But provided content type is: " + contentType);
		}
	}

	private void validateContentType(String inpputContentType, Map<String, String> errorMessage) {
		boolean isValid = false;

		if (inpputContentType == null || inpputContentType.isBlank()) {
			errorMessage.put("contentType", "must be present");
			return;
		}

		if (inpputContentType.contains("/")) {
			inpputContentType = inpputContentType.substring(inpputContentType.lastIndexOf('/') + 1).toLowerCase();
		}

		for (ContentType contentType : ContentType.values()) {
			if (contentType.getContentType().toLowerCase().equalsIgnoreCase(inpputContentType.toLowerCase())) {
				isValid = true;
				break;
			}
		}
		if (!isValid) {
			errorMessage.put("contentType", "not valid type. Provided contentType " + inpputContentType);
		}
	}

	private void validateType(String scannedAppType, Map<String, String> errorMessage) {
		boolean isValidClassType = Arrays.stream(ScannedAppType.values())
			.anyMatch(classType -> classType.name().equalsIgnoreCase(scannedAppType));
		if (!isValidClassType) {
			errorMessage.put("scannedAppType",
					"scannedAppType/type should be Addendum or New_Request. Provided artefact class type: "
							+ scannedAppType);
		}
	}

	private void validateArtefactBatchClassType(String artefactClassType, Map<String, String> errorMessage) {
		EnumSet<classType> allowedClassTypes = EnumSet.of(classType.CERTIFICATE, classType.DOCUMENT);

		boolean isValidClassType = allowedClassTypes.stream()
			.anyMatch(classType -> classType.name().equalsIgnoreCase(artefactClassType));

		if (!isValidClassType) {
			String allowedClassTypesStr = allowedClassTypes.stream().map(Enum::name).collect(Collectors.joining(", "));

			errorMessage.put("classType", "ArtefactBatch class type is not valid. Provided artefact class type: "
					+ artefactClassType + " Allowed class type " + allowedClassTypesStr);
		}
	}

	private void validateArtefactClassType(String artefactClassType, Map<String, String> errorMessage) {
		boolean isValidClassType = Arrays.stream(classType.values())
			.anyMatch(classType -> classType.name().equalsIgnoreCase(artefactClassType));

		if (!isValidClassType) {
			String allowedClassTypes = Arrays.stream(classType.values())
				.map(Enum::name)
				.collect(Collectors.joining(", "));

			errorMessage.put("classType", "Artefact class type is not valid. Provided artefact class type: "
					+ artefactClassType + ". Allowed class types: " + allowedClassTypes);
		}
	}

	private void validateMirisDocId(String mirisDocId, Map<String, String> errorMessage) {
		if (mirisDocId == null || mirisDocId.isEmpty()) {
			errorMessage.put("mirisDocId", "MirisDocId is not present");
		}
		if (mirisDocId.contains(" ")) {
			errorMessage.put("mirisDocId", "mirisDocId should not contain spaces.");
		}
		String mirisDocIdValidationPattern = "^[0-9]{5,8}$";

		if (!Pattern.matches(mirisDocIdValidationPattern, mirisDocId)) {
			if (mirisDocId.length() < 5) {
				errorMessage.put("mirisDocId", "Document ID must be at least 5 digits long.");
			}
			else if (mirisDocId.length() > 8) {
				errorMessage.put("mirisDocId", "Document ID must be no more than 8 digits long.");
			}
			else if (!mirisDocId.matches("^[0-9]+$")) {
				errorMessage.put("mirisDocId", "Document ID should only contain digits.");
			}
		}

		if (SystemEnvironmentVariables.Aws_ENVIRONMENT.equalsIgnoreCase(AwsEnvironmentEnum.PROD.name())) {
			boolean isMirisCheckEnabled = Boolean.parseBoolean(SystemEnvironmentVariables.MIRIS_CHECK_ENABLED);
			if (isMirisCheckEnabled && !isDocIdValid(mirisDocId)) {
				errorMessage.put("mirisDocId", "MirisDocId is not valid");
			}
		}
	}

	private <T> void validateInputDocument(T document, Map<String, String> errorMessage, String scanedApp) {
		if (document instanceof ArtefactInput artefactInput) {
			if (artefactInput.getArtefactName() == null || artefactInput.getArtefactName().isBlank()) {
				errorMessage.put("artefactName", "artefactName cannot be null or empty A");
			}
			if (artefactInput.getArtefactClassType() == null || artefactInput.getArtefactClassType().isBlank()) {
				errorMessage.put("artefactClassType", "artefactClassType cannot be null or empty");
			}
		}
		if (document instanceof ArtefactBatch artefactBatch) {
			if (artefactBatch.getArtefactName() == null || artefactBatch.getArtefactName().isBlank()) {
				if (artefactBatch.getArtefactMergeId() == null || artefactBatch.getArtefactMergeId().isBlank()) {
					errorMessage.put("artefactName", "artefactName cannot be null or empty B");
				}
			}
			if (artefactBatch.getArtefactClassType() == null || artefactBatch.getArtefactClassType().isBlank()) {
				errorMessage.put("artefactClassType", "artefactClassType cannot be null or empty");
			}
		}
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(document);

		for (ConstraintViolation<T> violation : violations) {
			errorMessage.put(violation.getPropertyPath().toString(), violation.getMessage());
		}
		if (ScannedAppType.NEW_REQUEST.toString().equalsIgnoreCase(scanedApp)) {
			errorMessage.remove("mirisDocId");
		}
	}

	@Override
	public Artefact getArtefactByTags(final Collection<ArtefactTag> tags) {
		// TODO Auto-generated method stub
		Map<String, AttributeValue> keys = new HashMap<>();
		keys = tagsToKeysDocument(keys, tags);
		GetItemRequest get = GetItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.key(keys)
			.build();
		Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(get).item();
		Artefact artefact;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			artefact = objectMapper.readValue(returnedItem.toString(), Artefact.class);
		}
		catch (Exception e) {
			log.info("Exception: " + e);
			artefact = null;
		}
		return artefact;
	}

	@Override
	public List<List<ArtefactTag>> getAllArtefactItemTags() {
		return new ArrayList<>();
	}

	private Map<String, AttributeValue> tagsToKeysDocument(final Map<String, AttributeValue> keys,
			Collection<ArtefactTag> tags) {
		for (ArtefactTag tag : tags) {
			addS(keys, tag.getKey(), tag.getKey());
		}
		return keys;
	}

	public Map<String, String> validateInputMirisDocId(final ArtefactInput document) {
		Map<String, String> errorMessage = new HashMap<>();
		if (document.getMirisDocId() == null || document.getMirisDocId().equals("")) {
			errorMessage.put("mirisDocId", "not valid");
		}
		return errorMessage;
	}

	@Override
	public void updateArtefactWithStatus(String artefactId, String statusValue) {
		DynamoDbHelper.updateTableItem(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				keysDocument(artefactId), "status", statusValue);

		// sideeffect to save item to DB
		// Artefact artefact = this.getArtefactById(artefactId);
		// log.warn("Artefact 1");
		// if (artefact != null) {
		// // Create ArtefactItem
		// ArtefactItemsEntityDTO item = ArtefactItemsEntityDTO.builder()
		// .s3Key(artefact.getS3Key())
		// .fileName(artefact.getArtefactName())
		// .contentLength(artefact.getContentLength() != null ?
		// Long.valueOf(artefact.getContentLength()) : null)
		// .contentType(artefact.getItems() != null && !artefact.getItems().isEmpty()
		// && artefact.getItems().get(0) != null ?
		// artefact.getItems().get(0).getContentType() : null) //
		// .contentType(artefact.getConentType)
		// // artefactItemType()
		// // .contentType(artefact.getItems().get(0).getContentType())
		// // .scanType(artefact.getItems().get(0).getArtefactType())
		// .fragmentType(FragmentTypeEnum.RAW.toString())
		// .mergedArtefactId(artefact.getArtefactMergeId())
		// .totalPages(artefact.getItems() != null && !artefact.getItems().isEmpty()
		// && artefact.getItems().get(0) != null ?
		// artefact.getItems().get(0).getTotalPages() : null)
		// .createdDate(artefact.getArchiveDate())
		// .lastModificationDate(artefact.getArchiveDate())
		// .build();
		//
		// // Create ArtefactsEntity object
		// ArtefactsEntityDTO artefactsEntity = ArtefactsEntityDTO.builder()
		// // .artefactItemId(artefact.getArtefactItemId())
		// .archiveDate(artefact.getArchiveDate())
		// // .lastModificationUser(artefact.getUserId())
		// .s3Bucket(artefact.getS3Bucket())
		// // .key(artefact.getKey())
		// .mirisDocId(artefact.getMirisDocId())
		// // .sizeWarning(artefact.getSizeWarning())
		// .artefactClass(artefact.getArtefactClassType())
		// .lastModificationUser("system")
		// .indexationDate(artefact.getIndexationDate())
		// .artefactUUID(artefactId)
		// .status(statusValue)
		// .build();
		//
		// // Call ArtefactApiClient to create Artefact in DB
		//// try {
		//// log.warn("Artefact 2");
		//// ArtefactsEntityDTO createdArtefact =
		// artefactApiClient.createArtefact(artefactsEntity);
		//// log.warn("Artefact created successfully: {}", createdArtefact);
		//// }
		//// catch (Exception e) {
		//// log.error("Error creating artefact: {}", e.getMessage());
		//// }
		// }
		// else {
		// log.error("Artefact not found for id: {}", artefactId);
		// }

	}

	@Override
	public List<Artefact> getAllArtefacts(String date, String status) {
		Map<String, String> conditions = new HashMap<>();
		conditions.put("date", date);
		conditions.put("status", status);
		return DynamoDbPartiQ.getAllArtefacts(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				conditions);
	}

	@Override
	public List<Artefact> getAllArtefactsByInterval(String fromDate, String untilDate, String status) {
		Map<String, String> conditions = new HashMap<>();
		conditions.put("fromDate", fromDate);
		conditions.put("untilDate", untilDate);
		conditions.put("status", status);
		return DynamoDbPartiQ.getAllArtefactsByInterval(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				conditions);
	}

	@Override
	public Artefact getArtefactById(String artefactId) {
		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(artefactId))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();
		Map<String, AttributeValue> result = dynamoDbClient.getItem(r).item();
		log.warn("getArtefactById  artefactId {}", artefactId);
		if (!result.isEmpty()) {
			log.warn(" getArtefactById Query successful.");
			log.warn(result.toString());
			return new AttributeValueToArtefactMapper().apply(result);
		}
		return null;
	}

	public List<ArtefactBatch> getArtefactBatchByBatchSequenceAndDate(String batchSequence, ZonedDateTime date) {
		String formattedDate;
		try {
			formattedDate = DateUtils.getFullDateFromZonedDateTime(date);
		}
		catch (Exception e) {
			formattedDate = DateUtils.getCurrentDatetimeUtcStr();
		}

		Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("BatchSequenceId", "BatchSequence");
		expressionAttributeNames.put("insertedDate", "Date");

		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put("batchSeqVal", AttributeValue.builder().s(batchSequence).build());
		expressionAttributeValues.put("dateVal", AttributeValue.builder().s(formattedDate).build());

		QueryRequest queryRequest = QueryRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.keyConditionExpression("BatchSequenceId = batchSeqVal and insertedDate = dateVal")
			.expressionAttributeNames(expressionAttributeNames)
			.expressionAttributeValues(expressionAttributeValues)
			.build();

		QueryResponse response = dynamoDbClient.query(queryRequest);
		List<ArtefactBatch> artefactBatchList = new ArrayList<>();
		if (!response.items().isEmpty()) {
			response.items().forEach(valueMap -> {
				ArtefactBatch artefactBatch = new AttributeValueToBatchedArtefactMapper()
					.apply(Objects.requireNonNull(valueMap));
				artefactBatchList.add(artefactBatch);
			});
			return artefactBatchList;
		}
		return null;
	}

	public ArtefactBatch getArtectBatchById(String artefactId) {
		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(artefactId))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();
		Map<String, AttributeValue> result = dynamoDbClient.getItem(r).item();

		if (!result.isEmpty()) {
			// log.info(" getArtectBatchById Query successful.");
			// log.info(result.toString());
			return new AttributeValueToBatchedArtefactMapper().apply(result);
		}
		return null;
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
			dynamoDbClient.putItem(put);
		}
		catch (ConditionalCheckFailedException e) {
			// Conditional Check Fails on second insert attempt
		}
	}

	@Override
	public List<Artefact> getArtefactbyMirisDocId(String mirisDocId) {
		// get types artefact by class types
		return DynamoDbPartiQ.getArtefactsByMirisDocid(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				mirisDocId);
	}

	@Override
	public List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList) {
		List<String> derivedTypeList = typeList.stream().map(DbKeys::getTypeKey).collect(Collectors.toList());
		return DynamoDbPartiQ.getArtefactsByMirisDocidAndType(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, mirisDocId, derivedTypeList);
	}

	@Override
	public List<Artefact> getArtefactByFilterCritera(ArtefactFilterCriteria filterCriteria) {
		QueryRequest queryRequest = queryRequestBuilder.prepareQueryByCriteria(filterCriteria).build();
		QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
		AttributeValueToArtefactMapper artefactMapper = new AttributeValueToArtefactMapper();
		return queryResponse.items().stream().map(artefactMapper).collect(Collectors.toList());
	}

	@Override
	public void softDeleteArtefactById(String artefactId) {
		updateArtefactWithStatus(artefactId, ArtefactStatus.DELETED.getStatus());
	}

	@Override
	public void indexArtefact(String artefactId, ArtefactIndexDto artefactIndexDto, ArtefactStatus artefactStatus) {
		Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
		updatedValues.put(KEY_STATUS, buildAttrValueUpdate(artefactStatus.getStatus(), AttributeAction.PUT));
		updatedValues.put(KEY_MIRIS_DOCID, buildAttrValueUpdate(artefactIndexDto.getMirisDocId(), AttributeAction.PUT));
		DynamoDbHelper.updateAttributes(dynamoDbClient,
				SystemEnvironmentVariables.REGISTRY_TABLE_NAME, keysDocument(artefactId), updatedValues);
	}

	@Override
	public boolean isValidClassType(String type) {
		return Arrays.stream(classType.values()).anyMatch(val -> val.toString().equalsIgnoreCase(type));
	}

	public String getAllClassTypes() {
		return Arrays.toString(classType.values());
	}

	@Override
	public void updateArtefact(String artefactId, Map<String, String> attributMap) {
		Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
		attributMap.forEach(
				(attrKey, attrVal) -> updatedValues.put(attrKey, buildAttrValueUpdate(attrVal, AttributeAction.PUT)));
		DynamoDbHelper.updateAttributes(dynamoDbClient, SystemEnvironmentVariables.REGISTRY_TABLE_NAME,
				keysDocument(artefactId), updatedValues);
	}

	@Override
	public ArtefactMetadata getArtefactInfoById(String artefactId) {
		GetItemRequest r = GetItemRequest.builder()
			.key(keysDocument(artefactId))
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.build();
		Map<String, AttributeValue> result = dynamoDbClient.getItem(r).item();
		if (!result.isEmpty()) {
			return new AttributeValueToArtefactInfoMapper().apply(result);
		}
		return null;
	}

	@Override
	public Artefact createArtefact(Artefact artefact) {
		return artefact;
	}

	@Override
	public boolean hasFileWithSameDocId(String mirisDocId, String classType) {
		List<String> onlyOneArtefactClassTypes = List.of(ArtefactClassType.SOUND.name(),
				ArtefactClassType.MULTIMEDIA.name(), ArtefactClassType.BWLOGO.name(),
				ArtefactClassType.COLOURLOGO.name());

		if (onlyOneArtefactClassTypes.contains(classType.toUpperCase())) {
			List<Artefact> existingArtefacts = getArtefactbyMirisDocId(mirisDocId);
			log.info("existingArtefacts with docId {} are: {} ", mirisDocId, existingArtefacts);
			if (ArtefactClassType.BWLOGO.name().equalsIgnoreCase(classType)
					|| ArtefactClassType.COLOURLOGO.name().equalsIgnoreCase(classType)) {
				log.info("Found classType BWLOGO or COLOURLOGO");
				return replaceExistingLogo(existingArtefacts);
			}
			else if (ArtefactClassType.MULTIMEDIA.name().equalsIgnoreCase(classType)
					|| ArtefactClassType.SOUND.name().equalsIgnoreCase(classType)) {
				log.info("Found classType MULTIMEDIA or SOUND ");
				return replaceExistingMultimedia(existingArtefacts);
			}
		}
		return false;
	}

	private boolean replaceExistingLogo(List<Artefact> existingArtefacts) {
		List<String> existingLogoArtefactIds = existingArtefacts.stream()
			.filter(artefact -> artefact.getArtefactClassType().equals(ArtefactClassType.BWLOGO.name())
					|| artefact.getArtefactClassType().equals(ArtefactClassType.COLOURLOGO.name()))
			.map(Artefact::getId)
			.peek(existingArtefactId -> updateArtefactWithStatus(existingArtefactId, ArtefactStatus.DELETED.toString()))
			.toList();
		log.info("Replaced Artefact ids: {} size: {}", existingLogoArtefactIds, existingLogoArtefactIds.size());
		return !existingLogoArtefactIds.isEmpty();
	}

	private boolean replaceExistingMultimedia(List<Artefact> existingArtefacts) {
		List<String> existingMultimediaArtefactIds = existingArtefacts.stream()
			.filter(artefact -> artefact.getArtefactClassType().equals(ArtefactClassType.MULTIMEDIA.name())
					|| artefact.getArtefactClassType().equals(ArtefactClassType.SOUND.name()))
			.map(Artefact::getId)
			.peek(existingArtefactId -> updateArtefactWithStatus(existingArtefactId, ArtefactStatus.DELETED.toString()))
			.toList();
		log.info("Replaced Artefact ids: {} size: {}", existingMultimediaArtefactIds,
				existingMultimediaArtefactIds.size());
		return !existingMultimediaArtefactIds.isEmpty();
	}

	@Override
	public void updateParentIdInBatchItems(String parentId, List<String> batchItemsIds) {
		List<Map<String, AttributeValue>> itemsToUpdate = batchItemsIds.stream()
			.map(id -> Map.of("PK", AttributeValue.builder().s("artefacts#" + id).build(), "SK",
					AttributeValue.builder().s("document").build()))
			.toList();

		// updating the parentId to all the items in a batch
		Map<String, AttributeValueUpdate> updatedValueMap = Map.of("MERGED_ARTEFACT_ID",
				buildAttrValueUpdate(parentId, AttributeAction.PUT));

		for (Map<String, AttributeValue> itemKey : itemsToUpdate) {
			UpdateItemRequest request = UpdateItemRequest.builder()
				.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
				.key(itemKey)
				.attributeUpdates(updatedValueMap)
				.build();
			dynamoDbClient.updateItem(request);
		}
	}

	@Override
	public boolean isDocIdValid(String docId) {
		return mirisService.isDocIdValid(docId);
	}

	public enum classType {

		CERTIFICATE, DOCUMENT, BWLOGO, COLOURLOGO, SOUND, MULTIMEDIA

	}

	@Override
	public void saveArtefactsAtomic(Collection<IArtefact> artefacts) {
		saveArtefactsWithTransactions(artefacts);
	}

}
