import {RemovalPolicy} from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {IVpc, SecurityGroup} from 'aws-cdk-lib/aws-ec2';
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
import {ApplicationLoadBalancer} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {AbstractEcsBaseStack} from "../ecs-service/ecs-abstract-base-stack";

const PORT = 80

export class AwsCoreVpcEcsPgWebStack extends AbstractEcsBaseStack {

    constructor(appContext: AppContext, stackConfig: StackConfig) {
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

        const databaseHostName = this.getParameter('DatabaseHostName');
        const databaseName = this.getParameter('DatabaseName');
        const databaseSecretArn = this.getParameter('DatabaseSecretArn');
        const databaseSecret = sm.Secret.fromSecretCompleteArn(this, 'secret', databaseSecretArn);
        const secretion = databaseSecret.secretValueFromJson("password").unsafeUnwrap().toString();

        this.createECSService(databaseHostName, databaseName, databaseSecret, secretion, baseVpc!, "db-access");

    }

    private createECSService(databaseHostName: string, databaseName: string, databaseSecret: sm.ISecret, secretion: string, vpc: ec2.IVpc, serviceName: string) {

        const ecsClusterName = this.getParameter('ECSClusterName');

        // this.commonVpc = baseVpc!;
        // this.cloudMapNamespace = this.loadCloudMapNamespace();
        // this.ecsCluster = this.loadEcsCluster(ecsClusterName, this.commonVpc, this.cloudMapNamespace);
        //
        // 1
        const taskRolePolicy = new PolicyStatement({
            effect: Effect.ALLOW,
            resources: ['*'],
            actions: [
                "ecs:DescribeTasks",
                "ecs:ListTasks",
                "dynamodb:*"
            ]
        });

        const taskrole = new Role(this, `Aws-${serviceName}-task-execution-role`, {
            roleName: `Aws-${serviceName}-task-execution-role`,
            assumedBy: new ServicePrincipal("ecs-tasks.amazonaws.com"),
        });

        taskrole.addToPolicy(taskRolePolicy);

        taskrole.addManagedPolicy(
            ManagedPolicy.fromAwsManagedPolicyName(
                "service-role/AmazonECSTaskExecutionRolePolicy"
            )
        );


        // Task Definitions
        const serviceTaskDefinition = new FargateTaskDefinition(
            this,
            `Aws-${serviceName}-service-task-definition`,
            {

                taskRole: taskrole,
                executionRole: taskrole,
                family: "ecs-blueprint",
                memoryLimitMiB: this.stackConfig.Memory,
                cpu: this.stackConfig.Cpu,
            }
        );


        // 1.

        // Log Groups
        const serviceLogGroup = new LogGroup(this, `web-${serviceName}-service-log-group`, {
            removalPolicy: RemovalPolicy.DESTROY,
            logGroupName: `web-${serviceName}-service-log-group`
        });

        const serviceLogDriver = new AwsLogDriver({
            logGroup: serviceLogGroup,
            streamPrefix: "service",
        });

        // 2.
        // Task Containers
        const serviceContainer = serviceTaskDefinition.addContainer(
            `web-${serviceName}`,
            {
                containerName: `web-${serviceName}`,
                // Replace the following line with the line below after the stack was initialized.
                // image: ecs.ContainerImage.fromRegistry('dpage/pgadmin4:6.20'),
                // image: ContainerImage.fromEcrRepository(repo),
                // image: ecs.ContainerImage.fromRegistry('public.ecr.aws/ecs-sample-image/amazon-ecs-sample:latest'),
                image: ecs.ContainerImage.fromAsset(this.stackConfig.FilePath),
                logging: serviceLogDriver,
                portMappings: [{
                    containerPort: PORT
                }],
                environment: {
                    HOST_NAME: databaseHostName,
                    DATABASE_NAME: databaseName,
                    SECRET_ARN: `${databaseSecret}`,
                    PGUSER: "postgres",
                    PGPASSWORD: secretion,
                    PGDATABASE: databaseName,
                    PGPORT: "5432",
                    PGHOST: databaseHostName,
                    DATABASE_URL: `ecto://postgres:${secretion}@${databaseHostName}:5432/${databaseName}`,
                    DEFAULT_DB_HOST_ENDPOINT: databaseHostName,
                    DEFAULT_DB_PORT: '5432',
                    DEFAULT_DB_NAME: databaseName,
                    PGADMIN_DEFAULT_EMAIL: 'devops@naakwu.ng',
                    PGADMIN_DEFAULT_PASSWORD: 'supersecretstuffname',
                    SPRING_DATASOURCE_URL: `jdbc:postgresql://${databaseHostName}:5432/${databaseName}`,
                    SPRING_DATASOURCE_USERNAME: 'postgres',
                    SPRING_DATASOURCE_PASSWORD: `${secretion}`,
                    SPRING_JPA_HIBERNATE_DDL_AUTO: 'update'
                },
            }
        );

        //2.


        //Security Groups
        const serviceSecGrp = new SecurityGroup(
            this,
            `web-${serviceName}-service-security-group`,
            {
                allowAllOutbound: true,
                securityGroupName: `${serviceName}sevSecurityGroup`,
                vpc,
            }
        );

        serviceSecGrp.connections.allowFromAnyIpv4(ec2.Port.tcp(PORT));


    }

