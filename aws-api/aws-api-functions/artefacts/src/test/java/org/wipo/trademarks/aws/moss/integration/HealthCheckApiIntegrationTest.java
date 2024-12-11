package org.wipo.trademarks.aws.Aws.integration;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.wipo.trademarks.aws.Aws.integration.stack.builder.APIGatewayFactory;
import org.wipo.trademarks.aws.Aws.integration.stack.builder.LambdaFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.wipo.trademarks.aws.Aws.integration.util.APIGatewayUtil.*;


public class HealthCheckApiIntegrationTest extends BaseIntegrationTest {

    private static final String FUNCTION_NAME ="healthCheck";

    static LocalStackContainer localStack;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void setUp(){
        localStack = LocalStackHandler.getInstance();
    }

    @AfterAll
    public static void tearUp(){
        localStack.close();
    }

    @Test
    public void testHealthCheckApiIntegration() throws Exception {

        //creating lambda
        String handler = "org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsHealthCheck";
        CreateFunctionResult createFunctionResult = LambdaFactory.createFunction(FUNCTION_NAME, handler);

        //Invoke and test healthcheck api
        invokeHealthCheckApi();

        //Test Via APIGateway
        integrateApiGateway(createFunctionResult.getFunctionArn());

    }

    private void invokeHealthCheckApi() throws Exception{
        APIGatewayV2HTTPEvent apiRequestEvent = createGatewayRequestEvent("/api/health","GET /api/health");

        String payload = objectMapper.writeValueAsString(apiRequestEvent);

        // create an invoke request for the Lambda function with APIGatewayV2HTTPEvent
        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(FUNCTION_NAME).withPayload(payload);
        InvokeResult invokeResult = LambdaFactory.getLambdaClient().invoke(invokeRequest);

        String responseBody = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
        String extractedResBody = objectMapper.readTree(responseBody).get("body").asText();

        //Get actual response from InvocationResult
        APIGatewayV2HTTPResponse actualApiResponse = buildGatewayResponse(invokeResult.getStatusCode(),extractedResBody);

        //build expected response
        APIGatewayV2HTTPResponse expectedApiResponse = buildGatewayResponse(HttpStatus.SC_OK,
                objectMapper.writeValueAsString(Map.of("status","ok")));

        assertApiResponseEquals(expectedApiResponse,actualApiResponse);
    }




    private void integrateApiGateway(String lambdaFunctionArn) throws Exception {
        String healthStatusOk = "ok";
        String requestUrl = APIGatewayFactory.createApi("healthCheck",lambdaFunctionArn,HttpMethod.GET,"healthcheck");

        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(requestUrl);
            CloseableHttpResponse apiResp = client.execute(get);

            String responseStr = EntityUtils.toString(apiResp.getEntity());
            System.out.println(responseStr);
            assertEquals(HttpStatus.SC_OK, apiResp.getStatusLine().getStatusCode());
            String healthCheckStatus = objectMapper.readTree(responseStr).get("status").asText();

            assertNotNull(healthCheckStatus);
            assertEquals(healthStatusOk, healthCheckStatus);
        }
    }
}

