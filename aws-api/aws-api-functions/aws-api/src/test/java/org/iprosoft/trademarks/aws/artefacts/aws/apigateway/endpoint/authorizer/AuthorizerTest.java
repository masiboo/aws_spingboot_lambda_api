package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.authorizer;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.SSM })
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuthorizerTest {

	public static final String DENY = "Deny";

	public static final String ALLOW = "Allow";

	private static final String AUTHORIZATION = "authorization";

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareSSMParamStore();
	}

	@Test
	public void testAuthorizerSuccess() throws Exception {
		APIGatewayV2CustomAuthorizerEvent event = new APIGatewayV2CustomAuthorizerEvent();
		String authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.mLPE7jfDT4tGl5TQmqmP5DyBktiBrv-Xpxh6A7fUqLY";
		event.setHeaders(Map.of(AUTHORIZATION, authToken));
		event.setRouteArn("/Aws-api/api/v2/authorizer");
		// Call the Spring Cloud Function endpoint
		ResponseEntity<Map<String, Object>> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/authorizer")).body(event), new ParameterizedTypeReference<>() {
			});
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		assertEffect(ALLOW, result);
	}

	@Test
	public void testAuthorizerWhenInavlidTokenPassed() throws Exception {
		APIGatewayV2CustomAuthorizerEvent event = new APIGatewayV2CustomAuthorizerEvent();
		String authToken = "invalid_token";
		event.setHeaders(Map.of(AUTHORIZATION, authToken));
		event.setRouteArn("/Aws-api/api/v2/authorizer");
		// Call the Spring Cloud Function endpoint
		ResponseEntity<Map<String, Object>> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/authorizer")).body(event), new ParameterizedTypeReference<>() {
			});
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		assertEffect(DENY, result);
	}

	@Test
	public void testAuthorizerWhenAuthorizationNotPassed() throws Exception {
		APIGatewayV2CustomAuthorizerEvent event = new APIGatewayV2CustomAuthorizerEvent();
		event.setHeaders(Map.of());
		// Call the Spring Cloud Function endpoint
		ResponseEntity<Map<String, Object>> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/authorizer")).body(event), new ParameterizedTypeReference<>() {
			});
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		assertEffect(DENY, result);
	}

	private void assertEffect(String effect, ResponseEntity<Map<String, Object>> result) {
		JsonNode rootNode = objectMapper.valueToTree(result.getBody());
		JsonPointer effectPointer = JsonPointer.compile("/policyDocument/Statement/0/Effect");
		JsonNode effectNode = rootNode.at(effectPointer);

		// Should be 200
		Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
		Assertions.assertNotNull(result.getBody());
		// Ensure Effect is not missing and matching with given value
		Assertions.assertFalse(effectNode.isMissingNode());
		Assertions.assertEquals(effect, effectNode.textValue());
	}

}
