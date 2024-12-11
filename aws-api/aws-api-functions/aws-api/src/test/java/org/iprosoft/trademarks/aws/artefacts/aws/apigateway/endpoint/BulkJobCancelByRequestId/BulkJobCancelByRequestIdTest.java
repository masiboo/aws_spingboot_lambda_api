package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.BulkJobCancelByRequestId;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.easymock.EasyMock.createMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkJobCancelByRequestIdTest {

	private BulkJobCancelByRequestId handler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createBulkJobCancelByRequestId();
	}

	@Test
	public void testProcessJobStatusResponseMapSuccess() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("All job statuses set to CANCELED"));
	}

	@Test
	public void testApplyWithEmptyJobStatusResponseMap() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchJobOrBulkJobCancelByRequestIdStream();
		// act
		APIGatewayV2HTTPResponse response = handler.apply(inputStream);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("All job statuses set to CANCELED"));
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

}