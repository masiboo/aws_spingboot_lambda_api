package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BatchJobCancelByRequestId;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchJobCancelByRequestIdTest {

	private BatchJobCancelByRequestId handler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createBatchJobCancelByRequestId();
	}

	@Test
	public void testProcessJobStatusResponseMapSuccess() throws IOException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setEmptyJobResponse = true;
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createBatchJobCancelByRequestId();
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(
				response.getBody().contains("No jobs found for this requestId: 821708a2-d8a5-4503-8186-6a7ae92a91f6"));
	}

	@Test
	public void testApplyWithEmptyJobStatusResponseMap() throws IOException {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setEmptyJobResponse = true;
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createBatchJobCancelByRequestId();
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("404"));
		assertTrue(response.getBody().contains("No jobs found"));
	}

	@Test
	public void testApplyWithInvalidRequestId() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelWithoutRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'requestId' parameter in path"));
	}

	private APIGatewayV2HTTPEvent createEventWithHttpMethod(String requestId) {
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("requestId", requestId);
		event.setPathParameters(pathParameters);
		return event;
	}

}