package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.websocket;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class WSDefaultMessageHandler implements Function<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

	@Override
	public APIGatewayV2WebSocketResponse apply(APIGatewayV2WebSocketEvent apiGatewayV2WebSocketEvent) {
		// default message handler will be executed when no specific rule/routeKey matches
		// default handlers will just acknowledge the messages for now
		APIGatewayV2WebSocketResponse wsResp = new APIGatewayV2WebSocketResponse();
		wsResp.setBody("Message acknowledged but not processed");
		wsResp.setStatusCode(HttpStatus.OK.value());
		return wsResp;
	}

}
