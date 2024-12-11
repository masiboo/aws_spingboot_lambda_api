package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getmosshealth;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class GetAwsHealth implements Function<String, APIGatewayV2HTTPResponse> {

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(final String event) {
		return APIGatewayV2HTTPResponse.builder()
			.withStatusCode(HttpStatusCode.OK)
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody("{\"status\":\"" + HttpStatus.OK.getReasonPhrase() + "\"}")
			.withIsBase64Encoded(false)
			.build();
	}

}
