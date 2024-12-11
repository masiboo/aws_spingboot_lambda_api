import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as ssm from 'aws-cdk-lib/aws-ssm';
import * as servicediscovery from 'aws-cdk-lib/aws-servicediscovery';
import * as base from "../../../lib/template/construct/base/base-construct";
import {Bucket} from "aws-cdk-lib/aws-s3";
import {Table} from "aws-cdk-lib/aws-dynamodb";
import {IPrivateDnsNamespace} from "aws-cdk-lib/aws-servicediscovery/lib/private-dns-namespace";
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import {cloudMapNamespaceArnKey, cloudMapNamespaceIdKey, cloudMapNamespaceNameKey} from "../../app-main";
import {Tags} from "aws-cdk-lib";
import * as iam from 'aws-cdk-lib/aws-iam';

export interface EcsInfraProps extends base.ConstructCommonProps {
    shortStackName: string;
    infraVersion: string;
    vpc: ec2.IVpc;
    cluster: ecs.ICluster;
    dockerImageType: string;
    ecrRepo?: ecr.IRepository;
    containerPort: number;
    internetFacing: boolean;
    dockerPath?: string;
    memory: number;
    cpu: number;
    desiredTasks: number;
    autoscaling: boolean;
    minTasks: number;
    maxTasks: number;
    servicePath: string;
    healthCheckPath?: string;
    priority: number;
    enableMigrationWithLiquibase: boolean;
}

export class EcsInfraConstruct extends base.BaseConstruct {
    public readonly service: ecs.FargateService;
    public readonly taskDefinition: ecs.FargateTaskDefinition;
    containerName: string;

    constructor(scope: Construct, id: string, props: EcsInfraProps) {
        super(scope, id, props);

        // Read the image URI from the environment variable

        const albDnsName = this.getParameter(`core/${this.projectPrefix}AlbDnsName`);
        const listenerArn = this.getParameter(`core/${this.projectPrefix}AlbListenerARN`);
        const securityGroupId = this.getParameter(`core/${this.projectPrefix}ServiceSecurityGroupId`)

        const bucketArn = this.getParameter("registryBucketArn");
        const registryBucket = Bucket.fromBucketArn(this, "registry-bucket", bucketArn);

        const tableArn = this.getParameter("registryTableArn");
        const registryTable = Table.fromTableArn(this, "registry-table", tableArn);

        const databaseHostName = this.getParameter('databaseHostName');
        const databaseName = "Awsdb";

        const AwsDbSecret = secretsmanager.Secret.fromSecretNameV2(this, `${props.servicePath}Awsdbsecret`, '/Aws/rds/credentials/master');

        const securityGroup = ec2.SecurityGroup.fromSecurityGroupId(this, `${props.servicePath}importedSecurityId`, securityGroupId)
        // Import the listener
        const listener = elbv2.ApplicationListener.fromApplicationListenerAttributes(
            this,
            `${props.servicePath}ImportedListener`,
            {
                securityGroup: securityGroup,
                listenerArn: listenerArn
            }
        );

        // Create a CloudWatch log group #todo: configuration
        const logGroup = new logs.LogGroup(this, `${props.servicePath}ServiceLogGroup`, {
            logGroupName: `/ecs/${props.servicePath}/${props.shortStackName}`,
            retention: logs.RetentionDays.ONE_WEEK,
            removalPolicy: cdk.RemovalPolicy.DESTROY
        });

        const taskExecutionRole = new iam.Role(this, 'TaskExecutionRole', {
            assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonECSTaskExecutionRolePolicy'),
                iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryReadOnly'),
            ],
        });

        this.taskDefinition = new ecs.FargateTaskDefinition(this, `${props.servicePath}TaskDef`, {
            family: `Aws-${props.servicePath}-task`,
            memoryLimitMiB: props.memory,
            cpu: props.cpu,
            executionRole: taskExecutionRole,
        });
        Tags.of(this.taskDefinition).add("schedule", "6am-8pm");

        // Add container to the task definition
        const containerName = `${props.shortStackName}Container`;
        const container = this.taskDefinition.addContainer(containerName, {
            image: this.getContainerImage(props),
            logging: new ecs.AwsLogDriver({
                logGroup: logGroup,
                streamPrefix: props.shortStackName,
            }),
            environment: {
                APP_NAME: props.servicePath,
                INFRA_VERSION: props.infraVersion,
                CONTAINER_SERVICE: 'AWS ECS',
                PORT: props.containerPort.toString(),
                DDB_TABLE: 'no-table',
                PORT_IN: `${props.containerPort}`,
                Namespace: `${props.projectPrefix}-NS`,
                // TargetServiceName: targetServiceStackName != undefined ? targetServiceStackName : 'not-defined',
                SPRING_DATASOURCE_URL: `jdbc:postgresql://${(databaseHostName)}:5432/${(databaseName)}`,
                SPRING_DATASOURCE_USERNAME: 'Aws_master',
                SPRING_DATASOURCE_PASSWORD: AwsDbSecret.secretValueFromJson("password").unsafeUnwrap(),
                SPRING_JPA_HIBERNATE_DDL_AUTO: 'validate',
                SERVER_SERVLET_CONTEXT_PATH: `/${props.servicePath}`,
                SPRING_LIQUIBASE_CHANGE_LOG: 'classpath:db/changelog/db.changelog-master.xml',
                SPRING_LIQUIBASE_ENABLED: props.enableMigrationWithLiquibase.toString(),
                SPRING_LIQUIBASE_CLEAR_CHECKSUMS: 'true',
                // SPRING_LIQUIBASE_DROP_FIRST: 'false', // DANGER
                //SPRING_LIQUIBASE_DEFAULT_SCHEMA: 'Aws',
                //SPRING_LIQUIBASE_LIQUIBASE_SCHEMA: 'Aws',
                SPRING_LIQUIBASE_SHOW_SUMMARY_OUTPUT: 'all',
            },
        });

