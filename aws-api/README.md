# Infrastructure

### Deployment

Deployment is done using [AWS CDK](https://aws.amazon.com/cdk/).
Full instructions are in the cdk dedicated directory

`deployment/templates/iac/aws-api-prod-ts`

[Readme is here](deployment/templates/iac/moss-api-prod-ts/README.md)

# Development

Make sure Docker, AWS and SAM CLI are installed. this should already be the case in AWS Workspace

Make sure you can run docker as the logged in user

Make sure Java is at 17 or higher

Make sure Maven is installed and it can be run

Make sure NPM and Node is installed (LTS version only)

Make sure NPM is set to AWS's registry `npm set registry https://intranet.iprosfot.int/nexus/repository/aws-npm-central/`

## Developer : Linux build and install

Make sure you ENVIRONMENT variables are set


Run the build script 

`./build.sh`

Make sure you can run a local build instance - this uses SAM CLI

`./provision-local-infrastructure.sh`

Provision AWS by running this script

`./provision-aws-infrastructure.sh`

At the end of the day - make sure to clean up any provisioned resources in AWS

`/cleanup-aws.sh`


## Windows Install

### DockerBuild

`docker build -f ./docker/Dockerfile.jre --progress=plain -t lambda-custom-runtime-minimal-jre-18-x86 .`

`docker run --rm --entrypoint cat lambda-custom-runtime-minimal-jre-18-x86 runtime.zip | Set-Content -Path ./iac/runtime.zip`

### Local testing

set environment variables 

for Powershell

`$Env:developer="donmadrid"`
`$Env:buildnumber="5767"`

CD into the IAC directory

`cd .\iac\aws-api-java\`

Run synth - which creates a cloudformation template.
Deploy the AWS infrastructure via AWS CDK and store the outputs in a file

`npx cdk synth --no-staging`

Test the Amazon API Gateway endpoint - We should see a "successful" message

`sam local invoke HealthCheckFunction --no-event -t ./cdk.out/AwsApiJavaStack-dev-$(developer)-$(buildnumber).template.json`

## Serverless GraalVM Demo

## Requirements

- [AWS CLI](https://aws.amazon.com/cli/)
- [AWS CDK](https://aws.amazon.com/cdk/)
- Java 11
- Maven
- [Artillery](https://www.artillery.io/) for load-testing the application

## Software

Within the software folder is the products maven project. This single maven project contains all the code for all four
Lambda functions. It uses the hexagonal architecture pattern to decouple the entry points, from the main domain logic
and the storage logic.

### Custom Runtime

The GraalVM native-image tool will produce a stand-alone executable binary. This does not require the JVM to run. To run
our application on Lambda we must make
a [custom runtime](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html)
and implement the [Lambda Runtime API](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html). This is done by
including the `aws-lambda-java-runtime-interface-client` dependency in our project.
The [maven assembly build plugin](https://github.com/aws-samples/serverless-graalvm-demo/blob/main/software/products/src/assembly/zip.xml)
is used to create a zip file which includes the executable binary as well as the entry
point [bootstrap](https://github.com/aws-samples/serverless-graalvm-demo/blob/main/software/products/src/main/config/bootstrap)
file.

<p align="center">
  <img src="imgs/execution-environment.png" alt="AWS Lambda execution environment"/>
</p>

## Run form CLI

`java -jar aws-api-0.2.0-SNAPSHOT-aws.jar --spring.profiles.active=prod`  

Or by Maven

`mvn spring-boot:run -Dspring-boot.run.profiles=prod
`

## Load Test

[Artillery](https://www.artillery.io/) is used to make 300 requests / second for 10 minutes to our API endpoints. You
can run this with the following command.

```bash
cd load-test
./run-load-test.sh
```

This is a demanding load test, to change the rate alter the `arrivalRate` value in `load-test.yml`.

### CloudWatch Logs Insights

Using this CloudWatch Logs Insights query you can analyse the latency of the requests made to the Lambda functions.

The query separates cold starts from other requests and then gives you p50, p90 and p99 percentiles.

```
filter @type="REPORT"
| fields greatest(@initDuration, 0) + @duration as duration, ispresent(@initDuration) as coldStart
| stats count(*) as count, pct(duration, 50) as p50, pct(duration, 90) as p90, pct(duration, 99) as p99, max(duration) as max by coldStart
```

<p align="center">
  <img src="imgs/performance_results.png" alt="CloudWatch Logs Insights results"/>
</p>

## AWS X-Ray Tracing

You can add additional detail to your X-Ray tracing by adding a TracingInterceptor to your AWS SDK clients. Here is the
code for my DynamoDbClient from
the [DynamoDbProductStore](https://github.com/aws-samples/serverless-graalvm-demo/blob/aws-xray-support/software/products/src/main/java/software/amazonaws/example/product/store/dynamodb/DynamoDbProductStore.java)
class.

```java
private final DynamoDbClient dynamoDbClient=DynamoDbClient.builder()
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
        .overrideConfiguration(ClientOverrideConfiguration.builder()
        .addExecutionInterceptor(new TracingInterceptor())
        .build())
        .build();
```

Example cold start trace

<p align="center">
  <img src="imgs/xray-cold.png" alt="Cold start X-Ray trace"/>
</p>

Example warm start trace

<p align="center">
  <img src="imgs/xray-warm.png" alt="Warm start X-Ray trace"/>
</p>

## 👀 With other languages

You can find implementations of this project in other languages here:

* [🦀 Rust](https://github.com/aws-samples/serverless-rust-demo)
* [🏗️ TypeScript](https://github.com/aws-samples/serverless-typescript-demo)
* [🐿️ Go](https://github.com/aws-samples/serverless-go-demo)
* [⭐ Groovy](https://github.com/aws-samples/serverless-groovy-demo)
* [🤖 Kotlin](https://github.com/aws-samples/serverless-kotlin-demo)
* [🥅 .NET](https://github.com/aws-samples/serverless-dotnet-demo)

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

