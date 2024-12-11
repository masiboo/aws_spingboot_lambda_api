#!/bin/bash

# Default values
CLUSTER_IDENTIFIER=""
AWS_PROFILE=""

# Function to display usage
usage() {
    echo "Usage: $0 --cluster <cluster-identifier> [--profile <aws-profile>]"
    exit 1
}

# Parse named parameters
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --cluster) CLUSTER_IDENTIFIER="$2"; shift ;;
        --profile) AWS_PROFILE="$2"; shift ;;
        *) usage ;;
    esac
    shift
done

# Check if required parameters are provided
if [[ -z "$CLUSTER_IDENTIFIER" ]]; then
    usage
fi

# Set AWS profile if provided
if [[ -n "$AWS_PROFILE" ]]; then
    export AWS_PROFILE="$AWS_PROFILE"
fi

# Check if the cluster exists and get information (function here)
echo "Checking information for RDS cluster '$CLUSTER_IDENTIFIER'..."
CLUSTER_INFO=$(aws rds describe-db-clusters --db-cluster-identifier "$CLUSTER_IDENTIFIER" 2>&1)

if [[ $? -ne 0 ]]; then
    echo "Error: The cluster '$CLUSTER_IDENTIFIER' does not exist or you don't have permission to access it."
    echo "AWS CLI Error: $CLUSTER_INFO"
    exit 1
fi

# Extract and display relevant information
CLUSTER_STATUS=$(echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].Status')
ENGINE=$(echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].Engine')
ENGINE_VERSION=$(echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].EngineVersion')
INSTANCE_COUNT=$(echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].DBClusterMembers | length')

echo "Cluster Information:"
echo "  Status: $CLUSTER_STATUS"
echo "  Engine: $ENGINE"
echo "  Engine Version: $ENGINE_VERSION"
echo "  Number of Instances: $INSTANCE_COUNT"

# List associated instances
echo "Associated Instances:"
echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].DBClusterMembers[] | "  - " + .DBInstanceIdentifier + " (Is Writer: " + (.IsClusterWriter | tostring) + ")"'

echo "RDS cluster information retrieved successfully."

# Function to start a stopped RDS instance
start_instance() {
    local instance_id="$1"
    echo "Starting stopped instance '$instance_id'..."
    aws rds start-db-instance --db-instance-identifier "$instance_id"
    if [[ $? -ne 0 ]]; then
        echo "Failed to start instance '$instance_id'."
        return 1
    fi
    echo "Start command sent successfully for instance '$instance_id'."
    return 0
}

# Function to restart an RDS instance
restart_instance() {
    local instance_id="$1"
    local instance_state=$(aws rds describe-db-instances --db-instance-identifier "$instance_id" --query 'DBInstances[0].DBInstanceStatus' --output text)

    if [[ "$instance_state" == "stopped" ]]; then
        start_instance "$instance_id"
    elif [[ "$instance_state" == "available" || "$instance_state" == "storage-optimization" || "$instance_state" == "storage-initialization" || "$instance_state" == "incompatible-credentials" || "$instance_state" == "incompatible-parameters" ]]; then
        echo "Restarting instance '$instance_id'..."
        aws rds reboot-db-instance --db-instance-identifier "$instance_id"
        if [[ $? -ne 0 ]]; then
            echo "Failed to restart instance '$instance_id'."
            return 1
        fi
        echo "Reboot command sent successfully for instance '$instance_id'."
    else
        echo "Instance '$instance_id' is in '$instance_state' state and cannot be restarted."
        return 1
    fi
    return 0
}

# Replace the cluster reboot section with instance restarts
echo "Restarting or starting instances in RDS cluster '$CLUSTER_IDENTIFIER'..."
INSTANCES=$(echo "$CLUSTER_INFO" | jq -r '.DBClusters[0].DBClusterMembers[].DBInstanceIdentifier')

for instance in $INSTANCES; do
    restart_instance "$instance"
    if [[ $? -ne 0 ]]; then
        echo "Failed to restart or start all instances. Please check your AWS credentials and instance identifiers."
        exit 1
    fi
done

echo "Waiting for all instances to become available..."
for instance in $INSTANCES; do
    aws rds wait db-instance-available --db-instance-identifier "$instance"
done

echo "All instances in RDS cluster '$CLUSTER_IDENTIFIER' have been restarted or started successfully."
