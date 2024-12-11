package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.ArtefactUploadRequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.easymock.EasyMock;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ArtefactUploadRequestTest {

	private ArtefactUploadRequest requestHandler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();

		requestHandler = TestSetupUtils.createArtefactUploadRequest();
	}

	@Test
	void testUploadArtefactSuccess() throws IOException {
		// arrange
		InputStream inputStream = TestData.getArtefactInputStream();
		// act
		APIGatewayV2HTTPResponse response = requestHandler.apply(inputStream);
		// assert
		assertNotNull(response);
		assertNotNull(response.getBody());
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("artefactId"));
		assertTrue(response.getBody().contains("signedS3Url"));
		assertTrue(response.getBody().contains("Aws-"));
		assertTrue(response.getBody().contains("jobId"));
	}

	@Test
	void testUploadArtefactValidation() throws IOException {
		// arrange
		InputStream inputStream = TestData.getArtefactInputStreamWithEmptyArtefactName();
		// act
		APIGatewayV2HTTPResponse response = requestHandler.apply(inputStream);
		// assert
		assertNotNull(response);
		assertNotNull(response.getBody());
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("ERROR"));
		assertTrue(response.getBody().contains("artefactName cannot be null or empty"));
	}

	@Test
	void testUploadArtefactInvalidJson() throws IOException {
		// arrange
		InputStream inputStream = TestData.getArtefactInputStreamWithInvalidJson();
		// act
		APIGatewayV2HTTPResponse response = requestHandler.apply(inputStream);
		// assert
		assertNotNull(response);
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid request body"));
	}

	@Test
	void testUploadArtefactValidationError() throws IOException {
		// arrange
		InputStream inputStream = TestData.getInvalidArtefactInputStream();
		// act
		APIGatewayV2HTTPResponse response = requestHandler.apply(inputStream);
		// assert
		assertNotNull(response);
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("More than 1 item found. Only one item is allowed"));
	}

	@Test
	void testUploadArtefactEmptyRequestBody() throws IOException {
		// arrange
		InputStream inputStream = TestData.getArtefactInputStreamWithNullBody();
		// act
		APIGatewayV2HTTPResponse response = requestHandler.apply(inputStream);
		// asset
		assertNotNull(response);
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Empty request body"));
	}

}