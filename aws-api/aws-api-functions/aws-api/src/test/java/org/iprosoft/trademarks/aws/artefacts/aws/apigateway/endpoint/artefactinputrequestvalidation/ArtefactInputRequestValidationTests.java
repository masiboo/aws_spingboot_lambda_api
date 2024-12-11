package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.artefactinputrequestvalidation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.dto.ArtefactInputRequestValidationDTO;
import org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactInput;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ArtefactItemInput;
import org.iprosoft.trademarks.aws.artefacts.util.ApiGatewayResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Disabled("Temporarily disabled")
public class ArtefactInputRequestValidationTests {

	private final ObjectMapper objectMapper;

	private final TestRestTemplate testRestTemplate;

	@Test
	public void ArtefactInputRequestValidationSuccess() throws JsonProcessingException, URISyntaxException {
		// Create an ArtefactInput object
		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("iprosoft.pdf");
		artefact.setArtefactClassType("DOCUMENT");
		artefact.setMirisDocId("123456789");
		List<ArtefactItemInput> artefactItemInputList = new ArrayList<>();
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setPath("mp4");
		artefactItemInput.setFilename("mov_bbb.mp4");
		artefactItemInput.setContentType("video/mp4");
		artefactItemInputList.add(artefactItemInput);
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/artefactinputrequestvalidation")).body(event),
				APIGatewayV2HTTPResponse.class);
		// Log the response body of the function
		log.info(objectMapper.writerWithDefaultPrettyPrinter()
			.writeValueAsString(Objects.requireNonNull(result.getBody()).getBody()));
		// Map the response object body to ArtefactInputRequestValidationDTO
		ArtefactInputRequestValidationDTO validationDTO = objectMapper.readValue(result.getBody().getBody(),
				ArtefactInputRequestValidationDTO.class);
		log.info("isValid: " + validationDTO.getValidation().isEmpty());
		Assertions.assertTrue(validationDTO.getValidation().isEmpty());
	}

	@Test
	public void ArtefactInputRequestValidationFailureNoMirisDocId() throws JsonProcessingException, URISyntaxException {
		// Create an ArtefactInput object
		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("iprosoft.pdf");
		artefact.setArtefactClassType("DOCUMENT");
		List<ArtefactItemInput> artefactItemInputList = new ArrayList<>();
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setPath("mp4");
		artefactItemInput.setFilename("mov_bbb.mp4");
		artefactItemInput.setContentType("video/mp4");
		artefactItemInputList.add(artefactItemInput);
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/artefactinputrequestvalidation")).body(event),
				APIGatewayV2HTTPResponse.class);
		// Log the response body of the function
		log.info(objectMapper.writerWithDefaultPrettyPrinter()
			.writeValueAsString(Objects.requireNonNull(result.getBody()).getBody()));
		// Map the response object body to ArtefactInputRequestValidationDTO
		ArtefactInputRequestValidationDTO validationDTO = objectMapper.readValue(result.getBody().getBody(),
				ArtefactInputRequestValidationDTO.class);
		log.info("isValid: " + validationDTO.getValidation().isEmpty());
		Assertions.assertFalse(validationDTO.getValidation().isEmpty());
	}

	@Test
	public void ArtefactInputRequestValidationFailureNoClassType() throws JsonProcessingException, URISyntaxException {
		// Create an ArtefactInput object
		ArtefactInput artefact = new ArtefactInput();
		artefact.setArtefactName("iprosoft.pdf");
		artefact.setMirisDocId("123456789");
		List<ArtefactItemInput> artefactItemInputList = new ArrayList<>();
		ArtefactItemInput artefactItemInput = new ArtefactItemInput();
		artefactItemInput.setPath("mp4");
		artefactItemInput.setFilename("mov_bbb.mp4");
		artefactItemInput.setContentType("video/mp4");
		artefactItemInputList.add(artefactItemInput);
		artefact.setItems(artefactItemInputList);
		// Create API Gateway event
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withHeaders(ApiGatewayResponseUtil.createHeaders())
			.withBody(objectMapper.writeValueAsString(artefact))
			.build();
		// Call the Spring Cloud Function endpoint
		ResponseEntity<APIGatewayV2HTTPResponse> result = testRestTemplate.exchange(
				RequestEntity.post(new URI("/artefactinputrequestvalidation")).body(event),
				APIGatewayV2HTTPResponse.class);
		// Log the response body of the function
		log.info(objectMapper.writerWithDefaultPrettyPrinter()
			.writeValueAsString(Objects.requireNonNull(result.getBody()).getBody()));
		// Map the response object body to ArtefactInputRequestValidationDTO
		ArtefactInputRequestValidationDTO validationDTO = objectMapper.readValue(result.getBody().getBody(),
				ArtefactInputRequestValidationDTO.class);
		log.info("isValid: " + validationDTO.getValidation().isEmpty());
		Assertions.assertFalse(validationDTO.getValidation().isEmpty());
	}

}
