package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.service.metadata.MetadataService;
import org.iprosoft.trademarks.aws.artefacts.util.AppConstants;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.DbKeys.*;
import static org.iprosoft.trademarks.aws.artefacts.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;

@AllArgsConstructor
@Slf4j
public class SQSS3EventHandler implements Function<SQSEvent, String> {

	private final MetadataService metadataService;

	private final MediaProcessingService mediaProcessingService;

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	private final BatchService batchService;

	private final DynamoDbClient dynamoDbClient;

	@Override
	public String apply(SQSEvent s3event) {
		String artefactId = "";
		Map<String, String> metadataMap = null;
		long startTime = System.currentTimeMillis();
		try {
			log.info("Event Dump: " + s3event);
			for (SQSEvent.SQSMessage message : s3event.getRecords()) {
				String messageBody = message.getBody();
				S3EventDTO payloadDto = objectMapper.readValue(messageBody, S3EventDTO.class);
				log.warn("payloadDto {}", payloadDto);
				String srcBucket = payloadDto.getDetail().getBucket().getName();
				String srcKey = payloadDto.getDetail().getObject().getKey();

				S3ObjectMetadata md = s3Service.getObjectMetadata(srcBucket, srcKey);

				if (md.isObjectExists()) {
					String isMergedFileEvent = md.getMetadata()
						.getOrDefault(AppConstants.METADATA_KEY_IS_MERGED_FILE, null);
					if (Boolean.toString(true).equalsIgnoreCase(isMergedFileEvent)) {
						createMergedArtefact(srcBucket, srcKey, md);
					}
					else {
						metadataMap = md.getMetadata();
						log.info("object tgs {}", metadataMap.toString());
						log.info("artefact id: {} ", metadataMap.get(AppConstants.METADATA_KEY_ARTEFACT_ID));
						log.info("job id: {}", metadataMap.get(AppConstants.METADATA_KEY_TRACE_ID));

						String status;
						String jobStatus;
						if (!metadataService.isMetadataValid(metadataMap)) {
							log.error("Metadata validation failed artefact&Job status will become ERROR");
							status = jobStatus = ArtefactStatus.ERROR.getStatus();
						}
						else {
							// Since the validation has been done already either
							// mirisDocId or
							// batchSeqId should be present
							status = getStatusByMetadata(metadataMap);
							jobStatus = ArtefactStatus.UPLOADED.getStatus();

							// update S3Object metadata tags for media items
							artefactId = metadataMap.get(AppConstants.METADATA_KEY_ARTEFACT_ID);
							Artefact artefact = artefactService.getArtefactById(artefactId);
							String mediaType;
							if (artefact != null) {
								mediaType = artefact.getArtefactClassType();
							}
							else {
								mediaType = "";
							}

							List<String> mediaTypes = List.of(ArtefactClassType.SOUND.name(),
									ArtefactClassType.BWLOGO.name(), ArtefactClassType.COLOURLOGO.name(),
									ArtefactClassType.MULTIMEDIA.name());
							if (mediaTypes.stream().anyMatch(type -> type.equalsIgnoreCase(mediaType))) {
								Map<String, String> bucketDtlMap = Map.of("bucket", srcBucket, "key", srcKey);
								// When media-processor down or any error while metadata
								// extraction JobStatus => ERROR , ArtefactStatus => INIT
								// MPD-688

								try {

									boolean mediaStatus = updateArtefactAndTags(md, bucketDtlMap, mediaType);
									if (!mediaStatus) {
										jobStatus = ArtefactStatus.ERROR.getStatus();
										status = ArtefactStatus.INIT.getStatus();
									}

								}
								catch (RuntimeException e) {

									jobStatus = ArtefactStatus.ERROR.getStatus();
									status = ArtefactStatus.INIT.getStatus();

									log.warn("updateJobWithStatus: 1");

									artefactJobService.updateJobWithStatus(
											metadataMap.get(AppConstants.METADATA_KEY_TRACE_ID), jobStatus);
									artefactService.updateArtefactWithStatus(
											metadataMap.get(AppConstants.METADATA_KEY_ARTEFACT_ID), status);

									throw new RuntimeException(e);
								}

							}
						}
						log.warn("updateJobWithStatus: 2");
						if (!jobStatus.equalsIgnoreCase(ArtefactStatus.UPLOADED.getStatus())) {
							log.warn("jobStatus {} is updating other than UPLOADED", jobStatus);
							log.warn("job id: {}", metadataMap.get(AppConstants.METADATA_KEY_TRACE_ID));
						}
						artefactJobService.updateJobWithStatus(metadataMap.get(AppConstants.METADATA_KEY_TRACE_ID),
								jobStatus);
						artefactService.updateArtefactWithStatus(metadataMap.get(AppConstants.METADATA_KEY_ARTEFACT_ID),
								status);

						// only for batch events
						updateBatchStatus(artefactId);

					}
				}

			}

		}
		catch (IOException | InterruptedException e) {

			registerJobStatus(metadataMap.get(AppConstants.METADATA_KEY_TRACE_ID));
			log.error("error while handling the SQSEvents", e);
			throw new RuntimeException(e);
		}
		if (SystemEnvironmentVariables.Aws_SQS_INSTRUMENTAL) {
			long endTime = System.currentTimeMillis();
			long executionTime = endTime - startTime;
			Instant timestamp = Instant.now();
			log.warn("Execution time: " + executionTime + " milliseconds");
			ZonedDateTime insertedDate = ZonedDateTime.now();
			String shortDate = DateUtils.getShortDateFromZonedDateTime(insertedDate);
			String fullDate = DateUtils.getFullDateFromZonedDateTime(insertedDate);
			Map<String, AttributeValue> values = new HashMap<>();
			addS(values, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
			addS(values, GSI1_SK, fullDate + TAG_DELIMINATOR + artefactId);
			addS(values, "ExecutionTime", String.valueOf(executionTime));
			addS(values, "SQSS3EventHandlerCalled", String.valueOf(AppConstants.SQS_S3_EVENT_INVOCATION_COUNTER));
			addS(values, "Timestamp", timestamp.toString());
			saveExecutionTime(values);
			AppConstants.SQS_S3_EVENT_INVOCATION_COUNTER += 1;
		}
		return "";
	}

	private void updateBatchStatus(String artefactId) {
		ArtefactBatch artefactBatch = artefactService.getArtectBatchById(artefactId);
		if (artefactBatch.getBatchSequence() != null) {
			log.info("batch event for artefact, {}", artefactBatch);
			batchService.updateBatchIfAllInserted(artefactBatch.getBatchSequence());
		}
	}

	private void registerJobStatus(String jobId) {
		log.warn("updateJobWithStatus: 3");
		artefactJobService.updateJobWithStatus(jobId, ArtefactStatus.ERROR.getStatus());
	}

	private void createMergedArtefact(String srcBucket, String srcKey, S3ObjectMetadata md) {
		log.warn("createMergedArtefact event: key {}, and metadata {}", srcKey, md.getMetadata());
		String artefactId = md.getMetadata().get(AppConstants.METADATA_KEY_ARTEFACT_ID);
		String jobId = md.getMetadata().get(AppConstants.METADATA_KEY_TRACE_ID);

		List<ArtefactTag> tags = new ArrayList<>();
		String batchSeq = md.getMetadata().get(AppConstants.METADATA_KEY_BATCH_SEQ);

		IArtefact item = new ArtefactDynamoDb(artefactId, DateUtils.getCurrentDatetimeUtc(), "Anonymous");
		item.setBucket(srcBucket);
		item.setKey(srcKey);

		// Addendum ONLY
		String mirisDocId = md.getMetadata().getOrDefault(AppConstants.METADATA_KEY_MIRIS_DOCID, null);

		// Persist object to DynamoDB
		item.setMirisDocId(mirisDocId);
		item.setArtefactClassType(ArtefactClassType.DOCUMENT.toString());
		item.setStatus(getStatusByMetadata(md.getMetadata()));
		item.setSizeWarning(false);
		String fileName = this.getFileNameFromKey(srcKey);
		item.setFileName(fileName != null ? fileName : AppConstants.MERGED_PDF_ARTEFACT_NAME);
		tags.add(ArtefactTag.builder()
			.documentId(artefactId)
			.key("untagged")
			.value("true")
			.insertedDate(DateUtils.getCurrentDatetimeUtc())
			.userId("Anonymous")
			.documentTagType(DocumentTagType.USERDEFINED)
			.build());

		log.warn("tags event: item {}, and tags {}", item, tags);
		artefactService.saveDocument(item, tags);

		ArtefactJob job = new ArtefactJob().withId(jobId)
			.withArtefactId(artefactId)
			.withStatus(ArtefactStatus.INSERTED.getStatus());

		log.warn("job event: job {}, and batchSeq {}", job, batchSeq);
		artefactJobService.saveJob(job);

		BatchInputDynamoDb batch = new BatchInputDynamoDb().withBatchSequence(batchSeq);
		ArtefactDynamoDb artefactItem = new ArtefactDynamoDb();
		artefactItem.setArtefactItemId(artefactId);

		batch.setArtefacts(List.of(artefactItem));

		log.warn("STEP @@@@ createAndSaveBatchChildren @@@@ Creating batch {}", batch);
		batchService.saveBatchSequenceWithChildren(batch);

		// update the parent/mergedArtefactId on each item in the batch
		List<String> batchItemsIds = extractBatchItemsId(batchSeq, artefactId);
		log.warn("batchItemsIds items {}", batchItemsIds);
		if (batchItemsIds != null && !batchItemsIds.isEmpty())
			artefactService.updateParentIdInBatchItems(artefactId, batchItemsIds);

		// #todo:mpd807 ? needed ??
		updateBatchStatus(artefactId);
		log.warn("createMergedArtefact return");
	}

	private List<String> extractBatchItemsId(String batchSequence, String parentId) {
		List<String> artefactIds = Collections.emptyList();
		List<ArtefactOutput> artefactList = batchService.getAllArtefactsForBatch(batchSequence, "artefact");
		if (artefactList != null && !artefactList.isEmpty()) {
			artefactIds = artefactList.stream()
				.filter(artefactOutput -> !artefactOutput.getId().equalsIgnoreCase(parentId))
				.map(ArtefactOutput::getId)
				.toList();
		}
		return artefactIds;
	}

	private String getStatusByMetadata(Map<String, String> metadataMap) {
		String status = null;
		if (StringUtils.hasText(metadataMap.get(AppConstants.METADATA_KEY_MIRIS_DOCID))) {
			status = ArtefactStatus.INDEXED.getStatus();

			// #todo - check if all artefact (or job)
		}
		else if (StringUtils.hasText(metadataMap.get(AppConstants.METADATA_KEY_BATCH_SEQ))) {
			status = ArtefactStatus.INSERTED.getStatus();

			// #todo -
		}
		return status;
	}

	public boolean updateArtefactAndTags(S3ObjectMetadata md, Map<String, String> bucketDtlMap, String mediaType)
			throws IOException, InterruptedException {
		boolean isUpdateSuccess = false;

		if (md == null || bucketDtlMap == null || mediaType == null) {
			log.error("Invalid arguments provided: md={}, bucketDtlMap={}, mediaType={}", md, bucketDtlMap, mediaType);
			throw new IllegalArgumentException("Invalid arguments passed to updateArtefactAndTags");
		}

		Map<String, String> objectTagMap = new HashMap<>();
		objectTagMap.put("fileType", md.getContentType() != null ? md.getContentType() : "unknown");
		objectTagMap.put("size", md.getContentLength() != null ? md.getContentLength().toString() : "unknown");
		objectTagMap.put("mediaType", mediaType);

		if (!bucketDtlMap.containsKey("bucket") || !bucketDtlMap.containsKey("key")) {
			log.error("Bucket details are missing or incomplete in bucketDtlMap: {}", bucketDtlMap);
			throw new IllegalArgumentException("Bucket details are missing in bucketDtlMap");
		}

		try {
			// Invoke media-processor to extract metadata
			MultimediaFileResponse mediaProcessResp = mediaProcessingService.insertMultimediaMetadata(bucketDtlMap);
			if (mediaProcessResp == null || mediaProcessResp.getS3ObjectTags() == null) {
				log.warn("No media processing response or S3ObjectTags are null for mediaType: {}", mediaType);
			}
			else {
				switch (mediaType.toUpperCase()) {
					case "SOUND" -> {
						String bitDepth = mediaProcessResp.getS3ObjectTags().getBitDepth();
						String samplingFrequency = mediaProcessResp.getS3ObjectTags().getSamplingFrequency();
						String totalDuration = mediaProcessResp.getS3ObjectTags().getTotalDuration();
						objectTagMap.put("bitDepth", bitDepth != null ? bitDepth : "unknown");
						objectTagMap.put("samplingFrequency",
								samplingFrequency != null ? samplingFrequency : "unknown");
						objectTagMap.put("totalDuration", totalDuration != null ? totalDuration : "unknown");
					}
					case "BWLOGO", "COLOURLOGO" -> {
						String resolutionInDpi = mediaProcessResp.getS3ObjectTags().getResolutionInDpi();
						objectTagMap.put("resolutionInDpi", resolutionInDpi != null ? resolutionInDpi : "unknown");
					}
					case "MULTIMEDIA" -> {
						String format = mediaProcessResp.getS3ObjectTags().getFormat();
						String codec = mediaProcessResp.getS3ObjectTags().getCodec();
						String frameRate = mediaProcessResp.getS3ObjectTags().getFrameRate();
						String totalDuration = mediaProcessResp.getS3ObjectTags().getTotalDuration();
						objectTagMap.put("format", format != null ? format : "unknown");
						objectTagMap.put("codec", codec != null ? codec : "unknown");
						objectTagMap.put("frameRate", frameRate != null ? frameRate : "unknown");
						objectTagMap.put("totalDuration", totalDuration != null ? totalDuration : "unknown");
					}
					case "CERTIFICATE", "DOCUMENT", "PART" -> {
						// Do nothing for these media types, just continue
					}
					default -> {
						log.warn("Unsupported media type: {}", mediaType);
						return false;
					}
				}
				log.info("Media metadata extracted and S3 tags updated for mediaType: {}", mediaType);
				if (bucketDtlMap.get("bucket") != null && bucketDtlMap.get("key") != null) {
					s3Service.setObjectTag(bucketDtlMap.get("bucket"), bucketDtlMap.get("key"), objectTagMap);
					String artefactId = md.getMetadata().get(AppConstants.METADATA_KEY_ARTEFACT_ID);

					if (artefactId != null) {
						artefactService.updateArtefact(artefactId, objectTagMap);
						isUpdateSuccess = true;
					}
					else {
						log.warn("No artefactId found in metadata for object: {}", bucketDtlMap.get("key"));
					}
				}
				else {
					log.error("Bucket or key information is missing from bucketDtlMap");
					throw new IllegalArgumentException("Invalid bucket or key in bucketDtlMap");
				}
			}
		}
		catch (Exception e) {
			log.error("Unexpected error occurred while updating artefact and tags {}", e.getMessage());
			throw new RuntimeException("Failed to update artefact and tags", e);
		}
		return isUpdateSuccess;
	}

	private String getFileNameFromKey(String inputKey) {

		// Split the string by forward slash "/"
		String[] parts = inputKey.split("/");

		// Extract and return the last part
		if (parts.length > 0) {
			String lastPart = parts[parts.length - 1];
			return lastPart;
		}
		else {
			log.error("No parts found.");
			return null;
		}
	}

	void addS(final Map<String, AttributeValue> map, final String key, final String value) {
		if (value != null) {
			map.put(key, AttributeValue.builder().s(value).build());
		}
	}

	private void saveExecutionTime(final Map<String, AttributeValue> values) {
		PutItemRequest put = PutItemRequest.builder()
			.tableName(SystemEnvironmentVariables.REGISTRY_TABLE_NAME)
			.item(values)
			.build();
		try {
			dynamoDbClient.putItem(put);
		}
		catch (Exception e) {
			log.error("SQS dynamoDB error : {}", e.getMessage());
		}

	}

}