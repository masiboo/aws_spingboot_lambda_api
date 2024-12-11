
import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';

import * as base from '../../lib/template/stack/vpc/vpc-base-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as ecs from "aws-cdk-lib/aws-ecs";
import {ISubnet, IVpc, SecurityGroup} from "aws-cdk-lib/aws-ec2";
import {SubnetGroup} from "aws-cdk-lib/aws-rds";
import {aws_secretsmanager, Stack, Tags} from "aws-cdk-lib";
import * as bastion from "@moia-oss/bastion-host-forward";
import {ISecret, Secret} from "aws-cdk-lib/aws-secretsmanager";
import * as sm from "aws-cdk-lib/aws-secretsmanager";

export interface AwsCoreVpcRdsStackProps extends StackConfig {

    readonly auroraDbUsername?: string;
    readonly auroraDbPassword?: string;

    readonly snapshotIdentifier?: string;

}

export class AwsCoreVpcRdsStack extends base.VpcBaseStack {

    constructor(appContext: AppContext, stackConfig: AwsCoreVpcRdsStackProps) {
        super(appContext, stackConfig);
    }

    @Override
    onLookupLegacyVpc(): base.VpcLegacyLookupProps | undefined {
        return {
            vpcNameLegacy: this.getVariable('VpcName'),
            // vpcIdLegacy: this.getVariable('VpcId')

        };
    }

    @Override
    onPostConstructor(baseVpc?: ec2.IVpc) {

        const subnetIds: ec2.ISubnet[] = baseVpc!.privateSubnets
        const dataSubnet: ec2.ISubnet[] = [
            ec2.Subnet.fromSubnetAttributes(this, 'Aws-data-private-subnet-1', {
                subnetId: this.getVariable('dataPrivateSubnet1')
            }),
            ec2.Subnet.fromSubnetAttributes(this, 'Aws-data-private-subnet-2', {
                subnetId: this.getVariable('dataPrivateSubnet2')
            }),
            ec2.Subnet.fromSubnetAttributes(this, 'Aws-data-private-subnet-3', {
                subnetId: this.getVariable('dataPrivateSubnet3')
            }),
        ]

        //- RDS Group //
        const rdsSecurityGroup = new ec2.SecurityGroup(this, 'security-group-rds', {
            vpc: baseVpc!,
            allowAllOutbound: false,
            securityGroupName: 'security-group-rds',

        });

        rdsSecurityGroup.addIngressRule(ec2.Peer.ipv4(baseVpc!.vpcCidrBlock), ec2.Port.tcp(5432), 'Allows access from the vpc');

        const dbName = "AwsDb"
        const snapshotIdentifier = process.env.SNAPSHOT_IDENTIFIER || undefined;

        const rdCluster:  rds.DatabaseCluster = this.rdsAuroraCluster(baseVpc!, dataSubnet, rdsSecurityGroup, snapshotIdentifier ? snapshotIdentifier : null);

        // dbInstance.connections.allowFrom(ec2Instance, ec2.Port.tcp(5432));
        this.putParameter('rdsDatabaseHostName', rdCluster.clusterEndpoint.hostname);
        this.putParameter('rdsDatabaseAddress', rdCluster.clusterEndpoint.socketAddress); // includes port number
        this.putParameter('rdsDatabaseName', dbName);
        this.putParameter('rdsDatabaseSecretArn', rdCluster.secret?.secretArn!);
        this.putParameter('rdsDatabaseSecurityGroupId', rdCluster.connections.securityGroups[0].securityGroupId);

        this.putParameter('databaseHostName', rdCluster.clusterEndpoint.hostname);
        this.putParameter('databaseAddress', rdCluster.clusterEndpoint.socketAddress);
        this.putParameter('databaseName', this.stackConfig.DatabaseName);
        this.putParameter('databaseSecretArn', rdCluster.secret?.secretArn!);
        this.putParameter('databaseSecurityGroup', rdCluster.connections.securityGroups[0].securityGroupId);
    }

