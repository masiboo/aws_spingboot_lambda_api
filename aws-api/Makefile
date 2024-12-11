

APP_CONFIG := $(firstword $(MAKECMDGOALS))
$(eval $(APP_CONFIG):;@:)

PROJECT_NAME := $(shell cat $(APP_CONFIG) | jq -r '.Project.Name')
PROJECT_STAGE := $(shell cat $(APP_CONFIG) | jq -r '.Project.Stage')
PROFILE_NAME := $(shell cat $(APP_CONFIG) | jq -r '.Project.Profile')
ACCOUNT := $(shell cat $(APP_CONFIG) | jq -r '.Project.Account')
REGION := $(shell cat $(APP_CONFIG) | jq -r '.Project.Region')
PROJECT_PREFIX := $(PROJECT_NAME)

.PHONY: all clean build-docs build-codes deploy

all: clean build-docs build-codes deploy

codes: clean build-docs build-codes

clean:
	rm -rf deployment/package.zip deployment/artifacts/lambda/runtime.zip

build-docs:
	@echo "---documentation build script---"
	@cd documentation && sh build-docs-bamboo.sh && cd -

build-codes:
	@cd Aws-api-functions && sh build.sh && cd -

deploy:
	@echo "---deploying to s3---"
	@cd deployment && sh cicd-package.sh && aws s3 cp package.zip s3://$(PROJECT_PREFIX)-artifacts-$(REGION)-$(ACCOUNT)/package.zip --profile $(PROFILE_NAME) && cd -

