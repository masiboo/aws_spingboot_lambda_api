package org.iprosoft.trademarks.aws.artefacts.aws.sqs;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.aws.sqs.SQSDBEventHandler;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactsDTO;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventArtefact;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DDBEventDetail;
import org.iprosoft.trademarks.aws.artefacts.model.dto.DynamoDBEventDTO;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactClassEnum;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.ArtefactsEntity;
import org.iprosoft.trademarks.aws.artefacts.service.database.DatabaseService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.SQS, ServiceName.DYNAMO })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class SQSDBEventHandlerTest {

	private ObjectMapper objectMapper;

	private static WireMockServer wireMockServer;

	private SQSDBEventHandler sqsdbEventHandler;

	private ObjectMapper mockObjectMapper;

	private DatabaseService mockDatabaseService;

	@BeforeAll
	static void setUp() {
		// Start the WireMockServer on a random port
		/*
		 * wireMockServer = new WireMockServer(); wireMockServer.start();
		 * WireMock.configureFor("localhost", wireMockServer.port());
		 */
	}

	@BeforeEach
	void init() {
		mockObjectMapper = createMock(ObjectMapper.class);
		mockDatabaseService = createMock(DatabaseService.class);
		sqsdbEventHandler = new SQSDBEventHandler(mockObjectMapper, mockDatabaseService);
	}

	@AfterAll
	static void tearDown() {
		// Stop the WireMockServer after each test
		// wireMockServer.stop();
	}

	@Test
	@Disabled
	public void testDBEvent() throws Exception {

		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:sqs-event/SQS_DBEvent.json").toPath());
		JsonNode artefactNode = objectMapper
			.readTree(objectMapper.readTree(jsonEventPayload).get("Records").get(0).get("body").asText())
			.get("detail")
			.get("artefact");

		ArtefactsEntity artefact = objectMapper.treeToValue(artefactNode, ArtefactsEntity.class);
		artefact.setArtefactClass(ArtefactClassEnum.DOCUMENT);

		// stub creation
		wireMockServer.stubFor(post(urlMatching("/db-init-access/api/v1/artefacts"))
			.withHeader("Content-Type", matching("application/json"))
			.withRequestBody(containing(objectMapper.writeValueAsString(artefact)))
			.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(artefactNode.toString())));

		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSDBEventHandler");

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);

	}

	@Test
	public void testApplySuccess() throws Exception {
		// Arrange
		SQSEvent sqsEvent = new SQSEvent();
		SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
		message.setBody("{\"detail\": {\"artefact\": {\"id\": \"testId\", \"status\": \"INDEXED\"}}}");
		sqsEvent.setRecords(List.of(message));

		DynamoDBEventDTO mockEventDTO = new DynamoDBEventDTO();
		DDBEventDetail mockEventDetail = new DDBEventDetail();
		DDBEventArtefact mockEventArtefact = new DDBEventArtefact();
		mockEventArtefact.setStatus("INDEXED");
		mockEventDetail.setArtefact(mockEventArtefact);
		mockEventDTO.setDetail(mockEventDetail);

		expect(mockObjectMapper.readValue(message.getBody(), DynamoDBEventDTO.class)).andReturn(mockEventDTO);
		expect(mockDatabaseService.createArtefact(anyObject(ArtefactsDTO.class))).andReturn(TestData.getArtefactsDTO());
		expectLastCall().once();
		replay(mockObjectMapper, mockDatabaseService);

		// Act
		String result = sqsdbEventHandler.apply(sqsEvent);

		// Assert
		assertEquals("", result);
		verify(mockObjectMapper, mockDatabaseService);
	}

	@Test
	public void testApplyJsonProcessingException() throws Exception {
		// Arrange
		SQSEvent sqsEvent = new SQSEvent();
		SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
		message.setBody("{\"detail\": {\"artefact\": {\"id\": \"testId\", \"status\": \"INDEXED\"}}}");
		sqsEvent.setRecords(List.of(message));

		expect(mockObjectMapper.readValue(message.getBody(), DynamoDBEventDTO.class))
			.andThrow(new JsonProcessingException("error") {
			});
		replay(mockObjectMapper, mockDatabaseService);

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class, () -> sqsdbEventHandler.apply(sqsEvent));
		assertEquals("Artefact Creation Failed", exception.getMessage());
		verify(mockObjectMapper, mockDatabaseService);
	}

}