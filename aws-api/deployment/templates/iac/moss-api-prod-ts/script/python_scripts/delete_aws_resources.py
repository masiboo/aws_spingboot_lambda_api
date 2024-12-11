import boto3
import botocore
import argparse

def get_session(profile_name):
    return boto3.Session(profile_name=profile_name)

def delete_bucket(session, bucket):
    print(f"Bucket is being deleted....")
    s3 = session.resource('s3')
    bucket = s3.Bucket(bucket)

    try:
        # Delete all objects and their versions
        bucket.object_versions.all().delete()

        # Delete any remaining objects (if versioning was not enabled)
        bucket.objects.all().delete()

        # Delete the bucket
        bucket.delete()
        print(f"Bucket {bucket.name} has been deleted.")
    except botocore.exceptions.ClientError as e:
        print(f"Error deleting bucket {bucket.name}: {e}")

def delete_stack(session, stack):
    print(f"stack {stack} is being deleted..1")
    cf = session.client('cloudformation')
    print(f"stack {stack} is being deleted..2")
    try:
        cf.delete_stack(StackName=stack)
        waiter = cf.get_waiter('stack_delete_complete')
        print(f"Deleting stack {stack}. This may take a few minutes...")
        waiter.wait(StackName=stack)
        print(f"Stack {stack} has been deleted.")
    except botocore.exceptions.ClientError as e:
        print(f"Error deleting stack {stack}: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Delete S3 bucket and CloudFormation stack")
    parser.add_argument("--profile", required=True, help="AWS profile name")
    parser.add_argument("--bucket", required=True, help="S3 bucket name")
    parser.add_argument("--stack", required=True, help="CloudFormation stack name")
    args = parser.parse_args()

    # Create a session using the specified profile
    session = get_session(args.profile)

    # Delete the S3 bucket
    delete_bucket(session, args.bucket)

    # Delete the CloudFormation stack
    delete_stack(session, args.stack)