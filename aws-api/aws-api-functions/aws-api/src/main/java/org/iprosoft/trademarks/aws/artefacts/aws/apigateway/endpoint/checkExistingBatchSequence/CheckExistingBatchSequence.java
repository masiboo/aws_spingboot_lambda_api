package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.checkExistingBatchSequence;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiMapResponse;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiRequestHandlerResponse;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactUploadRequestUtil;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import org.iprosoft.trademarks.aws.artefacts.util.JsonConverterUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.iprosoft.trademarks.aws.artefacts.util.JsonConverterUtil.getStringFromObject;

@AllArgsConstructor
@Slf4j
public class CheckExistingBatchSequence implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private static final int SIZE_LIMIT = 10000;

	private final BatchService batchService;

	@Override
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: {}", in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("Event Dump: {}", event);

		if (event.getBody() == null || event.getBody().isEmpty()) {
			log.error("Event body is missing");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST, "Empty request body",
					true);
		}
		try {
			if (event.getBody() == null || event.getBody().isEmpty()) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Empty request body");
				ApiRequestHandlerResponse response = new ApiRequestHandlerResponse(SC_BAD_REQUEST,
						new ApiMapResponse(map));
				return ArtefactUploadRequestUtil.buildResponse(response.getStatus(), response.getHeaders(),
						response.getResponse());
			}

			log.info("STEP 1 Loading body: ");
			String requestBody = event.getBody();
			log.info("Loaded body complete String: {}", requestBody);
			List<String> batchSequences = JsonConverterUtil.getObjectListFromJson(requestBody, String.class);
			log.info("Converted batchSequences {}", batchSequences);
			if (batchSequences.size() > SIZE_LIMIT) {
				Map<String, Object> map = new HashMap<>();
				map.put("message", "Too many records. Maximum permitted: " + SIZE_LIMIT);
				return ArtefactUploadRequestUtil.createErrorResponse(map);
			}
			log.info("STEP 2 check existing batch");
			Set<String> existingBatchSequence = ArtefactUploadRequestUtil.getExistedBatchSequence(batchService,
					batchSequences);
			log.info("Found existing BatchSequence: {}", existingBatchSequence);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
					getStringFromObject(existingBatchSequence), false);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpResponseConstant.STATUS_CODE_SERVER_ERROR,
					e.getMessage(), true);
		}
	}

}
