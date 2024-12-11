package org.iprosoft.trademarks.aws.artefacts;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.*;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.*;
import org.iprosoft.trademarks.aws.artefacts.service.reporter.IndexedFileReportGeneratorImpl;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.mockito.Mockito;
import org.springframework.util.ResourceUtils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactServiceImpl.classType.*;
import static org.iprosoft.trademarks.aws.artefacts.service.artefactvalidation.ArtefactValidationServiceImpl.ArtefactClassType.MULTIMEDIAFILE;

@Slf4j
public class TestData {

	public static final String S3_BUCKET_NAME = "unit-test-bucket";

	public static final String S3_BUCKET_KEY = "test.jgp";

	public static final String BatchOutputsJsonList = """
						        [
						          {
						            "id": "batch1",
						            "batch_sequence": "sequence1",
						            "lockedDate": "2024-05-18T08:30:00Z",
						            "creationDate": "2024-05-17T09:00:00Z",
						            "lastModificationDate": "2024-05-18T10:00:00Z",
						            "status": "INSERTED",
						            "operator": {
						              "id": "1",
						              "name": "Operator Name"
						            },
						            "lockedBy": {
						              "id": "2",
						              "name": "User Name"
						            },
						            "lastModUser": {
						              "id": "3",
						              "name": "Another User"
						            },
						            "requestType": "TYPE_A",
						            "user": "user1",
						            "artefacts": [
						              {
						                "id": "123",
						                "artefactName": "Artefact Name 1",
						                "artefactClassType": "DOCUMENT",
						                "status": "INDEXED",
						                "error": "None",
						                "indexationDate": "2024-05-17T09:15:00Z",
						                "archiveDate": "2024-06-17T09:15:00Z",
						                "gets3Bucket": "bucket1",
						                "mirisDocId": "miris1",
						                "artefactItemTags": [
						                  {
						                    "key": "tagKey1",
						                    "value": "tagValue1",
						                    "type": "tagType1"
						                  }
						                ],
						                "items": [
						                  {
						                    "id": "123",
						                    "filename": "document1.pdf",
						                    "contentType": "application/pdf",
						                    "path": "/documents/doc1",
						                    "storage": "s3",
						                    "contentLength": "1024",
						                    "artefactType": "PDF",
						                    "jobId": "job1",
						                    "jobStatus": "SUCCESS",
						                    "totalPages": 10
						                  }
						                ]
						              },
						              {
						                "id": "artefact2",
						                "artefactName": "Artefact Name 2",
						                "artefactClassType": "IMAGE",
						                "status": "INDEXED",
						                "error": "None",
						                "indexationDate": "2024-05-17T10:15:00Z",
						                "archiveDate": "2024-06-17T10:15:00Z",
						                "gets3Bucket": "bucket2",
						                "mirisDocId": "miris2",
						                "artefactItemTags": [
						                  {
						                    "key": "tagKey2",
						                    "value": "tagValue2",
						                    "type": "tagType2"
						                  }
						                ],
						                "items": [
						                  {
						                    "id": "1123",
						                    "filename": "image1.png",
						                    "contentType": "image/png",
						                    "path": "/images/img1",
						                    "storage": "s3",
						                    "contentLength": "2048",
						                    "artefactType": "PNG",
						                    "jobId": "job2",
						                    "jobStatus": "QUEUED",
						                    "totalPages": 1
						                  }
						                ]
						              }
						            ]
						          },
						          {
						            "id": "123",
						            "batch_sequence": "sequence2",
						            "lockedDate": "2024-05-18T09:30:00Z",
						            "creationDate": "2024-05-17T10:00:00Z",
						            "lastModificationDate": "2024-05-18T11:00:00Z",
						            "status": "INDEXED",
						            "operator": {
						              "id": "4",
						              "name": "Another Operator"
						            },
						            "lockedBy": {
						              "id": "5",
						              "name": "Third User"
						            },
						            "lastModUser": {
						              "id": "6",
						              "name": "Fourth User"
						            },
						            "requestType": "TYPE_B",
						            "user": "user3",
						            "artefacts": [
						              {
						                "id": "1231",
						                "artefactName": "Artefact Name 3",
						                "artefactClassType": "VIDEO",
						                "status": "INDEXED",
						                "error": "File not found",
						                "indexationDate": "2024-05-17T11:15:00Z",
						                "archiveDate": "2024-06-17T11:15:00Z",
						                "gets3Bucket": "bucket3",
						                "mirisDocId": "miris3",
						                "artefactItemTags": [
						                  {
						                    "key": "tagKey3",
						                    "value": "tagValue3",
						                    "type": "tagType3"
						                  }
						                ],
						                "items": [
						                  {
						                    "id": "123654",
						                    "filename": "video1.mp4",
						                    "contentType": "video/mp4",
						                    "path": "/videos/vid1",
						                    "storage": "s3",
						                    "contentLength": "3072",
						                    "artefactType": "MP4",
						                    "jobId": "job3",
						                    "jobStatus": "FAILED",
						                    "totalPages": 0
						                  }
						                ]
						              }
						            ]
						          }
						        ]
			""";

	public static final String artefactBatchListJson = """
			[
			   {
			      "type":"Addendum",
			      "filename":"00000000.TIF",
			      "batchSequence":"99889924.051",
			      "artefactName":"99889924.051-0000D",
			      "artefactClassType":"DOCUMENT",
			      "artefactItemFileName":"99889924.051-00000000.TIF",
			      "creationDate":"20221123",
			      "path":"99889924.051",
			      "contentType":"TIFF",
			      "mirisDocId":"16532350",
			      "page":1
			""";

