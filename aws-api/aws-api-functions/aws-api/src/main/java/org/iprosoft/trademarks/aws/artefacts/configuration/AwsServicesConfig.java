package org.iprosoft.trademarks.aws.artefacts.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.net.URI;

@Configuration
public class AwsServicesConfig {

	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
			.region(Region.EU_CENTRAL_1)
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.region(Region.EU_CENTRAL_1)
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}

	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
			.region(Region.EU_CENTRAL_1)
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}

	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
	}

	@Bean
	public SsmClient ssmClient() {
		return SsmClient.builder()
			.region(Region.EU_CENTRAL_1)
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}

	@Bean
	public LambdaAsyncClient lambdaAsyncClient() {
		return LambdaAsyncClient.builder()
			.credentialsProvider(DefaultCredentialsProvider.create())
			.region(Region.EU_CENTRAL_1)
			.build();
	}

	@Bean
	public ApiGatewayManagementApiClient apiGatewayManagementApiClient() throws Exception {
		return ApiGatewayManagementApiClient.builder()
			.region(Region.EU_CENTRAL_1)
			.endpointOverride(new URI("https://api-gateway-endpoint"))
			.build();
	}

}
