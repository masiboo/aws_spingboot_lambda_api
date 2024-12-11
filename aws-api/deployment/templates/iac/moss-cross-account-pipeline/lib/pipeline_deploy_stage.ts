
import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from './vpc_stack';
import { S3BucketZonesStack } from './s3_bucket_zones_stack';
import { tag } from './tagging';
import { getLogicalIdPrefix } from './configuration';

export class PipelineDeployStage extends cdk.Stage {
    constructor(
        scope: Construct,
        id: string,
        targetEnvironment: string,
        deploymentAccountId: string,
        env: cdk.Environment,
        ...rest: any[]
    ) {
        /**
         * Adds deploy stage to CodePipeline
         *
         * @param scope cdk.Construct: Parent of this stack, usually an App or a Stage, but could be any construct.
         * @param id string: The construct ID of this stack. If stackName is not explicitly defined,
         *                   this id (and any parent IDs) will be used to determine the physical ID of the stack.
         * @param targetEnvironment string: The target environment for stacks in the deploy stage.
         * @param deploymentAccountId string: The id for the deployment account.
         * @param env cdk.Environment: CDK Environment.
         * @param rest any[]: Additional arguments.
         */
        super(scope, id, ...rest);

        const logicalIdPrefix = getLogicalIdPrefix();

        const vpcStack = new VpcStack(
            this,
            `${targetEnvironment}${logicalIdPrefix}InfrastructureVpc`,
            targetEnvironment,
            env,
            ...rest,
        );

        const bucketStack = new S3BucketZonesStack(
            this,
            `${targetEnvironment}${logicalIdPrefix}InfrastructureS3BucketZones`,
            targetEnvironment,
            deploymentAccountId,
            env,
            ...rest
        );

        tag(vpcStack, targetEnvironment);
        tag(bucketStack, targetEnvironment);
    }
}