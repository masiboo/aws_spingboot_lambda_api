package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BatchJobStatusByRequestId;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchJobStatusByRequestIdTest {

	private BatchJobStatusByRequestId handler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.getBatchJobStatusByRequestId();
	}

	@Test
	public void testHandleGetRequestSuccess() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("jobs"));
		assertTrue(response.getBody().contains("requestId"));
	}

	@Test
	public void testApplyJobStatusResponseMap() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);

		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("jobs"));
		assertTrue(response.getBody().contains("requestId"));
	}

	@Test
	public void testApplyWithInvalidRequestId() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelWithoutRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);

		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'requestId' parameter in path"));
	}

}