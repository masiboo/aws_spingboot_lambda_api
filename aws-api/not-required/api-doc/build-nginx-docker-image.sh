#!/bin/sh
set -e

# Enable build kit
export DOCKER_BUILDKIT=1

## Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

# Get the latest openapi
cp openapi.yaml nginx-host-docker/src

# Build and export the build files
docker build -f ./nginx-host-docker/Dockerfile.build -t wipo-Aws-api-docs:latest --output type=local,dest=$(pwd)/deployment/ ./nginx-host-docker