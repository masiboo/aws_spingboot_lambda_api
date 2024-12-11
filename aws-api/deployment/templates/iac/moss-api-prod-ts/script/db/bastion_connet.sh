#!/bin/bash

# Check if profile argument is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <aws-profile>"
    exit 1
fi

PROFILE=$1

# Get the parameter
instance_id=$(aws ssm get-parameter --name /Aws/bastionProvisionedInstacneId --profile "$PROFILE" --query "Parameter.Value" --output text)

# Start the EC2 instance (suppressing output)
aws ec2 start-instances --instance-ids "$instance_id" --profile "$PROFILE" > /dev/null 2>&1

# Start the SSM session with port forwarding
aws ssm start-session --target "$instance_id" --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters '{"portNumber": ["5432"], "localPortNumber":["5432"]}' --profile "$PROFILE"
