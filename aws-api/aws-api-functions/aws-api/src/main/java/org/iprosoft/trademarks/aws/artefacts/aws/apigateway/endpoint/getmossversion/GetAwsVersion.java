package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getAwsversion;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class GetAwsVersion implements Function<String, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(final String event) {
		String apiVersion = SystemEnvironmentVariables.API_VERSION;
		String coreVersion = SystemEnvironmentVariables.CORE_VERSION;
		Map<String, Object> map = new HashMap<>();
		map.put("apiVersion", (apiVersion != null) ? apiVersion : "0.1.0");
		map.put("coreVersion", (coreVersion != null) ? coreVersion : "0.1.0");
		return APIGatewayV2HTTPResponse.builder()
			.withStatusCode(HttpStatusCode.OK)
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(map))
			.withIsBase64Encoded(false)
			.build();
	}

}