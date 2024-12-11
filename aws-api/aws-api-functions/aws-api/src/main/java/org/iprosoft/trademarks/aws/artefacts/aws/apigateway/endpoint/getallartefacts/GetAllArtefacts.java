package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getallartefacts;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.MiscUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetAllArtefacts implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		String date = null;
		String status = null;
		String fromDate = null;
		String untilDate = null;
		if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
			date = event.getQueryStringParameters().get("date");
			status = event.getQueryStringParameters().get("status");
			fromDate = event.getQueryStringParameters().get("fromDate");
			untilDate = event.getQueryStringParameters().get("untilDate");
			log.info("date: {} status: {} fromDate: {} untilDate {}", date, status, fromDate, untilDate);
			Map<String, String> validationErrors = MiscUtils.validateQueryParams(date, status, fromDate, untilDate);
			if (!validationErrors.isEmpty()) {
				log.error("Error: {}", objectMapper.writeValueAsString(validationErrors));
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						objectMapper.writeValueAsString(validationErrors), true);
			}
		}
		List<Artefact> artefacts = new ArrayList<>();
		if (StringUtils.hasText(fromDate) && StringUtils.hasText(untilDate) && StringUtils.hasText(status)) {
			log.info("GetAllArtefacts by duration and status");
			artefacts = artefactService.getAllArtefactsByInterval(fromDate, untilDate, status);
		}
		else if (StringUtils.hasText(date) && StringUtils.hasText(status)) {
			log.info("GetAllArtefacts by specific date and status");
			artefacts = artefactService.getAllArtefacts(date, status);
		}
		if (!artefacts.isEmpty()) {
			log.info("Found artefacts {}", artefacts);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefacts), false);
		}
		else {
			log.warn("No artefact found");
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No artefacts found");
		}
	}

}