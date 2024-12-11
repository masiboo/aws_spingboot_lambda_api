# We use a Java 12 image, but any image could serve as a base image.
FROM openjdk:17

# Add the lambda-runtime-interface-emulator to enable local testing.
ADD https://github.com/aws/aws-lambda-runtime-interface-emulator/releases/latest/download/aws-lambda-rie /usr/bin/aws-lambda-rie
RUN chmod +x /usr/bin/aws-lambda-rie

# Add the entrypoint script.
ADD container/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

# Add the JAR to a known path.
ENV JAR_DIR="/jar"
ADD target/* $JAR_DIR/

# Set our
CMD ["nl.p4c.lambdacontainers.handlers.StreamLambdaHandler::handleRequest"]