	public static List<BatchOutput> getBatchOutputs() {
		ObjectMapper objectMapper = new ObjectMapper();
		List<BatchOutput> batchOutputs = null;
		try {
			batchOutputs = objectMapper.readValue(BatchOutputsJsonList, new TypeReference<>() {
			});
			batchOutputs.forEach(System.out::println);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return batchOutputs;

	}

	public static ArtefactInput createArtefactInput() {
		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("iprosoft.pdf");
		artefact.setArtefactClassType("Document");
		artefact.setMirisDocId("123456789");
		return artefact;
	}

	public static ArtefactInput createInvalidArtefactInput() {
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setContentType("png");
		artefactItemInput.setFilename("iprosoft.pdf");

		ArtefactItemInput artefactItemInput2 = new ArtefactItemInput();
		artefactItemInput.setContentType("png");
		artefactItemInput.setFilename("iprosoft.pdf");

		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("iprosoft.pdf");
		artefact.setArtefactClassType(MULTIMEDIA.name());
		artefact.setItems(List.of(artefactItemInput, artefactItemInput2));
		artefact.setMirisDocId("123456789");
		return artefact;
	}

	public static ArtefactInput createWithXmlArtefactInput() {
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setContentType("xml");
		artefactItemInput.setFilename("test.xml");

		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("test.xml");
		artefact.setArtefactClassType(DOCUMENT.name());
		artefact.setItems(List.of(artefactItemInput));
		artefact.setMirisDocId("123654");
		return artefact;
	}

	public static ArtefactInput createWithXlsArtefactInput() {
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setContentType("xls");
		artefactItemInput.setFilename("test.xls");

		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("test.xls");
		artefact.setArtefactClassType(CERTIFICATE.name());
		artefact.setItems(List.of(artefactItemInput));
		artefact.setMirisDocId("123654");
		return artefact;
	}

	public static ArtefactItemInput createArtefactInputItem() {
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setPath("mp4");
		artefactItemInput.setFilename("mov_bbb.mp4");
		artefactItemInput.setContentType("video/mp4");
		return artefactItemInput;
	}

	public static APIGatewayV2HTTPEvent getAPIGatewayV2HTTPEventAddendumOverwriteBatchTrue() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		Map<String, String> pathParameters = new HashMap<>();
		String type = "ADDENDUM";
		pathParameters.put("scannedApp", type);
		pathParameters.put("overwriteBatch", "true");
		event.setQueryStringParameters(pathParameters);
		event.setBody(getEventBody(type));
		return event;
	}

