package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.convertresizeimagetotif;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.http.HttpStatus;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.ImageToTifResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_BAD_REQUEST;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_CREATED;

@SpringBootTest
@Slf4j
class ConvertResizeImageToTifTest {

	private ConvertResizeImageToTif convertResizeImageToTif;

	@BeforeEach
	void setUp() throws Exception {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.isCancelJobStatus = true;
		TestSetupUtils.setEmptyArtefact = false;
		TestSetupUtils.commonSetup();
		convertResizeImageToTif = TestSetupUtils.createConvertResizeImageToTif();
	}

	@Test
	void InvokeFunctionByFunctionInvokerSuccessfully() throws IOException {
		// arrange
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "convertResizeImageToTif");
		InputStream inputStream = TestData.getConvertResizeImageToTifInputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		// act
		invoker.handleRequest(inputStream, output, null);
	}

	@Test
	void testApplySuccess() throws IOException {
		// arrange
		InputStream inputStream = TestData.getConvertResizeImageToTifInputStream();
		// act
		APIGatewayV2HTTPResponse res = convertResizeImageToTif.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		List<ImageToTifResponse> imageToTifResponseList = TestSetupUtils.objectMapper.readValue(res.getBody(),
				new TypeReference<>() {
				});
		assertEquals(imageToTifResponseList.get(0).getSignedS3Url(), TestSetupUtils.expectedSignedS3Url);
		assertEquals(imageToTifResponseList.get(0).getStatusCode(), HttpStatus.CREATED.toString());
		assertEquals(imageToTifResponseList.get(0).getMetaData().get("classType"), "COLORLOGO");
		assertEquals(imageToTifResponseList.get(0).getMetaData().get("artefactId"), "123");
		assertEquals(imageToTifResponseList.get(0).getMetaData().get("resolutionInDpi"), "266");
		assertEquals(imageToTifResponseList.get(0).getMetaData().get("fileType"), "tif");
		assertEquals(imageToTifResponseList.get(0).getMetaData().get("size"), "1050");
	}

	@Test
	void testWhenArtefactNotFound() throws Exception {
		// arrange
		TestSetupUtils.setEmptyArtefact = true;
		TestSetupUtils.commonSetup();
		convertResizeImageToTif = TestSetupUtils.createConvertResizeImageToTif();
		InputStream inputStream = TestData.getConvertResizeImageToTifInputStream();
		// act
		APIGatewayV2HTTPResponse res = convertResizeImageToTif.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		assertTrue(res.getBody().contains("404"));
		assertTrue(res.getBody().contains("Artefact not found by mirisDocId"));
	}

	@Test
	void testApplyWithNoPathParam() throws IOException {
		// arrange
		InputStream inputStream = TestData.getConvertResizeImageToTifInputStreamWithNoPathParam();
		// act
		APIGatewayV2HTTPResponse res = convertResizeImageToTif.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_BAD_REQUEST.getStatusCode(), res.getStatusCode());
		assertEquals(res.getBody(), "'mirisDocId' must be present with non-empty values.");
	}

	@Test
	void testApplyWithWarningMaxLimit100MB() throws Exception {
		// arrange
		TestSetupUtils.resetBooleans();
		TestSetupUtils.setEmptyArtefact = false;
		TestSetupUtils.commonSetup();
		TestSetupUtils.setMaxAllowed100Mb();
		convertResizeImageToTif = TestSetupUtils.createConvertResizeImageToTif();
		InputStream inputStream = TestData.getConvertResizeImageToTifInputStream();
		// act
		APIGatewayV2HTTPResponse res = convertResizeImageToTif.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		List<ImageToTifResponse> imageToTifResponseList = TestSetupUtils.objectMapper.readValue(res.getBody(),
				new TypeReference<>() {
				});
		assertEquals(imageToTifResponseList.get(0).getSignedS3Url(),
				"Input image exceeded maximum allowed limit is 100 MB");
	}

}