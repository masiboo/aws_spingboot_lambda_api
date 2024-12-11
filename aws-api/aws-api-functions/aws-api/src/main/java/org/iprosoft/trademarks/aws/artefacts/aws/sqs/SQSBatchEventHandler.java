package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchEventDTO;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.SqsHelperUtility;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class SQSBatchEventHandler implements Function<SQSEvent, String> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	private final BatchService batchService;

	public String apply(SQSEvent s3event) {

		try {
			log.info("Event Dump: " + s3event);
			for (SQSEvent.SQSMessage message : s3event.getRecords()) {

				String messageBody = message.getBody();
				BatchEventDTO sqsBatchData = objectMapper.readValue(messageBody, BatchEventDTO.class);

				String batchSequence = sqsBatchData.getDetail().getBatchSequence();

				String scanType = null; // miris docId exist within the files - not at the
										// batch level!
				try {
					scanType = sqsBatchData.getDetail().getBatch().getScanType();
				}
				catch (Exception e) {
					// no miris docid
					log.info("no miris docid: " + s3event);
				}

				List<ArtefactBatch> batchItems = extractBatchItems(sqsBatchData.getDetail().getBatchSequence());
				log.info("extractBatchItems items {}", batchItems);

				if (isMergeEligibleEvent(sqsBatchData, batchItems)) {
					log.info("batchItems  is merge eligible event");
					Set<String> mergeSet = new HashSet<>();
					for (ArtefactBatch item : batchItems) {
						// extract group set #todo:mpd807
						mergeSet.add(item.getArtefactMergeId());
					}

					for (String merge : mergeSet) {
						List<ArtefactBatch> subGroup = new ArrayList<>();
						String mirisDocID = null;
						for (ArtefactBatch item : batchItems) {
							// #todo:mpd807
							if (Objects.equals(item.getArtefactMergeId(), merge)) {
								subGroup.add(item);
								mirisDocID = item.getMirisDocId(); // all have the same
																	// docId
							}
						}

						// FIXME: merge event if successful needs the new artefact-id to
						// attach to Batch
						mergeFiles(batchSequence, subGroup, mirisDocID, merge);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("error while handling the SQSBatchEvents", e);
			throw new RuntimeException(e);
		}
		return "";

	}

	private void mergeFiles(String batchSeqId, List<ArtefactBatch> batchItems, String mirisDocId, String mergeId) {

		String AwsCoreMediaService = SystemEnvironmentVariables.Aws_CORE_MEDIA_PROCESS_API_URL;
		String AwsCoreApiUrl = AwsCoreMediaService + "/api/v1/convert-tiff-to-pdf";
		try {
			String requestBody;
			if (mirisDocId != null) {
				requestBody = prepareRequest(batchSeqId, batchItems, mergeId, mirisDocId);
			}
			else {
				requestBody = prepareRequest(batchSeqId, batchItems, mergeId, null);
			}

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(AwsCoreApiUrl))
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.header("Content-Type", "application/json")
				.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("response of the merge API : {}", response.body());

			if (response.statusCode() != HttpStatusCode.OK)
				throw new RuntimeException("Merging TIFF documents is unsuccessful : " + response.body());

			// return new merged artifact ID
		}
		catch (JsonProcessingException e) {
			log.error("Exception while preparing the merge request", e);
			throw new RuntimeException(e);
		}
		catch (IOException | InterruptedException e) {
			log.error("Exception while merge api call", e);
			throw new RuntimeException(e);
		}
		log.info("..CONVERTED");
	}

	private String prepareRequest(String batchSequence, List<ArtefactBatch> batchItems, String mergeId,
			String mirisDocId) throws JsonProcessingException {
		String bucket = SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET;

		List<String> s3Keys = batchItems.stream().map(Artefact::getS3Key).collect(Collectors.toList());

		Map<String, String> metaData = null;
		if (mirisDocId != null) {
			metaData = Map.of("batchSequence", batchSequence, "mirisDocId", mirisDocId, "mergeId", mergeId);
		}
		else {
			metaData = Map.of("batchSequence", batchSequence, "mergeId", mergeId);
		}

		Map<String, Object> mergeReqMap = Map.of("bucket", bucket, "key", s3Keys, "metadata", metaData);

		return objectMapper.writeValueAsString(mergeReqMap);
	}

	private boolean isMergeEligibleEvent(BatchEventDTO sqsBatchData, List<ArtefactBatch> batchItems) {
		log.info("isMergeEligibleEvent event {} and artefact {}", sqsBatchData, batchItems);
		String batchStatus = sqsBatchData.getDetail().getBatch().getBatchStatus();
		return isMergeEligibleUpdate(batchStatus) && SqsHelperUtility.isMergeEligibleArtefact(batchItems);
	}

	private boolean isMergeEligibleUpdate(String batchStatus) {

		return ArtefactStatus.INSERTED.getStatus().equalsIgnoreCase(batchStatus)
				|| ArtefactStatus.INDEXED.getStatus().equalsIgnoreCase(batchStatus);
	}

	private List<ArtefactBatch> extractBatchItems(String batchSequence) {
		List<ArtefactOutput> batchItems = batchService.getAllArtefactsForBatch(batchSequence, "artefact");
		List<ArtefactBatch> artefactitems = CollectionUtils.isEmpty(batchItems) ? Collections.emptyList()
				: batchItems.stream().map(x -> artefactService.getArtectBatchById(x.getId())).toList();
		return artefactitems;
	}

}
