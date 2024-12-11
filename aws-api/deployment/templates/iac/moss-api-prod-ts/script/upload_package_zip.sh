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

ACCOUNT=$(cat $APP_CONFIG | jq -r '.Project.Account')
REGION=$(cat $APP_CONFIG | jq -r '.Project.Region')
PROJECT_PREFIX=$PROJECT_NAME

echo "profile name"
echo $PROFILE_NAME

pwd

cd deployment
rm -rf package.zip && rm -rf artifacts/lambda/runtime.zip

cd -

cd Aws-api-functions
sh build.sh
#sh build_native.sh

cd -

cd documentation
echo "---documentation build script---"
sh build-docs-bamboo.sh

cd -

echo "---deploying to s3---"
echo .
echo .
cd deployment
sh cicd-package.sh && aws s3 cp package.zip s3://$PROJECT_PREFIX-artifacts-$REGION-$ACCOUNT/package.zip --profile $PROFILE_NAME

echo "--- END ----"
echo .
echo .