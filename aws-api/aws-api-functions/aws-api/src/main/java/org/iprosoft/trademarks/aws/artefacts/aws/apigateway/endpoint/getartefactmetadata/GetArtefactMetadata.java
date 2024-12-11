package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactmetadata;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactMetadata;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactMetadata implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("artefactId")) {
			String artefactId = event.getPathParameters().get("artefactId");
			ArtefactMetadata artefactInfo = this.artefactService.getArtefactInfoById(artefactId);
			if (artefactInfo == null) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No artefact/metadata found");
			}
			else {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(artefactInfo), false);
			}
		}
		else {
			log.warn("Missing 'artefactId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'artefactId' parameter in path", true);
		}
	}

}
