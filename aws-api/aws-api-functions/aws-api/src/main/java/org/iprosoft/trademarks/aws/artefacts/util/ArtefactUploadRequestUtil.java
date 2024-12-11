package org.iprosoft.trademarks.aws.artefacts.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.*;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.AwsEnvironmentEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ScannedAppType;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.*;

@Slf4j
public class ArtefactUploadRequestUtil {

	private static final int DEFAULT_DURATION_HOURS = 48;

	public static final String MIRIS_DOC_ID_KEY = "mirisDocId";

	public static String validateQueryParams(String scannedApp, String overwriteBatchStr) {
		StringBuilder errorMsg = new StringBuilder();
		if (StringUtils.isNotBlank(overwriteBatchStr)) {
			try {
				boolean overwriteBatch = Boolean.parseBoolean(overwriteBatchStr);
				log.info("overwriteBatch has valid boolean value: " + overwriteBatch);
			}
			catch (Exception e) {
				errorMsg.append("overwriteBatch must be either true or false").append("\n");
			}
		}
		if (StringUtils.isNotBlank(scannedApp)) {
			if (!ScannedAppType.isAllowedType(scannedApp)) {
				String error = "Invalid 'scannedApp' provided :'" + scannedApp + "'  and allowed values are "
						+ Arrays.toString(ScannedAppType.values());
				errorMsg.append(error).append("\n");
			}
		}
		else {
			errorMsg.append(" 'scannedApp' query parameter is missing");
		}
		return errorMsg.toString();
	}

	public static String validateEnvironment(String environment) {
		if (StringUtils.isNotBlank(environment)) {
			if (!AwsEnvironmentEnum.isAllowedType(environment)) {
				return "Invalid 'environment' provided :'" + environment + "'  and allowed values are "
						+ Arrays.toString(ScannedAppType.values());
			}
		}
		else {
			return " 'environment' parameter is missing";
		}
		return "";
	}

	public static String validateQueryParamsDataAndBatchSequence(String dateStr) {
		StringBuilder errorMsg = new StringBuilder();
		try {
			dateStr = DateUtils.ensureCorrectFormat(dateStr);
			ZonedDateTime date = SafeParserUtil.safeParseZonedDateTime(dateStr);
			log.info("Provided date pares correctly: " + date.toString());
		}
		catch (Exception e) {
			errorMsg.append("Date format is incorrect. It must be in yyyy-MM-dd'T'HH:mm:ssZ format. Error msg: ")
				.append(e.getMessage())
				.append("\n");
		}
		return errorMsg.toString();
	}

	public static String generatePresidedUrl(S3Service s3Service, final String key, final Map<String, String> query,
			final Map<String, String> metadata) {
		Duration duration = caculateDuration(query);
		URL url = s3Service.presignPutUrl(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET, key, duration, metadata);
		return url != null ? url.toString() : null;
	}

	private static Duration caculateDuration(final Map<String, String> query) {
		Integer durationHours = query != null && query.containsKey("duration") ? Integer.valueOf(query.get("duration"))
				: Integer.valueOf(DEFAULT_DURATION_HOURS);
		return Duration.ofHours(durationHours);
	}

	public static Set<String> getExistedBatchSequence(BatchService batchService, List<String> artefactBatchList) {
		if (artefactBatchList != null) {
			return artefactBatchList.stream().filter(batchSequence -> {
				BatchOutput batchOutput = batchService.getBatchDetail(batchSequence);
				return batchOutput != null && batchOutput.getBatchSequence().equalsIgnoreCase(batchSequence);
			}).collect(Collectors.toSet());
		}
		else {
			return new HashSet<>();
		}
	}

	public static void deleteExistedBatchSequence(BatchService batchService, Set<String> existingBatchSequence) {
		if (existingBatchSequence != null) {
			existingBatchSequence.forEach(batchSequence -> {
				// soft delete artefacts by sequence
				batchService.updateBatchWithStatus(batchSequence, BatchStatus.DELETED.toString());
				log.info("artefacts with batchSequence {} deleted", batchSequence);
			});
		}
	}

