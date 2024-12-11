package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbyfiltercriteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactsByFilterCriteria implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	@Override
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {

		List<Artefact> artefactList;
		try {
			ArtefactFilterCriteria filterCriteria = objectMapper.readValue(event.getBody(),
					ArtefactFilterCriteria.class);
			if (!StringUtils.hasText(filterCriteria.getMirisDocId())
					&& !StringUtils.hasText(filterCriteria.getInsertedDate())
					&& !StringUtils.hasText(filterCriteria.getMirisDocId())
					&& !StringUtils.hasText(filterCriteria.getDateFrom())
					&& !StringUtils.hasText(filterCriteria.getBatchStatus())
					&& !StringUtils.hasText(filterCriteria.getReportDate())
					&& !StringUtils.hasText(filterCriteria.getDateTo())) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"Invalid request : Filter criteria not provided ", true);
			}

			artefactList = artefactService.getArtefactByFilterCritera(filterCriteria);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefactList), false);
		}
		catch (Exception e) {
			log.error("Exception while filtering the Artefacts ", e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception while filtering the Artefacts " + e.getMessage(), true);
		}
	}

}
