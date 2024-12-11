#!/bin/sh

# Remember the projects root directory location
CURRENT_DIRECTORY=$(pwd)

echo $CURRENT_DIRECTORY

# set collection filepath and variable name
COLLECTION_FILE_PATH=$CURRENT_DIRECTORY/Aws-api.postman_collection.json
KEY_NAME="baseUrl"
NEW_VALUE=$1

echo $COLLECTION_FILE_PATH

npx jq ".variable |= map(if .key == \"$KEY_NAME\" then .value = \"$NEW_VALUE\" else . end)" "$COLLECTION_FILE_PATH" > tmp && mv tmp "$COLLECTION_FILE_PATH"

#Execute only for the request that has test cases
npx newman run $COLLECTION_FILE_PATH
