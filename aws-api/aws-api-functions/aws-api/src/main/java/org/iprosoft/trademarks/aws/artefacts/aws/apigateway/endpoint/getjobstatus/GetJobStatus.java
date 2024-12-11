package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getjobstatus;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ExtractApiParameterHttpMethodUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetJobStatus implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactJobService artefactJobService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: {}", event);

		String jobId = ExtractApiParameterHttpMethodUtil.extractFirstParameterWithoutName(event, true);
		log.info("Found from PathParameters jobId {}", jobId);

		if (StringUtils.hasText(jobId)) {
			ArtefactJob job = null;
			try {
				job = artefactJobService.getJobStatus(jobId);
			}
			catch (Exception e) {
				log.error("Exception in artefactJobService.getJobStatus(jobId): {}", e.getMessage());
			}
			log.info("Found job {} by jobId {}", job, jobId);
			if (job != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("jobStatus", job.getStatus());
				map.put("id", job.getId());
				map.put("artefactId", job.getArtefactId());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(map), false);
			}
			else {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"No job found by jobId " + jobId);
			}
		}
		else {
			log.warn("Missing 'jobId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'jobId' parameter in path", true);
		}
	}

}