	public static APIGatewayV2HTTPResponse buildConflictResponse(Set<String> existingBatchSequence) {
		Map<String, java.lang.Object> responseMap = buildWarningResponseMap(existingBatchSequence);
		ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_CONFLICT,
				new ApiMapResponse(responseMap));
		try {
			return buildResponse(response.getStatus(), response.getHeaders(), response.getResponse());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, java.lang.Object> buildWarningResponseMap(Set<String> existingBatchSequence) {
		String batches = existingBatchSequence.stream().map(String::valueOf).collect(Collectors.joining(","));
		Map<String, java.lang.Object> map = new HashMap<>();
		map.put("batches", batches);
		map.put("message", " batch sequence already exists");
		return map;
	}

	public static APIGatewayV2HTTPResponse buildResponse(final ApiResponseStatus status,
			final Map<String, String> headers, final ApiResponse apiResponse) throws IOException {
		String gatewayResponse = "";
		Map<String, java.lang.Object> response = new HashMap<>();
		response.put("statusCode", status.getStatusCode());

		if (apiResponse instanceof ApiRedirectResponse) { // http redirect
			headers.clear();
			headers.put("Location", ((ApiRedirectResponse) apiResponse).getRedirectUri());
		}
		else if (status.getStatusCode() == SC_FOUND.getStatusCode() && apiResponse instanceof ApiMessageResponse) { // string
			// response
			headers.clear();
			headers.put("Location", ((ApiMessageResponse) apiResponse).getMessage());
		}
		else if (apiResponse instanceof ApiMapResponse) { // json response
			gatewayResponse = GsonUtil.getInstance().toJson(((ApiMapResponse) apiResponse).getMap());
			response.put("body", gatewayResponse);
		}
		else if (apiResponse instanceof ApiMapBatchResponse) { // json response
			if (((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.CSV)) {
				gatewayResponse = CsvConverterUtil.convertToCSV(((ApiMapBatchResponse) apiResponse).getMapList());
			}
			else if (((ApiMapBatchResponse) apiResponse).getFormat().equals(DocumentFormatType.JSON)) {
				gatewayResponse = GsonUtil.getInstance().toJson(((ApiMapBatchResponse) apiResponse).getMapList());
			}
			response.put("body", gatewayResponse);
		}
		else {
			gatewayResponse = GsonUtil.getInstance().toJson(apiResponse);
			response.put("body", gatewayResponse);
		}

		Map<String, String> jsonheaders = createJsonHeaders();
		jsonheaders.putAll(headers);
		response.put("headers", jsonheaders);

		APIGatewayV2HTTPResponse gatewayV2HTTPResponse = APIGatewayV2HTTPResponse.builder()
			.withStatusCode(status.getStatusCode())
			.withHeaders(jsonheaders)
			.withBody(gatewayResponse)
			.build();

		return gatewayV2HTTPResponse;
	}

	public static APIGatewayV2HTTPResponse createErrorResponse(Map<String, java.lang.Object> input) {
		ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST, new ApiMapResponse(input));
		APIGatewayV2HTTPResponse gatewayV2HTTPResponse;
		try {
			gatewayV2HTTPResponse = buildResponse(response.getStatus(), response.getHeaders(), response.getResponse());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return gatewayV2HTTPResponse;
	}

	private static Map<String, String> createJsonHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Content-Type", "application/json");
		return headers;
	}

	public static String logValidationErrors(Map<String, String> errors) {
		String errorMessage = null;
		if (!errors.isEmpty()) {
			StringJoiner joiner = new StringJoiner(", ");
			errors.forEach((key, value) -> joiner.add(key + ": " + value));
			errorMessage = joiner.toString();
			log.info(errorMessage);
		}
		return errorMessage;
	}

	public static String mapToString(Map<String, String> map) {
		StringJoiner sj = new StringJoiner(", ");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sj.add(entry.getKey() + "=" + entry.getValue());
		}
		return sj.toString();
	}

	public static String objectMapToString(Map<String, java.lang.Object> map) {
		StringJoiner sj = new StringJoiner(", ");
		for (Map.Entry<String, java.lang.Object> entry : map.entrySet()) {
			sj.add(entry.getKey() + "=" + entry.getValue());
		}
		return sj.toString();
	}

	public static String listMapToString(List<Map<String, String>> listOfMaps) {
		StringJoiner listJoiner = new StringJoiner("; ");
		for (Map<String, String> map : listOfMaps) {
			listJoiner.add(mapToString(map));
		}
		return listJoiner.toString();
	}

	public static Map<String, String> createDurationMap() {
		Map<String, String> duration = new HashMap<>();
		duration.put("duration", String.valueOf(Duration.ofHours(1).toHours()));
		return duration;
	}

}
