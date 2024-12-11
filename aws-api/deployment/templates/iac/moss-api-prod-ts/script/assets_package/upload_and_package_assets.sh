#!/bin/bash

set -e
#!/bin/bash

# Function to print usage information
usage() {
    echo "Usage: $0 --cdk-out-dir <CDK_OUTPUT_DIRECTORY> --s3-bucket <S3_BUCKET_NAME> --synthesized-template <SYNTHESIZED_TEMPLATE_FILE> --output-template <OUTPUT_TEMPLATE>"
    exit 1
}

# Parse command-line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --cdk-out-dir) CDK_OUT_DIR="$2"; shift ;;
        --s3-bucket) S3_BUCKET="$2"; shift ;;
        --synthesized-template) TEMPLATE_FILE="$2"; shift ;;
        --output-template) OUTPUT_TEMPLATE="$2"; shift ;;
        *) usage ;;
    esac
    shift
done

# Check if required arguments are provided
if [ -z "$CDK_OUT_DIR" ] || [ -z "$S3_BUCKET" ] || [ -z "$TEMPLATE_FILE" ] || [ -z "$OUTPUT_TEMPLATE" ]; then
    usage
fi

# Check if the manifest.json file exists
MANIFEST_FILE="$CDK_OUT_DIR/manifest.json"
if [ ! -f "$MANIFEST_FILE" ]; then
    echo "Manifest file not found at $MANIFEST_FILE"
    exit 1
fi

# Check if the synthesized template file exists
if [ ! -f "$TEMPLATE_FILE" ]; then
    echo "Synthesized template file not found at $TEMPLATE_FILE"
    exit 1
fi

# Step 1: Upload assets defined in manifest.json
echo "Uploading assets to S3..."
while IFS= read -r line; do
    ASSET_TYPE=$(echo "$line" | jq -r '.type')
    if [ "$ASSET_TYPE" == "file" ]; then
        SOURCE_FILE="$CDK_OUT_DIR/$(echo "$line" | jq -r '.properties.file')"
        DESTINATION_KEY=$(echo "$line" | jq -r '.properties.s3ObjectKey')
        echo "Uploading $SOURCE_FILE to s3://$S3_BUCKET/$DESTINATION_KEY"
        aws s3 cp "$SOURCE_FILE" "s3://$S3_BUCKET/$DESTINATION_KEY"
    fi
done < <(jq -c '.artifacts[]' "$MANIFEST_FILE")

# Step 2: Package the CloudFormation template
echo "Packaging CloudFormation template..."
aws cloudformation package \
    --template-file "$TEMPLATE_FILE" \
    --s3-bucket "$S3_BUCKET" \
    --output-template-file "$OUTPUT_TEMPLATE"

echo "Packaging complete. Packaged template saved to $OUTPUT_TEMPLATE."
