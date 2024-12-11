package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getallbatches;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import org.iprosoft.trademarks.aws.artefacts.service.batch.BatchService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.BatchStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DebugInfoUtil;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class GetAllBatches implements Function<InputStream, APIGatewayV2HTTPResponse> {

	private final ObjectMapper objectMapper;

	private final BatchService batchService;

	@Override
	@SneakyThrows
	public APIGatewayV2HTTPResponse apply(InputStream in) {
		log.info("InputStream Dump: {}", in);

		APIGatewayV2HTTPEvent event = GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);

		log.info("APIGatewayV2HTTPEvent: {}", event);
		try {
			// Only retrieve those un-indexed but inserted batches
			List<BatchOutput> batches = batchService.getAllBatchByStatus(BatchStatus.INSERTED.getStatus());
			if (!batches.isEmpty()) {
				log.info("Found batches with status INSERTED {}", batches);
				return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.OK,
						objectMapper.writeValueAsString(batches), false);
			}
			else {
				return ApiGatewayResponseUtil.notFoundResponse(HttpStatusCode.CREATED, "No batches found");
			}
		}
		catch (Exception e) {
			log.error("Unexpected exception occurred {}", e.getMessage());
			DebugInfoUtil.logExceptionDetails(e);
			return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(HttpStatusCode.INTERNAL_SERVER_ERROR,
					"Unexpected exception occurred " + e.getMessage(), false);
		}
	}

}
