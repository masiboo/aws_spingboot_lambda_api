FROM public.ecr.aws/lambda/java:21

ENV AWS_REGION=eu-central-1
ENV AWS_DEFAULT_REGION=eu-central-1
ENV AWS_ACCESS_KEY_ID=test
ENV AWS_SECRET_ACCESS_KEY=test
ENV ARTEFACT_SERVICE_INTERFACE=DBACCESS
ENV Aws_CORE_DB_INIT_ACCESS_URL="http://dbapp/"

ENV spring_cloud_function_definition=getArtefactsByMirisDocId
ENV MAIN_CLASS="org.iprosoft.trademarks.Aws.artefacts.AwsApiApplication"
#ENV APP_ENVIRONMENT=`${stage}`,
#ENV API_VERSION=`${version}-${buildNumber}`,
#ENV CORE_VERSION=`${version}-${buildNumber}`,
#ENV Aws_API_SIGNED_URL_NAME=signedUrlName,
#ENV BATCH_S3_REPORTS_BUCKET=reportsBucket.bucketName,
ENV ARTEFACT_SERVICE_INTERFACE: "DBACCESS"

#COPY . .
#RUN ./mvnw spring-javaformat:apply
#RUN ./mvnw compile dependency:copy-dependencies -DincludeScope=runtime

# Copy function code and runtime dependencies from Maven layout
COPY target/classes ${LAMBDA_TASK_ROOT}
COPY target/dependency/* ${LAMBDA_TASK_ROOT}/lib/

# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
CMD [ "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest" ]