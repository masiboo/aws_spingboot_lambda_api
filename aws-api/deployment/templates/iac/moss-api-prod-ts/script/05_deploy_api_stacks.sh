#!/bin/bash

# Deploy All stacks with no Microservices
# Configuration File Path
export APP_CONFIG=$1

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
#PROFILE_NAME="madrid-acc-devops"

VERSION="0.5.0"
HASH=$(git rev-parse --short HEAD)

BUILDNUMBER="$HASH"
echo "$BUILDNUMBER"

HASH=$(git rev-parse --short HEAD)
echo "$HASH"
export BUILDNUMBER=$HASH

if [[ -z "${PROFILE_NAME}" ]]; then
  echo $PROFILE_NAME
  echo "null"
else
  echo $PROFILE_NAME
  echo "value"
fi

echo ==--------ConfigInfo---------==
echo $APP_CONFIG
echo $PROFILE_NAME
echo .
echo .

echo ==--------InstallCDKDependencies---------==
npm install
echo .
echo .

echo ==--------ListStacks---------==
npx aws-cdk list
echo .
echo .

echo ==--------DeployStacksStepByStep---------==
if [ -z "${PROFILE_NAME}" ]; then

#   npx aws-cdk deploy Aws*AwsAuthStack --require-approval never
    npx aws-cdk deploy Aws*AwsApiV1Stack --require-approval never
    npx aws-cdk deploy Aws*AwsApiV2Stack --require-approval never
#   npx aws-cdk deploy Aws*AwsApiDocsStack --require-approval never


else

    npx aws-cdk deploy Aws*AwsAuthStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiV1Stack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiV2Stack --require-approval never --profile $PROFILE_NAME
#   npx aws-cdk deploy Aws*AwsApiDocsStack --require-approval never --profile $PROFILE_NAME


fi
echo .
echo .

