package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getmossversion;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetAwsVersionTests {

	private final TestRestTemplate rest;

	private org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getAwsversion.GetAwsVersion getAwsVersion;

	private ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		getAwsVersion = new org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getAwsversion.GetAwsVersion(objectMapper);
	}

	@Test
	@Disabled
	public void test() throws Exception {
		ResponseEntity<APIGatewayV2HTTPResponse> result = this.rest
			.exchange(RequestEntity.get(new URI("/version")).build(), APIGatewayV2HTTPResponse.class);
		log.info("Response body: " + Objects.requireNonNull(result.getBody()));
		// Status Should be 200
		log.info("Response HTTP Status Code: " + Objects.requireNonNull(result.getBody()).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, result.getStatusCode().value());
	}

	@Test
	void testApplySuccess() {
		// act
		APIGatewayV2HTTPResponse response = getAwsVersion.apply("test-event");

		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("{\"apiVersion\":\"1.0\",\"coreVersion\":\"1.0\"}"));
	}

}
