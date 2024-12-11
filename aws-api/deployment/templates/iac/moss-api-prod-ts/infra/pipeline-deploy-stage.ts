import * as cdk from 'aws-cdk-lib';
import {Environment, Tags} from "aws-cdk-lib";
import {AwsCfnECRBucketStack} from "./stack/Aws-infra-cfn-ecr-bucket-stack";
import {AppContext, ProjectPrefixType} from "../lib/template/app-context";
import {StackConfig} from "../lib/template/app-config";
import {StackCommonProps} from "../lib/template/stack/base/base-stack";
import {Construct} from "constructs";
import {ConstructCommonProps} from "../lib/template/construct/base/base-construct";

export class PipelineDeployStage extends cdk.Stage {

    protected stackConfig: StackConfig;
    protected projectPrefix: string;
    protected commonProps: ConstructCommonProps;

    constructor(appContext: AppContext, id: string, props: ConstructCommonProps,
                // targetEnvironment: { targetEnvironment: string; env: Environment}
    ) {

        super(appContext.cdkApp, id, props);

        this.stackConfig = props.stackConfig;
        this.commonProps = props;
        this.projectPrefix = props.projectPrefix;

        const suffixStageInName = false;
        const budgetUnitCode = "madrid-Aws"

        const bucketStack = new AwsCfnECRBucketStack(appContext,
            appContext.appConfig.Stack.AwsInfraCfnBucketsECR);
        Tags.of(bucketStack).add("budget-unit-code", `${budgetUnitCode}`);

    }
}