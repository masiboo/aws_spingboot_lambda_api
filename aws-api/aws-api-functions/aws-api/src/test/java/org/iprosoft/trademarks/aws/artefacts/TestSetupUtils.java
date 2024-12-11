package org.iprosoft.trademarks.aws.artefacts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.easymock.EasyMock;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BatchJobStatusByRequestId.BatchJobStatusByRequestId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BulkJobStatusByRequestId.BulkJobStatusByRequestId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.artefactbatchuploadurlrequesthandler.ArtefactBatchUploadURLRequestHandler;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.bulkuploadrequest.BulkUploadRequest;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.checkExistingBatchSequence.CheckExistingBatchSequence;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.convertresizeimagetotif.ConvertResizeImageToTif;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getArtefactByDateStatus.GetArtefactByDateStatus;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getallartefacts.GetAllArtefacts;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getalljobs.GetAllJobs;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbyfiltercriteria.GetArtefactsByFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getindexedfilereport.GetIndexedFileReport;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.indexartefact.IndexArtefact;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactIndexDto;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageResponse;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactServiceImpl;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.service.miris.MirisService;
import org.iprosoft.trademarks.aws.artefacts.service.reporter.IndexedFileReportGenerator;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.iprosoft.trademarks.aws.artefacts.util.QueryRequestBuilder;
import org.springframework.http.HttpStatus;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BatchJobCancelByRequestId.BatchJobCancelByRequestId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BulkJobCancelByRequestId.BulkJobCancelByRequestId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.canceljobrequest.CancelJobRequest;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.convertgiftojpg.ConvertGIFToJPG;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createartefactnote.CreateArtefactNote;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.deleteBatchByBatchSequence.DeleteArtefactByBatchSequence;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getallbatches.GetAllBatches;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactnotesbyfiltercriteria.GetArtefactNotesByFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbymirisdocid.GetArtefactsByMirisDocId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefacturl.GetArtefactURL;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getjobstatus.GetJobStatus;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.lists3files.ListS3Files;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.validatemirisdocid.ValidateMirisDocId;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.ArtefactUploadRequest.ArtefactUploadRequest;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static org.easymock.EasyMock.*;

@Data
@Slf4j
public class TestSetupUtils {

	public static DynamoDbClient dynamoDbClient;

	public static QueryRequestBuilder queryRequestBuilder;

	public static MirisService mirisService;

	public static ArtefactServiceImpl artefactService;

	public static BatchService batchService;

	public static ArtefactJobService artefactJobService;

	public static S3Service s3Service;

	public static MediaProcessingService mediaProcessingService;

	public static DatabaseService databaseService;

	public static IndexedFileReportGenerator indexedFileReportGenerator;

	public static ObjectMapper objectMapper;

	public static ArtefactJob job;

	public static boolean setEmptyArtefact;

	public static boolean setEmptyJobResponse;

	public static boolean isCancelJobStatus;

	public static boolean isDocIdValid;

	public static boolean isS3ObjectKeysSet;

	public static boolean isBatchSequenceExist;

	public static boolean setEmptyBatch;

	public static boolean setNullArtefact;

	public static boolean setNoS3Object;

	public static boolean setNoS3Bucket;

	public static String expectedSignedS3Url = "http://Aws-2024-08-08//d1994ed0-c50d-4976-8b8f-66d902b320db/picture_129.png";

	public static void commonSetup() throws MalformedURLException {
		configureArtefactServiceDependencyMocks();
		configureArtefactServiceMock();
		configureBatchServiceMock();
		configureArtefactJobServiceMock();
		configureS3ServiceMock();
		configureMediaProcessingServiceMock();
		configureDatabaseServiceMock();
		configureIndexedFileReportGeneratorMock();
		replayAllMocks();
		setupObjectMapper();
	}

	public static void resetBooleans() {
		setEmptyArtefact = false;
		setEmptyJobResponse = false;
		isCancelJobStatus = false;
		isDocIdValid = false;
		isS3ObjectKeysSet = false;
		isBatchSequenceExist = false;
		setEmptyBatch = false;
		setNullArtefact = false;
		setNoS3Object = false;
		setNoS3Bucket = false;
	}

