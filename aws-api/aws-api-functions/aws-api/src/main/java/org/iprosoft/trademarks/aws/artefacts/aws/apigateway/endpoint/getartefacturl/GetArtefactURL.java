package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefacturl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetArtefactURL implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final S3Service s3Service;

	private final ArtefactService artefactService;

	@Override
	// @SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: " + event);
		String artefactId = "";
		if (CollectionUtils.isNotEmpty(event.getPathParameters())
				&& event.getPathParameters().containsKey("artefactId")) {

			artefactId = event.getPathParameters().get("artefactId");

			if (!StringUtils.hasText(artefactId)) {
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'artefactId' parameter is empty", true);
			}
		}
		else {
			log.warn("Missing 'artefactId' parameter in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'artefactId' parameter in path", true);
		}
		Artefact artefact;
		try {
			artefact = artefactService.getArtefactById(artefactId);
			if (artefact == null) {
				log.warn("artefact not found by artefactId {}", artefactId);
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"artefact not found by artefactId: " + artefactId);
			}
		}
		catch (Exception e) {
			log.error("Exception when artefactService.getArtefactById(artefactId) {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception when artefactService.getArtefactById(artefactId) {}" + e.getMessage(), true);
		}
		String signedS3Url;
		try {
			if (!s3Service.isObjectExist(artefact.getS3Bucket(), artefact.getS3Key())) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED,
						"S3 object does not exist in bucket");
			}
			if (!s3Service.bucketExists(artefact.getS3Bucket())) {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "S3 bucket does not exist");
			}
			signedS3Url = s3Service
				.presignGetUrl(artefact.getS3Bucket(), artefact.getS3Key(), Duration.ofHours(1), null)
				.toString();
		}
		catch (Exception e) {
			log.error("Exception when  s3Service.presignGetUrl {}", e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Exception when  s3Service.presignGetUrl: " + e.getMessage(), true);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("artefactId", artefact.getId());
		map.put("signedS3Url", signedS3Url);

		try {
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					objectMapper.writeValueAsString(map), false);
		}
		catch (JsonProcessingException e) {
			log.error("Exception in objectMapper.writeValueAsString when create response {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

}