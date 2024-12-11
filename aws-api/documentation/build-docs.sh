#!/bin/sh
set -e

## Navigate back into the projects root directory
PROJECT_ROOT_DIRECTORY=$(pwd)
#cd $PROJECT_ROOT_DIRECTORY

echo "Aws Docs Builder"
echo .
cp ../deployment/artifacts/openapi.yaml docs-source/app/Aws-api-v1/openapi.yaml
cd ./docs-source
echo "---documentation build start---"
echo $PROJECT_ROOT_DIRECTORY
docker build -t docusaurus:latest .
cd -
echo "---documentation build end---"
docker run --rm --entrypoint cat  docusaurus:latest /usr/share/nginx/html/doc.zip > ../deployment/artifacts/lambda/doc.zip

echo "---------END BUILD ----------"