	private static void configureArtefactServiceDependencyMocks() {
		dynamoDbClient = EasyMock.createMock(DynamoDbClient.class);
		queryRequestBuilder = EasyMock.createMock(QueryRequestBuilder.class);
		mirisService = EasyMock.createMock(MirisService.class);
		expect(mirisService.isDocIdValid(anyString())).andReturn(isDocIdValid).anyTimes();
	}

	private static void configureArtefactServiceMock() throws MalformedURLException {
		artefactService = EasyMock.createMockBuilder(ArtefactServiceImpl.class)
			.withConstructor(dynamoDbClient, queryRequestBuilder, mirisService)
			.addMockedMethod("hasFileWithSameDocId")
			.addMockedMethod("saveDocument")
			.addMockedMethod("getAllArtefacts")
			.addMockedMethod("getArtefactbyMirisDocId")
			.addMockedMethod("getArtefactInfoById")
			.addMockedMethod("getAllArtefactsByInterval")
			.addMockedMethod("getArtefactByFilterCritera")
			.addMockedMethod("getArtefactById")
			.addMockedMethod("saveArtefactsAtomic")
			.addMockedMethod("isDocIdValid")
			.addMockedMethod("getArtectBatchById")
			.addMockedMethod("indexArtefact")
			.createMock();

		expect(artefactService.hasFileWithSameDocId(anyString(), anyString())).andReturn(false).anyTimes();
		expect(artefactService.getAllArtefacts(anyString(), anyString())).andReturn(TestData.getMixedArtefactList())
			.anyTimes();
		artefactService.saveDocument(anyObject(), anyObject());
		expectLastCall().anyTimes();

		setupArtefactServiceMocksForData();

		List<Artefact> artefacts = TestData.getMixedArtefactList();
		expect(artefactService.getArtefactByFilterCritera(anyObject())).andReturn(artefacts).anyTimes();
		Artefact artefact;
		if (setNullArtefact) {
			artefact = null;
		}
		else {
			artefact = TestData.getArtefact(ArtefactStatus.UPLOADED.getStatus());
		}
		expect(artefactService.getArtefactById(anyString())).andReturn(artefact).anyTimes();
		artefactService.saveArtefactsAtomic(anyObject());
		expectLastCall().anyTimes();

		expect(artefactService.isDocIdValid(anyString())).andReturn(isDocIdValid).anyTimes();

		ArtefactBatch artefactBatch = TestData.getArtefactBatch();
		expect(artefactService.getArtectBatchById(anyString())).andReturn(artefactBatch).anyTimes();

		ArtefactIndexDto artefactIndexDto = TestData.getArtefactIndexDto();
		artefactService.indexArtefact(anyString(), anyObject(ArtefactIndexDto.class), eq(ArtefactStatus.INDEXED));
		expectLastCall().anyTimes();

	}

	public static void setArtefactStatusIndexed() {
		reset(artefactService);
		Artefact artefact = TestData.getArtefact(ArtefactStatus.INDEXED.getStatus());
		expect(artefactService.getArtefactById(anyString())).andReturn(artefact).anyTimes();
		expect(artefactService.isDocIdValid(anyString())).andReturn(isDocIdValid).anyTimes();
		replay(artefactService);
	}

	private static void setupArtefactServiceMocksForData() {
		List<Artefact> expectedArtefactList = TestData.getMixedArtefactList();
		expect(artefactService.getAllArtefactsByInterval(anyString(), anyString(), anyString()))
			.andReturn(expectedArtefactList)
			.anyTimes();

		if (setEmptyArtefact) {
			expect(artefactService.getArtefactbyMirisDocId(anyString())).andReturn(new ArrayList<>()).anyTimes();
		}
		else {
			expect(artefactService.getArtefactbyMirisDocId(anyString())).andReturn(TestData.getMixedArtefactList())
				.anyTimes();
		}

		expect(artefactService.getArtefactInfoById(anyString())).andReturn(TestData.getArtefactMetadata()).anyTimes();
	}

