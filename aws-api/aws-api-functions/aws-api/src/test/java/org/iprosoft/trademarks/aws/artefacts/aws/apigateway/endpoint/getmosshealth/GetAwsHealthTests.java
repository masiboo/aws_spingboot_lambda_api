package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getmosshealth;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getmosshealth.GetAwsHealth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.dto.ResponseStatusDTO;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.URI;
import java.util.Objects;

import static org.easymock.EasyMock.createMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetAwsHealthTests {

	private final TestRestTemplate rest;

	private ObjectMapper objectMapper;

	private GetAwsHealth getAwsHealth;

	@BeforeEach
	void init() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		getAwsHealth = new GetAwsHealth();
	}

	@Test
	@Disabled
	public void test() throws Exception {
		ResponseEntity<APIGatewayV2HTTPResponse> result = this.rest
			.exchange(RequestEntity.get(new URI("/health")).build(), APIGatewayV2HTTPResponse.class);
		// Status Should be 200
		log.info("Response HTTP Status Code: " + Objects.requireNonNull(result.getStatusCode()));
		Assertions.assertEquals(HttpStatusCode.OK, result.getStatusCode().value());
		// Status should be OK
		log.info("Response body: " + Objects.requireNonNull(result.getBody()));
		log.info(result.getBody().getBody());
		ResponseStatusDTO responseStatusDTO = objectMapper.readValue(result.getBody().getBody(),
				ResponseStatusDTO.class);
		Assertions.assertEquals("OK", responseStatusDTO.getStatus());
	}

	@Test
	void testApplySuccess() {
		// act
		APIGatewayV2HTTPResponse response = getAwsHealth.apply("test-event");

		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("{\"status\":\"OK\"}"));
	}

}
