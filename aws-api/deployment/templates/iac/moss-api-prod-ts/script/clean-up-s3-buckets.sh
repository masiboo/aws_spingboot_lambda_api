#!/bin/bash

set -e

export APP_CONFIG=$1
export S3_KEY=$2

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
ACCOUNT=$(cat $APP_CONFIG | jq -r '.Project.Account')
REGION=$(cat $APP_CONFIG | jq -r '.Project.Region')
PROJECT_PREFIX=$PROJECT_NAME

pwd

echo "S3 Key"
echo $S3_KEY
echo .
echo "Profile Name"
echo $PROFILE_NAME

echo .
echo .
echo "buckets to be deleted"

buckets=$(aws s3 ls --profile $PROFILE_NAME | grep $S3_KEY | awk '{print $3}')
echo $buckets

read -p "Delete? y/n: " y_var
echo $y_var

if [[ "$y_var" == "y" ]]
then
  echo "deleting.."
  deletedbuckets=$(aws s3 ls --profile $PROFILE_NAME | grep $S3_KEY | awk '{print $3}' | xargs -I{} aws s3 rb s3://{} --force --profile $PROFILE_NAME)
fi
