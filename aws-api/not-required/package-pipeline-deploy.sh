#!/bin/bash

export APP_CONFIG=$1

# write version info to dist file
VERSION="0.1.2"
HASH=$(git rev-parse --short HEAD)

echo "$VERSION $HASH"

jq -n --arg v "$VERSION $HASH" '{"version":$v}' > version.json

echo .
echo .


echo .
echo .

# create zip archive
git archive -o ../Aws-api-develop.zip HEAD


echo .
echo .

# update zip with dist folder
zip -ur ../Aws-api-develop.zip ./iac/runtime.zip

echo .
echo .

# push to s3 with aws-cli
aws s3 cp ../Aws-api-develop.zip s3://Aws-cicd-dev-bucket/Aws-api-develop.zip

echo .
echo .

cd iac/Aws-api-prod-ts/

if [ -z "$2" ]
  then
    echo "No argument supplied"
  else
  echo ..
  sh script/deploy_pipeline_stack.sh $APP_CONFIG
fi

echo ...
echo .

cd -
# clean up


