package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.websocket;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.helper.FunctionInvokeHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.API_GATEWAY })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class WSMessageHandlerTest {

	private WSMessageHandler wsMessageHandler;

	private ApiGatewayManagementApiClient mockApiGatewayManagementApiClient;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() {
		mockApiGatewayManagementApiClient = createMock(ApiGatewayManagementApiClient.class);
		objectMapper = new ObjectMapper();
		wsMessageHandler = new WSMessageHandler(mockApiGatewayManagementApiClient, objectMapper);
	}

	// API action 'PostToConnection' for service 'apigatewaymanagementapi' not yet
	// implemented or pro feature -
	// check https://docs.localstack.cloud/user-guide/aws/feature-coverage
	@Test
	@Disabled
	public void testWSMessageHandler() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "WSMessageHandler");

		// Create API Gateway Websocket event
		APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
		APIGatewayV2WebSocketEvent.RequestContext requestContext = new APIGatewayV2WebSocketEvent.RequestContext();
		requestContext.setConnectionId("cnxId001");
		requestContext.setRouteKey("sendMessage");
		requestContext.setApiId("testApi001");
		event.setHeaders(ApiGatewayResponseUtil.createHeaders());
		event.setRequestContext(requestContext);
		event.setBody("{\"message\" :\"testSendMessage\"}");

		FunctionInvokeHelper invokeHelper = new FunctionInvokeHelper();
		APIGatewayV2WebSocketResponse response = invokeHelper.invoke(event, APIGatewayV2WebSocketResponse.class);

		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
		Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		String respMsg = objectMapper.readTree(response.getBody()).get("body").asText();
		Assertions.assertEquals("Exception while sending message", respMsg);
	}

	@Test
	public void testApplySuccess() throws Exception {
		// Arrange
		APIGatewayV2WebSocketEvent.RequestContext requestContext = new APIGatewayV2WebSocketEvent.RequestContext();
		requestContext.setConnectionId("testConnectionId");
		requestContext.setDomainName("testDomain");
		requestContext.setStage("testStage");

		APIGatewayV2WebSocketEvent wsEvent = new APIGatewayV2WebSocketEvent();
		wsEvent.setBody("{\"message\":\"test\"}");
		wsEvent.setRequestContext(requestContext);

		expect(mockApiGatewayManagementApiClient.postToConnection(anyObject(PostToConnectionRequest.class)))
			.andReturn(null);
		replay(mockApiGatewayManagementApiClient);

		// Act
		APIGatewayV2WebSocketResponse response = wsMessageHandler.apply(wsEvent);

		// Assert
		assertEquals(HttpStatus.OK.value(), response.getStatusCode());
		assertEquals("Message processed successfully", response.getBody());
		verify(mockApiGatewayManagementApiClient);
	}

	@Test
	public void testApplyException() {
		// Arrange
		APIGatewayV2WebSocketEvent.RequestContext requestContext = new APIGatewayV2WebSocketEvent.RequestContext();
		requestContext.setConnectionId("testConnectionId");
		requestContext.setDomainName("testDomain");
		requestContext.setStage("testStage");

		APIGatewayV2WebSocketEvent wsEvent = new APIGatewayV2WebSocketEvent();
		wsEvent.setBody("{\"message\":\"test\"}");
		wsEvent.setRequestContext(requestContext);

		expect(mockApiGatewayManagementApiClient.postToConnection(anyObject(PostToConnectionRequest.class)))
			.andThrow(new RuntimeException("Simulated exception"));
		replay(mockApiGatewayManagementApiClient);

		// Act
		APIGatewayV2WebSocketResponse response = wsMessageHandler.apply(wsEvent);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
		assertEquals("Exception while sending message", response.getBody());
		verify(mockApiGatewayManagementApiClient);
	}

}
