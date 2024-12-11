import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as base from '../../../lib/template/construct/base/base-construct'
import {ISecurityGroup} from "aws-cdk-lib/aws-ec2";

export interface AlbStackProps extends base.ConstructCommonProps {
    vpc: ec2.IVpc;
    internetFacing: boolean;
}

export class AlbConstruct extends base.BaseConstruct {
    public readonly alb: elbv2.IApplicationLoadBalancer;
    public readonly listener: elbv2.IApplicationListener;

    public readonly serviceSecurityGroup: ISecurityGroup;

    constructor(scope: Construct, id: string, props: AlbStackProps) {
        super(scope, id, props);

        // Create a security group for the ALB
        this.serviceSecurityGroup = new ec2.SecurityGroup(this, `${this.projectPrefix}-AlbSecurityGroup`, {
            vpc: props.vpc,
            allowAllOutbound: props.internetFacing,
            description: 'Security group for ALB',
        });

        // Create the ALB
        this.alb = new elbv2.ApplicationLoadBalancer(this, `${this.projectPrefix}-ALB`, {
            loadBalancerName: `${this.projectPrefix}-core-alb`,
            vpc: props.vpc,
            internetFacing: props.internetFacing,
            securityGroup: this.serviceSecurityGroup,
        });

        // Create a listener
        this.listener = this.alb.addListener(`${this.projectPrefix}-Listener`, {
            port: 80,
            open: true,
        });

        // Add a default action to the listener
        this.listener.addAction(`${this.projectPrefix}-DefaultAction`, {
            action: elbv2.ListenerAction.fixedResponse(200, {
                contentType: 'application/json',
                messageBody: '{ "message": "Welcome to Aws Core" }',
            }),
        });

        // Output the ALB DNS name
        new cdk.CfnOutput(this, `${this.projectPrefix}AlbDnsName`, {
            value: this.alb.loadBalancerDnsName,
            description: 'ALB DNS Name',
            exportName: 'AlbDnsName',
        });

        // Output the Listener ARN
        new cdk.CfnOutput(this, `${this.projectPrefix}ListenerArn`, {
            value: this.listener.listenerArn,
            description: 'ALB Listener ARN',
            exportName: 'AlbListenerArn',
        });

        this.putParameter(`core/${this.projectPrefix}AlbDnsName`, this.alb.loadBalancerDnsName);
        this.putParameter(`core/${this.projectPrefix}ServiceSecurityGroupId`, this.serviceSecurityGroup.securityGroupId);
        this.putParameter(`core/${this.projectPrefix}AlbListenerARN`, this.listener.listenerArn);

    }
}