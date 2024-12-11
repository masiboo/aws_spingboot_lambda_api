#!/bin/bash

set -e

export APP_CONFIG=$1

PROJECT_NAME=$(cat $APP_CONFIG | jq -r '.Project.Name') #ex> IoTData
PROJECT_STAGE=$(cat $APP_CONFIG | jq -r '.Project.Stage') #ex> Dev
PROFILE_NAME=$(cat $APP_CONFIG | jq -r '.Project.Profile') #ex> cdk-demo
ACCOUNT=$(cat $APP_CONFIG | jq -r '.Project.Account')
REGION=$(cat $APP_CONFIG | jq -r '.Project.Region')
PROJECT_PREFIX=$PROJECT_NAME

pwd

echo .
echo $PROFILE_NAME

echo .
echo .
echo "ecr to be deleted"

ecs_tasks=$(aws ecs list-task-definitions --profile $PROFILE_NAME --output json| jq -M -r '.taskDefinitionArns | .[]' | grep Aws)
echo $ecs_tasks

read -p "Delete? y/n: " y_var
echo $y_var

if [[ "$y_var" == "y" ]]
then
  echo "deleting.."
  deleted_task=$(aws ecs list-task-definitions --profile $PROFILE_NAME --output json \
  | jq -M -r '.taskDefinitionArns | .[]' | grep Aws | xargs -I{} aws ecs deregister-task-definition \
  --region $REGION \
  --task-definition {} \
  --profile $PROFILE_NAME)
fi
