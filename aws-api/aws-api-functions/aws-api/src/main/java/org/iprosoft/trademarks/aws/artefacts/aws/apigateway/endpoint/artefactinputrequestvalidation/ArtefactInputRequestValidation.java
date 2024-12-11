package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.artefactinputrequestvalidation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ArtefactInputRequestValidation implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final ArtefactService artefactService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("Event Dump: " + event);
		if (event.getBody() == null || event.getBody().isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}
		ArtefactInput artefact;
		try {
			artefact = objectMapper.readValue(event.getBody(), ArtefactInput.class);
		}
		catch (IOException e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Failed to parse product from request body " + e.getMessage(), true);
		}
		String artefactId = UUID.randomUUID().toString();
		Map<String, String> inputValidation = artefactService.validateArtefact(artefact);
		Map<String, Object> outputmap = new HashMap<>();
		outputmap.put("artefactId", artefactId);
		outputmap.put("validation", inputValidation);
		if (inputValidation.isEmpty()) {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(outputmap), false);
		}
		else {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					objectMapper.writeValueAsString(outputmap), true);
		}
	}

}