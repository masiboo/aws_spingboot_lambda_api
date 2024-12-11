package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.createartefactnote;

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
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CreateArtefactNoteTests {

	private ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private WireMockServer wireMockServer;

	private CreateArtefactNote createArtefactNote;

	@BeforeEach
	public void setup() {
		// Start the WireMockServer on a random port
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@AfterEach
	public void tearDown() {
		// Stop the WireMockServer after each test
		wireMockServer.stop();
	}

	@Test
	@Disabled
	public void createArtefactNoteSuccessfully() throws Exception {
		ArtefactNotesEntity artefactNote = new ArtefactNotesEntity();
		artefactNote.setAuthor("ALOGOTHETIS");
		artefactNote.setMirisDocId("DOC001");
		artefactNote.setContent("This is a note.");
		artefactNote.setCreatedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNote.setModifiedDate(DateUtils.getCurrentDatetimeUtc());

		ArtefactNotesEntity createdArtefactNote = new ArtefactNotesEntity();
		createdArtefactNote.setId(1L);
		createdArtefactNote.setAuthor("ALOGOTHETIS");
		createdArtefactNote.setMirisDocId("DOC001");
		createdArtefactNote.setContent("This is a note.");
		createdArtefactNote.setCreatedDate(artefactNote.getCreatedDate());
		createdArtefactNote.setModifiedDate(artefactNote.getModifiedDate());

		wireMockServer.stubFor(post(urlMatching("/db-init-access/api/v1/artefact-notes"))
			.withRequestBody(containing(objectMapper.writeValueAsString(artefactNote)))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(objectMapper.writeValueAsString(createdArtefactNote))));

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefactNote))
			.build();

		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate
			.exchange(RequestEntity.post(new URI("/createartefactnote")).body(event), APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	void testApplySuccess() throws JsonProcessingException, MalformedURLException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		createArtefactNote = TestSetupUtils.createCreateArtefactNote();
		ArtefactNotesEntity artefactNote = TestData.getArtefactNotesEntity();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefactNote))
			.build();
		// act
		APIGatewayV2HTTPResponse res = createArtefactNote.apply(event);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_OK.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains(artefactNote.getAuthor()));
	}

	@Test
	void testApplyFailed() throws MalformedURLException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		createArtefactNote = TestSetupUtils.createCreateArtefactNote();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody("")
			.build();
		// act
		APIGatewayV2HTTPResponse res = createArtefactNote.apply(event);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_BAD_REQUEST.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains("Empty request body"));
	}

}
