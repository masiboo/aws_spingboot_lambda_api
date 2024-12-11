package org.wipo.trademarks.aws.Aws.integration;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class BaseIntegrationTest {

    public static final String BUCKET_NAME = "lambda-code-bucket";

    public static final String LAMBDA_CODE_S3_KEY = "LambdaCode";


    private static final LocalStackContainer localStack;


    //    This code inside the static block is executed only once: the first time the class is loaded into memory
    static {
        try {
            localStack = LocalStackHandler.getInstance();
            uploadLambdaCodeToS3();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void  uploadLambdaCodeToS3() throws IOException {
        String artefactPath = Paths.get(System.getProperty("user.dir")).getParent().getParent()
                .resolve("iac")
                .resolve("runtime.zip")
                .toString();

        byte [] lambdaCode = Files.readAllBytes(Path.of(artefactPath));
        try(S3Client s3 = S3Client
                .builder()
                .endpointOverride(localStack.getEndpointOverride(S3))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
                        )
                )
                .httpClient(UrlConnectionHttpClient.builder().build())
                .region(Region.of(localStack.getRegion()))
                .build()) {
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());


            s3.putObject(PutObjectRequest
                            .builder()
                            .bucket(BUCKET_NAME)
                            .key(LAMBDA_CODE_S3_KEY).build(),
                    RequestBody.fromBytes(lambdaCode));
        }
    }
}
