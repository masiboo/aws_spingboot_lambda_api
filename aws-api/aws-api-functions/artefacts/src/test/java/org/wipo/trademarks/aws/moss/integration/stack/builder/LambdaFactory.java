package org.wipo.trademarks.aws.Aws.integration.stack.builder;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.TracingConfig;
import com.amazonaws.services.lambda.model.TracingMode;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.wipo.trademarks.aws.Aws.integration.LocalStackHandler;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.wipo.trademarks.aws.Aws.integration.BaseIntegrationTest.BUCKET_NAME;
import static org.wipo.trademarks.aws.Aws.integration.BaseIntegrationTest.LAMBDA_CODE_S3_KEY;

public class LambdaFactory {

    private static final Runtime DEFAULT_RUNTIME  = Runtime.ProvidedAl2;

    private static final LocalStackContainer localStack = LocalStackHandler.getInstance();


    /**
     *
     * This method is to build the Lambda function for the Integration testing using localstack
     * Note : It's expected to have the lambdacode uploaded using BaseIntegrationTest.java
     *
     * @param functionName Name of the Lambda Function
     * @param handler Handler for hte Lambda
     * @return CreateFunctionResult with functionArn and other metadata
     */
    public static CreateFunctionResult createFunction(String functionName,String handler){
        String testLambdaRole = "arn:aws:iam::123456789012:role/lambda-role";
        AWSLambda lambda = getLambdaClient();
        CreateFunctionRequest request = new CreateFunctionRequest()
                .withFunctionName(functionName)
                .withRuntime(DEFAULT_RUNTIME)
                .withCode(new FunctionCode().withS3Key(LAMBDA_CODE_S3_KEY).withS3Bucket(BUCKET_NAME))
                .withHandler(handler)
                .withTracingConfig(new TracingConfig().withMode(TracingMode.PassThrough))
                .withRole(testLambdaRole);
        return lambda.createFunction(request);
    }

    public static AWSLambda getLambdaClient(){
        String dynamoDbEndpoint = localStack.getEndpointOverride(DYNAMODB).toString();
        return AWSLambdaClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, LocalStackHandler.DEFAULT_REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(localStack.getAccessKey(), localStack.getSecretKey())))
                .build();
    }
}
