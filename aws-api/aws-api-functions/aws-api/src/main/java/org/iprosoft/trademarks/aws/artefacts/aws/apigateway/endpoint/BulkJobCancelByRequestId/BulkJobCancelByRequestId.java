package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BulkJobCancelByRequestId;

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
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class BulkJobCancelByRequestId implements Function<InputStream, APIGatewayV2HTTPResponse> {

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
			log.info("Found path parameter requestId: {}", requestId);
		}
		else {
			log.error("Missing 'requestId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, "Missing 'requestId' parameter in path",
					true);
		}

		Map<String, Object> jobStatusResponseMap;
		try {
			jobStatusResponseMap = artefactJobService.getAllBulkJobStatusByRequestId(requestId);
			log.info("Found job status {}", jobStatusResponseMap);
			log.info("Result of CollectionUtils.isNullOrEmpty(jobStatusResponseMap) {}",
					CollectionUtils.isNullOrEmpty(jobStatusResponseMap));
			if (CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
				log.warn("No jobs found for this requestId: {}", requestId);
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"No jobs found for this requestId: " + requestId);
			}
			List<String> jobIds = getAllJobIdWithNotCancelledStatus(jobStatusResponseMap);
			log.info(" getAllJobIdStatusNotCancelled(jobStatusResponseMap) jobIds {}", jobIds);
			if (jobIds != null) {
				for (String jobId : jobIds) {
					try {
						UpdateItemResponse response = artefactJobService.updateJobWithStatus(jobId,
								ArtefactStatus.CANCELED.getStatus());
						log.info("artefactJobService.updateJobWithStatus response {}", response);
						if (response != null) {
							log.info("artefactJobService.updateJobWithStatus response {}", response);
							log.info("Job jobId: {} is set to CANCELED", jobId);
						}
						else {
							log.warn("UpdateItemResponse for jobId: {} is not valid: {}", jobId, response);
						}
					}
					catch (Exception e) {
						log.error("Exception in artefactJobService.updateJobWithStatus {}", e.getMessage());
						return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
								"Exception in artefactJobService.updateJobWithStatus: " + e.getMessage(), true);
					}
				}
			}
			else {
				log.warn("jobIds is null {}", jobIds);
				log.warn("No jobs found for this requestId: {}", requestId);
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"No jobs found for this requestId: " + requestId);
			}
		}
		catch (Exception e) {
			log.error("Exception for artefactJobService.getAllJobStatusByRequestId(requestId) reason: {}",
					e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception for artefactJobService.getAllJobStatusByRequestId(requestId) reason: " + e.getMessage(),
					true);
		}
		return buildResponse(requestId);
	}

	private List<String> getAllJobIdWithNotCancelledStatus(Map<String, Object> jobStatusResponseMap) {
		if (!CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
			List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobStatusResponseMap.get("jobs");
			if (!CollectionUtils.isNullOrEmpty(jobs)) {
				log.info("From Map<String, Object> jobStatusResponseMap found jobs {}, size {}", jobs, jobs.size());
				List<String> jobId = jobs.stream().filter(job -> {
					boolean statusCanceled = !ArtefactStatus.CANCELED.getStatus()
						.equalsIgnoreCase(job.get("jobStatus").toString());
					if (statusCanceled) {
						log.info("Job status is not CANCELED for jobId: {}", job.get("jobId"));
					}
					return statusCanceled;
				})
					.peek(job -> log.info("Processing job: {}", job))
					.map(job -> (String) job.get("jobId"))
					.peek(id -> log.info("Collectors.toList() jobId: {}", id))
					.collect(Collectors.toList());
				log.info("Found all jobId {}", jobId);
				return jobId;
			}
			else {
				log.warn("jobStatusResponseMap.get('jobs') jobs is null {}", jobs);
				return null;
			}
		}
		else {
			log.warn("Map<String, Object> jobStatusResponseMap is empty or null {}", jobStatusResponseMap);
			return null;
		}
	}

	private APIGatewayV2HTTPResponse buildResponse(String requestId) {
		try {
			String responseBody = String.format("For the requestId %s. All job statuses set to CANCELED", requestId);
			log.info("Return successful responseBody {} and HttpStatusCode.CREATED {}", responseBody,
					HttpStatusCode.CREATED);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.CREATED, responseBody, false);
		}
		catch (Exception e) {
			log.error("Exception when returning APIGatewayV2HTTPResponse: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception when returning APIGatewayV2HTTPResponse: " + e.getMessage(), true);
		}
	}

}
