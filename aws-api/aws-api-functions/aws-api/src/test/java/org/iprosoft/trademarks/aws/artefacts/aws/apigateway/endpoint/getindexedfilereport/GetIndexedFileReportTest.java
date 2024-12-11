package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.endpoint.getindexedfilereport;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.util.ResourceUtils;
import org.iprosoft.trademarks.aws.artefacts.AwsApiApplication;
import org.iprosoft.trademarks.aws.artefacts.TestData;
import org.iprosoft.trademarks.aws.artefacts.TestSetupUtils;
import org.iprosoft.trademarks.aws.artefacts.model.dto.BatchOutput;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.iprosoft.trademarks.aws.artefacts.aws.apigateway.response.ApiResponseStatus.SC_CREATED;

@SpringBootTest
@Slf4j
class GetIndexedFileReportTest {

	private GetIndexedFileReport getIndexedFileReport;

	@BeforeEach
	void setUp() throws MalformedURLException {
		TestSetupUtils.resetBooleans();
		TestSetupUtils.commonSetup();
		getIndexedFileReport = TestSetupUtils.createGetIndexedFileReport();
	}

	@Test
	public void InvokeFunctionByFunctionInvokerSuccessfully() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "getIndexedFileReport");

		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:api-events/api-get-index-file-report-request.json").toPath());

		FunctionInvoker invoker = new FunctionInvoker();
		assert jsonEventPayload != null;
		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(targetStream, output, null);
		log.info(output.toString());
		APIGatewayV2HTTPResponse response = TestSetupUtils.objectMapper.readValue(output.toString(),
				APIGatewayV2HTTPResponse.class);
		assertNotNull(response, "Response should not be null");
		assertEquals(HttpStatusCode.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("No batch found"));
	}

	@Test
	public void testApplySuccessWithDateParameter() throws IOException {
		// arrange
		InputStream inputStream = TestData.getInputStreamForIndexFileReportWithDateParameter();
		// act
		APIGatewayV2HTTPResponse res = getIndexedFileReport.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
	}

	@Test
	public void testApplySuccessWithoutParameter() throws IOException {
		// arrange
		InputStream inputStream = TestData.getInputStreamForIndexFileReportWithoutParameters();
		// act
		APIGatewayV2HTTPResponse res = getIndexedFileReport.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		log.info(res.getBody());
	}

	@Test
	public void testApplyInvalidDateBeCurrentDate() throws IOException {
		// arrange
		InputStream inputStream = TestData.getInputStreamWithInvalidDateForIndexFileReport();
		// act
		APIGatewayV2HTTPResponse res = getIndexedFileReport.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(SC_CREATED.getStatusCode(), res.getStatusCode());
		List<BatchOutput> batchOutputs = TestSetupUtils.objectMapper.readValue(res.getBody(), new TypeReference<>() {
		});
		assertNotNull(batchOutputs);
		assertNotNull(batchOutputs.get(0).getReportUrl());
	}

	@Test
	public void testApplySuccessWithDateParameterReturnEmpty() throws IOException, ParseException {
		// arrange
		InputStream inputStream = TestData.getInputStreamForIndexFileReportWithDateParameter();
		// act
		APIGatewayV2HTTPResponse res = getIndexedFileReport.apply(inputStream);
		// assert
		assertNotNull(res, "Response should not be null");
		assertEquals(HttpStatusCode.CREATED, res.getStatusCode());
		assertNotNull(res.getBody());
	}

}