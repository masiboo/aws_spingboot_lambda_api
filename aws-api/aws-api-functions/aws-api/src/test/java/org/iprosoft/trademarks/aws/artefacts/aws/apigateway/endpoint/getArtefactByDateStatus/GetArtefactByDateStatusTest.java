package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getArtefactByDateStatus;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.iprosoft.trademarks.aws.artefacts.util.ArtefactStatus;
import org.iprosoft.trademarks.aws.artefacts.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import software.amazon.awssdk.http.HttpStatusCode;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class GetArtefactByDateStatusTest {

	private GetArtefactByDateStatus getArtefactByDateStatus;

	@Test
	void testApplySuccessWithoutMock() throws MalformedURLException {
		// arrange
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("date", DateUtils.getCurrentDateShortStr());
		queryStringParameters.put("status", ArtefactStatus.INDEXED.name());
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withQueryStringParameters(queryStringParameters)
			.build();
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		getArtefactByDateStatus = TestSetupUtils.createGetArtefactByDateStatus();
		// act
		APIGatewayV2HTTPResponse response = getArtefactByDateStatus.apply(event);
		// assert
		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("artefacts"));
	}

	@Test
	void testApplySuccessWithMock() throws MalformedURLException {
		// arrange
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("date", DateUtils.getCurrentDateShortStr());
		queryStringParameters.put("status", ArtefactStatus.INDEXED.name());
		APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
			.withQueryStringParameters(queryStringParameters)
			.build();
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		getArtefactByDateStatus = TestSetupUtils.createGetArtefactByDateStatus();
		// act
		APIGatewayV2HTTPResponse response = getArtefactByDateStatus.apply(event);
		// assert
		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatusCode.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("artefacts"));
	}

}