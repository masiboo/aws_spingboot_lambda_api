package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.validatemirisdocid;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.service.miris.MirisService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ValidateMirisDocId implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final MirisService mirisService;

	@Override
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("mirisDocId")) {
			String mirisDocId = event.getPathParameters().get("mirisDocId");
			if (StringUtils.hasText(mirisDocId)) {
				boolean isValid = mirisService.isDocIdValid(mirisDocId);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						"MirisDocId: " + mirisDocId + " is valid " + isValid, false);
			}
			else {
				log.warn("'mirisDocId' parameter in path is empty");
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'mirisDocId' parameter in path is empty", true);
			}
		}
		else {
			log.warn("Missing 'mirisDocId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'mirisDocId' parameter in path", true);
		}
	}

}
