package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.websocket;

import cloud.localstack.docker.LocalstackDockerExtension;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.helper.FunctionInvokeHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class WSDisconnectionHandlerTest {

	private final ObjectMapper objectMapper;

	@Test
	public void testWSDisconnectionHandler() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "WSDisconnectionHandler");

		// Create API Gateway Websocket event
		APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
		APIGatewayV2WebSocketEvent.RequestContext requestContext = new APIGatewayV2WebSocketEvent.RequestContext();
		requestContext.setConnectionId("cnxId001");
		requestContext.setRouteKey("$disconnect");
		requestContext.setApiId("testApi001");
		event.setHeaders(ApiGatewayResponseUtil.createHeaders());
		event.setRequestContext(requestContext);

		FunctionInvokeHelper invokeHelper = new FunctionInvokeHelper();
		APIGatewayV2WebSocketResponse response = invokeHelper.invoke(event, APIGatewayV2WebSocketResponse.class);

		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
		Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		String respMsg = response.getBody();
		Assertions.assertEquals("Disconnected successfully", respMsg);
	}

}
