#!/bin/bash

# Default values
#CLUSTER_NAME=""
#SERVICE_NAME=""
#DESIRED_COUNT=1

#!/bin/bash

# Default values
CLUSTER_NAME=""
SERVICE_NAME=""
AWS_PROFILE=""

# Function to display usage
usage() {
    echo "Usage: $0 --cluster <cluster-name> --service <service-name> [--profile <aws-profile>]"
    exit 1
}

# Parse named parameters
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --cluster) CLUSTER_NAME="$2"; shift ;;
        --service) SERVICE_NAME="$2"; shift ;;
        --profile) AWS_PROFILE="$2"; shift ;;
        *) usage ;;
    esac
    shift
done

# Check if required parameters are provided
if [[ -z "$CLUSTER_NAME" || -z "$SERVICE_NAME" ]]; then
    usage
fi

# Set AWS profile if provided
if [[ -n "$AWS_PROFILE" ]]; then
    export AWS_PROFILE="$AWS_PROFILE"
fi

# Update the service's desired count
echo "Updating service desired count..."
aws ecs update-service --cluster "$CLUSTER_NAME" --service "$SERVICE_NAME" --desired-count 1

# Check if the update was successful
if [[ $? -ne 0 ]]; then
    echo "Failed to update service. Please check your AWS credentials and parameters."
    exit 1
fi

# Wait for the service to stabilize
echo "Waiting for service to stabilize..."
aws ecs wait services-stable --cluster "$CLUSTER_NAME" --services "$SERVICE_NAME"

# Check if the service is stable
if [[ $? -ne 0 ]]; then
    echo "Service failed to stabilize. You may need to check the ECS console for more details."
    exit 1
fi

echo "ECS service '$SERVICE_NAME' in cluster '$CLUSTER_NAME' has been started successfully."

#
## Function to display script usage
#usage() {
#    echo "Usage: $0 --cluster <cluster-name> --service <service-name> [--count <desired-count>]"
#    exit 1
#}
#
## Parse named parameters
#while [[ "$#" -gt 0 ]]; do
#    case $1 in
#        --cluster) CLUSTER_NAME="$2"; shift ;;
#        --service) SERVICE_NAME="$2"; shift ;;
#        --count) DESIRED_COUNT="$2"; shift ;;
#        *) usage ;;
#    esac
#    shift
#done
#
## Check if required parameters are provided
#if [[ -z "$CLUSTER_NAME" || -z "$SERVICE_NAME" ]]; then
#    usage
#fi
#
## Update the service
#echo "Updating service $SERVICE_NAME in cluster $CLUSTER_NAME to desired count $DESIRED_COUNT"
#aws ecs update-service --cluster "$CLUSTER_NAME" --service "$SERVICE_NAME" --desired-count "$DESIRED_COUNT"
#
## Wait for the service to stabilize
#echo "Waiting for service to stabilize..."
#aws ecs wait services-stable --cluster "$CLUSTER_NAME" --services "$SERVICE_NAME" --profile
#
## Check if any tasks are running
#RUNNING_TASKS=$(aws ecs list-tasks --cluster "$CLUSTER_NAME" --service-name "$SERVICE_NAME" --desired-status RUNNING --query 'taskArns' --output text)
#
#if [[ -z "$RUNNING_TASKS" ]]; then
#    echo "No tasks are running. Starting a new task..."
#    TASK_DEFINITION=$(aws ecs describe-services --cluster "$CLUSTER_NAME" --services "$SERVICE_NAME" --query 'services[0].taskDefinition' --output text)
#    aws ecs run-task --cluster "$CLUSTER_NAME" --task-definition "$TASK_DEFINITION"
#else
#    echo "Tasks are running. No need to start a new task."
#fi
#
#echo "ECS service and task start process completed."