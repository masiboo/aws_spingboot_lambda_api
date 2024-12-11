package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.lists3files;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.configuration.SystemEnvironmentVariables;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class ListS3Files implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		Set<String> objectKeys = s3Service.listObjectKeys(SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET);

		if (CollectionUtils.isEmpty(objectKeys)) {
			return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No S3 files found");
		}
		return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
				objectMapper.writeValueAsString(Map.of("files", objectKeys)), false);
	}

}
