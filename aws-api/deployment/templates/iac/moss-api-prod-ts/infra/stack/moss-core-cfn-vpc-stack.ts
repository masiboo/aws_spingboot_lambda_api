import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';
import * as core from 'aws-cdk-lib';

import * as base from '../../lib/template/stack/cfn/cfn-include-stackwith-nested';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as sd from "aws-cdk-lib/aws-servicediscovery";

export class AwsCoreCfnVpcStack extends base.CfnIncludeStackNested {

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);
    }

    @Override
    onLoadTemplateProps(): base.CfnTemplateNestedProps | undefined {

        let buildId = process.env.BUILD_NUMBER


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
                Key: "BuildId",
                Value: buildId ? buildId : stage
            },

            {
                Key: "BusinessUnit",
                Value: this.commonProps.appConfig.Project.BusinessUnit
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
            infrastructureTemplatePath: this.stackConfig.InfrastructureTemplatePath,
            dnsTemplatePath: this.stackConfig.DnsTemplatePath,
            certTemplatePath: this.stackConfig.CertTemplatePath,
            initTemplatePath: this.stackConfig.InitTemplatePath,
        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {
        const includedInfraChildStack = cfnTemplate?.getNestedStack('Infrastructure');
        const infraChildTemplate: cfn_inc.CfnInclude | undefined = includedInfraChildStack?.includedTemplate;

        const cfnVpcId = infraChildTemplate?.getOutput('VpcId').value as string
        const privateSubnet1 =  infraChildTemplate?.getResource('AppSubnet1') as ec2.CfnSubnet;
        const privateSubnet2 =  infraChildTemplate?.getResource('AppSubnet2') as ec2.CfnSubnet;
        const privateSubnet3 =  infraChildTemplate?.getResource('AppSubnet3') as ec2.CfnSubnet;

        const dataPrivateSubnet1 =  infraChildTemplate?.getResource('DataSubnet1') as ec2.CfnSubnet;
        const dataPrivateSubnet2 =  infraChildTemplate?.getResource('DataSubnet2') as ec2.CfnSubnet;
        const dataPrivateSubnet3 =  infraChildTemplate?.getResource('DataSubnet3') as ec2.CfnSubnet;

        this.putVariable('VpcId', cfnVpcId);
        this.putVariable('VpcName', `${this.commonProps.projectPrefix}-vpc`); //

        this.putVariable('privateSubnet1', privateSubnet1.attrSubnetId);
        this.putVariable('privateSubnet2', privateSubnet2.attrSubnetId);
        this.putVariable('privateSubnet3', privateSubnet3.attrSubnetId);

        this.putVariable('dataPrivateSubnet1', dataPrivateSubnet1.attrSubnetId);
        this.putVariable('dataPrivateSubnet2', dataPrivateSubnet2.attrSubnetId);
        this.putVariable('dataPrivateSubnet3', dataPrivateSubnet3.attrSubnetId);

    }

}