    private createPGWebStack(serviceName: string, vpc: IVpc, serviceTaskDefinition: FargateTaskDefinition, serviceSecGrp: SecurityGroup) {

        const pgWebAlbSecurityGroup = new SecurityGroup(this, 'pg-web-alb-security-group', {
            vpc: vpc,
            securityGroupName: `pg-${serviceName}-security-group`,
            allowAllOutbound: true
        });

        const pgWebApplicationLoadBalancer = new ApplicationLoadBalancer(this, `pg-web-alb`, {
            loadBalancerName: `alb-${serviceName}-pgweb`,
            vpc: vpc,
            internetFacing: true,
            securityGroup: pgWebAlbSecurityGroup,
        });

        const pattern = new ApplicationLoadBalancedFargateService(this, `web-${serviceName}-fargate-service`, {
            cluster: this.ecsCluster,
            loadBalancer: pgWebApplicationLoadBalancer,
            assignPublicIp: true,
            taskDefinition: serviceTaskDefinition,
            securityGroups: [serviceSecGrp],
        });

        pattern.targetGroup.configureHealthCheck({
            path: "/misc/ping",
        });
    }

// private newOriginal(databaseHostName: string, databaseName: string, databaseSecret: sm.ISecret, secretion: string, vpc: ec2.IVpc, serviceName: string) {


    //     const cluster = new ecs.Cluster(this, `core-${serviceName}-cluster`, {
    //         vpc,
    //         clusterName: `core-${serviceName}-cluster`
    //     });

    //     // 1
    //     const taskRolePolicy = new PolicyStatement({
    //         effect: Effect.ALLOW,
    //         resources: ['*'],
    //         actions: [
    //             "ecs:DescribeTasks",
    //             "ecs:ListTasks",
    //             "dynamodb:*"
    //         ]
    //     });

    //     const taskrole = new Role(this, `core-${serviceName}-task-execution-role`, {
    //         roleName: `core-${serviceName}-task-execution-role`,
    //         assumedBy: new ServicePrincipal("ecs-tasks.amazonaws.com"),
    //     });

    //     taskrole.addToPolicy(taskRolePolicy);

    //     taskrole.addManagedPolicy(
    //         ManagedPolicy.fromAwsManagedPolicyName(
    //             "service-role/AmazonECSTaskExecutionRolePolicy"
    //         )
    //     );

    //     // Task Definitions
    //     const serviceTaskDefinition = new FargateTaskDefinition(
    //         this,
    //         `core-${serviceName}-service-task-definition`,
    //         {

