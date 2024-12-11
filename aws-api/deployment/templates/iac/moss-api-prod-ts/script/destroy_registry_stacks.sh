#!/bin/bash

set -e
# Configuration File Path
export APP_CONFIG=$1

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
#PROFILE_NAME="madrid-dev-devops"

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

    npx aws-cdk destroy Aws*AwsInfraRegistryStack --force
    npx aws-cdk destroy Aws*AwsEventsStack --force

else

    npx aws-cdk destroy Aws*AwsInfraRegistryStack --profile $PROFILE_NAME
#    npx aws-cdk destroy Aws*AwsEventsStack --profile $PROFILE_NAME


fi
echo .
echo .

