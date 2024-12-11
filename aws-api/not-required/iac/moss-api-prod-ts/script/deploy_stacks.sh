#!/bin/sh

# Configuration File Path
export APP_CONFIG=$1

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo

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
cdk list
echo .
echo .

echo ==--------DeployStacksStepByStep---------==
if [ -z "$PROFILE_NAME" ]; then
    cdk deploy Awsdev-AwsCoreCfnVpcStack --require-approval never
    cdk deploy Awsdev-AwsCoreCfnVpcStack --require-approval never
    cdk deploy Awsdev-AwsCoreEcsStack --require-approval never

else
    cdk deploy Awsdev-AwsCoreCfnVpcStack --require-approval never --profile $PROFILE_NAME
    cdk deploy Awsdev-AwsCoreRdsStack --require-approval never --profile $PROFILE_NAME
    cdk deploy Awsdev-AwsCoreEcsStack --require-approval never --profile $PROFILE_NAME

fi
echo .
echo .

