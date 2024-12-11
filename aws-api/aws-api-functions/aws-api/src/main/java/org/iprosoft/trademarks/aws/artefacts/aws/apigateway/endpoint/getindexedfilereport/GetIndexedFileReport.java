package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getindexedfilereport;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;
import org.iprosoft.trademarks.aws.artefacts.service.reporter.IndexedFileReportGenerator;
import org.iprosoft.trademarks.aws.artefacts.service.reporter.IndexedFileReportGeneratorImpl;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactUploadRequestUtil;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant.STATUS_CODE_BAD_REQUEST;

@AllArgsConstructor
@Slf4j
public class GetIndexedFileReport implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private final IndexedFileReportGenerator indexedFileReportGenerator;

	@Override
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: " + in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("Event Dump: " + event);

		String dateStr = null;
		try {
			if (event.getQueryStringParameters() != null) {
				dateStr = event.getQueryStringParameters().get("date");
				log.info("Date parameter: " + dateStr);
				if (StringUtils.isNotBlank(dateStr)) {
					String validationErrors = ArtefactUploadRequestUtil
						.validateQueryParamsDataAndBatchSequence(dateStr);
					if (StringUtils.isNotBlank(validationErrors)) {
						log.error("Query parameter validation error " + validationErrors);
						return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(STATUS_CODE_BAD_REQUEST,
								validationErrors, true);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("error getting  query parameters", e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(STATUS_CODE_BAD_REQUEST,
					"Exception for query parameter is handling " + e.getMessage(), true);
		}
		IndexedFileReportGeneratorImpl.ReportGeneratorResult reportGeneratorResult;
		if (StringUtils.isNotBlank(dateStr)) {
			reportGeneratorResult = indexedFileReportGenerator.getAllBatchOutputReportByDate(dateStr);
		}
		else {
			reportGeneratorResult = indexedFileReportGenerator.generateIndexFileReport();
		}
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		if ((reportGeneratorResult != null) && (reportGeneratorResult.getBatchOutputs() != null)) {
			if (!Objects.requireNonNull(reportGeneratorResult.getBatchOutputs()).isEmpty()) {
				log.info("List of BatchOutput is generated with size: {}",
						reportGeneratorResult.getBatchOutputs().size());
				try {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_CREATED,
							objectMapper.writeValueAsString(reportGeneratorResult.getBatchOutputs()), false);
				}
				catch (JsonProcessingException e) {
					log.error("Exception when create response objectMapper.writeValueAsString {}", e.getMessage());
					throw new RuntimeException(e);
				}
			}
			else {
				log.error(
						"Error: IndexedFileReportGenerator returned reportGeneratorResult.getBatchOutputs null/empty.");
				try {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
							objectMapper.writeValueAsString(reportGeneratorResult));
				}
				catch (JsonProcessingException e) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(
							HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
							"Error:  objectMapper.writeValueAsString exception: " + e.getMessage(), true);
				}
			}
		}
		else {
			log.error(
					"Error: (reportGeneratorResult != null) && (reportGeneratorResult.getBatchOutputs() != null is false");
			try {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, objectMapper
					.writeValueAsString(Objects.requireNonNull(reportGeneratorResult).getBatchOutputs()));
			}
			catch (JsonProcessingException e) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
						"Error:  objectMapper.writeValueAsString exception: " + e.getMessage(), true);
			}
		}
	}

}
