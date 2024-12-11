package org.wipo.trademarks.aws.Aws.artefact.aws.dynamodbv2;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoDBClientBuilder {

    private AmazonDynamoDB dbClient;
    private DynamoDBMapper mapper;


    public DynamoDBClientBuilder() {
        this.dbClient = AmazonDynamoDBClientBuilder.defaultClient();
        this.mapper = new DynamoDBMapper(dbClient);
    }
}
