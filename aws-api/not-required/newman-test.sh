#!/bin/sh

# Projects root directory location
PROJECT_ROOT_DIRECTORY=$(pwd)

echo $PROJECT_ROOT_DIRECTORY

# set collection filepath and variable name
COLLECTION_FILE_PATH=$PROJECT_ROOT_DIRECTORY/software/artefacts/src/test/postman-collection/Aws-api.postman_collection.json
KEY_NAME="baseUrl"
NEW_VALUE="https://p8ynixv569.execute-api.eu-central-1.amazonaws.com/{{basePath}}"

echo $collection_file_path

npx jq ".variable |= map(if .key == \"$KEY_NAME\" then .value = \"$NEW_VALUE\" else . end)" "$COLLECTION_FILE_PATH" > tmp && mv tmp "$COLLECTION_FILE_PATH"

npx newman run $COLLECTION_FILE_PATH