package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.sendemail;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.model.dto.EmailDetails;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SendEmailTests {

	private ObjectMapper objectMapper;

	private TestRestTemplate testRestTemplate;

	private WireMockServer wireMockServer;

	private SendEmail sendEmail;

	private RestTemplate restTemplate;

	@BeforeEach
	public void setup() {
		// Start the WireMockServer on a random port
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		objectMapper = createNiceMock(ObjectMapper.class);
		restTemplate = createNiceMock(RestTemplate.class);
		sendEmail = new SendEmail(objectMapper, restTemplate);
	}

	@AfterEach
	public void tearDown() {
		// Stop the WireMockServer after each test
		wireMockServer.stop();
	}

	@Test
	@Disabled
	public void sendEmailSuccess() throws URISyntaxException, IOException {
		// Create email payload
		EmailDetails emailDetails = new EmailDetails();
		emailDetails.setFrom("antonios.logothetis@sword-group.com");
		emailDetails
			.setTo(Arrays.asList("antonios.logothetis2@sword-group.com", "antonios.logothetis3@sword-group.com"));
		emailDetails
			.setCc(Arrays.asList("antonios.logothetis2@sword-group.com", "antonios.logothetis3@sword-group.com"));
		emailDetails.setSubject("test email");
		emailDetails.setBody("This is a wonderful email body.");
		EmailDetails.Attachment attachment = new EmailDetails.Attachment();
		attachment.setFilename("advert.pdf");
		attachment.setContentType("application/pdf");
		attachment.setBase64Content(Files.readString(ResourceUtils.getFile("classpath:files/base64.txt").toPath()));
		emailDetails.setAttachment(attachment);

		wireMockServer.stubFor(post(urlEqualTo("/email-svc/api/v1/email"))
			.withRequestBody(containing(objectMapper.writeValueAsString(emailDetails)))
			.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value())));

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(emailDetails))
			.build();
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/sendemail")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
		Assertions.assertEquals(HttpStatus.OK.value(), Objects.requireNonNull(result.getBody()).getStatusCode());
	}

	@Test
	void testApplyValidRequest() throws Exception {
		// arrange
		EmailDetails emailDetails = TestData.getEmailDetails();
		String emailDetailsJson = "valid email";
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setBody(emailDetailsJson);
		expect(objectMapper.readValue(emailDetailsJson, EmailDetails.class)).andReturn(emailDetails);
		replay(restTemplate);

		// act
		APIGatewayV2HTTPResponse response = sendEmail.apply(event);

		// asset
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertEquals("Email sent successfully.", response.getBody());
	}

	@Test
	void testApplyEmptyBody() {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setBody("");

		APIGatewayV2HTTPResponse response = sendEmail.apply(event);

		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Empty request body"));
	}

}
