#!/bin/bash

# Configuration File Path
export APP_CONFIG=$1

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
#PROFILE_NAME="madrid-dev-devops"

echo ==--------ConfigInfo---------==
echo $APP_CONFIG
echo $PROFILE_NAME
echo .
echo .

echo ==--------ListStacks---------==
npx aws-cdk list
echo .
echo .

echo ==--------DestroyStacksStepByStep---------==
if [ -z "$PROFILE_NAME" ]; then

    npx aws-cdk destroy *-AwsApiDocsStack --force

else


#        npx aws-cdk destroy *-AwsFrontEndStack --force  --profile $PROFILE_NAME
#        npx aws-cdk destroy *-AwsUsEastCertificateStack --force --profile $PROFILE_NAME
        npx aws-cdk destroy *-AwsApiDocsStack --force --profile $PROFILE_NAME
fi
echo .
echo .
