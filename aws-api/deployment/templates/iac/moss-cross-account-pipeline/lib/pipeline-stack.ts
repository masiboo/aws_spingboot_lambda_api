
import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as Pipelines from 'aws-cdk-lib/pipelines';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as CodePipelineActions from 'aws-cdk-lib/aws-codepipeline-actions';
import * as CodeBuild from 'aws-cdk-lib/aws-codebuild';

import {
    ACCOUNT_ID, DEPLOYMENT,
    getLogicalIdPrefix, getResourceNamePrefix, getAllConfigurations
} from './configuration';
import { PipelineDeployStage } from './pipeline_deploy_stage';
import {Bucket} from "aws-cdk-lib/aws-s3";

export class PipelineStack extends cdk.Stack {

    private mappings: any;

    constructor(
        scope: Construct,
        id: string,
        targetEnvironment: string,
        targetBranch: string,
        targetAwsEnv: { [key: string]: string },
        ...rest: any[]
    ) {
        /**
         * CloudFormation stack to create CDK Pipeline resources (Code Pipeline, Code Build, and ancillary resources).
         *
         * @param scope cdk.Construct: Parent of this stack, usually an App or a Stage, but could be any construct.
         * @param id string: The construct ID of this stack. If stackName is not explicitly defined,
         *                   this id (and any parent IDs) will be used to determine the physical ID of the stack.
         * @param targetEnvironment string: The target environment for stacks in the deploy stage.
         * @param targetBranch string: The source branch for polling.
         * @param targetAwsEnv { [key: string]: string }: The CDK env variable used for stacks in the deploy stage.
         */
        super(scope, id, ...rest);

        this.mappings = getAllConfigurations();
        this.createEnvironmentPipeline(targetEnvironment, targetBranch, targetAwsEnv);
    }

    private createEnvironmentPipeline(targetEnvironment: string, targetBranch: string, targetAwsEnv: { [key: string]: string }) {
        /**
         * Creates CloudFormation stack to create CDK Pipeline resources such as:
         * Code Pipeline, Code Build, and ancillary resources.
         *
         * @param targetEnvironment string: The target environment for stacks in the deploy stage.
         * @param targetBranch string: The source branch for polling.
         * @param targetAwsEnv { [key: string]: string }: The CDK env variable used for stacks in the deploy stage.
         */

        const logicalIdPrefix = getLogicalIdPrefix();
        const resourceNamePrefix = getResourceNamePrefix();

        const codeBuildEnv = {
            buildImage: CodeBuild.LinuxBuildImage.STANDARD_5_0,
            privileged: false,
        };

        const bucketName = `newfoundland-pipeline-artifacts`
        const artifactBucket = Bucket.fromBucketName(this, `artifact-bucket`, bucketName);

        const codeBuildOpt: Pipelines.CodeBuildOptions = {
            buildEnvironment: codeBuildEnv,
            rolePolicy: [
                new iam.PolicyStatement({
                    sid: 'InfrastructurePipelineSecretsManagerPolicy',
                    effect: iam.Effect.ALLOW,
                    actions: ['secretsmanager:*'],
                    resources: [
                        `arn:aws:secretsmanager:${this.region}:${this.account}:secret:/DataLake/*`,
                    ],
                }),
                new iam.PolicyStatement({
                    sid: 'InfrastructurePipelineSTSAssumeRolePolicy',
                    effect: iam.Effect.ALLOW,
                    actions: ['sts:AssumeRole'],
                    resources: ['*'],
                }),
                new iam.PolicyStatement({
                    sid: 'InfrastructurePipelineKmsPolicy',
                    effect: iam.Effect.ALLOW,
                    actions: ['kms:*'],
                    resources: ['*'],
                }),
                new iam.PolicyStatement({
                    sid: 'InfrastructurePipelineVpcPolicy',
                    effect: iam.Effect.ALLOW,
                    actions: ['vpc:*'],
                    resources: ['*'],
                }),
                new iam.PolicyStatement({
                    sid: 'InfrastructurePipelineEc2Policy',
                    effect: iam.Effect.ALLOW,
                    actions: ['ec2:*'],
                    resources: ['*'],
                })
            ],
        };

        console.log("target environment: ", targetEnvironment);
        console.log("target account: ", targetAwsEnv["account"]);
        console.log("target region: ",  targetAwsEnv["region"]);

        const pipeline = new Pipelines.CodePipeline(this, `${targetEnvironment}${logicalIdPrefix}InfrastructurePipeline`, {
            pipelineName: `${targetEnvironment.toLowerCase()}-${resourceNamePrefix}-infrastructure-pipeline`,
            codeBuildDefaults: codeBuildOpt,
            selfMutation: true,
            synth: new Pipelines.ShellStep("Synth", {
                input: Pipelines.CodePipelineSource.s3(artifactBucket, "package.zip", {
                }),
                commands: [
                    "npm install -g aws-cdk",
                    "npm install",
                    "cdk synth"
                ],
            }),
            crossAccountKeys: true,
            // artifactBucket: artifactBucket
        });


        const deploymentAccountId = this.mappings[DEPLOYMENT][ACCOUNT_ID];
        const pipeLineDeplyStage =  new PipelineDeployStage(
            this,
            "pipeline-cross-stack",
            targetEnvironment,
            deploymentAccountId,
            { account: targetAwsEnv["account"], region: targetAwsEnv["region"] }
          )

        pipeline.addStage(pipeLineDeplyStage);
    }
}
