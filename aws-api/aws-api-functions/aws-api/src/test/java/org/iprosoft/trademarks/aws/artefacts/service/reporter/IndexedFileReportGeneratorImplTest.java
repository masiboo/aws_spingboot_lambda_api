package org.iprosoft.trademarks.aws.artefacts.service.reporter;

import lombok.extern.slf4j.Slf4j;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.iprosoft.trademarks.aws.artefacts.TestData.getS3HttpsUrl;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class IndexedFileReportGeneratorImplTest {

	private BatchService batchService;

	private S3Service s3Service;

	private IndexedFileReportGeneratorImpl indexedFileReport;

	@BeforeEach
	void setUp() {
		batchService = createMock(BatchService.class);
		s3Service = createMock(S3Service.class);
		indexedFileReport = new IndexedFileReportGeneratorImpl(batchService, s3Service);
	}

	@Test
	void generateIndexFileReportSuccessful() throws MalformedURLException {
		// Arrange
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus()))
			.andReturn(TestData.getBatchOutputListWithNoReportDate());
		expect(batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus()))
			.andReturn(TestData.getBatchOutputListWithNoReportDate());
		Capture<File> capturedReportFile = newCapture(CaptureType.ALL);
		// Mock S3 service methods
		expect(s3Service.putFile(anyString(), anyString(), capture(capturedReportFile), eq("text/html")))
			.andReturn(getS3HttpsUrl().toString())
			.anyTimes();

		replay(batchService, s3Service);

		// Act
		indexedFileReport.generateIndexFileReport();

		// Assert
		verify(batchService);
		verify(s3Service);
		assertNotNull(capturedReportFile.getValue(), "Report file should be captured");
		assertFalse(capturedReportFile.getValue().getName().isEmpty(), "Captured report file name should be empty'");
		assertTrue(capturedReportFile.getValue().exists(), "Captured report file should exist");
	}

	@Test
	void generateIndexFileReportWhenNoDateExistSuccessful() throws MalformedURLException {
		// Arrange
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus()))
			.andReturn(TestData.getBatchOutputListWithNoDate());
		expect(batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus()))
			.andReturn(TestData.getBatchOutputListWithNoReportDate());
		Capture<File> capturedReportFile = newCapture(CaptureType.ALL);
		// Mock S3 service methods
		expect(s3Service.putFile(anyString(), anyString(), capture(capturedReportFile), eq("text/html")))
			.andReturn(getS3HttpsUrl().toString())
			.anyTimes();

		replay(batchService, s3Service);

		// Act
		IndexedFileReportGeneratorImpl.ReportGeneratorResult result = indexedFileReport.generateIndexFileReport();

		// Assert
		verify(batchService);
		verify(s3Service);
		assertNotNull(capturedReportFile.getValue(), "Report file should be captured");
		assertFalse(capturedReportFile.getValue().getName().isEmpty(), "Captured report file name should be empty'");
		assertTrue(capturedReportFile.getValue().exists(), "Captured report file should exist");
		assertNotNull(result);
		assertNotNull(result.getBatchOutputs());
	}

	@Test
	void getAllBatchOutputReportByDateWithDateSuccessful() throws MalformedURLException {
		// Arrange
		String reportDate = "2024-05-20T08:30:00+0000";
		List<BatchOutput> expectedBatchOutputs = TestData.getBatchOutputList();
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus())).andReturn(expectedBatchOutputs);
		expect(batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus())).andReturn(expectedBatchOutputs);

		Capture<File> capturedReportFile = newCapture(CaptureType.ALL);
		// Mock S3 service methods
		expect(s3Service.putFile(anyString(), anyString(), capture(capturedReportFile), eq("text/html")))
			.andReturn(getS3HttpsUrl().toString())
			.anyTimes();

		replay(batchService, s3Service);

		IndexedFileReportGeneratorImpl.ReportGeneratorResult result = indexedFileReport
			.getAllBatchOutputReportByDate(reportDate);

		// Assert
		verify(batchService);
		verify(s3Service);
		assertNotNull(result);
		assertNull(result.getErrorMsg());
		assertNotNull(result.getBatchOutputs());
	}

	@Test
	void getAllBatchOutputSinceLastReportWithErrorMsg() throws MalformedURLException {
		// Arrange
		String reportDate = "2024-05-20T08:30:00+0000";
		List<BatchOutput> expectedBatchOutputs = TestData.getBatchOutputListWithNoDate();
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus())).andReturn(expectedBatchOutputs);
		expect(batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus())).andReturn(expectedBatchOutputs);
		Capture<File> capturedReportFile = newCapture(CaptureType.ALL);
		// Mock S3 service methods
		expect(s3Service.putFile(anyString(), anyString(), capture(capturedReportFile), eq("text/html")))
			.andReturn(getS3HttpsUrl().toString())
			.anyTimes();

		replay(batchService, s3Service);

		IndexedFileReportGeneratorImpl.ReportGeneratorResult result = indexedFileReport
			.getAllBatchOutputReportByDate(reportDate);

		// Assert
		verify(batchService);
		verify(s3Service);
		assertNotNull(result);
		assertNotNull(result.getErrorMsg());
		assertNotNull(result.getBatchOutputs());
	}

	@Test
	void getAllBatchOutputSinceLastReportWithUtcDateTimeSuccessful() throws MalformedURLException {
		// Arrange
		String reportDate = "Sat Jun 01 16:41:52 UTC 2024";
		List<BatchOutput> expectedBatchOutputs = TestData.getBatchOutputWithUtcDateTimeList();
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus())).andReturn(expectedBatchOutputs);
		expect(batchService.getAllBatchByStatus(BatchStatus.INDEXED.getStatus())).andReturn(expectedBatchOutputs);

		Capture<File> capturedReportFile = newCapture(CaptureType.ALL);
		// Mock S3 service methods
		expect(s3Service.putFile(anyString(), anyString(), capture(capturedReportFile), eq("text/html")))
			.andReturn(getS3HttpsUrl().toString())
			.anyTimes();

		replay(batchService, s3Service);

		IndexedFileReportGeneratorImpl.ReportGeneratorResult result = indexedFileReport
			.getAllBatchOutputReportByDate(reportDate);

		// Assert
		verify(batchService);
		verify(s3Service);
		assertNotNull(result);
		assertEquals(1, result.getBatchOutputs().size(), "Same should be same");
		assertNull(result.getErrorMsg());
		assertNotNull(result.getBatchOutputs());
	}

}
