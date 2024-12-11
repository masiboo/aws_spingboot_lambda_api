package artefact.apigateway;

import artefact.aws.dynamodb.DynamoDbConnectionBuilder;
import artefact.aws.s3.S3ConnectionBuilder;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.util.Map;

public abstract class CoreRequestHandler extends AbstractRequestHandler {


//    This code inside the static block is executed only once: the first time the class is loaded into memory
    static {
        if (System.getenv("AWS_REGION") != null) {
            setUpHandler(System.getenv(),
                    new DynamoDbConnectionBuilder().setRegion(Region.of(System.getenv("AWS_REGION")))
                            .setCredentials(EnvironmentVariableCredentialsProvider.create()),
                    new S3ConnectionBuilder().setRegion(Region.of(System.getenv("AWS_REGION")))
                            .setCredentials(EnvironmentVariableCredentialsProvider.create()) );
        }
    }

    protected static void setUpHandler(final Map<String, String> map,
                                       final DynamoDbConnectionBuilder builder, final S3ConnectionBuilder s3) {

        setAwsServiceCache(map, builder, s3);

    }
}
