package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.deleteartefact;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class DeleteArtefact implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("artefactId")) {
			String artefactId = event.getPathParameters().get("artefactId");
			if (StringUtils.hasText(artefactId)) {
				Artefact Artefact = artefactService.getArtefactById(artefactId);
				if (Artefact == null) {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "The Artefact is not found");
				}
				// Delete artefact
				artefactService.softDeleteArtefactById(artefactId);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						"Artefact deleted successfully mirisDocId: " + artefactId, false);
			}
			else {
				log.warn("'artefactId' parameter is empty");
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"'artefactId' parameter is empty");

			}
		}
		else {
			log.warn("Missing 'artefactId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'artefactId' parameter in path", true);
		}
	}

}
