package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbymirisdocid;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactStatusEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactsByMirisDocId implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final DatabaseService databaseService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getQueryStringParameters())
				&& event.getQueryStringParameters().containsKey("mirisDocId")) {
			String mirisDocId = event.getQueryStringParameters().get("mirisDocId");
			// Artefact Status should be INDEXED to be returned by the method
			List<ArtefactsEntity> artefacts = databaseService.filterArtefacts(ArtefactStatusEnum.INDEXED, mirisDocId);
			if (artefacts.isEmpty()) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"No artefact found with mirisDocId: " + mirisDocId);
			}
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(artefacts), false);
		}
		else {
			log.warn("Missing 'mirisDocId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'mirisDocId' parameter in path", true);
		}
	}

}