#!/bin/sh
set -e

# Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

cd $PROJECT_ROOT_DIRECTORY/iac

## Remove a previously created custom runtime
file="runtime.zip"
if [ -f "$file" ] ; then
    rm "$file"
fi


# Navigate into the infrastructure sub-directory
cd $PROJECT_ROOT_DIRECTORY/iac/Aws-api-java

mvn clean

rm -rf cdk.out

cd $PROJECT_ROOT_DIRECTORY/software/artefacts

mvn clean


# Navigate back into the projects root directory
cd $PROJECT_ROOT_DIRECTORY
