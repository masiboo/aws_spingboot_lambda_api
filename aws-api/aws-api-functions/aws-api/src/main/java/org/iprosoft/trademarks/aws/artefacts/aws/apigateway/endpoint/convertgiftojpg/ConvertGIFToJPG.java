package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.convertgiftojpg;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ConvertImageRequest;
import org.iprosoft.trademarks.aws.artefacts.service.mediaprocessing.MediaProcessingService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ConvertGIFToJPG implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final MediaProcessingService mediaProcessingService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);

		Map<String, String> pathParameters = event.getPathParameters();

		if (pathParameters != null && pathParameters.containsKey("bucket") && pathParameters.containsKey("key")
				&& StringUtils.hasText(pathParameters.get("bucket"))
				&& StringUtils.hasText(pathParameters.get("key"))) {

			ConvertImageRequest request = new ConvertImageRequest();
			request.setKey(pathParameters.get("key"));
			request.setBucket(pathParameters.get("bucket"));
			String signedS3Url = mediaProcessingService.convertGIFToJPG(request);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK, "signedS3Url : " + signedS3Url,
					false);
		}
		else {
			log.warn("Missing 'key' or 'bucket' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Both 'key' and 'bucket' must be present with non-empty values.", true);
		}
	}

}
