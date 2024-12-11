#!/bin/bash

set -e

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    --config)
      APP_CONFIG="$2"
      shift # past argument
      shift # past value
      ;;
    --profile)
      PROFILE_NAME="$2"
      shift # past argument
      shift # past value
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

# Check if APP_CONFIG is provided
if [ -z "$APP_CONFIG" ]; then
  echo "Error: --config argument is required"
  exit 1
fi

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev

# Use PROFILE_NAME from command line if provided, otherwise use from config
if [ -z "$PROFILE_NAME" ]; then
  PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
fi

export APP_CONFIG=$APP_CONFIG

# read from cicd-package.json
VERSION="0.9.3"
HASH=$(git rev-parse --short HEAD)

BUILDNUMBER="$HASH"
echo "$BUILDNUMBER"

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
    npx aws-cdk deploy Aws*AwsUsEastCertificateStack --require-approval never
    npx aws-cdk deploy Aws*AwsFrontEndStack --require-approval never
#   npx aws-cdk deploy Aws*AwsApiDocsStack --require-approval never

else
#    npx aws-cdk deploy Aws*AwsCfnWafCdnStack --require-approval never --profile $PROFILE_NAME
#    npx aws-cdk deploy Aws*AwsUsEastCertificateStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsFrontEndStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiDocsStack --require-approval never --profile $PROFILE_NAME
#    npx aws-cdk deploy Aws*AwsApiCertStack --require-approval never --profile $PROFILE_NAME

fi
echo .
echo .

