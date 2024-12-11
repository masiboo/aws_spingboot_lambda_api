package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BatchJobCancelByRequestId;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.ExtractApiParameterHttpMethodUtil;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class BatchJobCancelByRequestId implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private final ArtefactJobService artefactJobService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: {}", in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("Event Dump: {}", event);

		List<String> paramList = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);
		String requestId = paramList.stream().findFirst().orElse(null);
		if (requestId != null) {
			log.info("requestId: {}", requestId);
		}
		else {
			log.error("Missing 'requestId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, "Missing 'requestId' parameter in path",
					true);
		}

		Map<String, Object> jobStatusResponseMap;
		try {
			jobStatusResponseMap = artefactJobService.getAllJobStatusByRequestId(requestId);
			log.info("Found job status {}", jobStatusResponseMap);
		}
		catch (Exception e) {
			log.error("Exception for artefactJobService.getAllJobStatusByRequestId(requestId) reason: {}",
					e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception for artefactJobService.getAllJobStatusByRequestId(requestId) reason: " + e.getMessage(),
					true);
		}

		if (CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
					"No jobs found for this requestId: " + requestId);
		}

		return processJobStatusResponseMap(requestId, jobStatusResponseMap);
	}

	private APIGatewayV2HTTPResponse processJobStatusResponseMap(String requestId,
			Map<String, Object> jobStatusResponseMap) {
		if (!StringUtils.hasText(requestId)) {
			log.error("requestId is null/empty {}", requestId);
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
					String.format("requestId %s is null/empty", requestId));
		}

		if (CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
			log.error("No jobs found for this requestId: {}", requestId);
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
					"No jobs found for this requestId: " + requestId);
		}
		try {
			processJobStatusResponseMap(jobStatusResponseMap);
		}
		catch (Exception e) {
			log.error("Exception when processing job status response map: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception when processing job status response map: " + e.getMessage(), true);
		}
		return buildResponse(requestId);
	}

	private void processJobStatusResponseMap(Map<String, Object> jobStatusResponseMap) {
		jobStatusResponseMap.forEach((key, value) -> {
			log.info("jobStatusResponseMap's Key {} : Value {}", key, value);
			if ("jobs".equalsIgnoreCase(key)) {
				processJobs(value);
			}
		});
	}

	private void processJobs(Object value) {
		if (value instanceof List<?>) {
			List<?> jobs = (List<?>) value;
			log.info("Found jobs: {}", jobs);
			if (!jobs.isEmpty()) {
				log.info("Found jobs: {} size: {}", jobs, jobs.size());
				for (Object job : jobs) {
					if (job instanceof Map<?, ?> jobMap) {
						String jobId = (String) jobMap.get("jobId");
						log.info("jobId: {}", jobId);
						if (StringUtils.hasText(jobId)) {
							artefactJobService.updateJobWithStatus(jobId, ArtefactStatus.CANCELED.getStatus());
							log.info("Job jobId:{} is set to CANCELED", jobId);
						}
					}
				}
			}
		}
	}

	private APIGatewayV2HTTPResponse buildResponse(String requestId) {
		try {
			String responseBody = String.format("For the requestId %s. All job statuses set to CANCELED", requestId);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.CREATED, responseBody, false);
		}
		catch (Exception e) {
			log.error("Exception when returning APIGatewayV2HTTPResponse: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception when returning APIGatewayV2HTTPResponse: " + e.getMessage(), true);
		}
	}

}
