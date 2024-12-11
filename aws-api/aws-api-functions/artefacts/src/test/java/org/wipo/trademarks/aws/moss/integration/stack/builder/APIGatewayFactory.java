package org.wipo.trademarks.aws.Aws.integration.stack.builder;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.CreateDeploymentRequest;
import com.amazonaws.services.apigateway.model.CreateResourceRequest;
import com.amazonaws.services.apigateway.model.CreateResourceResult;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.GetResourcesRequest;
import com.amazonaws.services.apigateway.model.IntegrationType;
import com.amazonaws.services.apigateway.model.PutIntegrationRequest;
import com.amazonaws.services.apigateway.model.PutMethodRequest;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.wipo.trademarks.aws.Aws.integration.LocalStackHandler;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.API_GATEWAY;
import static org.wipo.trademarks.aws.Aws.integration.LocalStackHandler.DEFAULT_REGION;

public class APIGatewayFactory {

    private static final LocalStackContainer localStack = LocalStackHandler.getInstance();

    /**
     * This method is to build APIGateway and return the request URL for localstack testing
     * @param apiName name for the APIGateway
     * @param lambdaFunctionArn LambdaFunctionARN for the API Integration
     * @param httpMethod HttpMethod to be exposed
     * @param path Resource path for the API to be exposed
     * @return request URL to hit and test the API
     */

    public static String createApi(String apiName,String lambdaFunctionArn,HttpMethod httpMethod,String path)  {
        String apiGatewayEndpoint = localStack.getEndpointOverride(API_GATEWAY).toString();
        String uri = String.format("arn:aws:apigateway:%s:lambda:path/2015-03-31/functions/%s/invocations",localStack.getRegion(),lambdaFunctionArn);
        String stageTest = "test";
        String restApiId ;
        String resourceId ;

        AmazonApiGateway apiGatewayClient = AmazonApiGatewayClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(apiGatewayEndpoint, DEFAULT_REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(localStack.getAccessKey(), localStack.getSecretKey())))
                .build();


        restApiId = apiGatewayClient.createRestApi(new CreateRestApiRequest().withName(apiName)).getId();


        String rootResourceId = apiGatewayClient.getResources(new GetResourcesRequest().withRestApiId(restApiId)).getItems().get(0).getId();

        CreateResourceResult resourceResult = apiGatewayClient.createResource(new CreateResourceRequest()
                .withParentId(rootResourceId)
                .withRestApiId(restApiId)
                .withPathPart(path));
        resourceId = resourceResult.getId();


        PutMethodRequest putMethodRequest = new PutMethodRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod(httpMethod.name())
                .withAuthorizationType("NONE");

        apiGatewayClient.putMethod(putMethodRequest);

        PutIntegrationRequest putIntegrationRequest = new PutIntegrationRequest()
                .withRestApiId(restApiId)
                .withResourceId(resourceId)
                .withHttpMethod(httpMethod.name())
                .withType(IntegrationType.AWS_PROXY)
                .withIntegrationHttpMethod(HttpMethod.POST.name())
                .withUri(uri);

        apiGatewayClient.putIntegration(putIntegrationRequest);


        CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest()
                .withRestApiId(restApiId)
                .withStageName(stageTest);

        apiGatewayClient.createDeployment(createDeploymentRequest);

        // Invoke the API Gateway
        return String.format("%s/restapis/%s/%s/_user_request_/%s", apiGatewayEndpoint, restApiId, stageTest, resourceResult.getPath());

    }


}
