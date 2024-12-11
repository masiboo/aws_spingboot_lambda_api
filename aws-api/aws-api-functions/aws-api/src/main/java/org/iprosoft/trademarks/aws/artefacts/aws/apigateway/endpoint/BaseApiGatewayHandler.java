package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.util.GsonUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class BaseApiGatewayHandler {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	protected APIGatewayV2HTTPEvent processEventWithJackson(InputStream input) {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			// Convert byte[] to APIGatewayV2HTTPEvent
			APIGatewayV2HTTPEvent event = objectMapper.readValue(input, APIGatewayV2HTTPEvent.class);
			log.info("Event Dump: " + event);

			if (event.getBody() == null || event.getBody().isEmpty()) {
				log.error("Event body is missing");
				return null; // Indicate an error
			}
			return event;
		}
		catch (Exception e) {
			log.error("Error processing request", e);
			return null; // Indicate an error
		}
	}

	protected APIGatewayV2HTTPEvent processEventWithGson(InputStream in) {
		return GsonUtil.getInstance()
			.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), APIGatewayV2HTTPEvent.class);
	}

}
