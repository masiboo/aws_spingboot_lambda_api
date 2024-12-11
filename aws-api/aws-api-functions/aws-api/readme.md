#  1. Spring Java Format
A set of plugins that can be applied to any Java project to provide a consistent “Spring” style. 
The set currently consists of:
* A source formatter that applies wrapping and whitespace conventions
* A checkstyle plugin that enforces consistency across the codebase

To enforce the formatter execute: 
  ```shell 
     mvn spring-javaformat:apply 
  ```

### 2. Run project locally 
#### Prerequisites
* GraalVM 22.3 for Java 17 https://www.graalvm.org/downloads/
* Apache Maven 3.9.3 https://dlcdn.apache.org/maven/maven-3/3.9.3/binaries/apache-maven-3.9.3-bin.zip
* Set JAVA_HOME and MAVEN_HOME to PATH
* You need to set the environment variables that are fetched by the SystemEnvironmentVariables class
    1. REGISTRY_TABLE_NAME
    2. CACHE_TABLE
    3. APP_ENVIRONMENT
    4. ARTEFACTS_S3_BUCKET
    5. API_VERSION
    6. CORE_VERSION
    7. Aws_CORE_MIRIS_CHECK_API_URL
    8. Aws_CORE_DB_INIT_ACCESS_URL (/db-init-access/api/v1) 
    9. EMAIL_SVC_URL
* Install LocalStack https://docs.localstack.cloud/getting-started/installation/
* Install awslocal https://docs.localstack.cloud/user-guide/integrations/aws-cli/#localstack-aws-cli-awslocal
* Start LocalStack
* Create the DynamoDB table
    ```shell
    awslocal dynamodb create-table --table-name Aws-table --attribute-definitions AttributeName=PK,AttributeType=S AttributeName=SK,AttributeType=S AttributeName=type,AttributeType=S AttributeName=status,AttributeType=S AttributeName=mirisDocId,AttributeType=S AttributeName=requestId,AttributeType=S AttributeName=batchStatus,AttributeType=S AttributeName=insertedDate,AttributeType=S --key-schema AttributeName=PK,KeyType=HASH AttributeName=SK,KeyType=RANGE --billing-mode PAY_PER_REQUEST --global-secondary-indexes '[ { \"IndexName\": \"GSI-Artefact-1\", \"KeySchema\": [ { \"AttributeName\": \"type\", \"KeyType\": \"HASH\"}, { \"AttributeName\": \"status\", \"KeyType\": \"RANGE\" } ], \"Projection\": { \"ProjectionType\": \"ALL\" } }, { \"IndexName\": \"GSI-Artefact-2\", \"KeySchema\": [ { \"AttributeName\": \"mirisDocId\", \"KeyType\": \"HASH\" }, { \"AttributeName\": \"type\", \"KeyType\": \"RANGE\" } ], \"Projection\": { \"ProjectionType\": \"ALL\" } }, { \"IndexName\": \"GSI-Artefact-3\", \"KeySchema\": [ { \"AttributeName\": \"requestId\", \"KeyType\": \"HASH\" }, { \"AttributeName\": \"type\", \"KeyType\": \"RANGE\" } ], \"Projection\": { \"ProjectionType\": \"ALL\" } }, { \"IndexName\": \"GSI-Artefact-4\", \"KeySchema\": [ { \"AttributeName\": \"type\", \"KeyType\": \"HASH\" }, { \"AttributeName\": \"batchStatus\", \"KeyType\": \"RANGE\" } ], \"Projection\": { \"ProjectionType\": \"ALL\" } } ,{ "IndexName": "GSI-Artefact-5", "KeySchema": [ { "AttributeName": "insertedDate", "KeyType": "HASH"}, { "AttributeName": "mirisDocId", "KeyType": "RANGE" } ], "Projection": { "ProjectionType": "ALL" } }]'
    ```
* Put SSM parameter for secret key that will be used to decrypt the AuthToken
    ```shell
    awslocal ssm put-parameter --name "JWTSecretKey"  --value "test"  --type String --tags "Key=tag-key,Value=tag-value"
    ```

* Replace the AwsServicesConfig class implementation with the one in AWSServicesTestConfig class. 
  The clients implementation is specific to AWS, LocalStack requires a configuration that is slightly different.
* Uncomment the spring.cloud.function.scan.packages, spring.cloud.function.web.path properties for the function 
* you want to execute
* Run the project
  ```shell
    mvn spring-boot:run
  ```
### 3. Run the unit tests
The unit tests use their own configuration to connect to LocalStack in AWSServicesTestConfig class. Apart from that, 
there is a class named AwsServicesSetup where the DynamoDB tables Aws-table is created programmatically and then 
populated with data that come from resources/dynamodb/items/Aws-table-batch-write-items.json file. 
Furthermore, an S3 bucket is created.
* Set the environment variables mentioned in section 2.
* Uncomment the spring.cloud.function.scan.packages, spring.cloud.function.web.path properties for the function.
you wish to test
  ```shell
  mvn test -Dtest=TestClassName
  ```

### 4. Build native image
  The process may take up to 12 minutes
  ```shell
  mvn spring-boot:build-image
  ```

### 5. Run native image locally
To run the project through the native image you must do what is described in section 2 with a slight difference 
regarding the environment variables. The fastest option for testing purposes would be to hardcode the environment 
variables in SystemEnvironmentVariables class.
  ```shell
  docker run --rm -p 8080:8080 docker.io/library/Aws-api:0.0.1-SNAPSHOT
  ```

### 6. AWS Commands 
* Use to find out if Aws-table is created successfully


  ```shell
awslocal dynamodb describe-table --table-name Aws-table
  ```
  
* Use to populate Aws-table with data


  ```shell
awslocal dynamodb batch-write-item --request-items file://Aws-table-batch-write-items.json
  ```

* Query Aws-table by mirisDocId


  ```shell 
awslocal dynamodb query --table-name Aws-table --index-name GSI-Artefact-2 --key-condition-expression "mirisDocId = :v1" --expression-attribute-values '{\":v1\":{\"S\": \"12232\"}}'
  ```