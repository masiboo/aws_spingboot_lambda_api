package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.websocket;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import java.net.URI;
import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class WSMessageHandler implements Function<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

	private ApiGatewayManagementApiClient apiGatewayManagementApiClient;

	private final ObjectMapper objectMapper;

	@Override
	public APIGatewayV2WebSocketResponse apply(APIGatewayV2WebSocketEvent wsEvent) {
		log.info("Connection request :{}", wsEvent.getBody());
		log.info("Connection Id:{}", wsEvent.getRequestContext().getConnectionId());
		log.info("Connection routeKey:{}", wsEvent.getRequestContext().getRouteKey());

		String connectId = wsEvent.getRequestContext().getConnectionId();
		String domainName = wsEvent.getRequestContext().getDomainName();
		String stage = wsEvent.getRequestContext().getStage();

		try {
			String apgEndpoint = "https://" + domainName + "/" + stage;

			if (apiGatewayManagementApiClient == null) {
				apiGatewayManagementApiClient = ApiGatewayManagementApiClient.builder()
					.region(Region.EU_CENTRAL_1)
					.endpointOverride(new URI(apgEndpoint))
					.build();
			}

			String message = objectMapper.readTree(wsEvent.getBody()).get("message").asText();

			PostToConnectionRequest postToConnReq = PostToConnectionRequest.builder()
				.connectionId(connectId)
				.data(SdkBytes.fromUtf8String(message))
				.build();
			apiGatewayManagementApiClient.postToConnection(postToConnReq);

			APIGatewayV2WebSocketResponse wsResp = new APIGatewayV2WebSocketResponse();
			wsResp.setBody("Message processed successfully");
			wsResp.setStatusCode(HttpStatus.OK.value());
			return wsResp;
		}
		catch (Exception e) {
			log.error("Exception while posting message ", e);
			APIGatewayV2WebSocketResponse wsResp = new APIGatewayV2WebSocketResponse();
			wsResp.setBody("Exception while sending message");
			wsResp.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return wsResp;
		}

	}

}
