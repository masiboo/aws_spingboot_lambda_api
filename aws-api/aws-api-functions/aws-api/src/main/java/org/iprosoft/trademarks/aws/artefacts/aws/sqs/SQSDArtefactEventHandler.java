package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.iprosoft.trademarks.aws.artefacts.aws.audit.AuditEventService;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventDetail;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DynamoDBEventDTO;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys;

@AllArgsConstructor
@Slf4j
public class SQSDArtefactEventHandler implements Function<SQSEvent, String>, DbKeys {

	private final static String audit_evnt_type = "SQSDBatchEvent";

	private static final Logger logger = LoggerFactory.getLogger(SQSDBEventHandler.class);

	private final RestTemplate restTemplate;

	private static final String API_URL = SystemEnvironmentVariables.Aws_CORE_DB_INIT_ACCESS_URL + "/api/v1/artefacts";

	private static final String DEFAULT_BUCKET = SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET;

	private final MediaProcessingService mediaProcessingService;

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	private final BatchService batchService;

	private final DynamoDbClient dynamoDBClient;

	private final AuditEventService auditEventService;

	@Override
	public String apply(SQSEvent sqsEvent) {

		try {
			for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
				processMessage(message);
			}
		}
		catch (Exception e) {
			handleProcessingError(e);
		}
		return "";
	}

	private void processMessage(SQSEvent.SQSMessage message) throws JsonProcessingException {
		String messageBody = message.getBody();
		log.info("Processing SQS message with body: {}", messageBody);

		DynamoDBEventDTO sqsDDBEventData = objectMapper.readValue(messageBody, DynamoDBEventDTO.class);
		DDBEventDetail ddbEventDetail = sqsDDBEventData.getDetail();

		String eventId = sqsDDBEventData.getId();
		log.debug("Parsed DDB event detail: {}", ddbEventDetail);

		// Audit the message receipt
		auditEventService.saveAuditEvent(audit_evnt_type,
				String.format("Received SQS message for processing [MessageId: %s]", message.getMessageId()), eventId);

		if (!ddbEventDetail.getStatus().equalsIgnoreCase("INIT")) {
			try {
				// Audit before processing
				auditEventService.saveAuditEvent(audit_evnt_type,
						String.format("Starting DynamoDB to REST processing for artefact [ArtefactId: %s]",
								ddbEventDetail.getArtefactId()),
						eventId);

				dynamoToRestClient(ddbEventDetail.getArtefactId(), ddbEventDetail);

				// Audit successful processing
				auditEventService.saveAuditEvent(audit_evnt_type, String.format(
						"Successfully processed artefact [ArtefactId: %s]", ddbEventDetail.getArtefactId()), eventId);
			}
			catch (Exception e) {
				// Audit processing failure
				String errorMessage = String.format("Failed to process artefact [ArtefactId: %s]. Error: %s",
						ddbEventDetail.getArtefactId(), e.getMessage());
				log.error(errorMessage, e);
				auditEventService.saveAuditEvent(audit_evnt_type, errorMessage, eventId);
				throw e;
			}
		}
		else {
			log.info("Skipping INIT status message for artefact [ArtefactId: {}]", ddbEventDetail.getArtefactId());
		}
	}

	public static class ProcessingStats {

		private int successful;

		private final List<FailedItem> failed;

		public ProcessingStats() {
			this.successful = 0;
			this.failed = new ArrayList<>();
		}

		public void addSuccess() {
			this.successful++;
		}

		public void addFailure(Map<String, AttributeValue> item, String error) {
			failed.add(new FailedItem(item, error, ZonedDateTime.now(ZoneOffset.UTC)));
		}

		public int getSuccessful() {
			return successful;
		}

		public List<FailedItem> getFailed() {
			return failed;
		}

		public double getSuccessRate() {
			int total = successful + failed.size();
			return total > 0 ? (double) successful / total * 100 : 0.0;
		}

	}

	public static class FailedItem {

		final Map<String, AttributeValue> item;

		final String error;

		final String timestamp;

		public FailedItem(Map<String, AttributeValue> item, String error, ZonedDateTime timestamp) {
			this.item = item;
			this.error = error;
			this.timestamp = formatDateTime(timestamp);
		}

	}

	private static String formatDateTime(ZonedDateTime dt) {
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
	}

	private String processArtefactClass(AttributeValue artefactClass) {
		if (artefactClass != null && artefactClass.s() != null) {
			String[] parts = artefactClass.s().split("#");
			return parts.length > 1 ? parts[parts.length - 1] : artefactClass.s();
		}
		return "";
	}

	private double getContentLength(AttributeValue size) {
		if (size != null && size.s() != null) {
			try {
				return Double.parseDouble(size.s());
			}
			catch (NumberFormatException e) {
				logger.warn("Invalid size value: {}", size.s());
			}
		}
		return 0.0;
	}

	private ProcessingStats processItems(List<Map<String, AttributeValue>> items, DDBEventDetail ddbEventDetail) {
		ProcessingStats stats = new ProcessingStats();

		for (int i = 0; i < items.size(); i++) {
			Map<String, AttributeValue> item = items.get(i);
			try {
				pushItem(item, i, stats, ddbEventDetail);
			}
			catch (Exception e) {
				logger.error("Unexpected error processing item {}: {}", i + 1, e.getMessage());
				stats.addFailure(item, e.getMessage());
			}
		}
		return stats;
	}

	private void handleProcessingError(Exception e) {
		String eventId = UUID.randomUUID().toString();
		String errorMessage = String.format("Failed to process SQS event. Error: %s", e.getMessage());
		log.error(errorMessage, e);
		auditEventService.saveAuditEvent(audit_evnt_type, errorMessage, eventId);
		throw new RuntimeException("Artefact Creation Failed", e);
	}

	private void pushItem(Map<String, AttributeValue> item, int i, ProcessingStats stats,
			DDBEventDetail ddbEventDetail) {
		ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
		Map<String, Object> artefact = new HashMap<>();
		Map<String, Object> artefactItem = new HashMap<>();

		// Build artefact object
		artefact.put("status", getAttributeValue(item, "status", "INSERTED"));
		artefact.put("artefactClass", processArtefactClass(item.get("type")));
		artefact.put("indexationDate",
				formatDateTime(parseDateTime(getAttributeValue(item, "indexationDate", formatDateTime(currentTime)))));
		artefact.put("archiveDate",
				formatDateTime(parseDateTime(getAttributeValue(item, "archiveDate", formatDateTime(currentTime)))));
		artefact.put("s3Bucket", getAttributeValue(item, "s3Bucket", DEFAULT_BUCKET));
		artefact.put("mirisDocId", getAttributeValue(item, "mirisDocId", ""));
		artefact.put("lastModificationUser", getAttributeValue(item, "userId", "system"));
		artefact.put("artefactUUID", getAttributeValue(item, "artefactId", ""));
		artefact.put("artefactName", getAttributeValue(item, "fileName", ddbEventDetail.getArtefact().getArtefactId()));

		// Build artefactItem
		artefactItem.put("indexationDate",
				formatDateTime(parseDateTime(getAttributeValue(item, "indexationDate", formatDateTime(currentTime)))));
		artefactItem.put("contentType", getAttributeValue(item, "fileType", "application/octect-stream"));
		artefactItem.put("s3Key", getAttributeValue(item, "s3Key", ""));
		artefactItem.put("totalPages", Integer.parseInt(getAttributeValue(item, "totalPages", "1")));
		artefactItem.put("fileName", getAttributeValue(item, "fileName", ddbEventDetail.getArtefact().getArtefactId()));
		artefactItem.put("contentLength", getContentLength(item.get("size")));

		artefact.put("artefactItems", Collections.singletonList(artefactItem));

		try {
			logger.info("API URL {}", API_URL);
			ResponseEntity<String> response = restTemplate.postForEntity(API_URL, artefact, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				log.info("Successfully posted item {} to REST API: {}", i + 1, response.getBody());
				auditEventService.saveAuditEvent(audit_evnt_type,
						String.format("Successfully posted item %d to REST API for artefact [ArtefactId: %s]", i + 1,
								ddbEventDetail.getArtefactId()),
						ddbEventDetail.getArtefactId());
				stats.addSuccess();
			}
			else {
				String errorMessage = String.format("Failed to post item %d to REST API. Status code: %s, Response: %s",
						i + 1, response.getStatusCode(), response.getBody());
				throw new RuntimeException(errorMessage);
			}
		}
		catch (Exception e) {
			String errorMessage = String.format("Error processing item %d: %s", i + 1, e.getMessage());
			log.error(errorMessage, e);
			auditEventService.saveAuditEvent(audit_evnt_type,
					String.format("Failed to process item %d for artefact [ArtefactId: %s]. Error: %s", i + 1,
							ddbEventDetail.getArtefactId(), e.getMessage()),
					ddbEventDetail.getArtefactId());
			stats.addFailure(item, e.getMessage());
		}
	}

	private String getAttributeValue(Map<String, AttributeValue> item, String key, String defaultValue) {
		AttributeValue attr = item.get(key);
		return attr != null && attr.s() != null ? attr.s() : defaultValue;
	}

	private ZonedDateTime parseDateTime(String dateTimeStr) {
		try {
			return ZonedDateTime.parse(dateTimeStr);
		}
		catch (Exception e) {
			return ZonedDateTime.now(ZoneOffset.UTC);
		}
	}

	public void dynamoToRestClient(String artefactId, DDBEventDetail ddbEventDetail) {

		try {

			GetItemRequest r = GetItemRequest.builder()
				.key(keysDocument(artefactId))
				.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
				.build();
			Map<String, AttributeValue> result = dynamoDBClient.getItem(r).item();
			log.warn("getArtefactById  artefactId {}", artefactId);
			if (!result.isEmpty()) {
				log.warn(" getArtefactById Query successful.");
				log.warn(result.toString());

			}
			else {
				logger.info("No items returned from query");

			}

			List<Map<String, AttributeValue>> items = Collections.singletonList(result);
			processItems(items, ddbEventDetail);

		}
		catch (DynamoDbException e) {
			logger.error("Error querying DynamoDB on page {}: ", e.getMessage());
			// throw
		}

	}

}
