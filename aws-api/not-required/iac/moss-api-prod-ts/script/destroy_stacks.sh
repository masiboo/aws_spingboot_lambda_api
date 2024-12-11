#!/bin/sh

# Configuration File Path
export APP_CONFIG=$1

PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo

echo ==--------ConfigInfo---------==
echo $APP_CONFIG
echo $PROFILE_NAME
echo .
echo .

echo ==--------ListStacks---------==
cdk list
echo .
echo .

echo ==--------DestroyStacksStepByStep---------==
if [ -z "$PROFILE_NAME" ]; then
    cdk destroy *-AwsCoreVpcEcsStack --force

    cdk destroy *-AwsCoreVpcRdsStack --force
    cdk destroy *-AwsCoreCfnVpcStack --force
else
    cdk destroy *-AwsCoreVpcEcsStack --force --profile $PROFILE_NAME

    cdk destroy *-AwsCoreVpcRdsStack --force --profile $PROFILE_NAME
    cdk destroy *-AwsCoreCfnVpcStack --force --profile $PROFILE_NAME
fi
echo .
echo .
