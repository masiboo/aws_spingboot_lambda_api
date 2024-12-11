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
    npx aws-cdk deploy Aws*AwsCfnPreRequisiteStack --require-approval never
    npx aws-cdk deploy Aws*AwsCfnInfraVpcStack --require-approval never
    npx aws-cdk deploy Aws*AwsInfraRdsStack --require-approval never
    npx aws-cdk deploy Aws*AwsInfraCfnBucketsECRStack --require-approval never

    npx aws-cdk deploy Aws*AwsInfraRegistryStack --require-approval never
    npx aws-cdk deploy Aws*AwsCfnWafCdnStack --require-approval never

    npx aws-cdk deploy Aws*AwsUsEastCertificateStack --require-approval never
#    npx aws-cdk deploy Aws*AwsFrontEndStack --require-approval never
#
#    npx aws-cdk deploy Aws*AwsApiStack --require-approval never


else
  # Core + Network, Ops Bucket + ECR
    npx aws-cdk deploy Aws*AwsCfnPreRequisiteStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsCfnInfraVpcStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsInfraRdsStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsInfraCfnBucketsECRStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsCfnWafCdnStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsCoreAlbServiceStack --require-approval never --profile $PROFILE_NAME

#  # Registry + DB
    npx aws-cdk deploy Aws*AwsInfraRegistryStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsInfraECSClusterStack --require-approval never --profile $PROFILE_NAME
#    npx aws-cdk deploy Aws*AwsCoreDBAccessStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsCoreMediaProcessStack --require-approval never --profile $PROFILE_NAME

#    npx aws-cdk deploy Aws*AwsCoreMirisProxyStack --require-approval never --profile $PROFILE_NAME
#    npx aws-cdk deploy Aws*AwsCoreEmailSvctack --require-approval never --profile $PROFILE_NAME

    npx aws-cdk deploy Aws*AwsAuthStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiV1Stack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiV2Stack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsEventsStack --require-approval never --profile $PROFILE_NAME

    npx aws-cdk deploy Aws*AwsUsEastCertificateStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsFrontEndStack --require-approval never --profile $PROFILE_NAME
    npx aws-cdk deploy Aws*AwsApiDocsStack --require-approval never --profile $PROFILE_NAME

fi
echo .
echo .

