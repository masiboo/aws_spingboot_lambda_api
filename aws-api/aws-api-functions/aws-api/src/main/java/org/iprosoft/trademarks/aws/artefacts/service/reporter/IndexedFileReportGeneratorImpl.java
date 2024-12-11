package org.iprosoft.trademarks.aws.artefacts.service.reporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.mapper.BatchOutputToBatchInputDynamoDbMapper;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.springframework.stereotype.Service;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Service
public class IndexedFileReportGeneratorImpl
		implements org.iprosoft.trademarks.aws.artefacts.service.reporter.IndexedFileReportGenerator {

	private final BatchService batchService;

	private final S3Service s3Service;

	public ReportGeneratorResult generateIndexFileReport() {
		ReportGeneratorResult reportGeneratorResult = new ReportGeneratorResult();
		List<BatchOutput> insertedBatchOutputs = batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus());
		List<BatchOutput> indexedBatchOutputs = batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus());
		Set<BatchOutput> uniqueBatchOutputs = new HashSet<>();
		uniqueBatchOutputs.addAll(insertedBatchOutputs);
		uniqueBatchOutputs.addAll(indexedBatchOutputs);
		List<BatchOutput> combinedBatchOutputs = new ArrayList<>(uniqueBatchOutputs);
		log.info("All BatchOutputs with status INSERTED and INDEXED from DB batchOutputs.size() {} |  batchOutputs: {}",
				combinedBatchOutputs.size(), combinedBatchOutputs);
		if (combinedBatchOutputs != null) {
			combinedBatchOutputs = filterBatchOutputByNoReportDate(combinedBatchOutputs);
			log.info("After filterBatchOutputByNoReportDate batchOutputs.size() {} | batchOutputs:{} ",
					combinedBatchOutputs.size(), combinedBatchOutputs);
		}
		if (combinedBatchOutputs == null || Objects.requireNonNull(combinedBatchOutputs).isEmpty()) {
			String errorMsg = String.format("No batchOutputs found where status is INSERTED and no reportDate  %s",
					combinedBatchOutputs);
			log.error(errorMsg);
			reportGeneratorResult.setErrorMsg(errorMsg);
			return reportGeneratorResult;
		}
		reportGeneratorResult = insertReport(combinedBatchOutputs);
		log.info(
				"reportGeneratorResult after insertReport(batchOutputs) reportGeneratorResult.getBatchOutputs() {} reportGeneratorResult.getErrorMsg() {}",
				reportGeneratorResult.getBatchOutputs(), reportGeneratorResult.getErrorMsg());
		return reportGeneratorResult;
	}

	public ReportGeneratorResult getAllBatchOutputReportByDate(String requiredReportDate) {
		ReportGeneratorResult reportGeneratorResult = new ReportGeneratorResult();
		List<BatchOutput> insertedBatchOutputs = batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus());
		List<BatchOutput> indexedBatchOutputs = batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus());
		Set<BatchOutput> uniqueBatchOutputs = new HashSet<>();
		if (insertedBatchOutputs != null) {
			uniqueBatchOutputs.addAll(insertedBatchOutputs);
		}
		if (indexedBatchOutputs != null) {
			uniqueBatchOutputs.addAll(indexedBatchOutputs);
		}
		List<BatchOutput> combinedBatchOutputs = new ArrayList<>(uniqueBatchOutputs);
		if (!combinedBatchOutputs.isEmpty()) {
			log.info(
					"All BatchOutputs with status INSERTED and INDEXED from DB batchOutputs.size() {} | batchOutputs: {}",
					combinedBatchOutputs.size(), combinedBatchOutputs);
			combinedBatchOutputs = filterBatchOutputByRequiredDate(combinedBatchOutputs, requiredReportDate);
			log.info("After filterBatchOutputByReportDate batchOutputs.size() {} | batchOutputs {}",
					combinedBatchOutputs.size(), combinedBatchOutputs);
		}
		else {
			reportGeneratorResult.setErrorMsg("No batch found");
			return reportGeneratorResult;
		}

		if (combinedBatchOutputs == null || Objects.requireNonNull(combinedBatchOutputs).isEmpty()) {
			String errorMsg = String.format(
					"No batchOutputs where batchOutput creation date greater than or equal to report date %s",
					combinedBatchOutputs);
			log.warn(errorMsg);
			reportGeneratorResult.setErrorMsg(errorMsg);
			return reportGeneratorResult;
		}
		reportGeneratorResult = insertReport(combinedBatchOutputs);
		return reportGeneratorResult;
	}

	public List<BatchOutput> filterBatchOutputByNoReportDate(List<BatchOutput> batchOutputs) {
		return batchOutputs.stream()
			.filter(batch -> (BatchStatus.INSERTED.name().equalsIgnoreCase(batch.getStatus())
					|| BatchStatus.INDEXED.name().equalsIgnoreCase(batch.getStatus()))
					&& (batch.getReportDate() == null || batch.getReportDate().isEmpty()))
			.collect(Collectors.toList());
	}

	private List<BatchOutput> filterBatchOutputByRequiredDate(List<BatchOutput> batchOutputs,
			String requiredReportDate) {
		return batchOutputs.stream()
			.filter(batch -> (BatchStatus.INSERTED.name().equalsIgnoreCase(batch.getStatus())
					|| BatchStatus.INDEXED.name().equalsIgnoreCase(batch.getStatus()))
					&& DateUtils.isSameDay(batch.getCreationDate(), requiredReportDate))
			.collect(Collectors.toList());
	}

	private ReportGeneratorResult insertReport(List<BatchOutput> batchOutputs) {
		ReportGeneratorResult reportGeneratorResult = new ReportGeneratorResult();
		File reportFile = org.iprosoft.trademarks.aws.artefacts.service.reporter.ExtentReportManager
			.makeReport(Objects.requireNonNull(batchOutputs));
		if (reportFile == null) {
			String errorMsg = "Report generation failed";
			log.error(errorMsg);
			reportGeneratorResult.setErrorMsg(errorMsg);
			return reportGeneratorResult;
		}
		String s3Bucket = SystemEnvironmentVariables.BATCH_S3_REPORTS_BUCKET;
		String s3Key = "Aws-index-file-report/" + DateUtils.getCurrentDateShortStr() + "/" + reportFile.getName();
		String s3url = putReportInS3(s3Bucket, s3Key, Objects.requireNonNull(reportFile));
		log.info("s3Bucket: {},  s3Key:{}, s3url: {} ", s3Bucket, s3Key, s3url);
		reportGeneratorResult.setBatchOutputs(addReportInBatchOutputs(s3Bucket, s3Key, s3url, batchOutputs));
		return reportGeneratorResult;
	}

	private List<BatchOutput> addReportInBatchOutputs(String s3Bucket, String s3Key, String s3Url,
			List<BatchOutput> batchOutputs) {
		BatchOutputToBatchInputDynamoDbMapper.BatchMappingResult result = BatchOutputToBatchInputDynamoDbMapper
			.map(s3Bucket, s3Key, s3Url, batchOutputs);
		log.info(
				"BatchOutputToBatchInputDynamoDbMapper.BatchMappingResult batchInputDynamoDbs.size: {} | batchOutputs.size: {} | "
						+ "batchOutputProcessingErrors.size {}",
				result.getBatchInputDynamoDbs().size(), result.getBatchOutputs().size(),
				result.getBatchOutputProcessingErrors().size());
		return result.getBatchOutputs();
	}

	private String putReportInS3(String s3Bucket, String s3Key, File reportFile) {
		return s3Service.putFile(s3Bucket, s3Key, reportFile, "text/html");
	}

	@Data
	public static class ReportGeneratorResult {

		private List<BatchOutput> batchOutputs;

		private String errorMsg;

		public ReportGeneratorResult() {
			batchOutputs = new ArrayList<>();
		}

	}

}