	public static APIGatewayV2HTTPEvent getAPIGatewayV2HTTPEventAddendumWithInvalidMirisDocId() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		Map<String, String> pathParameters = new HashMap<>();
		String type = "ADDENDUM";
		pathParameters.put("scannedApp", type);
		pathParameters.put("overwriteBatch", "true");
		event.setQueryStringParameters(pathParameters);
		event.setBody(getEventBodyWithInvalidMirisDocId(type));
		return event;
	}

	public static APIGatewayV2HTTPEvent getAPIGatewayV2HTTPEventNewOverwriteBatchFalse() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		Map<String, String> pathParameters = new HashMap<>();
		String type = "NEW_REQUEST";
		pathParameters.put("scannedApp", type);
		pathParameters.put("overwriteBatch", "false");
		event.setQueryStringParameters(pathParameters);
		event.setBody(getEventBody(type));
		return event;
	}

	public static APIGatewayV2HTTPEvent getAPIGatewayV2HTTPEventNewOverwriteBatchTrue() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		Map<String, String> pathParameters = new HashMap<>();
		String type = "NEW_REQUEST";
		pathParameters.put("scannedApp", type);
		pathParameters.put("overwriteBatch", "true");
		event.setQueryStringParameters(pathParameters);
		event.setBody(getEventBody(type));
		return event;
	}

	private static String getEventBody(String type1) {
		return """
				[
				  {
				    type: '%s',
				    filename: '00000000.TIF',
				    batchSequence: '0221123.050',
				    contentType: 'TIFF',
				    path: '0221123.050',
				    artefactClassType: 'DOCUMENT',
				    artefactItemFileName: '0221123.050-00000000.TIF',
				    artefactName: '0221123.050-0000D',
				    mirisDocId: '16529487',
				    creationDate: '20240111',
				    page: '1',
				    user: 'Anonymous'
				  }
				]
				""".formatted(type1);
	}

	private static String getEventBodyWithInvalidMirisDocId(String type1) {
		return """
				[
				  {
				    type: '%s',
				    filename: '00000000.TIF',
				    batchSequence: '0221123.050',
				    contentType: 'TIFF',
				    path: '0221123.050',
				    artefactClassType: 'DOCUMENT',
				    artefactItemFileName: '0221123.050-00000000.TIF',
				    artefactName: '0221123.050-0000D',
				    mirisDocId: '112233445566',
				    creationDate: '20240111',
				    page: '1',
				    user: 'Anonymous'
				  }
				]
				""".formatted(type1);
	}

	public static List<ArtefactBatch> getListArtefactBatchList() {
		ArtefactBatch artefactBatch1 = ArtefactBatch.builder()
			.id("1")
			.type("Document")
			.filename("iprosoft.pdf")
			.path("/path/to/file1.pdf")
			.contentType("application/pdf")
			.batchSequence("99889924.111")
			.creationDate("2024-05-18")
			.user("user1")
			.jobId("job1")
			.artefactId("123654")
			.s3Url("s3://bucket/file1.pdf")
			.requestType("Type1")
			.validationStatus("ok")
			.page("1")
			// .artefactMergeId("123")
			.artefactItemFileName("itemfile1.pdf")
			.mirisDocId("11223344")
			.status(ArtefactStatus.INDEXED.name())
			.build();
		ArtefactBatch artefactBatch2 = ArtefactBatch.builder()
			.id("1")
			.type("Document")
			.filename("iprosoft.pdf")
			.path("/path/to/file1.pdf")
			.contentType("application/pdf")
			.batchSequence("0221123.222")
			.creationDate("2024-05-18")
			.user("user1")
			.jobId("job1")
			.artefactId("987654")
			.s3Url("s3://bucket/file1.pdf")
			.requestType("Type1")
			.validationStatus("ok")
			.page("1")
			// .artefactMergeId("123")
			.artefactItemFileName("itemfile1.pdf")
			.mirisDocId("11223344")
			.status(ArtefactStatus.INDEXED.name())
			.build();
		return Arrays.asList(artefactBatch1, artefactBatch2);
	}

	public static ArtefactBatch getArtefactBatch() {
		ArtefactBatch artefactBatch = ArtefactBatch.builder()
			.id("1")
			.artefactClassType("SOUND")
			.artefactName("artName")
			.type("ADDENDUM")
			.filename("12345678.pdf")
			.path("/path/to/file1.pdf")
			.contentType("application/pdf")
			.batchSequence("0221123.000")
			.creationDate("2024-05-18")
			.user("user1")
			.jobId("job1")
			.artefactId("artefact1")
			.s3Url("s3://bucket/file1.pdf")
			.requestType("Type1")
			.validationStatus("ok")
			.page("1")
			// .artefactMergeId("merge1")
			.artefactItemFileName("itemfile1.pdf")
			.mirisDocId("11223344")
			.status(ArtefactStatus.INDEXED.name())
			.build();
		return artefactBatch;
	}

	public static List<Artefact> getArtefactList() {
		Artefact artefact = Artefact.builder()
			.artefactName("artefactName")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		return List.of(artefact);
	}

	public static List<Artefact> getMultimediaArtefactList() {
		Artefact artefact = Artefact.builder()
			.id("123")
			.artefactName("video")
			.artefactClassType(ArtefactClassType.MULTIMEDIA.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact2 = Artefact.builder()
			.id("456")
			.artefactName("sound")
			.artefactClassType(ArtefactClassType.SOUND.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		return List.of(artefact, artefact2);
	}

	public static List<Artefact> getLogoArtefactList() {
		Artefact artefact = Artefact.builder()
			.id("123")
			.artefactName("video")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact2 = Artefact.builder()
			.id("456")
			.artefactName("sound")
			.artefactClassType(ArtefactClassType.COLOURLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		return List.of(artefact, artefact2);
	}

	public static List<Artefact> getMixedArtefactList() {
		Artefact artefact = Artefact.builder()
			.artefactName("video")
			.id("123")
			.artefactClassType(ArtefactClassType.MULTIMEDIA.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact2 = Artefact.builder()
			.artefactName("sound")
			.id("456")
			.artefactClassType(ArtefactClassType.SOUND.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact3 = Artefact.builder()
			.artefactName("bwLogo")
			.id("789")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact4 = Artefact.builder()
			.id("321")
			.artefactName("colorLogo")
			.artefactClassType(ArtefactClassType.COLOURLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		return List.of(artefact, artefact2, artefact3, artefact4);
	}

	public static List<Artefact> getAllTypeArtefactList() {
		Artefact artefact = Artefact.builder()
			.artefactName("video")
			.id("123")
			.artefactClassType(ArtefactClassType.MULTIMEDIA.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact2 = Artefact.builder()
			.artefactName("sound")
			.id("456")
			.artefactClassType(ArtefactClassType.SOUND.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact3 = Artefact.builder()
			.artefactName("bwLogo")
			.id("789")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();
		Artefact artefact4 = Artefact.builder()
			.id("321")
			.artefactName("colorLogo")
			.artefactClassType(ArtefactClassType.COLOURLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();

		Artefact artefact5 = Artefact.builder()
			.artefactName("pdf")
			.id("123")
			.artefactClassType(ArtefactClassType.CERTIFICATE.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();

		Artefact artefact6 = Artefact.builder()
			.artefactName("doc")
			.id("123")
			.artefactClassType(ArtefactClassType.DOCUMENT.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();

		Artefact artefact7 = Artefact.builder()
			.artefactName("sound1")
			.id("123")
			.artefactClassType(ArtefactClassType.SOUND.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();

		Artefact artefact8 = Artefact.builder()
			.artefactName("video1")
			.id("123")
			.artefactClassType(ArtefactClassType.MULTIMEDIA.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.build();

		return List.of(artefact, artefact2, artefact3, artefact4, artefact5, artefact6, artefact7, artefact8);
	}

	public static List<Artefact> getArtefactComplexList() {
		// Get the current date and time as ZonedDateTime
		ZonedDateTime now = DateUtils.getCurrentDatetimeUtc();

		// Subtract two months from the current date
		ZonedDateTime twoMonthsAgo = now.minus(2, ChronoUnit.MONTHS);

		// Set the time to 16:50:55
		ZonedDateTime twoMonthsAgoWithTime = twoMonthsAgo.withHour(16).withMinute(50).withSecond(55).withNano(0);

		// Convert ZonedDateTime to java.util.Date
		Date date = Date.from(twoMonthsAgoWithTime.toInstant());

		Artefact artefact = Artefact.builder()
			.artefactName("artefactName")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("1235678")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.indexationDate(twoMonthsAgoWithTime)
			.status(ArtefactStatus.INDEXED.name())
			.build();

		Artefact artefact2 = Artefact.builder()
			.artefactName("artefactName2")
			.artefactClassType(ArtefactClassType.BWLOGO.name())
			.mirisDocId("11223344")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.indexationDate(twoMonthsAgoWithTime)
			.status(ArtefactStatus.INDEXED.name())
			.build();
		return List.of(artefact, artefact2);
	}

	public static URL getS3HttpsUrl() throws MalformedURLException {
		return new URL(
				"https://unit-test-bucket/test.tif?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240521T061855Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Credential=test%2F20240521%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=3393a076c8dcd917a8efcf74130381124f1a275aafd92203e4f3d7568f6a86d9");
	}

	public static boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (responseCode == HttpURLConnection.HTTP_OK);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static ArtefactMetadata getArtefactMetadata() {
		ArtefactMetadata artefactMetadata = ArtefactMetadata.builder()
			.artefactId("123")
			.resolutionInDpi("266")
			.mediaType("image/tif")
			.fileType("tif")
			.classType(BWLOGO.name())
			.size("1050")
			.build();
		return artefactMetadata;
	}

	public static ArtefactMetadata getMultimediaArtefactMetadata() {
		ArtefactMetadata artefactMetadata = ArtefactMetadata.builder()
			.artefactId("123")
			.mediaType("audio/wav")
			.fileType("wav")
			.classType(MULTIMEDIAFILE.name())
			.sizeWarning(false)
			.build();
		return artefactMetadata;
	}

	public static ConvertImageRequest getConvertImageRequest() {
		ConvertImageRequest convertImageRequest = new ConvertImageRequest();
		convertImageRequest.setBucket("test-bucket");
		convertImageRequest.setKey("intranet.gif");
		return convertImageRequest;
	}

	public static BatchOutput getBatchOutput(String status) {
		BatchOutput batchOutput = BatchOutput.builder().batchSequence("221122.1").status(status).build();
		return batchOutput;
	}

	public static String setOverwriteBatchFalse(String inputJson) {
		String overwriteBatchTrue = "\"overwriteBatch\": \"true\"";
		String overwriteBatchFalse = "\"overwriteBatch\": \"false\"";
		return inputJson.replace(overwriteBatchTrue, overwriteBatchFalse);
	}

	public static String setOverwriteBatchTrue(String inputJson) {
		String overwriteBatchTrue = "\"overwriteBatch\": \"true\"";
		String overwriteBatchFalse = "\"overwriteBatch\": \"false\"";
		return inputJson.replace(overwriteBatchFalse, overwriteBatchTrue);
	}

	public static InputStream getBatchInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBatchWithNewRqustInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replaceAll("Addendum", "new_request");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBatchWithNewRqustWithoutUserInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replaceAll("Addendum", "new_request");
		jsonEventPayload = jsonEventPayload.replaceAll("test-user", "");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBatchWithNewRqustWithoutMirisDocIdInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replaceAll("\"mirisDocId\":16529262,", "");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getCheckBatchSequenceInputStream() throws IOException {
		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:api-events/api-post-check-batches-sequence-upload.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getGoodAndBadBatchInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("CERTIFICATE", "CERTIX");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInvalidArtefactInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-artefact-upload-invalid.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getArtefactInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-artefact-upload.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBatchJobOrBulkJobCancelByRequestIdStream() throws IOException {
		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:api-events/api-batch-or-bulk-job-cancel-by-request-id.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBatchJobOrBulkJobCancelWithoutRequestIdStream() throws IOException {
		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:api-events/api-batch-or-bulk-job-cancel-by-request-id.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("821708a2-d8a5-4503-8186-6a7ae92a91f6", "");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getArtefactInputStreamWithEmptyArtefactName() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-artefact-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("test.tif", "");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getArtefactInputStreamWithInvalidJson() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-artefact-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("filename", "");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getArtefactInputStreamWithNullBody() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-artefact-upload.json").toPath());
		assert jsonEventPayload != null;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(jsonEventPayload);
		// Set "body" to null
		((ObjectNode) rootNode).putNull("body");
		String updateJsonEventPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		return new ByteArrayInputStream(updateJsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInputStreamForIndexFileReportWithDateParameter() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-get-index-file-report-request.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInputStreamForIndexFileReportWithoutParameters() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-get-index-file-report-request.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("{\"date\": \"2024-05-17T14:30:00 0000\"}", "null");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInputStreamWithInvalidDateForIndexFileReport() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-get-index-file-report-request.json").toPath());
		jsonEventPayload = jsonEventPayload.replace("2024-05-17T14:30:00 0000", "2024-05-17T00:abc:00+0000");
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInputStreamWithInvalidBatchSequenceForIndexFileReport() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-get-index-file-report-request.json").toPath());
		jsonEventPayload = jsonEventPayload.replace("0221123.050", "0221123.00$");
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getConvertResizeImageToTifInputStream() throws IOException {
		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:api-events/api-post-convert-resize-image-to-tif.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getConvertResizeImageToTifInputStreamWithNoPathParam() throws IOException {
		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:api-events/api-post-convert-resize-image-to-tif.json").toPath());

		String pathParam = "\"pathParameters\":{\n" + "  \"mirisDocId\": \"12345678\"\n" + "}";
		String replacePathParam = "\"pathParameters\": null";
		jsonEventPayload = jsonEventPayload.replace(pathParam, replacePathParam);
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getInputStreamByClassPath(String path) throws IOException {
		String jsonEventPayload = Files.readString(ResourceUtils.getFile(path).toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static List<ArtefactOutput> getArtefactOutputList() {
		ArtefactItemTag tag1 = new ArtefactItemTag();
		tag1.setValue("tag1");
		ArtefactItemTag tag2 = new ArtefactItemTag();
		tag2.setValue("tag2");

		ArtefactItem item1 = new ArtefactItem();
		item1.setAdditionalProperty("item1", "type1");
		ArtefactItem item2 = new ArtefactItem();
		item2.setAdditionalProperty("item2", "type2");

		ArtefactOutput artefact1 = new ArtefactOutput("1", "ArtefactName1", "ClassType1", "Status1", "Error1",
				"2023-05-01", "2023-05-10", "s3Bucket1", "DocId1", Arrays.asList(tag1, tag2),
				Arrays.asList(item1, item2));
		artefact1.setBatchSequence("BatchSeq1");
		artefact1.setArtefactContainer("Container1");
		org.iprosoft.trademarks.aws.artefacts.model.dto.Object object = new org.iprosoft.trademarks.aws.artefacts.model.dto.Object();
		object.setEtag("etag1");
		artefact1.setAdditionalProperty("extraField1", object);

		ArtefactOutput artefact2 = new ArtefactOutput("2", "ArtefactName2", "ClassType2", "Status2", "Error2",
				"2023-06-01", "2023-06-10", "s3Bucket2", "DocId2", Arrays.asList(tag1), Arrays.asList(item1));
		artefact2.setBatchSequence("BatchSeq2");
		artefact2.setArtefactContainer("Container2");
		artefact2.setAdditionalProperty("extraField2", object);

		return Arrays.asList(artefact1, artefact2);
	}

	public static IndexedFileReportGeneratorImpl.ReportGeneratorResult getReportGeneratorResult() {
		IndexedFileReportGeneratorImpl.ReportGeneratorResult result = new IndexedFileReportGeneratorImpl.ReportGeneratorResult();
		result.setBatchOutputs(getBatchOutputList());
		return result;
	}

	public static List<BatchOutput> getBatchOutputList() {
		BatchOutput batchOutput1 = BatchOutput.builder()
			.id("1")
			.batchSequence("0221123.050")
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-05-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeA")
			.user("UserA")
			.reportDate("2024-05-20T08:30:00+0000")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		BatchOutput batchOutput2 = BatchOutput.builder()
			.id("2")
			.batchSequence("0221123.111")
			.artefacts(getArtefactOutputList())
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-05-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.status("INSERTED")
			.requestType("TypeB")
			.user("UserB")
			.reportDate("2024-05-20T08:30:00+0000")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(true)
			.build();

		BatchOutput batchOutput3 = BatchOutput.builder()
			.id("3")
			.batchSequence("0221123.222")
			.artefacts(getArtefactOutputList())
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-05-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.status("INSERTED")
			.requestType("TypeC")
			.user("UserC")
			.reportDate("2024-05-20T08:30:00+0000")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		List<BatchOutput> batchOutputs = new ArrayList<>();
		batchOutputs.add(batchOutput1);
		batchOutputs.add(batchOutput2);
		batchOutputs.add(batchOutput3);
		return batchOutputs;
	}

	public static List<BatchOutput> getBatchOutputWithUtcDateTimeList() {
		BatchOutput batchOutput1 = BatchOutput.builder()
			.id("1")
			.batchSequence("0221123.050")
			.lockedDate("Sat Jun 01 16:41:52 UTC 2024")
			.creationDate("Sat Jun 01 16:41:52 UTC 2024")
			.lastModificationDate("Sun Jun 02 16:41:52 UTC 2024")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeA")
			.user("UserA")
			.reportDate("Sat Jun 01 16:41:52 UTC 2024")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		BatchOutput batchOutput2 = BatchOutput.builder()
			.id("2")
			.batchSequence("0221123.111")
			.artefacts(getArtefactOutputList())
			.lockedDate("Sat Jun 01 16:41:52 UTC 2024")
			.creationDate("Sun Jun 02 16:41:52 UTC 2024")
			.lastModificationDate("Sun Jun 02 16:41:52 UTC 2024")
			.status("INSERTED")
			.requestType("TypeB")
			.user("UserB")
			.reportDate("Sat Jun 02 16:41:52 UTC 2024")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(true)
			.build();

		BatchOutput batchOutput3 = BatchOutput.builder()
			.id("3")
			.batchSequence("0221123.222")
			.artefacts(getArtefactOutputList())
			.lockedDate("Sat Jun 01 16:41:52 UTC 2024")
			.lastModificationDate("Sun Jun 02 16:41:52 UTC 2024")
			.status("INSERTED")
			.requestType("TypeC")
			.user("UserC")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		List<BatchOutput> batchOutputs = new ArrayList<>();
		batchOutputs.add(batchOutput1);
		batchOutputs.add(batchOutput2);
		batchOutputs.add(batchOutput3);
		return batchOutputs;
	}

	public static List<BatchOutput> getBatchOutputListWithNoReportDate() {
		BatchOutput batchOutput1 = BatchOutput.builder()
			.id("1")
			.batchSequence("0221123.050")
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-06-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeA")
			.user("UserA")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		BatchOutput batchOutput2 = BatchOutput.builder()
			.id("2")
			.batchSequence("0221123.111")
			.artefacts(getArtefactOutputList())
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-07-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.status("INSERTED")
			.requestType("TypeB")
			.user("UserB")
			.reportDate("")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(true)
			.build();

		BatchOutput batchOutput3 = BatchOutput.builder()
			.id("3")
			.batchSequence("0221123.222")
			.artefacts(getArtefactOutputList())
			.lockedDate("2024-05-25T10:15:30+0000")
			.creationDate("2024-08-20T08:30:00+0000")
			.lastModificationDate("2024-05-24T12:00:00+0000")
			.status("INSERTED")
			.requestType("TypeC")
			.user("UserC")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		List<BatchOutput> batchOutputs = new ArrayList<>();
		batchOutputs.add(batchOutput1);
		batchOutputs.add(batchOutput2);
		batchOutputs.add(batchOutput3);
		return batchOutputs;
	}

	public static List<BatchOutput> getBatchOutputListWithNoDate() {
		BatchOutput batchOutput1 = BatchOutput.builder()
			.id("1")
			.batchSequence("0221123.050")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeA")
			.user("UserA")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		BatchOutput batchOutput2 = BatchOutput.builder()
			.id("2")
			.batchSequence("0221123.111")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeB")
			.user("UserB")
			.reportDate("")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(true)
			.build();

		BatchOutput batchOutput3 = BatchOutput.builder()
			.id("3")
			.batchSequence("0221123.222")
			.artefacts(getArtefactOutputList())
			.status("INSERTED")
			.requestType("TypeC")
			.user("UserC")
			.s3Bucket(S3_BUCKET_NAME)
			.s3Key(S3_BUCKET_KEY)
			.reportUrl("http://example.com/report2")
			.locked(false)
			.build();

		List<BatchOutput> batchOutputs = new ArrayList<>();
		batchOutputs.add(batchOutput1);
		batchOutputs.add(batchOutput2);
		batchOutputs.add(batchOutput3);
		return batchOutputs;
	}

	public static InputStream getBulkInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-bulk-upload.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getBulkInputWith550ArtefactsStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-bulk-550-artefacts-upload.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream setInvalidMirisDocIdGetBulkInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-bulk-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("12345678", "x12345678");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream setInvalidClassTypeGetBulkInputStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-bulk-upload.json").toPath());
		assert jsonEventPayload != null;
		jsonEventPayload = jsonEventPayload.replace("DOCUMENT", "DOC");
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static InputStream getCsvBatchUploadStream() throws IOException {
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload-csv-body.json").toPath());
		assert jsonEventPayload != null;
		return new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
	}

	public static List<ArtefactInput> getArtefactInputList() {
		ArtefactItemInput item1 = new ArtefactItemInput("storage1", "path1", "item1", "image/png", "100");
		ArtefactItemInput item2 = new ArtefactItemInput("storage1", "path1", "item2", "application/pdf", "200");
		ArtefactItemInput item3 = new ArtefactItemInput("storage1", "path1", "item3", "image/jpeg", "300");

		List<ArtefactItemInput> items1 = new ArrayList<>();
		items1.add(item1);

		List<ArtefactItemInput> items2 = new ArrayList<>();
		items2.add(item2);

		List<ArtefactItemInput> items3 = new ArrayList<>();
		items3.add(item3);

		ArtefactInput artefact1 = new ArtefactInput("Artefact1", "DOCUMENT", new ArrayList<>(), items1, "12345678");
		ArtefactInput artefact2 = new ArtefactInput("Artefact2", "CERTIFICATE", new ArrayList<>(), items2, "11223344");
		ArtefactInput artefact3 = new ArtefactInput("Artefact3", "MULTIMEDIA", new ArrayList<>(), items3, "98765432");

		List<ArtefactInput> artefactInputList = new ArrayList<>();
		artefactInputList.add(artefact1);
		artefactInputList.add(artefact2);
		artefactInputList.add(artefact3);
		return artefactInputList;
	}

	public static Artefact getArtefact(String status) {
		ArtefactItemTags tag1 = new ArtefactItemTags();
		tag1.setId(1);
		tag1.setValue("Value1");
		tag1.setKey("Key1");
		tag1.setInsertedDate(DateUtils.getCurrentDatetimeUtc());
		tag1.setType("Type1");

		ArtefactItemTags tag2 = new ArtefactItemTags();
		tag2.setId(2);
		tag2.setValue("Value2");
		tag2.setKey("Key2");
		tag2.setInsertedDate(DateUtils.getCurrentDatetimeUtc());
		tag2.setType("Type2");

		Items item1 = new Items();
		item1.setId(1);
		item1.setStorage("Storage1");
		item1.setPath("Path1");
		item1.setFilename("Filename1");
		item1.setArtefactType("ArtefactType1");
		item1.setContentType("ContentType1");
		item1.setTotalPages(10);
		item1.setJobId("JobId1");
		item1.setJobStatus("JobStatus1");

		Items item2 = new Items();
		item2.setId(2);
		item2.setStorage("Storage2");
		item2.setPath("Path2");
		item2.setFilename("Filename2");
		item2.setArtefactType("ArtefactType2");
		item2.setContentType("ContentType2");
		item2.setTotalPages(20);
		item2.setJobId("JobId2");
		item2.setJobStatus("JobStatus2");

		Artefact artefact = Artefact.builder()
			.id("12345")
			.artefactName("ArtefactName")
			.artefactClassType("CERTIFICATE")
			.status(status)
			.error("No Error")
			.indexationDate(DateUtils.getCurrentDatetimeUtc())
			.archiveDate(DateUtils.getCurrentDatetimeUtc())
			.s3Bucket("TestBucket")
			.s3Key("TestKey")
			.artefactItemTags(List.of(tag1, tag2))
			.mirisDocId("87654321")
			.items(List.of(item1, item2))
			.sizeWarning(true)
			.contentLength("1024")
			.build();

		log.info(artefact.toString());

		return artefact;
	}

	public static Map<String, AttributeValue> convertArtefactItemTagsToAttributeValueMap(ArtefactItemTags tags) {
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		attributeValueMap.put("id", AttributeValue.fromN(Integer.toString(tags.getId())));
		attributeValueMap.put("value", AttributeValue.fromS(tags.getValue()));
		attributeValueMap.put("key", AttributeValue.fromS(tags.getKey()));
		attributeValueMap.put("insertedDate", AttributeValue.fromS(tags.getInsertedDate().toString()));
		attributeValueMap.put("type", AttributeValue.fromS(tags.getType()));
		return attributeValueMap;
	}

	public static Map<String, AttributeValue> convertArtefactItemToAttributeValueMap(Items items) {
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		attributeValueMap.put("id", AttributeValue.fromN(Integer.toString(items.getId())));
		attributeValueMap.put("storage", AttributeValue.fromS(items.getStorage()));
		attributeValueMap.put("path", AttributeValue.fromS(items.getPath()));
		attributeValueMap.put("filename", AttributeValue.fromS(items.getFilename()));
		attributeValueMap.put("artefactType", AttributeValue.fromS(items.getArtefactType()));
		attributeValueMap.put("contentType", AttributeValue.fromS(items.getContentType()));
		attributeValueMap.put("totalPages", AttributeValue.fromS(String.valueOf(items.getTotalPages())));
		attributeValueMap.put("jobId", AttributeValue.fromS(items.getJobId()));
		attributeValueMap.put("jobStatus", AttributeValue.fromS(items.getJobStatus()));
		return attributeValueMap;
	}

	public static List<ArtefactJob> getArtefactJobList(String batchSequence, String jobId) {
		List<ArtefactJob> artefactJobs = new ArrayList<>();
		ArtefactJob job = new ArtefactJob();
		job.setId(jobId);
		job.setBatchSequence(batchSequence);
		job.setArtefactId(UUID.randomUUID().toString());
		job.setStatus(ArtefactStatus.INIT.name());

		ArtefactJob job1 = new ArtefactJob();
		job1.setId(jobId);
		job1.setBatchSequence(batchSequence);
		job1.setArtefactId(UUID.randomUUID().toString());
		job1.setStatus(ArtefactStatus.INDEXED.name());

		ArtefactJob job2 = new ArtefactJob();
		job2.setId(jobId);
		job2.setBatchSequence(batchSequence);
		job2.setArtefactId(UUID.randomUUID().toString());
		job2.setStatus(ArtefactStatus.CANCELED.name());

		artefactJobs.add(job);
		artefactJobs.add(job1);
		artefactJobs.add(job2);
		return artefactJobs;
	}

	public static List<ArtefactJob> getArtefactJobsNotNull() {

		List<ArtefactJob> artefactJobs = new ArrayList<>();
		ArtefactJob job = new ArtefactJob();
		job.setId(UUID.randomUUID().toString());
		job.setBatchSequence("060624.999");
		job.setArtefactId(UUID.randomUUID().toString());
		job.setStatus(ArtefactStatus.INIT.name());

		ArtefactJob job1 = new ArtefactJob();
		job1.setId(UUID.randomUUID().toString());
		job1.setBatchSequence("060624.999");
		job1.setArtefactId(UUID.randomUUID().toString());
		job1.setStatus(ArtefactStatus.INDEXED.name());

		artefactJobs.add(job);
		artefactJobs.add(job1);
		return artefactJobs;
	}

	public static List<ArtefactJob> getArtefactJobListMixOfNull() {
		List<ArtefactJob> artefactJobs = new ArrayList<>();
		ArtefactJob job = new ArtefactJob();
		job.setStatus(ArtefactStatus.INIT.name());

		ArtefactJob job1 = new ArtefactJob();

		ArtefactJob job2 = new ArtefactJob();
		job2.setStatus(ArtefactStatus.INDEXED.name());

		artefactJobs.add(job);
		artefactJobs.add(job1);
		artefactJobs.add(job2);

		return artefactJobs;
	}

	public static List<ArtefactJob> getBulkArtefactJobsNotNull() {

		List<ArtefactJob> artefactJobs = new ArrayList<>();
		ArtefactJob job = new ArtefactJob();
		job.setId(UUID.randomUUID().toString());
		job.setArtefactId(UUID.randomUUID().toString());

		ArtefactJob job1 = new ArtefactJob();
		job1.setId(UUID.randomUUID().toString());
		job1.setArtefactId(UUID.randomUUID().toString());

		artefactJobs.add(job);
		artefactJobs.add(job1);
		return artefactJobs;
	}

	public static List<ArtefactJob> getBulkArtefactJobListMixOfNull() {
		List<ArtefactJob> artefactJobs = new ArrayList<>();
		ArtefactJob job = new ArtefactJob();
		job.setStatus(ArtefactStatus.INIT.name());

		ArtefactJob job1 = new ArtefactJob();

		ArtefactJob job2 = new ArtefactJob();
		job2.setStatus(ArtefactStatus.INDEXED.name());

		artefactJobs.add(job);
		artefactJobs.add(job1);
		artefactJobs.add(job2);

		return artefactJobs;
	}

	public static Map<String, java.lang.Object> getAllJobStatusByRequestId(String requestId, String batchSequence,
			String jobId) {
		List<Map<String, java.lang.Object>> jobStatusMapList = new ArrayList<>();
		List<String> jobStatusList = new ArrayList<>();
		List<ArtefactJob> artefactJobs = getArtefactJobList(batchSequence, jobId);

		if (artefactJobs == null || artefactJobs.isEmpty()) {
			return Collections.emptyMap();
		}

		for (ArtefactJob job : artefactJobs) {
			if (batchSequence == null) {
				batchSequence = job.getBatchSequence();
				log.info("batchSequence : {} ", batchSequence);
			}
			Map<String, java.lang.Object> jobStatusMap = new HashMap<>();
			jobStatusMap.put("jobId", job.getId());
			jobStatusMap.put("jobStatus", job.getStatus());
			jobStatusMap.put("artefactId", job.getArtefactId());
			jobStatusMapList.add(jobStatusMap);
			jobStatusList.add(job.getStatus());
		}

		Map<String, java.lang.Object> responseMap = new LinkedHashMap<>();
		responseMap.put("requestId", requestId);
		responseMap.put("batchSequence", batchSequence);
		responseMap.put("jobs", jobStatusMapList);
		responseMap.put("batchStatus", BatchStatus.INDEXED);

		responseMap.forEach((key, value) -> log
			.info("ArtefactJobServiceImpl getAllJobStatusByRequestId responseMap key:{} : value:{}", key, value));
		return responseMap;
	}

	public static List<ArtefactNotesEntity> getArtefactNotesEntityList() {
		ArtefactNotesEntity artefactNotes = new ArtefactNotesEntity();
		artefactNotes.setId(1L);
		artefactNotes.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNotes.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNotes.setContent("content");
		artefactNotes.setAuthor("author");
		artefactNotes.setMirisDocId("123123");
		ArtefactNotesEntity artefactNotes2 = new ArtefactNotesEntity();
		artefactNotes2.setId(2L);
		artefactNotes2.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNotes2.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNotes2.setContent("content");
		artefactNotes2.setAuthor("author");
		artefactNotes2.setMirisDocId("123123");
		return List.of(artefactNotes, artefactNotes2);
	}

	public static ArtefactFilterCriteria getArtefactFilterCriteria() {
		ArtefactFilterCriteria artefactFilterCriteria = ArtefactFilterCriteria.builder()
			.mirisDocId("123123")
			.batchStatus("654123.555")
			.docType(ArtefactClassType.CERTIFICATE.name())
			.dateFrom("dataForm")
			.dateTo(DateUtils.getCurrentDateShortStr())
			.insertedDate(DateUtils.getCurrentDateShortStr())
			.reportDate(DateUtils.getCurrentDateShortStr())
			.build();
		return artefactFilterCriteria;
	}

	public static ArtefactsDTO getArtefactsDTO() {
		ArtefactsDTO artefactsDTO = ArtefactsDTO.builder()
			.id(1L)
			.artefactClass(ArtefactClassEnum.DOCUMENT)
			.mirisDocId("1236547")
			.artefactName("name")
			.build();
		return artefactsDTO;
	}

	public static ArtefactsEntity getArtefactsEntity() {
		ArtefactsEntity artefactsEntity = new ArtefactsEntity();

		artefactsEntity.setId(1L);
		artefactsEntity.setStatus(ArtefactStatusEnum.INDEXED);
		artefactsEntity.setArtefactClass(ArtefactClassEnum.CERTIFICATE);
		artefactsEntity.setIndexationDate(ZonedDateTime.now());
		artefactsEntity.setArchiveDate(ZonedDateTime.now().plusDays(30));
		artefactsEntity.setS3Bucket("example-bucket");
		artefactsEntity.setMirisDocId("DOC001");
		artefactsEntity.setArtefactName("Example Artefact");
		artefactsEntity.setArtefactUUID(UUID.randomUUID().toString());
		artefactsEntity.setLastModDate(ZonedDateTime.now());
		artefactsEntity.setLastModUser("user123");
		artefactsEntity.setImapsGenId("IMAPS001");
		artefactsEntity.setImapsDocName("Document Name");
		artefactsEntity.setActiveArtefactItem(BigInteger.valueOf(1));
		artefactsEntity.setActiveJobId("123456");
		artefactsEntity.setLastError(null);
		artefactsEntity.setErrorDate(null);
		artefactsEntity.setArtefactNote(null);
		artefactsEntity.setArtefactItems(new ArrayList<>());
		artefactsEntity.setArtefactTags(new ArrayList<>());
		artefactsEntity.setBatch(null);

		return artefactsEntity;
	}

	public static List<ArtefactsEntity> getArtefactsEntityList() {
		List<ArtefactsEntity> artefactsEntityList = new ArrayList<>();
		ArtefactsEntity artefactsEntity1 = new ArtefactsEntity();
		artefactsEntity1.setId(1L);
		artefactsEntity1.setStatus(ArtefactStatusEnum.INDEXED);
		artefactsEntity1.setArtefactClass(ArtefactClassEnum.CERTIFICATE);
		artefactsEntity1.setIndexationDate(ZonedDateTime.now());
		artefactsEntity1.setArchiveDate(ZonedDateTime.now().plusDays(30));
		artefactsEntity1.setS3Bucket("example-bucket-1");
		artefactsEntity1.setMirisDocId("DOC001");
		artefactsEntity1.setArtefactName("Example Artefact 1");
		artefactsEntity1.setArtefactUUID(UUID.randomUUID().toString());
		artefactsEntity1.setLastModDate(ZonedDateTime.now());
		artefactsEntity1.setLastModUser("user123");
		artefactsEntity1.setImapsGenId("IMAPS001");
		artefactsEntity1.setImapsDocName("Document Name 1");
		artefactsEntity1.setActiveArtefactItem(BigInteger.ONE);
		artefactsEntity1.setActiveJobId("JOB123");
		artefactsEntity1.setLastError(null);
		artefactsEntity1.setErrorDate(null);
		artefactsEntity1.setArtefactItems(new ArrayList<>());
		artefactsEntity1.setArtefactTags(new ArrayList<>());
		artefactsEntity1.setBatch(null);
		artefactsEntityList.add(artefactsEntity1);

		ArtefactsEntity artefactsEntity2 = new ArtefactsEntity();
		artefactsEntity2.setId(2L);
		artefactsEntity2.setStatus(ArtefactStatusEnum.INDEXED);
		artefactsEntity2.setArtefactClass(ArtefactClassEnum.CERTIFICATE);
		artefactsEntity2.setIndexationDate(ZonedDateTime.now());
		artefactsEntity2.setArchiveDate(ZonedDateTime.now().plusDays(60));
		artefactsEntity2.setS3Bucket("example-bucket-2");
		artefactsEntity2.setMirisDocId("DOC002");
		artefactsEntity2.setArtefactName("Example Artefact 2");
		artefactsEntity2.setArtefactUUID(UUID.randomUUID().toString());
		artefactsEntity2.setLastModDate(ZonedDateTime.now());
		artefactsEntity2.setLastModUser("user456");
		artefactsEntity2.setImapsGenId("IMAPS002");
		artefactsEntity2.setImapsDocName("Document Name 2");
		artefactsEntity2.setActiveArtefactItem(BigInteger.TWO);
		artefactsEntity2.setActiveJobId("JOB456");
		artefactsEntity2.setLastError(null);
		artefactsEntity2.setErrorDate(null);
		artefactsEntity2.setArtefactItems(new ArrayList<>());
		artefactsEntity2.setArtefactTags(new ArrayList<>());
		artefactsEntity2.setBatch(null);
		artefactsEntityList.add(artefactsEntity2);
		return artefactsEntityList;
	}

	public static ArtefactJob getArtefactJob() {
		ArtefactJob job = new ArtefactJob();
		job.setStatus(ArtefactStatus.INSERTED.name());
		job.setId(UUID.randomUUID().toString());
		job.setArtefactId(UUID.randomUUID().toString());
		return job;
	}

	public static ArtefactJob getArtefactJobWithCancelStatus() {
		ArtefactJob job = new ArtefactJob();
		job.setStatus(ArtefactStatus.CANCELED.name());
		job.setId(UUID.randomUUID().toString());
		job.setArtefactId(UUID.randomUUID().toString());
		return job;
	}

	public static EmailDetails getEmailDetails() {
		EmailDetails emailDetails = new EmailDetails();

		// Setting sender's email
		emailDetails.setFrom("sender@example.com");

		// Setting recipients
		emailDetails.setTo(Arrays.asList("recipient1@example.com", "recipient2@example.com"));

		// Setting CC recipients
		emailDetails.setCc(Arrays.asList("cc1@example.com", "cc2@example.com"));

		// Setting BCC recipients
		emailDetails.setBcc(Arrays.asList("bcc1@example.com", "bcc2@example.com"));

		// Setting the subject of the email
		emailDetails.setSubject("Sample Email Subject");

		// Setting the body of the email
		emailDetails.setBody("This is a sample email body content.");

		// Setting an attachment
		EmailDetails.Attachment attachment = new EmailDetails.Attachment();
		attachment.setFilename("sample.pdf");
		attachment.setContentType("application/pdf");
		attachment.setBase64Content("base64EncodedString"); // Assuming base64 content for
		// attachment
		emailDetails.setAttachment(attachment);

		return emailDetails;
	}

	public static ArtefactNotesEntity getArtefactNotesEntity() {
		ArtefactNotesEntity artefactNote = new ArtefactNotesEntity();
		artefactNote.setAuthor("ALOGOTHETIS");
		artefactNote.setMirisDocId("DOC001");
		artefactNote.setContent("This is a note.");
		artefactNote.setCreatedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNote.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		return artefactNote;
	}

	public static Map<String, java.lang.Object> getJobStatusResponseMap() {
		Map<String, java.lang.Object> jobStatusResponseMap = new LinkedHashMap<>();
		jobStatusResponseMap.put("requestId", UUID.randomUUID().toString());
		jobStatusResponseMap.put("batchSequence", "060624.55");
		jobStatusResponseMap.put("jobs", getArtefactJobsNotNull());
		jobStatusResponseMap.put("batchStatus", "INIT");
		return jobStatusResponseMap;
	}

	public static List<IArtefact> createMockArtefacts(int count) {
		List<IArtefact> artefacts = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			IArtefact mockArtefact = Mockito.mock(IArtefact.class);
			Mockito.when(mockArtefact.getArtefactItemId()).thenReturn("artefact-" + i);
			artefacts.add(mockArtefact);
		}
		return artefacts;
	}

	public static BatchInputDynamoDb getBatchInputDynamoDb() {
		BatchInputDynamoDb batch = new BatchInputDynamoDb();
		batch.setBatchSequence("020202024.55");
		batch.setCreationDate(ZonedDateTime.now());
		batch.setStatus("INSERTED");
		batch.setRequestType("ADDENDUM");
		batch.setOperator(getOperator());
		batch.setJobs(getArtefactJobListMixOfNull());
		batch.setRequestId(UUID.randomUUID().toString());
		batch.setScannedType(ScannedAppType.ADDENDUM);
		batch.setArtefacts(getListArtefactDynamoDb(10));
		return batch;
	}

	public static List<ArtefactDynamoDb> getListArtefactDynamoDb(int count) {
		List<ArtefactDynamoDb> artefactList = new ArrayList<>();

		for (int i = 1; i <= count; i++) {
			ArtefactDynamoDb artefact = new ArtefactDynamoDb();
			artefact.setArtefactItemId("artefactId_" + i);
			artefact.setInsertedDate(ZonedDateTime.now());
			artefact.setPath("/path/to/artefact_" + i);
			artefact.setUserId("user_" + i);
			artefact.setContentType("application/pdf");
			artefact.setChecksum("checksum_" + i);
			artefact.setContentLength(1024L * i); // Example content length
			artefact.setStatus("INIT");
			artefact.setFileName("document_" + i + ".pdf");
			artefact.setBucket("bucket_" + i);
			artefact.setKey("key_" + i);
			artefact.setBatchSequenceId("020202024.00" + i);
			artefact.setScannedType(ScannedAppType.ADDENDUM);
			artefactList.add(artefact);
		}
		return artefactList;
	}

	public static Operator getOperator() {
		Operator operator = new Operator();
		operator.setUsername("userName");
		operator.setId(1L);
		operator.setCognitoId("CognitoId");
		operator.setAwsId("awsId");
		return operator;
	}

	public static ArtefactIndexDto getArtefactIndexDto() {
		ArtefactIndexDto artefactIndexDto = new ArtefactIndexDto();
		artefactIndexDto.setMirisDocId("123654");
		return artefactIndexDto;
	}

}
