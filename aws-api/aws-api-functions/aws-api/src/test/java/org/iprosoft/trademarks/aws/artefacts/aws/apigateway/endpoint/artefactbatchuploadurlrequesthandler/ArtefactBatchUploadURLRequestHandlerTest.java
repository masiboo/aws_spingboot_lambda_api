package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.artefactbatchuploadurlrequesthandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.entity.ArtefactBatch;
import org.iprosoft.trademarks.aws.artefacts.util.CsvConverterUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_CREATED;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ArtefactBatchUploadURLRequestHandlerTest {

	private ArtefactBatchUploadURLRequestHandler requestHandler;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = false;
		TestSetupUtils.isBatchSequenceExist = true;
		TestSetupUtils.commonSetup();
		requestHandler = TestSetupUtils.createArtefactBatchUploadURLRequestHandler();
	}

	@Test
	public void invokeFunctionByFunctionInvokerSuccessfully() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "artefactBatchUploadURLRequestHandler");
		InputStream inputStream = TestData.getBatchInputStream();

		FunctionInvoker invoker = new FunctionInvoker();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(inputStream, output, null);
	}

	@Test
	void testApply_Addendum_WithValidMirisDocId() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchInputStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertEquals("OK", jsonObject.get("validation").getAsString(), "Validation should be Ok");
		assertEquals("Addendum", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("16529262", jsonObject.get("mirisDocId").getAsString(), "16529262");
		assertEquals("CERTIFICATE", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
	}

	@Test
	void testApply_new_request_validation() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchWithNewRqustInputStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertEquals("OK", jsonObject.get("validation").getAsString(), "Validation should be Ok");
		assertEquals("new_request", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("CERTIFICATE", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
	}

	@Test
	void testApply_new_request_without_user() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchWithNewRqustWithoutUserInputStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertTrue(jsonObject.get("validation").getAsString().contains("ERROR"), "Validation should be Error");
		assertEquals("new_request", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("CERTIFICATE", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
	}

	@Test
	void testApply_new_request_without_mirisDocId() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchWithNewRqustWithoutMirisDocIdInputStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertTrue(jsonObject.get("validation").getAsString().contains("OK"), "Validation should be OK");
		assertEquals("Addendum", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("CERTIFICATE", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
	}

	@Test
	void testApplyMixOfGoodAndBadBatch() throws IOException {
		// arrange
		InputStream inputStream = TestData.getGoodAndBadBatchInputStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertTrue(jsonObject.get("validation").getAsString().contains("ERROR"), "Validation should be ERROR");
		assertEquals("Addendum", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("16529262", jsonObject.get("mirisDocId").getAsString(), "16529262");
		assertEquals("CERTIX", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
	}

	@Test
	void testApply_Addendum_WithCsvBody() throws IOException {
		// arrange
		InputStream inputStream = TestData.getCsvBatchUploadStream();
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		log.info(res.getBody());
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		List<ArtefactBatch> artefactBatchList = CsvConverterUtil.convertCSVClass(res.getBody(), ArtefactBatch.class);
		assertEquals(artefactBatchList.get(0).getType(), "Addendum");
		assertEquals(artefactBatchList.get(0).getArtefactClassType(), "DOCUMENT");
		assertEquals(artefactBatchList.get(0).getStatus(), "INIT");
		assertEquals(artefactBatchList.get(0).getContentType(), "TIFF");
	}

	@Test
	void testApply_TypeAddendum_WithNonExistingBatch() throws IOException {
		// arrange
		InputStream inputStream = TestData.getBatchInputStream();
		// Act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		JsonArray jsonArray = JsonParser.parseString(res.getBody()).getAsJsonArray();
		assertFalse(jsonArray.isEmpty(), "Response array should not be empty");
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertEquals("Addendum", jsonObject.get("type").getAsString(), "Type should be Addendum");
		assertEquals("CERTIFICATE", jsonObject.get("artefactClassType").getAsString(),
				"ArtefactClassType should be DOCUMENT");
		assertEquals("INIT", jsonObject.get("status").getAsString(), "Status should be INIT");
		assertEquals("TIFF", jsonObject.get("contentType").getAsString(), "Status should be TIFF");
	}

	@Test
	void testApply_WithExistingBatch() throws IOException {
		// Arrange
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		jsonEventPayload = TestData.setOverwriteBatchFalse(jsonEventPayload);
		InputStream inputStream = new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertEquals(SC_BAD_REQUEST.getStatusCode(), res.getStatusCode());
		assertNotNull(res.getBody());
	}

	@Test
	void testApply_OverwriteExistingBatch() throws IOException {
		// Arrange
		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-post-batches-upload.json").toPath());
		InputStream inputStream = new ByteArrayInputStream(jsonEventPayload.getBytes(StandardCharsets.UTF_8));
		// act
		APIGatewayV2HTTPResponse res = requestHandler.apply(inputStream);
		// assert
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		assertNotNull(res.getBody());
	}

}