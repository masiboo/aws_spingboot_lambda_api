package org.iprosoft.trademarks.aws.artefacts.aws.apigateway.testconfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.net.URI;

@TestConfiguration
public class AwsServicesTestConfig {

	@Value("${aws.access.key.id}")
	private String accessKey;

	@Value("${aws.secret.access.key}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.endpoint}")
	private String endpoint;

	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
	}

	@Bean
	public SsmClient ssmClient() {
		return SsmClient.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

	@Bean
	public ApiGatewayManagementApiClient apiGatewayManagementApiClient() {
		return ApiGatewayManagementApiClient.builder()
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.build();
	}

}
