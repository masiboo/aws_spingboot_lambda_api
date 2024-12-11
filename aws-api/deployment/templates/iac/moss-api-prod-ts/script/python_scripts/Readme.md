python install boto3

```sh
pip3 install boto3
```

Running the script to delete stack

```basH
python delete_stack.py --stack-name my-stack-name --profile my-aws-profile
```

If you also want to delete specific S3 buckets, you'd use:

```zsh
python delete_stack.py --stack-name my-stack-name --profile my-aws-profile --bucket-names bucket1 bucket2

```

```shell
python delete_stack.py --stack-name AwsInfrastructrePipelineStack --profile madrid-dev-551493771163-devops  --bucket-names Aws-artifacts-eu-central-1-551493771163
```


Deleting S3 Buckets and Versions

```shell
python3 delete_s3.py  madrid-dev-551493771163-devops Aws-Awsapidocsstack-Aws
```

```shell
python3 delete_s3.py  madrid-dev-551493771163-devops Aws-Awsapidocsstack-Aws 
```

```shell
python3 script/python_scripts/delete_s3.py  madrid-dev-551493771163-devops Aws-Awsapidocsstack-Aws
```