package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactnotesbyfiltercriteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactNotesFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactNotesByFilterCriteria implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final DatabaseService databaseService;

	@Override
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		List<ArtefactNotesEntity> artefactNotes;
		try {
			ArtefactNotesFilterCriteria filterCriteria = objectMapper.readValue(event.getBody(),
					ArtefactNotesFilterCriteria.class);
			if (!StringUtils.hasText(filterCriteria.getMirisDocId())) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"Invalid request : mirisDocId not provided", true);
			}

			artefactNotes = databaseService.filterArtefactNotes(filterCriteria.getMirisDocId());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefactNotes), false);
		}
		catch (Exception e) {
			log.error("Exception while filtering the ArtefactNotes ", e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception while filtering the ArtefactNotes " + e.getMessage(), true);
		}
	}

}
