import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {PipelineResourceConstruct} from "../common-infra/construct/pipeline-resource-construct";

export class AwsInfraPipelineResourceStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        // - s3-infra-artifact-bucket
        const infraBucket = new PipelineResourceConstruct(this, "infra-bucket", {
            account: this.commonProps.appConfig.Project.Account,
            env: this.commonProps.env!,
            projectPrefix: this.projectPrefix,
            region: this.commonProps.appConfig.Project.Region,
            s3BucketName:  this.stackConfig.InfraBucketName,
            stackConfig: this.stackConfig,
            stackName: this.stackName,
        })

        // - s3-api-artifact-bucket
        const apiBucket = new PipelineResourceConstruct(this, "api-bucket", {
            account: this.commonProps.appConfig.Project.Account,
            env: this.commonProps.env!,
            projectPrefix: this.projectPrefix,
            region: this.commonProps.appConfig.Project.Region,
            s3BucketName:  this.stackConfig.APIBucketName,
            stackConfig: this.stackConfig,
            stackName: this.stackName
        })

        // - s3-frontend-deploy-bucket
        const frontEndBucket = new PipelineResourceConstruct(this, "frontend-bucket", {
            account: this.commonProps.appConfig.Project.Account,
            env: this.commonProps.env!,
            projectPrefix: this.projectPrefix,
            region: this.commonProps.appConfig.Project.Region,
            s3BucketName:  this.stackConfig.FrontendBucketName,
            stackConfig: this.stackConfig,
            stackName: this.stackName
        })


    }



}