        container.addPortMappings({
            containerPort: props.containerPort,
            protocol: ecs.Protocol.TCP,
        });

        let cloudNS: IPrivateDnsNamespace;
        cloudNS = servicediscovery.PrivateDnsNamespace.fromPrivateDnsNamespaceAttributes(this, `${props.servicePath}-privateDns`, {
            namespaceArn: this.getParameter(cloudMapNamespaceArnKey),
            namespaceId: this.getParameter(cloudMapNamespaceIdKey),
            namespaceName: this.getParameter(cloudMapNamespaceNameKey),
        });

        // Create the Fargate service
        this.service = new ecs.FargateService(this, `${props.servicePath}Service`, {
            cluster: props.cluster,
            taskDefinition: this.taskDefinition,
            desiredCount: props.desiredTasks,
            // assignPublicIp: props.internetFacing,
            serviceName: `Aws-${props.servicePath}`,
            // cloudMapOptions:
            cloudMapOptions: {
                name: `${props.projectPrefix}-${props.servicePath}`.toLowerCase(),
                cloudMapNamespace: cloudNS,
            },
            circuitBreaker: {
                rollback: true
            },
        });

        // Create a new target group
        const targetGroup = new elbv2.ApplicationTargetGroup(this, `${props.servicePath}TargetGroup`, {
            targetGroupName: `Aws-${props.servicePath}-target-group`,
            vpc: props.vpc,
            port: props.containerPort,
            protocol: elbv2.ApplicationProtocol.HTTP,
            targets: [this.service],
            healthCheck: {
                path: `/${props.servicePath}${props.healthCheckPath}` || '/health',
                interval: cdk.Duration.seconds(30),
                timeout: cdk.Duration.seconds(5),
            },
        });

        registryTable.grantWriteData(this.taskDefinition.taskRole)

        // We need to give our fargate container permission to put events on our EventBridge
        // this.taskDefinition.addToTaskRolePolicy(eventbridgePutPolicy);
        // this.taskDefinition.addToTaskRolePolicy(ecrPolicy);
        // Grant fargate container access to the object that was uploaded to s3
        registryBucket.grantReadWrite(this.taskDefinition.taskRole);

        const serviceUrl = `http://${albDnsName}/${props.servicePath}`
        // Add the target group to the listener
        listener.addTargetGroups(`${props.servicePath}Rule`, {

            targetGroups: [targetGroup],
            priority: props.priority,
            conditions: [
                elbv2.ListenerCondition.pathPatterns([`/${props.servicePath}*`]),
            ],
        });

        // Set up autoscaling if enabled
        if (props.autoscaling) {
            const scaling = this.service.autoScaleTaskCount({
                minCapacity: props.minTasks,
                maxCapacity: props.maxTasks,
            });

            scaling.scaleOnCpuUtilization(`${props.servicePath}CpuScaling`, {
                targetUtilizationPercent: 50,
                scaleInCooldown: cdk.Duration.seconds(60),
                scaleOutCooldown: cdk.Duration.seconds(60),
            });
        }

        // Grant permissions to pull images from ECR if using ECR
        if (props.dockerImageType === 'ECR' && props.ecrRepo) {
            props.ecrRepo.grantPull(this.taskDefinition.executionRole!);
        }


        // Output the service URL
        new cdk.CfnOutput(this, `${props.servicePath}-serviceUrl`, {
            value: serviceUrl,
            description: `${props.servicePath} Service URL`,
        });

        this.putParameter(`core/${props.servicePath}AlbDnsName`, serviceUrl)
    }

    private getContainerImage(props: EcsInfraProps): ecs.ContainerImage {
        const imageTag = process.env.IMAGETAG;
        switch (props.dockerImageType) {
            case 'ECR':
                if (!props.ecrRepo) {
                    throw new Error('ECR repository must be provided when using ECR image type');
                }

                if (imageTag) {
                    // Use the specified image tag
                    return ecs.ContainerImage.fromEcrRepository(props.ecrRepo, imageTag);
                } else {
                    // Default to 'latest' if no tag is specified
                    return ecs.ContainerImage.fromEcrRepository(props.ecrRepo);
                }
            case 'DOCKER_HUB':
                return ecs.ContainerImage.fromRegistry('amazon/amazon-ecs-sample');
            case 'ASSET':
                if (!props.dockerPath) {
                    throw new Error('Docker path must be provided when using asset image type');
                }
                return ecs.ContainerImage.fromAsset(props.dockerPath);
            default:
                throw new Error(`Unsupported docker image type: ${props.dockerImageType}`);
        }
    }
}