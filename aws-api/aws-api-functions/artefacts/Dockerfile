FROM public.ecr.aws/lambda/java:21

ENV AWS_REGION=eu-central-1
ENV AWS_DEFAULT_REGION=eu-central-1
ENV AWS_ACCESS_KEY_ID=test
ENV AWS_SECRET_ACCESS_KEY=test
ENV ARTEFACT_SERVICE_INTERFACE=DBACCESS
ENV Aws_CORE_DB_ACCESS_API_URL="http://dbaccess-1693477013.eu-central-1.elb.amazonaws.com"

# Copy function code and runtime dependencies from Maven layout
COPY target/classes ${LAMBDA_TASK_ROOT}
COPY target/dependency/* ${LAMBDA_TASK_ROOT}/lib/

# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
CMD [ "org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDownloadUrlByArtefactId" ]