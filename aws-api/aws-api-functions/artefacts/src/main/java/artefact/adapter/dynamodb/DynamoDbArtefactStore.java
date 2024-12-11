package artefact.adapter.dynamodb;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import artefact.adapter.ArtefactStore;
import artefact.entity.Artefact;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoDbArtefactStore implements ArtefactStore {

    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                    .addExecutionInterceptor(new TracingInterceptor())
                    .build())
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    @Override
    public void addTags() {

    }

    @Override
    public void saveArtefactWithTags(Artefact artefact) {

    }

    @Override
    public String findTags(String tags) {
        return null;
    }

    @Override
	public void postArtefact(Artefact artefact) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public Artefact getArtefactByDocId(String docid) {
        return null;
    }

}
