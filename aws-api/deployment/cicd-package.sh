#!/bin/bash
zip -r package.zip artifacts templates cicd-package.json

#git archive -o package.zip HEAD
#zip -ur ./package.zip ./artifacts/lambda/runtime.jar
#zip -ur ./package.zip ./artifacts/lambda/runtimev2.jar
#zip -ur ./package.zip ./artifacts/lambda/runtimeV2.zip
#
#zip -ur ./package.zip ./artifacts/lambda/streamdeploy.zip
#zip -ur ./package.zip ./artifacts/lambda/artefactdeploy.zip
#
#zip -ur ./package.zip ./artifacts/lambda/service-connect.zip
#zip -ur ./package.zip ./artifacts/lambda/artefact-sqs.zip
#
#zip -ur ./package.zip ./artifacts/lambda/doc.zip
