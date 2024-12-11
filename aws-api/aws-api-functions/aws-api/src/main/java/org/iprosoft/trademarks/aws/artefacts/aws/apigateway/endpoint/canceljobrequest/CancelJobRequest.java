package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.canceljobrequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.iprosoft.trademarks.aws.artefacts.util.ExtractApiParameterHttpMethodUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class CancelJobRequest implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ArtefactJobService artefactJobService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: {}", event);
		List<String> paramList = ExtractApiParameterHttpMethodUtil.extractParameters(event, true);
		String jobId = paramList.stream().findFirst().orElse(null);
		if (jobId != null) {
			log.info("Found path parameter jobId: {}", jobId);
		}
		else {
			log.error("Missing 'jobId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(400, "Missing 'jobId' parameter in path", true);
		}

		if (jobId.isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"'jobId' parameter is null or empty", true);
		}
		ArtefactJob job;
		try {
			job = artefactJobService.getJobStatus(jobId);
		}
		catch (Exception e) {
			log.error("artefactJobService.getJobStatus(jobId) call failed: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"artefactJobService.getJobStatus(jobId) call failed: " + e.getMessage(), true);
		}

		if (job == null) {
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No job found by jobId: " + jobId);
		}

		if (ArtefactStatus.CANCELED.getStatus().equalsIgnoreCase(job.getStatus())) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					"Job is already CANCELED by jobId: " + jobId, false);
		}
		log.info("Found from DB job: {} status: {}", job.getId(), job.getStatus());
		try {
			job.setStatus(ArtefactStatus.CANCELED.getStatus());
			job.setCreationDate(DateUtils.getCurrentDatetimeUtc());
			job.setUpdatedDate(DateUtils.getCurrentDatetimeUtc());
			artefactJobService.saveJob(job);
			log.info("Job is set to status CANCELED by jobId: {}", job.getId());
		}
		catch (Exception e) {
			log.error("artefactJobService.saveJob(job); call failed: {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"artefactJobService.getJobStatus(jobId) call failed: " + e.getMessage(), true);
		}

		return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
				"Job status is set to CANCELED jobId " + jobId, false);
	}

}