    //             taskRole: taskrole,
    //             executionRole: taskrole,
    //             family: "ecs-blueprint",
    //             memoryLimitMiB: this.stackConfig.Memory,
    //             cpu: this.stackConfig.Cpu,
    //         }
    //     );

    //     // 1.

    //     // Log Groups
    //     const serviceLogGroup = new LogGroup(this, `core-${serviceName}-service-log-group`, {
    //         removalPolicy: RemovalPolicy.DESTROY,
    //         logGroupName: `core-${serviceName}-service-log-group`
    //     });

    //     const serviceLogDriver = new AwsLogDriver({
    //         logGroup: serviceLogGroup,
    //         streamPrefix: "service",
    //     });

    //     // 2.
    //     // Task Containers
    //     const serviceContainer = serviceTaskDefinition.addContainer(
    //         `core-${serviceName}`,
    //         {
    //             containerName: `core-${serviceName}`,
    //             // Replace the following line with the line below after the stack was initialized.
    //             // image: ecs.ContainerImage.fromRegistry('public.ecr.aws/ecs-sample-image/amazon-ecs-sample:latest'),
    //             // image: ContainerImage.fromEcrRepository(repo),
    //             image: ecs.ContainerImage.fromAsset(this.stackConfig.FilePath),
    //             logging: serviceLogDriver,
    //             portMappings: [{
    //                 containerPort: PORT
    //             }],
    //             environment: {
    //                 // HOST_NAME: databaseHostName,
    //                 // DATABASE_NAME: databaseName,
    //                 // SECRET_ARN: `${databaseSecret}`,
    //                 // PGUSER: "postgres",
    //                 // PGPASSWORD: secretion,
    //                 // PGDATABASE: databaseName,
    //                 // PGPORT: "5432",
    //                 // PGHOST: databaseHostName,
    //                 SPRING_DATASOURCE_URL: `jdbc:postgresql://${databaseHostName}:5432/${databaseName}`,
    //                 SPRING_DATASOURCE_USERNAME: "postgres",
    //                 SPRING_DATASOURCE_PASSWORD: secretion,
    //                 SPRING_JPA_HIBERNATE_DDL_AUTO: "update"

    //             },
    //         }
    //     );

    //     //2.


    //     //Security Groups
    //     const serviceSecGrp = new SecurityGroup(
    //         this,
    //         `core-${serviceName}-service-security-group`,
    //         {
    //             allowAllOutbound: true,
    //             securityGroupName: `${serviceName}sevSecurityGroup`,
    //             vpc,
    //         }
    //     );

    //     serviceSecGrp.connections.allowFromAnyIpv4(ec2.Port.tcp(PORT));

    //     // Fargate Services
    //     // const fargateService = new ecs.FargateService(this, `core-${serviceName}-fargate-service`, {
    //     //     serviceName: `core-${serviceName}-fargate-service`,
    //     //     cluster: cluster,
    //     //     taskDefinition: serviceTaskDefinition,
    //     //     assignPublicIp: true,
    //     //     desiredCount: 1,
    //     //     securityGroups: [serviceSecGrp],
    //     // });
    //     //
    //     // ALB

    //     const pgWebAlbSecurityGroup = new ec2.SecurityGroup(this, 'pg-web-alb-security-group', {
    //         vpc: vpc,
    //         securityGroupName: this.resolveResourceName('sgalb'),
    //         allowAllOutbound: true
    //     });

    //     const privateSubnet1 = this.getVariable('privateSubnet1')
    //     const privateSubnet2 = this.getVariable('privateSubnet2')
    //     const privateSubnet3 = this.getVariable('privateSubnet3')