    private provisionedRDS(baseVpc: IVpc | undefined,
                           subnetGroupRDS: SubnetGroup,
                           databaseCredentialsSecret: Secret,
                           dbName: string, rdsSecurityGroup: SecurityGroup) {

        const provisionedDbInstance = new rds.DatabaseInstance(this, 'Aws-db-instance', {
            vpc: baseVpc!,
            subnetGroup: subnetGroupRDS,
            // vpcSubnets: {
            //     subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
            // },
            engine: rds.DatabaseInstanceEngine.postgres({
                version: rds.PostgresEngineVersion.VER_14_8,
            }),
            instanceType: ec2.InstanceType.of(
                ec2.InstanceClass.BURSTABLE3,
                ec2.InstanceSize.MEDIUM,
            ),
            credentials: rds.Credentials.fromSecret(databaseCredentialsSecret), // Get both username and password from existing secret
            multiAz: true,
            allocatedStorage: 100,
            maxAllocatedStorage: 200,
            allowMajorVersionUpgrade: false,
            autoMinorVersionUpgrade: true,
            backupRetention: cdk.Duration.days(0),
            deleteAutomatedBackups: true,
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            deletionProtection: false,
            databaseName: dbName,
            publiclyAccessible: false,
            securityGroups: [rdsSecurityGroup],
        });
        return provisionedDbInstance;
    }

    private createEcsCluster(baseName: string, vpc: ec2.IVpc): ecs.Cluster {
        const cluster = new ecs.Cluster(this, baseName, {
            clusterName: `${this.projectPrefix}-${baseName}`,
            vpc: vpc,
            containerInsights: true
        });

        return cluster;
    }

    private createServerlessCluster(baseVpc: ec2.IVpc, subnet2: ec2.ISubnet[],
                                    databaseCredentialsSecret: aws_secretsmanager.Secret  ) {

        // const databaseCredentialsSecret = new aws_secretsmanager.Secret(this, `secret-aurora-credentials`, {
        //     secretName: this.resolveSecretName('rds-secret'),
        //     generateSecretString: {
        //         secretStringTemplate: JSON.stringify({
        //             username: this.getParameter('rds/databaseMasterUser'),
        //         }),
        //         excludePunctuation: true,
        //         includeSpace: false,
        //         generateStringKey: 'password'
        //     }
        // });

        const auroraSecurityGroup = new ec2.SecurityGroup(this, 'security-group-aurora', {
            vpc: baseVpc!,
            allowAllOutbound: false,
            securityGroupName: 'security-group-aurora',

        });
        // auroraSecurityGroup.addIngressRule(props.ec2SecurityGroup, Port.tcp(5432), 'Allows EC2 instance to talk to database');
        auroraSecurityGroup.addIngressRule(ec2.Peer.ipv4(baseVpc!.vpcCidrBlock), ec2.Port.tcp(5432), 'Allows access from the vpc');

        const subnetGroup = new rds.SubnetGroup(this, this.resolveResourceName('private-subnet-group'), {
            vpc: baseVpc!,
            description: "Subnet Group for Aurora Serverless",
            vpcSubnets: {subnets: subnet2} // TODO: set as parameter or variable
        });

        const serverlessCluster = new rds.ServerlessCluster(this, 'serverless-rds', {
            engine: rds.DatabaseClusterEngine.AURORA_POSTGRESQL,
            clusterIdentifier: this.withProjectPrefix(this.stackConfig.ClusterIdentifier),
            parameterGroup: rds.ParameterGroup.fromParameterGroupName(this, 'rds-aurora-parameter-group', 'default.aurora-postgresql13'),
            defaultDatabaseName: this.stackConfig.DatabaseName,
            vpc: baseVpc!,
            securityGroups: [auroraSecurityGroup],
            enableDataApi: true,
            subnetGroup: subnetGroup,
            scaling: {
                autoPause: cdk.Duration.minutes(10),
                minCapacity: rds.AuroraCapacityUnit.ACU_2, // #todo Prod AND Acc
                maxCapacity: rds.AuroraCapacityUnit.ACU_4,
            },

            // credentials: {
            //     username: databaseCredentialsSecret.secretValueFromJson('username').unsafeUnwrap(),
            //     password: databaseCredentialsSecret.secretValueFromJson('password'),
            // },
            // DEV VALUES
            removalPolicy: cdk.RemovalPolicy.DESTROY, // #todo  msfw
            deletionProtection: false,
            // backupRetention: cdk.Duration.days(1), // #todo Prod AND Acc
            // removalPolicy: cdk.RemovalPolicy.RETAIN, // #todo Prod AND Acc
            // deletionProtection: database.deleteProtection(this.props.envName), // #todo Prod AND Acc
        });
        return serverlessCluster;
    }

