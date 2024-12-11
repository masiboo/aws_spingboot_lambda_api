#!/bin/sh
set -e

if [[ -z "${BUILDNUMBER}" ]]; then
  echo " set BUILDNUMBER "
  exit 0
else
  BUILDNUMBER="${BUILDNUMBER}"
fi

if [[ -z "${DEVELOPER}" ]]; then
  echo " set DEVELOPER "
  exit 0
else
  DEVELOPER="${DEVELOPER}"
fi

export buildnumber=$BUILDNUMBER
export developer=$DEVELOPER

# Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

# Navigate into the infrastructure sub-directory
cd iac/Aws-api-dev-ts

npx cdk list

npx cdk bootstrap

# Deploy the AWS infrastructure via AWS CDK and store the outputs in a file
npx cdk deploy --outputs-file target/outputs.json --require-approval never


# Test the Amazon API Gateway endpoint - We should see a "successful" message
# curl -XPOST $(cat target/outputs.json | jq -r '.AwsApiJavaStack.apiendpoint')/custom-runtime

# Navigate back into the projects root directory
cd $(pwd)
