# Makefile for deployment

APP_CONFIG := $(firstword $(MAKECMDGOALS))
$(eval $(APP_CONFIG):;@:)

PROJECT_NAME := $(shell cat $(APP_CONFIG) | jq -r '.Project.Name')
PROJECT_STAGE := $(shell cat $(APP_CONFIG) | jq -r '.Project.Stage')
PROFILE_NAME := $(shell cat $(APP_CONFIG) | jq -r '.Project.Profile')
VERSION := 0.5.0
HASH := $(shell git rev-parse --short HEAD)
BUILDNUMBER := $(HASH)

.PHONY: all configinfo install list deploy deploy_api_v1 deploy_api_v2 deploy_events deploy_auth

all: configinfo install list deploy

configinfo:
	@echo "==--------ConfigInfo---------=="
	@echo $(APP_CONFIG)
	@echo $(PROFILE_NAME)
	@echo .
	@echo .

install:
	@echo "==--------InstallCDKDependencies---------=="
	npm install
	@echo .
	@echo .

list:
	@echo "==--------ListStacks---------=="
	npx aws-cdk list
	@echo .
	@echo .

deploy: deploy_api_v1 deploy_api_v2 deploy_events deploy_auth

deploy-api: configinfo install list deploy deploy_api_v1 deploy_api_v2 deploy_events

deploy_api_v1:
	@echo "==--------Deploying AwsApiV1Stack---------=="
ifneq ($(PROFILE_NAME),)
	npx aws-cdk deploy Aws*AwsApiV1Stack --require-approval never --profile $(PROFILE_NAME)
else
	npx aws-cdk deploy Aws*AwsApiV1Stack --require-approval never
endif

deploy_api_v2:
	@echo "==--------Deploying AwsApiV2Stack---------=="
ifneq ($(PROFILE_NAME),)
	npx aws-cdk deploy Aws*AwsApiV2Stack --require-approval never --profile $(PROFILE_NAME)
else
	npx aws-cdk deploy Aws*AwsApiV2Stack --require-approval never
endif

deploy_events:
	@echo "==--------Deploying AwsEventsStack---------=="
ifneq ($(PROFILE_NAME),)
	npx aws-cdk deploy Aws*AwsEventsStack --require-approval never --profile $(PROFILE_NAME)
else
	npx aws-cdk deploy Aws*AwsEventsStack --require-approval never
endif

deploy_auth:
	@echo "==--------Deploying AwsAuthTriggerStack---------=="
ifneq ($(PROFILE_NAME),)
	npx aws-cdk deploy Aws*AwsAuthTriggerStack --require-approval never --profile $(PROFILE_NAME)
else
	npx aws-cdk deploy Aws*AwsAuthTriggerStack --require-approval never
endif
	@echo .
	@echo .

