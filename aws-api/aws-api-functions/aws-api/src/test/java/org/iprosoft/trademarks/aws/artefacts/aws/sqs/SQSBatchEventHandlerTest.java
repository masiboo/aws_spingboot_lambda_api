package org.wipo.trademarks.Aws.artefacts.aws.sqs;

import cloud.localstack.ServiceName;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import org.wipo.trademarks.Aws.artefacts.AwsApiApplication;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesSetup;
import org.wipo.trademarks.Aws.artefacts.aws.apigateway.testconfiguration.AwsServicesTestConfig;
import org.wipo.trademarks.Aws.artefacts.configuration.SystemEnvironmentVariables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { AwsServicesTestConfig.class })
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { ServiceName.SQS, ServiceName.DYNAMO })
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SQSBatchEventHandlerTest {

	private final ObjectMapper objectMapper;

	private static WireMockServer wireMockServer;

	@BeforeAll
	static void setUp() {
		AwsServicesSetup.prepareDynamoDB();
		AwsServicesSetup.populateDynamoDB("Aws-table-batch-status-event.json");

		// Start the WireMockServer on a random port
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
	}

	@AfterAll
	static void tearDown() {
		// Stop the WireMockServer after each test
		wireMockServer.stop();
	}

	@Test
	public void testSQSBatchEvent_NotEligibleForMerge() throws Exception {
		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSBatchEventHandler");

		String jsonEventPayload = Files.readString(
				ResourceUtils.getFile("classpath:sqs-event/SQS_BatchEvent_not_MergeEligibleEvent.json").toPath());

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);

	}

	// set env ARTEFACTS_S3_BUCKET
	@Test
	public void testSQSBatchEvent_EligibleForMerge() throws Exception {
		Map<String, String> metaData = Map.of("batchSequence", "0221123.052");
		Map<String, Object> mergeReqMap = Map.of("bucket", SystemEnvironmentVariables.ARTEFACTS_S3_BUCKET, "key", List
			.of("Aws-2023-07-07/0221123.052-0000D/00000001.TIF", "Aws-2023-07-07/0221123.052-0000D/00000002.TIF"),
				"metadata", metaData);

		String requestBody = objectMapper.writeValueAsString(mergeReqMap);

		// stub creation metadata service
		wireMockServer.stubFor(
				post(urlMatching("/api/v1/merge-files-to-pdf")).withHeader("Content-Type", matching("application/json"))
					.withRequestBody(containing(requestBody))
					.willReturn(aResponse().withHeader("Content-Type", "application/json")));

		System.setProperty("MAIN_CLASS", AwsApiApplication.class.getName());
		System.setProperty("spring.cloud.function.definition", "SQSBatchEventHandler");

		String jsonEventPayload = Files
			.readString(ResourceUtils.getFile("classpath:sqs-event/SQS_BatchEvent_MergeEligibleEvent.json").toPath());

		InputStream targetStream = new ByteArrayInputStream(jsonEventPayload.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FunctionInvoker invoker = new FunctionInvoker();
		invoker.handleRequest(targetStream, output, null);

	}

}