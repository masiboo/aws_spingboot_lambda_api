This script generates JSON configuration files for the Aws project based on environment-specific settings.

## Usage

To generate a configuration file, run the script with the path to the desired config file:

```
./generate_config.sh -c config-acc.txt -s seed.json -o config-acc-generated.json
./generate_config.sh -c config-prod.txt -s seed.json -o config-prod-generated.json
```

This will generate `config-dev.json`, `config-acc.json`, or `config-prod.json` respectively.

## Configuration Files

- `configs/config-dev.txt`: Development environment settings
- `configs/config-acc.txt`: Acceptance environment settings
- `configs/config-prod.txt`: Production environment settings

Modify these files to adjust environment-specific settings.

## Generated Files

The script will generate JSON configuration files named `config-<stage>.json`, where `<stage>` is dev, acc, or prod, depending on the input configuration.


# Welcome to your CDK TypeScript project

You should explore the contents of this project. It demonstrates a CDK app with an instance of a stack (`AwsCoreAwsIacStack`)
which contains an Amazon SQS queue that is subscribed to an Amazon SNS topic.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk synth`       emits the synthesized CloudFormation template

# CICD Pipeline Deployment


### Logistical requirements


1. **Administrator privileges** - you need to administrator privileges to bootstrap your AWS environments and complete initial deployment. Usually, these steps can be performed by a DevOps administrator of your team. After these steps, you can revoke administrative privileges. Subsequent deployments are based on self-mutating natures of CDK Pipelines.


---

### AWS environment bootstrapping

1. Make Sure NPM 16 is active

   ```bash
   nvm use v16.0.0
   ```

1. Install dependencies

   ```bash
   npm install
   ```

1. Enable execute permissions for scripts

   ```bash
   chmod 700 ./script/bootstrap/bootstrap_deployment_account.sh
   chmod 700 ./script/bootstrap/bootstrap_target_account.sh
   ```

1. Before you bootstrap **central deployment account** account, set environment variable

   ```bash
   export AWS_PROFILE=replace_it_with_deployment_account_profile_name_b4_running
   ```

   **Important**:
    1. This command is based on the feature [Named Profiles](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html).
    1. If you want to use an alternative option then refer to [Configuring the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html) and [Environment variables to configure the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html) for details. Be sure to follow those steps for each configuration step moving forward.

1. Bootstrap central deployment account

   ```bash
    ./script/bootstrap/bootstrap_deployment_account.sh
   ```

1. When you see the following text, enter **y**, and press enter/return

   ```bash
   Are you sure you want to bootstrap {
      "UserId": "user_id",
      "Account": "deployment_account_id",
      "Arn": "arn:aws:iam::deployment_account_id:user/user_id"
   }? (y/n)y
   ```

1. Expected outputs:
    1. In your terminal, you see ✅  Environment aws://deployment_account_id/eu-central-1 bootstrapped.

    1. You see a stack created in your deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-<assets-deployment_account_id>-eu-central-1```

1. Before you bootstrap **dev** account, set environment variable

   ```bash
   export AWS_PROFILE=replace_it_with_dev_account_profile_name_b4_running
   ```

1. Bootstrap **dev** account

   **Important:** Your configured environment *must* target the Dev account

   ```bash
   ./lib/prerequisites/bootstrap_target_account.sh <central_deployment_account_id> arn:aws:iam::aws:policy/AdministratorAccess
   ```

   When you see the following text, enter **y**, and press enter/return

   ```bash
   Are you sure you want to bootstrap {
    "UserId": "user_id",
    "Account": "dev_account_id",
    "Arn": "arn:aws:iam::dev_account_id:user/user_id"
   } providing a trust relationship to: deployment_account_id using policy arn:aws:iam::aws:policy/AdministratorAccess? (y/n)
   ```

1. Expected outputs:
    1. In your terminal, you see ✅  Environment aws://dev_account_id/eu-central-1 bootstrapped.

    1. You see a stack created in your deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<dev_account_id>-eu-central-1```

1. Before you bootstrap **test** account, set environment variable

   ```bash
   export AWS_PROFILE=replace_it_with_test_account_profile_name_b4_running
   ```

1. Bootstrap acc account

   **Important:** Your configured environment *must* target the Test account

   ```bash
   ./lib/prerequisites/bootstrap_target_account.sh <central_deployment_account_id> arn:aws:iam::aws:policy/AdministratorAccess
   ```

   When you see the following text, enter **y**, and press enter/return

   ```bash
   Are you sure you want to bootstrap {
      "UserId": "user_id",
      "Account": "test_account_id",
      "Arn": "arn:aws:iam::test_account_id:user/user_id"
   } providing a trust relationship to: deployment_account_id using policy arn:aws:iam::aws:policy/AdministratorAccess? (y/n)
   ```

1. Expected outputs:
    1. In your terminal, you see ✅  Environment aws://test_account_id/eu-central-1 bootstrapped.

    1. You see a stack created in your Deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<test_account_id>-eu-central-1```

1. Before you bootstrap **prod** account, set environment variable

   ```bash
   export AWS_PROFILE=replace_it_with_prod_account_profile_name_b4_running
   ```

1. Bootstrap Prod account

   **Important:** Your configured environment *must* target the Prod account

   ```bash
   ./lib/prerequisites/bootstrap_target_account.sh <central_deployment_account_id> arn:aws:iam::aws:policy/AdministratorAccess
   ```

   When you see the following text, enter **y**, and press enter/return

   ```bash
   Are you sure you want to bootstrap {
      "UserId": "user_id",
      "Account": "prod_account_id",
      "Arn": "arn:aws:iam::prod_account_id:user/user_id"
   } providing a trust relationship to: deployment_account_id using policy arn:aws:iam::aws:policy/AdministratorAccess? (y/n)
   ```

1. Expected outputs:
    1. In your terminal, you see ✅  Environment aws://prod_account_id/eu-central-1 bootstrapped.

    1. You see a stack created in your Deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<prod_account_id>-eu-central-1```

---


### Prerequisite

* It must have been cdk-bootstrapped in the target account/region beforehand.
* ```Stage``` and ```Action``` terminologies are same as AWS CodePipeline.
* The basic configuration concept(Stack dependency management, configuration-based stack deployment) was implemented on [AWS CDK Project Template for DevOps](https://github.com/aws-samples/aws-cdk-project-template-for-devops) project. We recommend that you read this project first.

* create Pre-Req
```bash
sh script/01_deploy_prereq_stack.sh --config config/app-config-infra-pipeline-dev.json --profile  madrid-dev-551493771163-devops 

```

* create pipeline for infra (this will also create ecr-pipelines)

```bash
sh script/0A1_deploy_pipeline_stack.sh --config config/app-config-infra-pipeline-dev.json --profile  madrid-dev-551493771163-devops
```

the convenience script should be used when swapping between environments

Development

```bash
sh script/0A1_deploy_pipeline_stack.sh --config config/app-config-infra-pipeline-dev.json --profile  madrid-dev-551493771163-devops
```

Acceptance

```bash
sh script/0A1_deploy_pipeline_stack.sh --config config/app-config-infra-pipeline-acc.json --profile  madrid-dev-551493771163-devops
```

this will 
- deploys rds + vpc + dns
- deploys registry stack
- deploys frontend + waf
- deploys microservices stack
- this creates ECR source pipelines for each microservice

For manual deployment for when you want to test out stacks without creating  pipeline
