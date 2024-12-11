
import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {
    AVAILABILITY_ZONE_1, AVAILABILITY_ZONE_2, AVAILABILITY_ZONE_3,
    ROUTE_TABLE_1, ROUTE_TABLE_2, ROUTE_TABLE_3, SHARED_SECURITY_GROUP_ID,
    SUBNET_ID_1, SUBNET_ID_2, SUBNET_ID_3, VPC_CIDR, VPC_ID,
    getEnvironmentConfiguration, getLogicalIdPrefix
} from './configuration';

export class VpcStack extends cdk.Stack {
    constructor(scope: Construct,
                id: string,
                targetEnvironment: string,
                ...rest: any[])
    {
        /**
         * CloudFormation stack to create AWS KMS Key, Amazon S3 resources such as buckets and bucket policies.
         *
         * @param scope Construct: Parent of this stack, usually an App or a Stage, but could be any construct.
         * @param id string: The construct ID of this stack. If stackName is not explicitly defined,
         *                   this id (and any parent IDs) will be used to determine the physical ID of the stack.
         * @param targetEnvironment string: The target environment for stacks in the deploy stage
         */
        super(scope, id, ...rest);

        const mappings = getEnvironmentConfiguration(targetEnvironment);
        const vpcCidr = mappings[VPC_CIDR];
        const logicalIdPrefix = getLogicalIdPrefix();

        const vpc = new ec2.Vpc(this, `${logicalIdPrefix}Vpc`, {
            cidr: vpcCidr,
            maxAzs: 3,
        });

        const sharedSecurityGroupIngress = new ec2.SecurityGroup(this, `${targetEnvironment}${logicalIdPrefix}SharedIngressSecurityGroup`, {
            vpc: vpc,
            description: 'Shared Security Group for Data Lake resources with self-referencing ingress rule.',
        });

        sharedSecurityGroupIngress.addIngressRule(
            sharedSecurityGroupIngress,
            ec2.Port.allTraffic(),
            'Self-referencing ingress rule'
        );

        vpc.addGatewayEndpoint(`${targetEnvironment}${logicalIdPrefix}S3Endpoint`, {
            service: ec2.GatewayVpcEndpointAwsService.S3,
        });

        vpc.addGatewayEndpoint(`${targetEnvironment}${logicalIdPrefix}DynamoEndpoint`, {
            service: ec2.GatewayVpcEndpointAwsService.DYNAMODB,
        });

        vpc.addInterfaceEndpoint(`${targetEnvironment}${logicalIdPrefix}GlueEndpoint`, {
            service: ec2.InterfaceVpcEndpointAwsService.GLUE,
            securityGroups: [sharedSecurityGroupIngress],
        });

        vpc.addInterfaceEndpoint(`${targetEnvironment}${logicalIdPrefix}KmsEndpoint`, {
            service: ec2.InterfaceVpcEndpointAwsService.KMS,
            securityGroups: [sharedSecurityGroupIngress],
        });

        vpc.addInterfaceEndpoint(`${targetEnvironment}${logicalIdPrefix}SsmEndpoint`, {
            service: ec2.InterfaceVpcEndpointAwsService.SSM,
            securityGroups: [sharedSecurityGroupIngress],
        });

        vpc.addInterfaceEndpoint(`${targetEnvironment}${logicalIdPrefix}SecretsManagerEndpoint`, {
            service: ec2.InterfaceVpcEndpointAwsService.SECRETS_MANAGER,
            securityGroups: [sharedSecurityGroupIngress],
        });

        vpc.addInterfaceEndpoint(`${targetEnvironment}${logicalIdPrefix}StepFunctionsEndpoint`, {
            service: ec2.InterfaceVpcEndpointAwsService.STEP_FUNCTIONS,
            securityGroups: [sharedSecurityGroupIngress],
        });

        // Stack Outputs that are programmatically synchronized
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}Vpc`, {
        //     value: vpc.vpcId,
        //     exportName: mappings[VPC_ID],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcAvailabilityZone1`, {
        //     value: vpc.availabilityZones[0],
        //     exportName: mappings[AVAILABILITY_ZONE_1],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcAvailabilityZone2`, {
        //     value: vpc.availabilityZones[1],
        //     exportName: mappings[AVAILABILITY_ZONE_2],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcAvailabilityZone3`, {
        //     value: vpc.availabilityZones[2],
        //     exportName: mappings[AVAILABILITY_ZONE_3],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcPrivateSubnet1`, {
        //     value: vpc.privateSubnets[0].subnetId,
        //     exportName: mappings[SUBNET_ID_1],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcPrivateSubnet2`, {
        //     value: vpc.privateSubnets[1].subnetId,
        //     exportName: mappings[SUBNET_ID_2],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcPrivateSubnet3`, {
        //     value: vpc.privateSubnets[2].subnetId,
        //     exportName: mappings[SUBNET_ID_3],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcRouteTable1`, {
        //     value: vpc.privateSubnets[0].routeTable.routeTableId,
        //     exportName: mappings[ROUTE_TABLE_1],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcRouteTable2`, {
        //     value: vpc.privateSubnets[1].routeTable.routeTableId,
        //     exportName: mappings[ROUTE_TABLE_2],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}VpcRouteTable3`, {
        //     value: vpc.privateSubnets[2].routeTable.routeTableId,
        //     exportName: mappings[ROUTE_TABLE_3],
        // });
        //
        // new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}SharedSecurityGroup`, {
        //     value: sharedSecurityGroupIngress.securityGroupId,
        //     exportName: mappings[SHARED_SECURITY_GROUP_ID],
        // });
        //
    }
}
