#!/bin/sh
set -e

## Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)
#
### Begin Clean Up
#cd $PROJECT_ROOT_DIRECTORY/iac
#
### Remove a previously created custom runtime
#file="runtime.zip"
#if [ -f "$file" ] ; then
#    rm "$file"
#fi
#
#
## Navigate into the infrastructure sub-directory
#cd $PROJECT_ROOT_DIRECTORY/iac/Aws-api-java
#
#mvn clean
#
#rm -rf cdk.out
#
#cd $PROJECT_ROOT_DIRECTORY/software/artefacts
#
#mvn clean

## Begiin Build
#
#cd $PROJECT_ROOT_DIRECTORY/iac
#
### Remove a previously created custom runtime
#file="runtime.zip"
#if [ -f "$file" ] ; then
#    rm "$file"
#fi

## Navigate back into the projects root directory
cd $PROJECT_ROOT_DIRECTORY

# Build the custom Java runtime from the Dockerfile
docker build -f ./Dockerfile.artefact.native --progress=plain -t lambda-native-runtime-x86 .

# Extract the runtime.zip from the Docker environment and store it locally
docker run --rm --entrypoint cat lambda-native-runtime-x86 runtime.zip > ../deployment/artifacts/lambda/runtime_native.zip

# build v2 project:
cd $PROJECT_ROOT_DIRECTORY

#mvn spring-javaformat:apply
#
#mvn clean -DskipTests package
#
#cp target/Aws-api-*-SNAPSHOT-aws.jar  $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/runtimev2.jar

# Build the custom Java runtime from the Dockerfile
docker build -f ./Dockerfile.al2.native --progress=plain -t awssample:latest .

# Extract the runtime.zip from the Docker environment and store it locally
# docker run --rm --entrypoint cat awssample:latest runtime.zip > ../deployment/artifacts/lambda/runtime.zip

docker run --rm --entrypoint cat awssample:latest runtime.zip > ../../deployment/templates/iac/custom-runtime/lambda/runtime2.zip


# build stream enricher

cd $PROJECT_ROOT_DIRECTORY/streamTargetTs

npm install
npm run build
npm run zip
#zip -r $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/stream-deployment .
cp dist/streamdeploy.zip $PROJECT_ROOT_DIRECTORY/../deployment/artifacts/lambda/

echo .
echo .

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

