import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';

import * as base from '../../lib/template/stack/cfn/cfn-include-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import {CfnJson, CfnParameter} from "aws-cdk-lib";
import {TokenString} from "aws-cdk-lib/core/lib/private/encoding";


export class AwsCfnCloudfrontCdnStack extends base.CfnIncludeStack {

    private cloudfrontOAI: string
    private cdnWebCertificateArn: string
    private waFAcl: string
    private logsBucket: string
    private frontEndBucketDomain: string
    private frontEndBucketWebsite: string
    private hostedZoneId: string

    constructor(appContext: AppContext,
                stackConfig: StackConfig, cloudfrontOAI: string, cdnWebCertificateArn: string,
                waFAcl: string, logsBucket: string, frontEndBucketDomain: string) {

        super(appContext, stackConfig);

        this.cloudfrontOAI = cloudfrontOAI;
        this.cdnWebCertificateArn = cdnWebCertificateArn;
        this.waFAcl = waFAcl;
        this.logsBucket = logsBucket;
        this.frontEndBucketDomain = frontEndBucketDomain;

    }

    @Override
    onLoadTemplateProps(): base.CfnTemplateProps | undefined {

        const frontEndBucketDomain = this.getVariable("frontendBucketDomain")
        console.log(frontEndBucketDomain)
        console.log("frontEndBucketDomain")

        // # CDN_ALIAS.NAME_PREFIX.BU.ENV.wipo.int"

        const uploadBucketName = new CfnParameter(this, "uploadBucketName", {
            type: "String",
            description: "The name of the Amazon S3 bucket where uploaded files will be stored."});

        const cfnJson = new CfnJson(this, "buckhead", {
            value: undefined
        })

        const parameters: ({ Value: TokenString | string; Key: string })[] = [
            {
                Key: "NamePrefix",
                Value: this.commonProps.projectPrefix
            },
            {
                Key: "CloudfrontOAI",
                Value: "E2WH51DW8EFE30"
            },
            {
                Key: "OriginDomainName",
                Value: this.frontEndBucketDomain
            },
            {
                Key: "HostedZoneName",
                Value: this.hostedZoneId
            },   {
                Key: "AliasPrefix",
                Value: "cdn"
            },   {
                Key: "AlternativeAlias",
                Value: ''
            },   {
                Key: "Environment",
                Value: this.commonProps.appConfig.Project.Stage
            },   {
                Key: "cdnWebCertificateArn",
                Value: this.cdnWebCertificateArn
            },   {
                Key: "WAFAcl",
                Value: this.waFAcl
            },   {
                Key: "LogsBucket",
                Value: this.logsBucket
            },
            {
                Key: "FilesBucketDomainName",
                Value: this.commonProps.projectPrefix
            },
            {
                Key: "ApiGatewayArn",
                Value: this.commonProps.projectPrefix
            },
        ]

        return {
            templatePath: this.stackConfig.TemplatePath,
            parameters: parameters,
        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {

    }
}