    private rdsAuroraCluster(baseVpc: IVpc, subnet2: ISubnet[], securityGroup: any, snapshotIdentifier: string | null) {

        const secretMaster = this.getSecret("Aws-imported-secret", "rds/credentials/master")

        const subnetGroup = new rds.SubnetGroup(this, this.resolveResourceName('private-subnet-group'), {
            vpc: baseVpc!,
            description: "Subnet Group for Aurora Serverless",
            vpcSubnets: {subnets: subnet2} // TODO: set as parameter or variable
        })

       const writer =  rds.ClusterInstance.provisioned('Aws-writer-provisioned', {
           publiclyAccessible: false,
           enablePerformanceInsights: true,
           instanceType: ec2.InstanceType.of(ec2.InstanceClass.R5, ec2.InstanceSize.LARGE),
       });

        const reader =  rds.ClusterInstance.provisioned('Aws-reader-provisioned', {
            publiclyAccessible: false,
            enablePerformanceInsights: true,
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.R5, ec2.InstanceSize.LARGE),
        });

        let cluster;
        if (snapshotIdentifier && snapshotIdentifier.trim() !== '') {
            // Restore from snapshot
            cluster = new rds.DatabaseClusterFromSnapshot(this, 'AwsDatabaseClusterFromSnapshot', {
                snapshotIdentifier: snapshotIdentifier,
                engine: rds.DatabaseClusterEngine.auroraPostgres({
                    version: rds.AuroraPostgresEngineVersion.VER_16_1,
                }),
                snapshotCredentials: rds.SnapshotCredentials.fromGeneratedSecret('Aws_master'),
                vpc: baseVpc,
                vpcSubnets: {
                    subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
                },
                securityGroups: [securityGroup],
                defaultDatabaseName: 'Awsdb',
                writer: writer,
            });
            Tags.of(cluster).add("schedule", "6am-8pm");

        } else {
            cluster = new rds.DatabaseCluster(this, 'Aws-database', {
                engine: rds.DatabaseClusterEngine.auroraPostgres( {
                    version: rds.AuroraPostgresEngineVersion.VER_16_1
                }),
                credentials: rds.Credentials.fromSecret(secretMaster),
                writer: writer,
                readers: [
                    reader
                ],
                vpcSubnets: {
                    subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
                },
                subnetGroup: subnetGroup,
                securityGroups: [
                    securityGroup
                ],
                vpc: baseVpc,
                defaultDatabaseName: 'Awsdb',
                enableDataApi: true,
            });
            Tags.of(cluster).add("schedule", "6am-8pm", {
                excludeResourceTypes: ['AWS::RDS::DBInstance'],
            });

        }

        // Avoid deleting already provisioned snapshot instances
        (cluster.node.defaultChild as rds.CfnDBInstance).cfnOptions.deletionPolicy = cdk.CfnDeletionPolicy.RETAIN;

        const provisionedBastion =  new bastion.BastionHostAuroraServerlessForward(this, 'AwsBastionHostProvisioned', {
            serverlessCluster: cluster,
            name: "AwsBastionHostProvisioned",
            vpc: baseVpc!
        });

        this.putParameter('bastionProvisionedInstacneId', provisionedBastion.instanceId!)

        return cluster

    }

    protected getSecret(id: string, name: string): ISecret {
        const secretName = this.resolveSecretName(name, this.commonProps.projectPrefix);
        const arn = `arn:aws:secretsmanager:${this.commonProps.appConfig.Project.Region}:${this.commonProps.appConfig.Project.Account}:secret:${secretName}`;

        return Secret.fromSecretPartialArn(this, id, arn);
    }
    protected resolveSecretName(name: string, prefix: string): string {
        return `/${prefix}/${name}`;
    }

}
