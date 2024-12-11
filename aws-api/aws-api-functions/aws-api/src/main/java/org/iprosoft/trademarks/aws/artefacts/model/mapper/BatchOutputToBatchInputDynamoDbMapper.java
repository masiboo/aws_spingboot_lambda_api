package org.iprosoft.trademarks.aws.artefacts.model.mapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchInputDynamoDb;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.iprosoft.trademarks.aws.artefacts.util.SafeParserUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class BatchOutputToBatchInputDynamoDbMapper {

	public static BatchMappingResult map(String s3Bucket, String s3Key, String reportUrl,
			List<BatchOutput> batchOutputs) {
		log.info("s3Bucket: {}, s3Key: {}, reportUrl: {}, batchOutputs: {}", s3Bucket, s3Key, reportUrl, batchOutputs);
		List<BatchInputDynamoDb> batchInputDynamoDbs = new ArrayList<>();
		List<BatchOutput> updatedBatchOutputs = new ArrayList<>();
		List<BatchOutputProcessingError> batchOutputProcessingErrors = new ArrayList<>();

		batchOutputs.forEach(batchOutput -> {
			try {
				batchOutput.setReportDate(DateUtils.getCurrentDatetimeUtcStr());
				batchOutput.setReportUrl(reportUrl);
				batchOutput.setS3Bucket(s3Bucket);
				batchOutput.setS3Key(s3Key);
				updatedBatchOutputs.add(batchOutput);
				BatchInputDynamoDb batchInputDynamoDb = createBatchInputDynamoDb(batchOutput);
				batchInputDynamoDbs.add(batchInputDynamoDb);
			}
			catch (Exception e) {
				String errorMessage = String.format("Failed to process BatchOutput with ID %s: %s", batchOutput.getId(),
						e.getMessage());
				batchOutputProcessingErrors.add(new BatchOutputProcessingError(batchOutput.getId(), errorMessage));
				log.error(errorMessage, e);
			}
		});
		log.info("batchInputDynamoDbs: {}, updatedBatchOutputs: {}, batchOutputProcessingErrors: {}",
				batchInputDynamoDbs, updatedBatchOutputs, batchOutputProcessingErrors);
		return new BatchMappingResult(batchInputDynamoDbs, updatedBatchOutputs, batchOutputProcessingErrors);
	}

	private static BatchInputDynamoDb createBatchInputDynamoDb(BatchOutput batchOutput) {
		try {
			String correctReportDateTime = DateUtils.ensureCorrectFormat(batchOutput.getReportDate());
			ZonedDateTime reportDate = SafeParserUtil.safeParseZonedDateTime(correctReportDateTime);
			String correctCreationDateTime = DateUtils.ensureCorrectFormat(batchOutput.getCreationDate());
			ZonedDateTime creationDateTime = SafeParserUtil.safeParseZonedDateTime(correctCreationDateTime);
			return BatchInputDynamoDb.builder()
				.id(parseLong(batchOutput.getId()))
				.batchSequence(batchOutput.getBatchSequence())
				.s3Bucket(batchOutput.getS3Bucket())
				.s3Key(batchOutput.getS3Key())
				.reportUrl(batchOutput.getReportUrl())
				.reportDate(reportDate)
				.additionalProperties(batchOutput.getAdditionalProperties())
				.creationDate(creationDateTime)
				.lastModificationDate(reportDate)
				.status(batchOutput.getStatus())
				.requestType(batchOutput.getRequestType())
				.lastModUser(batchOutput.getLastModUser())
				.lockedBy(batchOutput.getLockedBy())
				.build();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Long parseLong(String value) {
		try {
			return Long.valueOf(value);
		}
		catch (NumberFormatException e) {
			// Handle the exception, for example, by logging it
			log.warn("Invalid number format: " + value + ". Return random long id.");
			Random random = new Random();
			return random.nextLong();
		}
	}

	@Data
	public static class BatchMappingResult {

		private final List<BatchInputDynamoDb> batchInputDynamoDbs;

		private final List<BatchOutput> batchOutputs;

		private final List<BatchOutputProcessingError> batchOutputProcessingErrors;

		public BatchMappingResult(List<BatchInputDynamoDb> batchInputDynamoDbs, List<BatchOutput> batchOutputs,
				List<BatchOutputProcessingError> batchOutputProcessingErrors) {
			this.batchInputDynamoDbs = batchInputDynamoDbs;
			this.batchOutputs = batchOutputs;
			this.batchOutputProcessingErrors = batchOutputProcessingErrors;
		}

	}

	@Data
	public static class BatchOutputProcessingError {

		private String batchOutputId;

		private String errorMessage;

		public BatchOutputProcessingError(String batchOutputId, String errorMessage) {
			this.batchOutputId = batchOutputId;
			this.errorMessage = errorMessage;
		}

	}

}
