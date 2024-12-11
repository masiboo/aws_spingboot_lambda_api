package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.bulkuploadrequest;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.v2.HttpResponseConstant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_CREATED;

@SpringBootTest
@Slf4j
class BulkUploadRequestTest {

	private BulkUploadRequest bulkUploadRequest;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.commonSetup();
		bulkUploadRequest = TestSetupUtils.createBulkUploadRequest();
	}

	@Test
	public void InvokeFunctionByFunctionInvokerSuccessfully() throws Exception {
		// Arrange
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "bulkUploadRequest");
		FunctionInvoker invoker = new FunctionInvoker();
		InputStream inputStream = TestData.getBulkInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		// Act
		invoker.handleRequest(inputStream, output, null);
		APIGatewayV2HTTPResponse response = TestSetupUtils.objectMapper.readValue(output.toString(),
				APIGatewayV2HTTPResponse.class);

		// Asset
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getBody(), "Response's body should not be null");
		assertEquals(HttpResponseConstant.STATUS_CODE_CREATED, response.getStatusCode());
	}

	@Test
	public void testApplySuccess() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBulkInputStream();
		// act
		APIGatewayV2HTTPResponse res = bulkUploadRequest.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		assertNotNull(res.getBody(), "artefactList should not be null");
	}

	@Test
	void testApplyWithInvalidMirisDocId() throws IOException {
		// arrange
		InputStream inputStream = TestData.setInvalidMirisDocIdGetBulkInputStream();
		// act
		APIGatewayV2HTTPResponse res = bulkUploadRequest.apply(inputStream);

		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		assertNotNull(res.getBody(), "res.getBody() should not be null");
		assertTrue(res.getBody().contains("ERROR"));
		assertTrue(res.getBody().contains("mirisDocId must be between 5 to 8 digits and no space allowed"));
		log.info(res.getBody());
	}

	@Test
	void testApplyWithInvalidClassType() throws IOException {
		// arrange
		InputStream inputStream = TestData.setInvalidClassTypeGetBulkInputStream();
		// act
		APIGatewayV2HTTPResponse res = bulkUploadRequest.apply(inputStream);

		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		assertNotNull(res.getBody(), "res.getBody() should not be null");
		assertTrue(res.getBody().contains("ERROR"));
		assertTrue(res.getBody().contains("Artefact class type is not valid"));
		log.info(res.getBody());
	}

}
