package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.deleteBatchByBatchSequence;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.iprosoft.trademarks.aws.artefacts.util.ExtractApiParameterHttpMethodUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class DeleteArtefactByBatchSequence implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
		log.info("APIGatewayV2HTTPEvent: {}", event);
		String batchSeq = ExtractApiParameterHttpMethodUtil.extractFirstParameterWithoutName(event, true);
		if (StringUtils.hasText(batchSeq)) {
			try {
				BatchOutput batchOutput = batchService.getBatchDetail(batchSeq);
				if (batchOutput == null) {
					return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "The Batch is not found");
				}
				if (ArtefactStatus.DELETED.getStatus().equalsIgnoreCase(batchOutput.getStatus())) {
					return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
							"Batch is already in DELETED", true);
				}
				// Delete batch
				batchService.updateBatchWithStatus(batchSeq, BatchStatus.DELETED.toString());
				log.info("Batch with batchSequence {} deleted.", batchSeq);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(batchService.getBatchDetail(batchSeq)), false);
			}
			catch (Exception e) {
				log.error("Internal server error occurred {}", e.getMessage());
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
						"Internal server error occurred" + e.getMessage(), true);
			}
		}
		else {
			log.warn("'batchSeq' parameter is empty");
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.BAD_REQUEST,
					"'batchSeq' parameter is empty", true);
		}
	}

}