	private static void configureBatchServiceMock() {
		batchService = EasyMock.createMock(BatchService.class);
		batchService.saveBatchSequence(anyObject());
		expectLastCall().anyTimes();
		batchService.saveBatchSequenceWithChildren(anyObject());
		expectLastCall().anyTimes();
		batchService.updateBatchWithStatus(anyString(), anyString());
		expectLastCall().anyTimes();

		if (isBatchSequenceExist) {
			BatchOutput batchOutputForTest = BatchOutput.builder().batchSequence("221122.1").status("INIT").build();
			expect(batchService.getBatchDetail(anyString())).andReturn(batchOutputForTest).anyTimes();
		}
		else {
			expect(batchService.getBatchDetail(anyString())).andReturn(null).anyTimes();
		}

		Map<String, Object> batchtatusResponseMap = new HashMap<>();
		batchtatusResponseMap.put("jobs", "test");
		batchtatusResponseMap.put("requestId", "test");
		expect(batchService.getPagedBatchesForRequestId(anyString())).andReturn(batchtatusResponseMap).anyTimes();

		batchService.updateBatchIfAllIndexed(anyString());
		expectLastCall().anyTimes();

		List<BatchOutput> batchOutputs;
		if (setEmptyBatch) {
			batchOutputs = new ArrayList<>();
		}
		else {
			batchOutputs = TestData.getBatchOutputs();
		}

		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus())).andReturn(batchOutputs);
	}

	public static void setDetestedBatch() {
		reset(batchService);
		BatchOutput batchOutputForTest = TestData.getBatchOutput("DELETED");
		expect(batchService.getBatchDetail(anyString())).andReturn(batchOutputForTest).anyTimes();
		replay(batchService);
	}

	public static void configBatchRuntimeExceptionForGetAllBatchByStatus() {
		reset(batchService);
		expect(batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus()))
			.andThrow(new RuntimeException("Service error"))
			.anyTimes();
		replay(batchService);
	}

	public static void configBatchRuntimeExceptionForGetBatchDetail() {
		reset(batchService);
		expect(batchService.getBatchDetail(anyString())).andThrow(new RuntimeException("Service error")).anyTimes();
		replay(batchService);
	}

	private static void configureArtefactJobServiceMock() {
		artefactJobService = EasyMock.createMock(ArtefactJobService.class);
		if (setEmptyJobResponse) {
			expect(artefactJobService.getAllJobStatusByRequestId(anyString())).andReturn(Collections.emptyMap())
				.anyTimes();
		}
		else {
			expect(artefactJobService.getAllJobStatusByRequestId(anyString()))
				.andReturn(TestData.getJobStatusResponseMap())
				.anyTimes();
		}

		Map<String, java.lang.Object> responseMap = TestData.getAllJobStatusByRequestId("123456", "4444.33", "12346");
		expect(artefactJobService.getAllBulkJobStatusByRequestId(anyString())).andReturn(responseMap).anyTimes();

		UpdateItemResponse updateItemResponse = UpdateItemResponse.builder().build();
		expect(artefactJobService.updateJobWithStatus(anyString(), anyString())).andReturn(updateItemResponse)
			.anyTimes();
		artefactJobService.saveJob(anyObject());
		expectLastCall().anyTimes();

		job = isCancelJobStatus ? TestData.getArtefactJobWithCancelStatus() : null;
		expect(artefactJobService.getJobStatus(anyString())).andReturn(job).anyTimes();

		List<ArtefactJob> artefactJobList = TestData.getArtefactJobsNotNull();
		expect(artefactJobService.getAllJobs(anyString(), anyString())).andReturn(artefactJobList).anyTimes();

		artefactJobService.saveJobAtomic(anyObject());
		expectLastCall().anyTimes();

	}

	private static void configureS3ServiceMock() throws MalformedURLException {
		s3Service = EasyMock.createMock(S3Service.class);
		URL expectedUrl = new URL(expectedSignedS3Url);
		expect(s3Service.presignPutUrl(anyString(), anyString(), anyObject(Duration.class), anyObject(Map.class)))
			.andReturn(expectedUrl)
			.anyTimes();
		expect(s3Service.presignGetUrl(anyString(), anyString(), anyObject(Duration.class), isNull()))
			.andReturn(expectedUrl)
			.anyTimes();

		Set<String> objectKeys = new HashSet<>();
		if (isS3ObjectKeysSet) {
			objectKeys.add("key1");
			objectKeys.add("key2");
		}
		expect(s3Service.listObjectKeys(anyString())).andReturn(objectKeys).anyTimes();
		TestSetupUtils.setMockS3ServiceGetObject();

		if (setNoS3Object) {
			expect(s3Service.isObjectExist(anyString(), anyString())).andReturn(false).anyTimes();
		}
		else {
			expect(s3Service.isObjectExist(anyString(), anyString())).andReturn(true).anyTimes();
		}

		if (setNoS3Bucket) {
			expect(s3Service.bucketExists(anyString())).andReturn(false).anyTimes();
		}
		else {
			expect(s3Service.bucketExists(anyString())).andReturn(true).anyTimes();
		}

	}

	public static void setMockS3ServiceGetObject() {
		GetObjectResponse getObjectResponse = GetObjectResponse.builder()
			.contentLength(1024L) // Example content length
			.build();
		byte[] data = "Dummy data for S3 object".getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		ResponseInputStream<GetObjectResponse> s3Object = new ResponseInputStream<>(getObjectResponse,
				byteArrayInputStream);
		log.info("Content Length: {}", s3Object.response().contentLength());
		expect(s3Service.getObject(anyString(), anyString())).andReturn(s3Object).anyTimes();
	}

	private static void configureMediaProcessingServiceMock() {
		mediaProcessingService = createMock(MediaProcessingService.class);
		EasyMock.expect(mediaProcessingService.convertGIFToJPG(EasyMock.anyObject(ConvertImageRequest.class)))
			.andReturn(expectedSignedS3Url)
			.anyTimes();

		ConvertImageResponse convertImageResponse = createConvertImageResponse();
		EasyMock
			.expect(mediaProcessingService.convertResizeImageToTif(EasyMock.anyObject(ConvertImageRequest.class),
					anyString()))
			.andReturn(convertImageResponse)
			.anyTimes();
	}

	private static ConvertImageResponse createConvertImageResponse() {
		Map<String, String> metaData = Map.of("artefactId", "123", "bitDepth", "24", "resolutionInDpi", "266",
				"fileType", "tif", "mediaType", "COLORLOGO", "classType", "COLORLOGO", "size", "1050");
		return ConvertImageResponse.builder()
			.signedS3Url(expectedSignedS3Url)
			.metaData(metaData)
			.httpStatus(HttpStatus.CREATED.toString())
			.build();
	}

	private static void configureDatabaseServiceMock() {
		databaseService = createMock(DatabaseService.class);
		expect(databaseService.filterArtefactNotes(anyString())).andReturn(TestData.getArtefactNotesEntityList())
			.anyTimes();

		List<ArtefactsEntity> artefactsEntities = TestData.getArtefactsEntityList();
		expect(databaseService.filterArtefacts(anyObject(), anyString())).andReturn(artefactsEntities).anyTimes();

		expect(databaseService.createArtefactNote(anyObject())).andReturn(TestData.getArtefactNotesEntity());
	}

	private static void configureIndexedFileReportGeneratorMock() {
		indexedFileReportGenerator = createMock(IndexedFileReportGenerator.class);
		expect(indexedFileReportGenerator.getAllBatchOutputReportByDate(anyString()))
			.andReturn(TestData.getReportGeneratorResult());
		expect(indexedFileReportGenerator.generateIndexFileReport()).andReturn(TestData.getReportGeneratorResult());
	}

	private static void replayAllMocks() {
		replay(artefactService, batchService, artefactJobService, s3Service, mediaProcessingService, databaseService,
				indexedFileReportGenerator, mirisService);
	}

	private static void setupObjectMapper() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public static void setMaxAllowed100Mb() {
		String signedS3Url = "Input image exceeded maximum allowed limit is 100 MB";
		ConvertImageResponse convertImageResponse = ConvertImageResponse.builder().signedS3Url(signedS3Url).build();
		Map<String, String> metaData = new HashMap<>();
		metaData.put("size", "1010");
		metaData.put("resolutionInDpi", "400");
		metaData.put("classType", "BWLOGO");
		convertImageResponse.setMetaData(metaData);
		EasyMock.reset(mediaProcessingService);
		EasyMock
			.expect(mediaProcessingService.convertResizeImageToTif(EasyMock.anyObject(ConvertImageRequest.class),
					anyString()))
			.andReturn(convertImageResponse)
			.anyTimes();
		replay(mediaProcessingService);
	}

	public static ArtefactBatchUploadURLRequestHandler createArtefactBatchUploadURLRequestHandler() {
		return new ArtefactBatchUploadURLRequestHandler(batchService, s3Service, artefactService, artefactJobService);
	}

	public static ArtefactUploadRequest createArtefactUploadRequest() {
		return new ArtefactUploadRequest(s3Service, artefactService, artefactJobService);
	}

	public static GetArtefactByDateStatus createGetArtefactByDateStatus() {
		return new GetArtefactByDateStatus(artefactService);
	}

	public static BulkUploadRequest createBulkUploadRequest() {
		return new BulkUploadRequest(s3Service, artefactService, artefactJobService);
	}

	public static BatchJobCancelByRequestId createBatchJobCancelByRequestId() {
		return new BatchJobCancelByRequestId(artefactJobService);
	}

	public static BatchJobStatusByRequestId createBatchJobStatusByRequestId() {
		return new BatchJobStatusByRequestId(batchService);
	}

	public static BulkJobCancelByRequestId createBulkJobCancelByRequestId() {
		return new BulkJobCancelByRequestId(artefactJobService);
	}

	public static BulkJobStatusByRequestId createBulkJobStatusByRequestId() {
		return new BulkJobStatusByRequestId(artefactJobService);
	}

	public static CancelJobRequest createCancelJobRequest() {
		return new CancelJobRequest(artefactJobService);
	}

	public static ConvertGIFToJPG createConvertGIFToJPG() {
		return new ConvertGIFToJPG(mediaProcessingService);
	}

	public static ConvertResizeImageToTif createConvertResizeImageToTif() {
		return new ConvertResizeImageToTif(mediaProcessingService, artefactService, s3Service, objectMapper);
	}

	public static GetAllArtefacts createGetAllArtefacts() {
		return new GetAllArtefacts(objectMapper, artefactService);
	}

	public static GetArtefactNotesByFilterCriteria createGetArtefactNotesByFilterCriteria() {
		return new GetArtefactNotesByFilterCriteria(objectMapper, databaseService);
	}

	public static GetArtefactsByFilterCriteria createGetArtefactsByFilterCriteria() {
		return new GetArtefactsByFilterCriteria(objectMapper, artefactService);
	}

	public static GetArtefactsByMirisDocId createGetArtefactsByMirisDocId() {
		return new GetArtefactsByMirisDocId(objectMapper, databaseService);
	}

	public static GetArtefactURL createGetArtefactURL() {
		return new GetArtefactURL(objectMapper, s3Service, artefactService);
	}

	public static GetIndexedFileReport createGetIndexedFileReport() {
		return new GetIndexedFileReport(indexedFileReportGenerator);
	}

	public static GetJobStatus createGetJobStatus() {
		return new GetJobStatus(objectMapper, artefactJobService);
	}

	public static ListS3Files createListS3Files() {
		return new ListS3Files(objectMapper, s3Service);
	}

	public static ValidateMirisDocId createValidateMirisDocId() {
		return new ValidateMirisDocId(mirisService);
	}

	public static CreateArtefactNote createCreateArtefactNote() {
		return new CreateArtefactNote(objectMapper, databaseService);
	}

	public static GetAllJobs createGetAllJobs() {
		return new GetAllJobs(objectMapper, artefactJobService);
	}

	public static BatchJobStatusByRequestId getBatchJobStatusByRequestId() {
		return new BatchJobStatusByRequestId(batchService);
	}

	public static CheckExistingBatchSequence createCheckExistingBatchSequence() {
		return new CheckExistingBatchSequence(batchService);
	}

	public static IndexArtefact createIndexArtefact() {
		return new IndexArtefact(artefactService, batchService);
	}

	public static GetAllBatches createGetAllBatches() {
		return new GetAllBatches(objectMapper, batchService);
	}

	public static DeleteArtefactByBatchSequence createDeleteArtefactByBatchSequence() {
		return new DeleteArtefactByBatchSequence(objectMapper, batchService);
	}

}
