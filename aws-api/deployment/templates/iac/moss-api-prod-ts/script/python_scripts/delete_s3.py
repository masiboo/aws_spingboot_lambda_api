import boto3
import sys


BUCKETS = [
   sys.argv[2]
]

PROFILE_NAME = sys.argv[1]

boto3.Session(profile_name=PROFILE_NAME)

s3 = boto3.resource("s3")

for bucket_name in BUCKETS:
    # For each bucket, create a resource object with boto3
    print ("The name of bucket is %s" % bucket_name)
    bucket = s3.Bucket(bucket_name)
    # Delete all of the objects in the bucket
    bucket.object_versions.delete()

    # Delete the bucket itself!
    bucket.delete()
    print ("The bucket %s is deleted" % bucket_name)

print ("DONE")
