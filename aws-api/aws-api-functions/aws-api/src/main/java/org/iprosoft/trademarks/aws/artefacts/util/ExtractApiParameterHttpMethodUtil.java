package org.iprosoft.trademarks.aws.artefacts.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ExtractApiParameterHttpMethodUtil {

	public static List<String> extractParameters(APIGatewayV2HTTPEvent event, boolean isPathParam) {
		List<String> paramList = new ArrayList<>();
		Map<String, String> parameterMap;

		if (isPathParam) {
			parameterMap = event.getPathParameters();
			if (parameterMap == null) {
				log.error("event.getPathParameters() is null");
				return paramList;
			}
		}
		else {
			parameterMap = event.getQueryStringParameters();
			if (parameterMap == null) {
				log.error("event.getQueryStringParameters() is null");
				return paramList;
			}
		}

		return parameterMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
	}

	public static String extractFirstParameterWithoutName(APIGatewayV2HTTPEvent event, boolean isPathParam) {
		Map<String, String> parameterMap;

		if (isPathParam) {
			parameterMap = event.getPathParameters();
			if (parameterMap == null) {
				log.error("event.getPathParameters() is null");
				return null;
			}
		}
		else {
			parameterMap = event.getQueryStringParameters();
			if (parameterMap == null) {
				log.error("event.getQueryStringParameters() is null");
				return null;
			}
		}
		List<String> paramList = parameterMap.values().stream().filter(StringUtils::isNotBlank).toList();
		return paramList.stream().findFirst().orElse(null);
	}

	public static String extractHttpMethod(APIGatewayV2HTTPEvent event) {
		return Optional.ofNullable(event.getRequestContext()).map(context -> {
			log.info("RequestContext found.");
			return context.getHttp();
		}).map(http -> {
			log.info("Http context found.");
			String method = http.getMethod();
			log.info("Found httpMethod: {}", method);
			return method;
		}).orElseGet(() -> {
			log.error("RequestContext or Http context is null.");
			return null;
		});
	}

}
