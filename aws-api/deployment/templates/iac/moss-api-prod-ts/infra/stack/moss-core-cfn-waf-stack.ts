import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';

import * as base from '../../lib/template/stack/cfn/cfn-include-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as certificatemanager from 'aws-cdk-lib/aws-certificatemanager'
import {CfnWebACL} from "aws-cdk-lib/aws-waf";

export class AwsCoreCfnWafStack extends base.CfnIncludeStack {

     public wafACL: string;

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);

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
                // List<String>
                Key: "IngressCidr",
                Value: '193.5.93.0/24,3.126.58.0/24'
            }
        ]

        return {
            templatePath: this.stackConfig.TemplatePath,
            parameters: parameters,
        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {
        const cfnWaf = cfnTemplate?.getResource('WebACLWipoIp') as CfnWebACL;

        this.wafACL = cfnWaf.ref;
    }
}
