package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.reportjobstatus;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ReportJobStatus implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactJobService artefactJobService;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("requestId")) {
			String id = event.getPathParameters().get("requestId");
			if (StringUtils.hasText(id)) {
				Map<String, Object> jobStatusResponseMap = artefactJobService.getAllJobStatusByRequestId(id);
				if (CollectionUtils.isNullOrEmpty(jobStatusResponseMap)) {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
							"No jobs found for this requestId: " + id);
				}
				// batchStatus will become INSERTED/INDEXED based on the RequestType once
				// all the
				// jobs are UPLOADED :
				// MPD-419 , MPD-661
				// if (ArtefactStatus.UPLOADED.getStatus()
				// .equalsIgnoreCase(jobStatusResponseMap.get("batchStatus").toString()))
				// {
				// String batchSequence =
				// jobStatusResponseMap.get("batchSequence").toString();
				// String statusByRequestType =
				// batchService.findStatusByRequestType(batchSequence);
				// batchService.updateStatus(batchSequence, statusByRequestType);
				// jobStatusResponseMap.put("batchStatus", statusByRequestType);
				// }
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(jobStatusResponseMap), false);
			}
		}
		else {
			log.warn("'requestId' parameter in path is empty");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"'requestId' parameter in path is empty", true);
		}
		log.warn("Missing 'requestId' parameter in path");
		return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
				"Missing 'requestId' parameter in path", true);
	}

}
