#!/bin/sh
set -e

if [[ -z "${BUILDNUMBER}" ]]; then
  echo " set BUILDNUMBER "
  exit 0
else
  BUILDNUMBER="${BUILDNUMBER}"
fi

if [[ -z "${DEVELOPER}" ]]; then
  echo " set DEVELOPER "
  exit 0
else
  DEVELOPER="${DEVELOPER}"
fi

export buildnumber=$BUILDNUMBER
export developer=$DEVELOPER

# Remember the projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

cd $PROJECT_ROOT_DIRECTORY/iac
# ## Remove a previously created custom runtime
 file="runtime.zip"
 if [ -f "$file" ] ; then
     rm "$file"
 fi

cd $PROJECT_ROOT_DIRECTORY/iac/Aws-api-dev-ts

# # Navigate into the infrastructure sub-directory

#npx cdk destroy AwsApiJavaStack-dev-$DEVELOPER-$BUILDNUMBER
npx cdk destroy --all

# Navigate back into the projects root directory
cd $PROJECT_ROOT_DIRECTORY
