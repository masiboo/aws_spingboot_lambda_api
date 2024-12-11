package org.iprosoft.trademarks.aws.artefacts.service.reporter;

public interface IndexedFileReportGenerator {

	IndexedFileReportGeneratorImpl.ReportGeneratorResult generateIndexFileReport();

	IndexedFileReportGeneratorImpl.ReportGeneratorResult getAllBatchOutputReportByDate(String reportDate);

}
