package org.iprosoft.trademarks.aws.artefacts.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeTypeUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ErrorResponse;
import software.amazon.awssdk.http.Header;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ApiGatewayResponseUtil {

	public static APIGatewayV2HTTPResponse getAPIGatewayV2HTTPResponse(int statusCode, String body, boolean isError) {
		return APIGatewayV2HTTPResponse.builder()
			.withStatusCode(statusCode)
			.withHeaders(createHeaders())
			.withBody(body)
			.build();
	}

	public static Map<String, String> createHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type,X-Amz-Date,Authorization,X-Api-Key");
		headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
		headers.put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.put(Header.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
		return headers;
	}

	public static APIGatewayV2HTTPResponse notFoundResponse(int statusCode, String body) {
		ObjectMapper objectMapper = new ObjectMapper();
		ErrorResponse errorResponse = ErrorResponse.builder().code("404").message(body).build();
		Map<String, Object> errorResponseMap = new HashMap<>();
		errorResponseMap.put("error", errorResponse);

		try {
			return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(createHeaders())
				.withBody(objectMapper.writeValueAsString(errorResponseMap))
				.build();
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
