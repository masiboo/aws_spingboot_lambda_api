package org.wipo.trademarks.Aws.artefacts.aws.apigateway.endpoint.validatemirisdocid;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.wipo.trademarks.Aws.artefacts.TestSetupUtils;
import org.wipo.trademarks.Aws.artefacts.util.ApiGatewayResponseUtil;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
class ValidateMirisDocIdTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private ValidateMirisDocId validateMirisDocId;

	@Test
	@Disabled
	public void ValidateMirisDocIdTrue() throws Exception {
		// Start WireMockServer and configure stub
		WireMockServer wireMockServer = new WireMockServer();
		wireMockServer.start();
		stubFor(get(urlEqualTo("/api/v1/validate/documents/12233"))
			.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("true")));

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("mirisDocId", "12233");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/validatemirisdocid")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, result.getBody().getStatusCode());
		// Contains "isValid": "true"
		Assertions.assertTrue(result.getBody().getBody().contains("true"));
		wireMockServer.stop();
	}

	@Test
	@Disabled
	public void ValidateMirisDocIdFalse() throws Exception {
		// Start WireMockServer and configure stub
		WireMockServer wireMockServer = new WireMockServer();
		wireMockServer.start();
		stubFor(get(urlEqualTo("/api/v1/validate/documents/12232"))
			.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("false")));

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("mirisDocId", "12232");
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withPathParameters(pathParameters)
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/validatemirisdocid")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, result.getBody().getStatusCode());
		// Contains "isValid": "true"
		Assertions.assertTrue(result.getBody().getBody().contains("false"));
		wireMockServer.stop();
	}

	@Test
	void testApplySuccess() throws MalformedURLException {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("mirisDocId", "12345678");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withPathParameters(pathParameters).build();
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isDocIdValid = true;
		TestSetupUtils.commonSetup();
		validateMirisDocId = TestSetupUtils.createValidateMirisDocId();
		// act
		APIGatewayV2HTTPResponse response = validateMirisDocId.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("true"));
	}

	@Test
	void testApplyMirisDocIdFalse() throws MalformedURLException {
		// arrange
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("mirisDocId", "1234");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder().withPathParameters(pathParameters).build();
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isDocIdValid = false;
		TestSetupUtils.commonSetup();
		validateMirisDocId = TestSetupUtils.createValidateMirisDocId();
		// act
		APIGatewayV2HTTPResponse response = validateMirisDocId.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("false"));
	}

}
