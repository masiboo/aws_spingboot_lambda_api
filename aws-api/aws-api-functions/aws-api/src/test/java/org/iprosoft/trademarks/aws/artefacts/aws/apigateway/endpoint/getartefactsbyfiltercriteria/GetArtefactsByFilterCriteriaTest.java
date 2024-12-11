package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getartefactsbyfiltercriteria;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactFilterCriteria;
import org.iprosoft.trademarks.aws.artefacts.model.entity.Artefact;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactClassType;
import org.iprosoft.trademarks.aws.artefacts.service.artefact.ArtefactService;
import org.iprosoft.trademarks.aws.artefacts.service.artefactjob.ArtefactJobService;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.DYNAMO, ServiceName.S3 })
@AllArgsConstructor(onConstructor = @__(@Autowired))*/
@Slf4j
public class GetArtefactsByFilterCriteriaTest {

	private TestRestTemplate testRestTemplate;

	private ObjectMapper objectMapper;

	private GetArtefactsByFilterCriteria getArtefactsByFilterCriteria;

	private ArtefactService artefactService;

	@BeforeAll
	static void setUp() {
		/*
		 * AwsServicesSetup.prepareDynamoDB(); AwsServicesSetup.prepareS3();
		 * AwsServicesSetup.populateDynamoDB();
		 */
	}

	@BeforeEach
	void init() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setEmptyArtefact = true;
		TestSetupUtils.commonSetup();
		getArtefactsByFilterCriteria = TestSetupUtils.createGetArtefactsByFilterCriteria();
	}

	@Test
	@Disabled
	public void GetArtefactsByFilterCriteria_NoValidCriteria() throws Exception {
		ArtefactFilterCriteria filterCriteria = ArtefactFilterCriteria.builder().build();

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.BAD_REQUEST, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetArtefactsByFilterCriteria_InvalidCriteria_supplied() throws Exception {
		ArtefactFilterCriteria filterCriteria = ArtefactFilterCriteria.builder()
			.dateFrom("2023-07-07T13:09:21+0000")
			.build();

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR,
				(Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetArtefactsByFilterCriteria_WithInsertedDate() throws Exception {
		ArtefactFilterCriteria filterCriteria = ArtefactFilterCriteria.builder()
			.insertedDate("2023-07-07T13:09:21+0000")
			.build();

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	@Disabled
	public void GetArtefactsByFilterCriteria_WithInsertedDateRange() throws Exception {
		ArtefactFilterCriteria filterCriteria = ArtefactFilterCriteria.builder()
			.mirisDocId("12232")
			.dateFrom("2023-07-07T13:00:00+0000")
			.dateTo("2023-07-07T14:00:00+0000")
			.build();

		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(filterCriteria))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/getartefactsbyfiltercriteria")).body(event),
				APIGatewayV2HTTPResponse.class);
		log.info(Objects.requireNonNull(result.getBody()).getBody());
		// Should be 400
		log.info("Response HTTP Status Code: " + (Objects.requireNonNull(result.getBody())).getStatusCode());
		Assertions.assertEquals(HttpStatusCode.OK, (Objects.requireNonNull(result.getBody())).getStatusCode());
	}

	@Test
	void testApplySuccess() throws JsonProcessingException {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withBody(TestSetupUtils.objectMapper.writeValueAsString(TestData.getArtefactFilterCriteria()))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactsByFilterCriteria.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains(TestData.getMixedArtefactList().get(0).getId()));
	}

	@Test
	void testApplyFailed() throws JsonProcessingException {
		// arrange
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withBody(TestSetupUtils.objectMapper.writeValueAsString(new ArtefactFilterCriteria()))
			.build();
		// act
		APIGatewayV2HTTPResponse response = getArtefactsByFilterCriteria.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Filter criteria not provided"));
	}

}
