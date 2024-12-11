import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';

import * as base from '../../lib/template/stack/cfn/cfn-include-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as certificatemanager from 'aws-cdk-lib/aws-certificatemanager'
import {CfnOutput} from "aws-cdk-lib";
import {OriginAccessIdentity} from "aws-cdk-lib/aws-cloudfront";
import {Bucket} from "aws-cdk-lib/aws-s3";
import {EcrResourceConstruct} from "../common-infra/construct/ecr-resource-construct";

export class AwsCfnECRBucketStack extends base.CfnIncludeStack {

    public cloudfrontOAI: string
    public logsBucket: string
    public frontEndBucketDomain: string
    private frontendBucketName: string;
    private frontendBucketNameOutput: CfnOutput;

    public apiDocBucketDomain: string
    private apiDocBucketName: string;
    private apiDocBucketNameOutput: CfnOutput;

    public opsBucketDomain: string
    private opsBucketName: string;
    private opsBucketNameOutput: CfnOutput;

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);

        const repoArray = this.stackConfig.EcrRepository

        // if (repoArray != undefined) {
        //     for(let param of repoArray) {
        //         const serviceName = param.ServiceName;
        //         new EcrResourceConstruct(this, `ecr-repo-${serviceName}`, {
        //             env:  this.commonProps.env!,
        //             projectPrefix: this.projectPrefix,
        //             stackConfig: this.stackConfig,
        //             stackName: this.stackName,
        //             repoSuffix: `${this.commonProps.appConfig.Project.Stage}-${this.commonProps.appConfig.Project.Account}`,
        //             serviceName,
        //         })
        //     }
        // }

    }

    @Override
    onLoadTemplateProps(): base.CfnTemplateProps | undefined {

        const allowedEnv = ["dev", "acc", "prod"];
        let stage = this.commonProps.appConfig.Project.Stage;
        const result = allowedEnv.findIndex(item => stage.toUpperCase() === item.toUpperCase());

        if (result !== -1) {

        } else {

            stage = "dev"
        }

        const computedParameters = [
            {
                Key: "NamePrefix",
                Value: this.commonProps.projectPrefix
            },

            {
                Key: "Environment",
                Value: stage
            }

        ]
        const configParameters = this.stackConfig.Parameters
        const parameters = configParameters.concat(computedParameters)

        return {
            templatePath: this.stackConfig.TemplatePath,
            parameters: parameters,
        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {

        const cloudfrontOAI = cfnTemplate!.getOutput('CloudfrontOAI').value as string;
        const logsBucket = cfnTemplate!.getResource('LogsBucket') as unknown as Bucket;
        this.logsBucket = logsBucket.bucketArn
        const frontEndBucket = cfnTemplate?.getResource('AwsFrontendBucket') as unknown as Bucket;
        this.frontendBucketName =  frontEndBucket.bucketName
        this.frontEndBucketDomain = frontEndBucket.bucketDomainName

        this.putVariable("frontendBucketDomain", frontEndBucket.bucketDomainName)
        this.putVariable("cloudfrontOAIRef", cloudfrontOAI)
        this.putVariable("logsBucketArn", this.logsBucket)
        this.putVariable("frontendBucketName", this.frontendBucketName)

    }
}
