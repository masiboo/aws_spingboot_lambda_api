#!/bin/sh
set -e

## Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

mkdir -p  $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda

## Navigate back into the projects root directory
cd $PROJECT_ROOT_DIRECTORY

# Build the custom Java runtime from the Dockerfile
echo .
echo "Build the custom Java runtime"
echo .
echo .


cd artefacts
mvn clean package -DskipTests
cp target/artefact.jar  $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/runtime.jar
cd ..
#docker build -f ./Dockerfile.jre --progress=plain -t lambda-custom-runtime-minimal-jre-17-x86 .

# Extract the runtime.zip from the Docker environment and store it locally
#docker run --rm --entrypoint cat lambda-custom-runtime-minimal-jre-17-x86 runtime.zip > ../deployment/artifacts/lambda/runtime.zip

# build v2 project:

echo .
echo "build v2 project"
echo .
echo .
cd $PROJECT_ROOT_DIRECTORY/Aws-api
mvn spring-javaformat:apply
mvn clean -DskipTests package
cp target/Aws-api-*-SNAPSHOT-aws.jar  $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/runtimev2.jar

#echo .
#echo "build v2 graal zip"
#echo .
#echo .
#cd $PROJECT_ROOT_DIRECTORY
#
#echo Build the custom Java runtime from the Dockerfile
#docker build -f ./Dockerfile.al2.native --progress=plain -t amazonlinux-graalvm:latest .

#echo extract the runtime.zip from the Docker environment and store it locally
#docker run --rm --entrypoint cat amazonlinux-graalvm:latest runtime.zip > ../deployment/artifacts/lambda/runtimeV2.zip
#echo done

# build stream enricher
echo .
echo "build stream enricher"
echo .
echo .
cd $PROJECT_ROOT_DIRECTORY/streamTargetTs

npm install
npm run build
npm run zip
#zip -r $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/stream-deployment .
cp dist/streamdeploy.zip $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/

# artefact-sqs handler (typescript)
npm run build:artefact
npm run zip:artefact
#zip -r $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/stream-deployment .
cp dist/artefactdeploy.zip $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/

# python codes
echo .
echo "service connect handler"
echo .
echo .
cd $PROJECT_ROOT_DIRECTORY/service-connect-handler
rm -rf package
mkdir package
pip3 install --target ./package -r requirements.txt
cd package
zip -r $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/service-connect.zip .
cd ..
zip $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/service-connect.zip index.py


echo "new artefact sqs handler"
echo .
cd $PROJECT_ROOT_DIRECTORY/artefact-sqs-handler
rm -rf package
mkdir package
pip3 install --target ./package -r requirements.txt
cd package
zip -r $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/artefact-sqs.zip .
cd ..
zip $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/artefact-sqs.zip check_status.py
