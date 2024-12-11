import boto3
from botocore.exceptions import ClientError
import argparse

def delete_s3_bucket(bucket_name, session):
    """Empty and delete an S3 bucket."""
    s3 = session.resource('s3')
    bucket = s3.Bucket(bucket_name)

    # Empty the bucket
    try:
        bucket.objects.all().delete()
        bucket.object_versions.all().delete()
        print(f"Bucket {bucket_name} emptied successfully.")
    except ClientError as e:
        print(f"Error emptying bucket {bucket_name}: {e}")

    # Delete the bucket
    try:
        bucket.delete()
        print(f"Bucket {bucket_name} deleted successfully.")
    except ClientError as e:
        print(f"Error deleting bucket {bucket_name}: {e}")

def delete_cloudformation_stack(stack_name, session):
    """Delete a CloudFormation stack."""
    cloudformation = session.client('cloudformation')

    try:
        # Get the stack resources to identify S3 buckets
        response = cloudformation.describe_stack_resources(StackName=stack_name)
        s3_buckets = []

        for resource in response['StackResources']:
            if resource['ResourceType'] == 'AWS::S3::Bucket':
                s3_buckets.append(resource['PhysicalResourceId'])

        # Delete the CloudFormation stack
        cloudformation.delete_stack(StackName=stack_name)
        print(f"Initiated deletion of stack {stack_name}.")

        # Wait until the stack deletion is complete
        waiter = cloudformation.get_waiter('stack_delete_complete')
        waiter.wait(StackName=stack_name)
        print(f"Stack {stack_name} deleted successfully.")

        # Empty and delete associated S3 buckets
        for bucket in s3_buckets:
            delete_s3_bucket(bucket, session)

    except ClientError as e:
        print(f"Error deleting stack {stack_name}: {e}")

def main():
    parser = argparse.ArgumentParser(description='Delete a CloudFormation stack and associated S3 buckets.')
    parser.add_argument('--stack-name', required=True, help='The name of the CloudFormation stack to delete.')
    parser.add_argument('--bucket-names', nargs='*', help='Additional S3 bucket names to delete.')
    parser.add_argument('--profile', required=True, help='The AWS CLI profile to use.')

    args = parser.parse_args()

    # Create a session using the specified profile
    session = boto3.Session(profile_name=args.profile)

    # Delete the CloudFormation stack
    delete_cloudformation_stack(args.stack_name, session)

   # If additional bucket names are provided, delete those as well
    if args.bucket_names:
        for bucket_name in args.bucket_names:
            delete_s3_bucket(bucket_name, session)

if __name__ == "__main__":
    main()
