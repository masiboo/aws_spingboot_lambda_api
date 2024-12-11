package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactnotesbyfiltercriteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactNotesFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactNotesEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetArtefactNotesByFilterCriteriaTests {

	private ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	private WireMockServer wireMockServer;

	private GetArtefactNotesByFilterCriteria getArtefactNotesByFilterCriteria;

	private DatabaseService databaseService;

	@BeforeEach
	public void setup() throws MalformedURLException {
		// Start the WireMockServer on a random port
		/*
		 * wireMockServer = new WireMockServer(); wireMockServer.start();
		 * WireMock.configureFor("localhost", wireMockServer.port()); databaseService =
		 * createMock(DatabaseService.class); objectMapper = new ObjectMapper();
		 * objectMapper.registerModule(new JavaTimeModule());
		 * objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 * getArtefactNotesByFilterCriteria = new
		 * GetArtefactNotesByFilterCriteria(objectMapper, databaseService);
		 */
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setEmptyArtefact = true;
		TestSetupUtils.commonSetup();
		getArtefactNotesByFilterCriteria = TestSetupUtils.createGetArtefactNotesByFilterCriteria();
	}

	@AfterEach
	public void tearDown() {
		// Stop the WireMockServer after each test
		// wireMockServer.stop();
	}

	@Test
	@Disabled
	public void getArtefactNotesSuccessfully() throws Exception {
		// Mock Aws core service
		List<ArtefactNotesEntity> artefactNotesEntityList = new ArrayList<>();
		ArtefactNotesEntity artefactNote = new ArtefactNotesEntity();
		artefactNote.setId(1L);
		artefactNote.setAuthor("ALOGOTHETIS");
		artefactNote.setMirisDocId("DOC001");
		artefactNote.setContent("This is a note.");
		artefactNote.setCreatedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNote.setModifiedDate(DateUtils.getCurrentDatetimeUtc());
		artefactNotesEntityList.add(artefactNote);
		wireMockServer.stubFor(get(urlMatching("/db-init-access/api/v1/artefact-notes/filter\\?mirisDocId=DOC001"))
			.withQueryParam("mirisDocId", equalTo("DOC001"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(objectMapper.writeValueAsString(artefactNotesEntityList))));

		// Create API Gateway event
		ArtefactNotesFilterCriteria filterCriteria = new ArtefactNotesFilterCriteria();
		filterCriteria.setMirisDocId("DOC001");
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();

		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactnotesbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		// Full response
		log.info(String.valueOf(result.getBody()));
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
		// Should be ALOGOTHETIS
		log.info("Response : " + (Objects.requireNonNull(result.getBody())).getBody());
		List<ArtefactNotesEntity> responseBody = objectMapper.readValue(result.getBody().getBody(),
				new TypeReference<List<ArtefactNotesEntity>>() {
				});
		String author = responseBody.get(0).getAuthor();
		Assertions.assertEquals("ALOGOTHETIS", author);
	}

	@Test
	@Disabled
	public void getArtefactNotesWithoutParam() throws Exception {
		// Mock Aws core service
		wireMockServer.stubFor(get(urlMatching("/db-init-access/api/v1/artefact-notes"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")));

		// Create API Gateway event
		ArtefactNotesFilterCriteria filterCriteria = new ArtefactNotesFilterCriteria();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();

		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactnotesbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 200
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		log.info("Response body: " + (Objects.requireNonNull(result.getBody())).getBody());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals("Invalid request : mirisDocId not provided",
				(Objects.requireNonNull(result.getBody())).getBody());
	}

	@Test
	void testApplySuccess() throws JsonProcessingException {
		// arrange
		ArtefactNotesFilterCriteria artefactNotesFilterCriteria = new ArtefactNotesFilterCriteria();
		String mirisDocId = "123123";
		artefactNotesFilterCriteria.setMirisDocId(mirisDocId);
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withBody(TestSetupUtils.objectMapper.writeValueAsString(artefactNotesFilterCriteria))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactNotesByFilterCriteria.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestData.getArtefactNotesEntityList().get(0).getMirisDocId()));
	}

	@Test
	void testApplyFail() throws JsonProcessingException {
		// arrange
		ArtefactNotesFilterCriteria artefactNotesFilterCriteria = new ArtefactNotesFilterCriteria();
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withBody(TestSetupUtils.objectMapper.writeValueAsString(artefactNotesFilterCriteria))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactNotesByFilterCriteria.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("mirisDocId not provided"));
	}

}
