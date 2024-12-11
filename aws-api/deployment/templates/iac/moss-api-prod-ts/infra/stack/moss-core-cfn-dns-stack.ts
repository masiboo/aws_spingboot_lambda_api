import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';

import * as base from '../../lib/template/stack/cfn/cfn-include-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as certificatemanager from 'aws-cdk-lib/aws-certificatemanager'

export class AwsCfnCertificateStack extends base.CfnIncludeStack {

     public certifacteArn: string;
     private hostedZoneId: string;

    constructor(appContext: AppContext, stackConfig: StackConfig, hostedZoneId: string) {
        super(appContext, stackConfig);
        this.hostedZoneId = hostedZoneId
    }

    @Override
    onLoadTemplateProps(): base.CfnTemplateProps | undefined {

        const hostedZoneId = this.getVariable("publicDnsNameSpaceId");

        // # CDN_ALIAS.NAME_PREFIX.BU.ENV.wipo.int"
        const parameters = [
            {
                Key: "NamePrefix",
                Value: this.commonProps.projectPrefix
            },
            {
                Key: "Environment",
                Value: this.commonProps.appConfig.Project.Stage
            },
            {
                Key: "HostedZoneId",
                Value: this.hostedZoneId
            },
            {
                Key: "AwsDomainName",
                Value: `Aws.Awsdev.madrid.dev.web1.wipo.int`
            },
        ]

        return {
            templatePath: this.stackConfig.TemplatePath,
            parameters: parameters,
        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {
        const cfnCert = cfnTemplate?.getResource('AwsCertificate') as certificatemanager.CfnCertificate;
        this.putParameter('cdnWebCertificate', cfnCert.certificateAuthorityArn!);


        this.certifacteArn = cfnCert.certificateAuthorityArn!;
    }
}
