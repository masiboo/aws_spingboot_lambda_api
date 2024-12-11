package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.updatebatchlockstate;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class UpdateBatchLockState implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		if (CollectionUtils.isNotEmpty(event.getPathParameters()) && event.getPathParameters().containsKey("batchSeq")
				&& event.getPathParameters().containsKey("lock")) {
			String id = event.getPathParameters().get("batchSeq");
			boolean lock = Boolean.parseBoolean(event.getPathParameters().get("lock"));
			if (StringUtils.hasText(id)) {
				BatchOutput batchOutput = batchService.getBatchDetail(id);
				if (batchOutput == null) {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "The Batch is not found");
				}
				if (batchOutput.isLocked() == lock) {
					String msg = batchOutput.isLocked() ? "LOCKED" : "UNLOCKED";
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatus.CONFLICT.value(),
							"The Batch is already " + msg, true);
				}
				// Lock batch
				batchService.updateLockState(id, lock);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(batchService.getBatchDetail(id)), false);
			}
			else {
				log.warn("'batchSeq' parameter is empty");

				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
						"'batchSeq' parameter is empty", true);
			}
		}
		else {
			log.warn("Missing 'batchSeq' and 'locked' parameters in path");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"Missing 'batchSeq' and 'locked' parameters in path", true);
		}
	}

}
