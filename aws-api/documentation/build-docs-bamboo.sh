#!/bin/sh
set -e

## Navigate back into the projects root directory
PROJECT_ROOT_DIRECTORY=$(pwd)
#cd $PROJECT_ROOT_DIRECTORY

echo "Aws Docs Builder"
echo .
cp ../deployment/artifacts/openapi.yaml docs-source/app/Aws-api-v1/openapi.yaml
cd ./docs-source/app
echo "---documentation build start---"
echo $PROJECT_ROOT_DIRECTORY
# docker build -t docusaurus:latest .

npm config set registry https://intranet.wipo.int/nexus/repository/wipo-npm-central/
npm config set ca ""
npm config set strict-ssl false
export NODE_TLS_REJECT_UNAUTHORIZED=0

yarn config set registry https://intranet.wipo.int/nexus/repository/wipo-npm-central/
yarn config set ca ""
yarn config set strict-ssl false
export NODE_TLS_REJECT_UNAUTHORIZED=0

# Reduce npm log spam and colour during install within Docker
NPM_CONFIG_LOGLEVEL=warn
NPM_CONFIG_COLOR=false

yarn
yarn clean-api-all
yarn gen-api-all

npm run build
cd build && zip -r doc.zip *
mv doc.zip ../../../../deployment/artifacts/lambda/doc.zip

cd -
echo "---documentation build end---"
echo "---------END BUILD ----------"

