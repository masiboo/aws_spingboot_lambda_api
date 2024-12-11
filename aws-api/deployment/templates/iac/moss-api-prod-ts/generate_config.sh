#!/bin/bash

set -e

# Function to print usage
print_usage() {
    echo "Usage: $0 -c <config_file> -s <seed_file> -o <output_file>"
    echo "  -c <config_file>   Path to the environment-specific configuration file"
    echo "  -s <seed_file>     Path to the seed JSON file (default: seed.json)"
    echo "  -o <output_file>   Path to the output JSON file (default: generated_config.json)"
    exit 1
}

# Initialize variables
CONFIG_FILE=""
SEED_FILE="seed.json"
OUTPUT_FILE="generated_config.json"

# Parse named parameters
while getopts "c:s:o:" opt; do
    case $opt in
        c) CONFIG_FILE="$OPTARG" ;;
        s) SEED_FILE="$OPTARG" ;;
        o) OUTPUT_FILE="$OPTARG" ;;
        *) print_usage ;;
    esac
done

# Check if required parameters are provided
if [ -z "$CONFIG_FILE" ]; then
    echo "Error: Config file is required."
    print_usage
fi

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Config file not found: $CONFIG_FILE"
    exit 1
fi

# Check if seed file exists
if [ ! -f "$SEED_FILE" ]; then
    echo "Error: Seed file not found: $SEED_FILE"
    exit 1
fi

# Load configuration
set -a
source "$CONFIG_FILE"
set +a

# Function to escape special characters for sed
escape_sed() {
    echo "$1" | sed -e 's/[\/&]/\\&/g'
}

# Read the seed file
SEED_CONTENT=$(<"$SEED_FILE")

# Replace placeholders in the seed content
for var in $(compgen -v | grep '^Aws_'); do
    value=$(escape_sed "${!var}")
    SEED_CONTENT=$(echo "$SEED_CONTENT" | sed "s/{{$var}}/$value/g")
done

# Write the generated configuration to the output file
echo "$SEED_CONTENT" > "$OUTPUT_FILE"

echo "Configuration file generated: $OUTPUT_FILE"