

### Software installation

1. **AWS CLI** - make sure you have AWS CLI configured on your system. If not, refer to [Configuring the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html) for more details.

1. **AWS CDK** - install compatible AWS CDK version

   ```bash
   npm install -g aws-cdk@latest
   ```

1. **Python** - make sure you have Python SDK installed on your system. We recommend Python 3.7 and above.

1. **GitHub Fork** - we recommend you [fork the repository](https://docs.github.com/en/get-started/quickstart/fork-a-repo) so you are in control of deployed resources.

### Logistical requirements

1. **Four AWS accounts.** One of them acts like a central deployment account. The other three are for dev, test, and prod accounts. **Optional:** To test this solution with central deployment account and one target environment for e.g. dev, refer to [developer_instructions.md](./resources/developer_instructions.md) for detailed instructions.

1. **Number of branches on your GitHub repo** - You need to start with at least one branch for e.g. main to start using this solution. test and prod branches can be added at the beginning or after the deployment of data lake infrastructure on dev environment.

1. **Administrator privileges** - you need to administrator privileges to bootstrap your AWS environments and complete initial deployment. Usually, these steps can be performed by a DevOps administrator of your team. After these steps, you can revoke administrative privileges. Subsequent deployments are based on self-mutating natures of CDK Pipelines.

1. **AWS Region selection** - we recommend you to use the same AWS region (e.g. us-east-2) for deployment, dev, test, and prod accounts for simplicity. However, this is not a hard requirement.

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
   chmod 700 ./lib/prerequisites/bootstrap_deployment_account.sh
   chmod 700 ./lib/prerequisites/bootstrap_target_account.sh
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
   ./lib/prerequisites/bootstrap_deployment_account.sh
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
    1. In your terminal, you see ✅  Environment aws://deployment_account_id/us-east-2 bootstrapped.

    1. You see a stack created in your deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-<assets-deployment_account_id>-us-east-2```

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
    1. In your terminal, you see ✅  Environment aws://dev_account_id/us-east-2 bootstrapped.

    1. You see a stack created in your deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<dev_account_id>-us-east-2```

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
    1. In your terminal, you see ✅  Environment aws://test_account_id/us-east-2 bootstrapped.

    1. You see a stack created in your Deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<test_account_id>-us-east-2```

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
    1. In your terminal, you see ✅  Environment aws://prod_account_id/us-east-2 bootstrapped.

    1. You see a stack created in your Deployment account as follows

       ![bootstrap_central_deployment_account](./resources/bootstrap_central_deployment_account_exp_output.png)

    1. You see an S3 bucket created in central deployment account. The name is like ```cdk-hnb659fds-assets-<prod_account_id>-us-east-2```

---

### Application configuration

Before we deploy our resources we must provide the manual variables and upon deployment the CDK Pipelines will programmatically export outputs for managed resources. Follow the below steps to setup your custom configuration:

1. **Note:** You can safely commit these values to your repository

1. Go to [configuration.ts](./lib/configuration.ts) and fill in values under `local_mapping` dictionary within the function `get_local_configuration` as desired.

   Example:

    ```typescript
    local_mapping = {
        DEPLOYMENT: {
            ACCOUNT_ID: 'add_your_deployment_account_id_here',
            REGION: 'us-east-2',
            # If you use GitHub / GitHub Enterprise, this will be the organization name
            GITHUB_REPOSITORY_OWNER_NAME: 'aws-samples',
            # Use your forked repo here!
            # This is used in the Logical Id of CloudFormation resources
            # We recommend capital case for consistency. e.g. DataLakeCdkBlog
            GITHUB_REPOSITORY_NAME: 'aws-cdk-pipelines-datalake-infrastructure',
            LOGICAL_ID_PREFIX: 'Aws',
            # This is used in resources that must be globally unique!
            # It may only contain alphanumeric characters, hyphens, and cannot contain trailing hyphens
            # E.g. unique-identifier-data-lake
            RESOURCE_NAME_PREFIX: 'cdkresource',
        },
        DEV: {
            ACCOUNT_ID: 'add_your_dev_account_id_here',
            REGION: 'eu-central-1',
            VPC_CIDR: '10.20.0.0/24'
        },
        TEST: {
            ACCOUNT_ID: 'add_your_test_account_id_here',
            REGION: 'ueu-central-1',
            VPC_CIDR: '10.10.0.0/24'
        },
        PROD: {
            ACCOUNT_ID: 'add_your_prod_account_id_here',
            REGION: 'eu-central-1',
            VPC_CIDR: '10.0.0.0/24'
        }
    }
    ```

## Deployment

---

### Deploying for the first time

Configure your AWS profile to target the central Deployment account as an Administrator and perform the following steps:

1. Open command line (terminal)
1. Go to project root directory where ```cdk.json``` exist
1. Run the command ```cdk ls```
1. Expected output: It lists CDK Pipelines and target account stacks on the console. A sample is below:

    ```bash
   
    ```

1. Set your environment variable back to deployment account

    ```bash
    export AWS_PROFILE=deployment_account_profile_name_here
    ```

1. Run the command ```cdk deploy --all```


### Iterative Deployment

Pipeline you have created using CDK Pipelines module is self mutating. That means, code checked to GitHub repository branch will kick off CDK Pipeline mapped to that branch.

---

