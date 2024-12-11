package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getalljobs;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactJob;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetAllJobs implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactJobService artefactJobService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		String date = null;
		String status = null;
		if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
			date = event.getQueryStringParameters().get("date");
			status = event.getQueryStringParameters().get("status");
			Map<String, String> validationErrors = validateQueryParams(date, status);
			if (validationErrors != null && !validationErrors.isEmpty()) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						objectMapper.writeValueAsString(validationErrors), true);
			}
		}

		List<ArtefactJob> artefactJobList = artefactJobService.getAllJobs(date, status);

		if (artefactJobList != null && !artefactJobList.isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefactJobList), false);
		}
		else {
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No jobs found");
		}
	}

	private Map<String, String> validateQueryParams(String date, String status) {
		Map<String, String> validationErrors = new HashMap<>();
		if (Strings.isBlank(date) && !DateUtils.isValidDate(date, DateUtils.getSimpleDateFormat())) {
			validationErrors.put("date", "Invalid dateFormat and valid format is yyyy-MM-dd");
		}
		if (Strings.isBlank(status)
				&& !Arrays.stream(ArtefactStatus.values()).anyMatch(val -> val.toString().equalsIgnoreCase(status))) {
			validationErrors.put("status",
					"Invalid status given and allowed types are " + Arrays.toString(ArtefactStatus.values()));
		}
		return validationErrors;
	}

}
