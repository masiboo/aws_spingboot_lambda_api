package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.websocket;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class WSDisconnectionHandler implements Function<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

	@Override
	public APIGatewayV2WebSocketResponse apply(APIGatewayV2WebSocketEvent wsEvent) {
		log.info("Connection request :{}", wsEvent.getBody());
		log.info("Connection Id:{}", wsEvent.getRequestContext().getConnectionId());
		log.info("Connection routeKey:{}", wsEvent.getRequestContext().getRouteKey());
		// TODO Have the logic to remove the connection details from the Dynamo/RDS
		APIGatewayV2WebSocketResponse wsResp = new APIGatewayV2WebSocketResponse();
		wsResp.setBody("Disconnected successfully");
		wsResp.setStatusCode(HttpStatus.OK.value());
		return wsResp;
	}

}
