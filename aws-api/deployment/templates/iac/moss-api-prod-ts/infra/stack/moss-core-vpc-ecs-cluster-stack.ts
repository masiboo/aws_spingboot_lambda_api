import {RemovalPolicy} from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {SecurityGroup} from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import {AwsLogDriver, FargateTaskDefinition} from 'aws-cdk-lib/aws-ecs';
import * as sm from 'aws-cdk-lib/aws-secretsmanager';
import {ApplicationLoadBalancedFargateService} from 'aws-cdk-lib/aws-ecs-patterns';

import * as base from '../../lib/template/stack/vpc/vpc-base-stack';
import {Override} from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {StackConfig} from '../../lib/template/app-config'
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from "aws-cdk-lib/aws-iam";
import {LogGroup} from "aws-cdk-lib/aws-logs";
import {ApplicationListener, ApplicationLoadBalancer} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {AbstractEcsBaseStack} from "../ecs-service/ecs-abstract-base-stack";
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';

export class AwsCoreVpcEcsClusterStack extends AbstractEcsBaseStack {

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);
    }

    @Override
    onLookupLegacyVpc(): base.VpcLegacyLookupProps | undefined {
        return {
            vpcNameLegacy: this.getVariable('VpcName'),
        };
    }

    @Override
    onPostConstructor(baseVpc?: ec2.IVpc) {

        const databaseHostName = this.getParameter('databaseHostName');
        const databaseName = this.getParameter('databaseName');
        const databaseSecretArn = this.getParameter('databaseSecretArn');
        const databaseSecret = sm.Secret.fromSecretCompleteArn(this, 'secret', databaseSecretArn);
        const secretion = databaseSecret.secretValueFromJson("password").unsafeUnwrap().toString();

        this.createECSService(databaseHostName, databaseName, databaseSecret, secretion, baseVpc!, "core");

    }

    private createECSService(databaseHostName: string, databaseName: string, databaseSecret: sm.ISecret, secretion: string, vpc: ec2.IVpc, serviceName: string) {

        const cluster = new ecs.Cluster(this, `Aws-${serviceName}-cluster`, {
            vpc,
            clusterName: `Aws-${serviceName}-cluster`
        });

        this.putParameter('ECSClusterName', cluster.clusterName);

        // let albSecurityGroup: SecurityGroup;
        // albSecurityGroup = new SecurityGroup(this, `${this.stackName}-security-group`, {
        //     vpc: vpc,
        //     securityGroupName: `${this.stackName}-sg`.substring(0, 32),
        //     allowAllOutbound: this.stackConfig.InternetFacing
        // });
        //
        // let alb: ApplicationLoadBalancer;



    }


}
