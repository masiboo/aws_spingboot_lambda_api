package org.wipo.trademarks.aws.Aws.integration;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.API_GATEWAY;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;


public class LocalStackHandler {

    public static final String DEFAULT_REGION = "us-east-1";


    private static volatile LocalStackContainer localStack;

    private LocalStackHandler(){
        //private constructor prevent unintended instantiation of the class
    }


    public static LocalStackContainer getInstance() {
        if(localStack == null){
            synchronized (LocalStackHandler.class) {
                localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
                        .withServices(LAMBDA, API_GATEWAY, S3, DYNAMODB)
                        .withEnv("DEFAULT_REGION", DEFAULT_REGION)
                        .withEnv("AWS_ACCESS_KEY_ID", "test")
                        .withEnv("AWS_SECRET_ACCESS_KEY", "test");
                localStack.start();
            }

        }
        return localStack;
    }

}
