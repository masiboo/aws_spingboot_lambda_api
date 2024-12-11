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
echo .
echo $PROFILE_NAME

echo .
echo .
echo "ecr to be deleted"

#buckets=$(aws s3 ls --profile $PROFILE_NAME | grep $S3_KEY | awk '{print $3}')
ecr=$(aws ecr describe-repositories --profile $PROFILE_NAME | jq '.repositories[] .repositoryName' | grep Aws/ )
echo $ecr

read -p "Delete? y/n: " y_var
echo $y_var

if [[ "$y_var" == "y" ]]
then
  echo "deleting.."
  deletedbuckets=$(aws ecr describe-repositories --profile $PROFILE_NAME | jq '.repositories[] .repositoryName' | grep Aws/ | xargs -I{} aws ecr delete-repository --repository-name {} --force --profile $PROFILE_NAME)
fi