    //     const pgWebApplicationLoadBalancer = new ApplicationLoadBalancer(this, `pg-alb`, {
    //         loadBalancerName: this.resolveResourceName('pgalb'),
    //         vpc: vpc,
    //         internetFacing: false,
    //         securityGroup: pgWebAlbSecurityGroup,
    //         vpcSubnets: {
    //             subnets: [ ec2.Subnet.fromSubnetId(this, 'alb-subnet-1', this.getVariable('privateSubnet1')),
    //                 ec2.Subnet.fromSubnetId(this, 'alb-subnet-2', this.getVariable('privateSubnet2')),
    //               ]
    //         }
    //     });

    //     const pattern = new ecsPatterns.ApplicationLoadBalancedFargateService(this, "PGWebDBAccessService", {
    //         serviceName: `core-${serviceName}-service`,
    //         cluster: cluster,
    //         loadBalancer: pgWebApplicationLoadBalancer,
    //         assignPublicIp: false,
    //         taskDefinition: serviceTaskDefinition,
    //         securityGroups: [serviceSecGrp],
    //         desiredCount: 1,
    //     });

    //     pattern.targetGroup.configureHealthCheck({
    //         path: "/actuator/health",
    //     });

    //     // NLB
    //     // const httpApiInternalNLB = new NetworkLoadBalancer(this, `core-${serviceName}-network-load-balancer`, {
    //     //     vpc,
    //     //     internetFacing: false,
    //     //     vpcSubnets: {
    //     //         subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
    //     //     },
    //     // })
    //     //
    //     // // NLB Listener
    //     // const httpApiListener = httpApiInternalNLB.addListener(`core-${serviceName}-api-listener`, {
    //     //     port: PORT,
    //     // });
    //     //
    //     // // Target Groups
    //     // httpApiListener.addTargets(
    //     //     `core-${serviceName}-service-target-group`,
    //     //     {
    //     //         port: PORT,
    //     //         targets: [fargateService],
    //     //     }
    //     // );

    //     //VPC Link
    //     // const httpVpcLink = new CfnResource(this, `core-${serviceName}-vpc-link`, {
    //     //     type: "AWS::ApiGatewayV2::VpcLink",
    //     //     properties: {
    //     //         Name: `api-vpclink-${serviceName}`,
    //     //         SubnetIds: vpc.publicSubnets.map((m) => m.subnetId),
    //     //     },
    //     // });

    //     const api = new HttpApi(this, `core-${serviceName}-http-api`, {
    //         createDefaultStage: true,
    //     });

    //     // API Integration
    //     // const integration = new CfnIntegration(
    //     //     this,
    //     //     `core-${serviceName}-api-integration`,
    //     //     {
    //     //         apiId: api.httpApiId,
    //     //         // connectionId: httpVpcLink.ref,
    //     //         // connectionType: "VPC_LINK",
    //     //         connectionType: "INTERNET",
    //     //         description: "API Integration",
    //     //         integrationMethod: "ANY",
    //     //         integrationType: "HTTP_PROXY",
    //     //         // For an HTTP API private integration, specify the ARN of an Application Load Balancer listener,
    //     //         integrationUri: pattern.targetGroup.targetGroupArn,
    //     //         payloadFormatVersion: "1.0",
    //     //     }
    //     // );

    //     // API Route
    //     // new CfnRoute(this, `core-${serviceName}-api-route`, {
    //     //     apiId: api.httpApiId,
    //     //     routeKey: "ANY /{proxy+}",
    //     //     target: `integrations/${integration.ref}`,
    //     // });

    //     // DynamoDB
    //     // const table = new cdk.aws_dynamodb.Table(this, "Customer", {
    //     //     partitionKey: { name: "Id", type: cdk.aws_dynamodb.AttributeType.STRING, },
    //     //     tableName: "Customer",
    //     //     readCapacity: 50,
    //     //     writeCapacity: 50,
    //     //     removalPolicy: cdk.RemovalPolicy.DESTROY, // NOT recommended for production code
    //     // });

    //     new CfnOutput(this, `${serviceName}-endpointURL`, {
    //         value: api.apiEndpoint,
    //     });

    // }


}
