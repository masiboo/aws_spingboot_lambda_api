package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.canceljobrequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelJobRequestTest {

	@Test
	public void testApply_ValidJobId() throws MalformedURLException {
		// arrange
		String jobId = "123";
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("jobId", jobId));
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.commonSetup();
		CancelJobRequest cancelJobRequest = TestSetupUtils.createCancelJobRequest();
		// act
		APIGatewayV2HTTPResponse response = cancelJobRequest.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("Job is already CANCELED"));
	}

	@Test
	public void testApply_InvalidJobId() throws MalformedURLException {
		// arrange
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		CancelJobRequest cancelJobRequest = TestSetupUtils.createCancelJobRequest();
		// act
		APIGatewayV2HTTPResponse response = cancelJobRequest.apply(event);
		// assert
		assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing 'jobId' parameter in path"));
	}

	@Test
	public void testApplyJobNotFound() throws MalformedURLException {
		// arrange
		String jobId = "123";
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("jobid", jobId));
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		CancelJobRequest cancelJobRequest = TestSetupUtils.createCancelJobRequest();
		// act
		APIGatewayV2HTTPResponse response = cancelJobRequest.apply(event);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("No job found"));
	}

	@Test
	public void testApplyValidJobId() throws MalformedURLException {
		// arrange
		String jobId = "123";
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("jobId", jobId));
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		CancelJobRequest cancelJobRequest = TestSetupUtils.createCancelJobRequest();
		// act
		APIGatewayV2HTTPResponse response = cancelJobRequest.apply(event);
		// assert
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("No job found by jobId: " + jobId));
	}

	@Test
	public void testApplyJobWithStatusCancelled() throws MalformedURLException {
		// arrange
		String jobId = "123";
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setPathParameters(Map.of("jobId", jobId));
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.commonSetup();
		CancelJobRequest cancelJobRequest = TestSetupUtils.createCancelJobRequest();
		// act
		APIGatewayV2HTTPResponse response = cancelJobRequest.apply(event);
		// assert
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertEquals("Job is already CANCELED by jobId: " + jobId, response.getBody());
	}

}