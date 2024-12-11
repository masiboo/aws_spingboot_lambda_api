import * as base from '../../lib/template/stack/base/us-east-base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AwsFrontEndConstruct} from "../common-infra/AwsFrontEndConstruct";
import {AwsApiCertConstruct} from "../common-infra/AwsApiCertConstruct";

export class AwsFrontEndStack extends base.CrossRegionBaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        // #todo revert to config file for prod and acc
        const domainName = `Aws.madrid.${appContext.appConfig.Project.Stage}.web1.wipo.int`
        const siteSubDomain = "admin"

        new AwsFrontEndConstruct(this, 'AwsFrontEndConstruct', {
            buildNumber: 0,
            buildStage: this.commonProps.projectPrefix,
            project: this.projectPrefix,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,
            domainName,
            siteSubDomain,
            certificateArnVariableKey: "certificateArn",
            lambdaRewrite: true,
            bucketSSMKey: "AwsFrontendBucketName",
            createS3Bucket: false,
            websiteDistSourcePath: "./frontend-assets/deployment"
        })


    }




}
