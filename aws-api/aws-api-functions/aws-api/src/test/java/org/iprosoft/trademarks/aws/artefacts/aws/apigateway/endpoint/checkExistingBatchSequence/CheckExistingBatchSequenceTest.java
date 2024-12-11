package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.checkExistingBatchSequence;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_OK;

class CheckExistingBatchSequenceTest {

	private CheckExistingBatchSequence handler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isBatchSequenceExist = true;
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createCheckExistingBatchSequence();
	}

	@Test
	public void invokeFunctionByFunctionInvokerSuccessfully() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "checkExistingBatchSequence");
		InputStream inputStream = TestData.getCheckBatchSequenceInputStream();

		FunctionInvoker invoker = new FunctionInvoker();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(inputStream, output, null);
	}

	@Test
	public void testWhenBatchSequenceExistReturn200WithArrayOfConflictSequenceId() throws Exception {
		// arrange
		InputStream inputStream = TestData.getCheckBatchSequenceInputStream();
		// Act
		APIGatewayV2HTTPResponse res = handler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_OK.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains("221122.1"));
	}

	@Test
	public void testWhenBatchSequenceNotExistReturn200WithEmptyArray() throws Exception {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		handler = TestSetupUtils.createCheckExistingBatchSequence();

		InputStream inputStream = TestData.getCheckBatchSequenceInputStream();
		// Act
		APIGatewayV2HTTPResponse res = handler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_OK.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains("[]"));
	}

}