package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createartefactnote;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class CreateArtefactNote implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final DatabaseService databaseService;

	@Override
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}

		try {
			String eventBody = event.getBody();
			ArtefactNotesEntity artefactNote = objectMapper.readValue(eventBody, ArtefactNotesEntity.class);

			ArtefactNotesEntity createdArtefactNote = databaseService.createArtefactNote(artefactNote);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(createdArtefactNote), false);
		}
		catch (IOException e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Failed to parse artefact note " + e.getMessage(), true);
		}
	}

}
