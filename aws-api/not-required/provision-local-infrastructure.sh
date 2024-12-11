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

# Deploy the AWS infrastructure via AWS CDK and store the outputs in a file
#npx cdk deploy --outputs-file target/outputs.json

npx cdk synth --no-staging

# Test the Amazon API Gateway endpoint - We should see a "successful" message
#curl -XPOST $(cat target/outputs.json | jq -r '.LambdaCustomRuntimeMinimalJRE18InfrastructureStack.apiendpoint')/custom-runtime
sam local invoke HealthCheckFunction --no-event -t ./cdk.out/AwsApiDevTsStack.template.json

# Navigate back into the projects root directory
cd $(pwd)
