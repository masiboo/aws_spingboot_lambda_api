package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.ArtefactUploadRequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ArtefactWriterFunction implements Function<InputStream, String> {

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	private final ArtefactJobService artefactJobService;

	@Override
	public String apply(InputStream in) {
		log.info("InputStream Dump: {}", in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("Event Dump: {}", event);

		if (event.getBody() == null || event.getBody().isEmpty()) {
			log.error("Event body is missing");
			return "";
		}

		// try {
		// ArtefactInput artefact;
		// try {
		// String jsonString = event.getBody();
		// artefact = JsonConverterUtil.getArtefactInputFromJson(jsonString);
		// }
		// catch (Exception e) {
		// log.error("Invalid request body error: {}", e.getMessage());
		// return
		// ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_BAD_REQUEST,
		// "Invalid request body error " + e.getMessage(), true);
		// }
		//
		// Map<String, String> inputValidation =
		// artefactService.validateArtefact(artefact);
		// String validation = "OK";
		// String artefactId = UUID.randomUUID().toString();
		// if (!inputValidation.isEmpty()) {
		// validation = "ERROR: " +
		// ArtefactUploadRequestUtil.mapToString(inputValidation);
		// log.error("Validation errors {}", validation);
		// return
		// ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_BAD_REQUEST,
		// validation, true);
		// }
		//
		// String filename = "";
		// if (!artefact.getItems().isEmpty()) {
		// filename = artefact.getItems().get(0).getFilename();
		// }
		// String username = "Anonymous";
		// ArtefactDynamoDb item = new ArtefactDynamoDb(artefactId, ZonedDateTime.now(),
		// username);
		// String bucket = SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET;
		// String key = "Aws-" + DateUtils.getCurrentDateShortStr() + "/" + artefactId +
		// "/" + filename;
		// item.setFileName(filename);
		// item.setBucket(bucket);
		// item.setKey(key);
		//
		// Long contentLength = artefact.getItems()
		// .stream()
		// .findFirst()
		// .map(ArtefactItemInput::getContentLength)
		// .map(SafeParserUtil::safeParseLong)
		// .orElse(0L);
		// log.info("contentLength in bytes: {}", contentLength);
		// FileSizeValidator fileSizeValidator = new FileSizeValidator();
		// String sizeWarning = fileSizeValidator.validate(contentLength,
		// artefact.getArtefactClassType());
		// item.setSizeWarning(StringUtils.isNotBlank(sizeWarning));
		// item.setContentLength(contentLength);
		//
		// // existing logo, multimedia and sound Artefact will be marked as DELETE and
		// // new one will become
		// // INDEXED
		// // MPD-431, MPD-439 and MPD-747
		// if (StringUtils.isNotBlank(artefact.getMirisDocId()) && artefactService
		// .hasFileWithSameDocId(artefact.getMirisDocId(),
		// artefact.getArtefactClassType())) {
		// log.info("artefactService.hasFileWithSameDocId returns true");
		// item.setStatus(ArtefactStatus.INDEXED.toString());
		// log.info("set current artefact as indexed with mirisDocId: {}",
		// artefact.getMirisDocId());
		// }
		// else {
		// log.info(
		// "artefactService.hasFileWithSameDocId returns false meaning no existing
		// artefact with mirisDocId: {}",
		// artefact.getMirisDocId());
		//
		// }
		// item.setMirisDocId(artefact.getMirisDocId());
		// item.setArtefactClassType(artefact.getArtefactClassType());
		//
		// List<ArtefactTag> tags = new ArrayList<>();
		// tags.add(new ArtefactTag(artefactId, "untagged", "true", ZonedDateTime.now(),
		// username,
		// DocumentTagType.SYSTEMDEFINED));
		// log.info("saving transaction|correlation-id: {} on path",
		// item.getArtefactItemId());
		// artefactService.saveDocument(item, tags);
		//
		// String jobId = UUID.randomUUID().toString();
		// Map<String, String> metadata = new HashMap<>();
		// metadata.put(METADATA_KEY_ARTEFACT_ID, artefactId);
		// metadata.put(METADATA_KEY_TRACE_ID, jobId);
		// metadata.put(METADATA_KEY_MIRIS_DOCID, artefact.getMirisDocId());
		//
		// String preSignedGetUrl = "";
		// try {
		// preSignedGetUrl = ArtefactUploadRequestUtil.generatePresidedUrl(s3Service,
		// item.getKey(),
		// ArtefactUploadRequestUtil.createDurationMap(), metadata);
		// }
		// catch (Exception e) {
		// log.warn("ArtefactUploadRequestUtil.generatePresidedUrl() exception: {}",
		// e.getMessage());
		// }
		// log.info("String preSignedGetUrl = {}", preSignedGetUrl);
		//
		// ArtefactJob job = new ArtefactJob().withId(jobId)
		// .withArtefactId(artefactId)
		// .withS3SignedUrl(preSignedGetUrl)
		// .withStatus("INIT");
		// artefactJobService.saveJob(job);
		//
		// Map<String, Object> outputmap = new HashMap<>();
		// outputmap.put("artefactId", artefactId);
		// outputmap.put("signedS3Url", preSignedGetUrl);
		// outputmap.put("jobId", jobId);
		// if (StringUtils.isNotBlank(sizeWarning)) {
		// outputmap.put("warning", sizeWarning);
		// }
		//
		// ObjectMapper objectMapper = new ObjectMapper();
		// return
		// ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(SC_CREATED.getStatusCode(),
		// objectMapper.writeValueAsString(outputmap), false);
		// }
		// catch (Exception e) {
		// log.error("Exception while uploading single artefact: {}", e.getMessage());
		// return
		// ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
		// "Exception while uploading single artefact " + e.getMessage(), true);
		// }

		return null;
	}

}
