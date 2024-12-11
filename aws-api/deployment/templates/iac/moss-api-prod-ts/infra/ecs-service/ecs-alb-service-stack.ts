
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as sd from 'aws-cdk-lib/aws-servicediscovery'

import * as base from './ecs-base-stack';
import { AppContext } from '../../lib/template/app-context';
import { Override } from '../../lib/template/stack/base/base-stack';

import { EcsECRConstruct } from './construct/ecs-repo-const'
import { EcsInfraConstruct } from './construct/ecs-infra-const'
import { EcsCicdConstruct } from './construct/ecs-cicd-const'
import { EcsAlbMonitorConstruct } from './construct/ecs-monitor-const'
import * as ecr from "aws-cdk-lib/aws-ecr";
import {RemovalPolicy, Tags} from "aws-cdk-lib";

export class EcsAlbServiceStack extends base.EcsBaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);
    }

    @Override
    onEcsPostConstructor(vpc: ec2.IVpc, cluster: ecs.ICluster, ns: sd.IPrivateDnsNamespace) {

        const repoName = this.stackConfig.EcrRepoName
        const account_id = this.stackConfig.EcrAccount

        // Import the ECR repository
        const importedRepository = ecr.Repository.fromRepositoryAttributes(this, 'ImportedRepo', {
            repositoryArn: `arn:aws:ecr:${this.region}:${account_id}:repository/${repoName}`,
            repositoryName: `${repoName}`,
        });

        const infra = new EcsInfraConstruct(this, `${this.stackConfig.ServicePath}coreconstruct`, {
            priority: this.stackConfig.Priority,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            shortStackName: this.stackConfig.ShortStackName,
            infraVersion: this.stackConfig.InfraVersion,
            dockerImageType: this.stackConfig.DockerImageType,
            vpc: vpc,
            cluster: cluster!,
            ecrRepo: importedRepository,
            healthCheckPath: this.stackConfig.HealthCheckPath,
            internetFacing: this.stackConfig.InternetFacing,
            containerPort: this.stackConfig.PortNumber,
            dockerPath: this.stackConfig.AppPath,
            cpu: this.stackConfig.Cpu,
            memory: this.stackConfig.Memory,
            desiredTasks: parseInt(this.stackConfig.DesiredTasks, 10),
            autoscaling: this.stackConfig.AutoScalingEnable,
            minTasks: parseInt(this.stackConfig.AutoScalingMinCapacity,10),
            maxTasks: parseInt(this.stackConfig.AutoScalingMaxCapacity,10),
            servicePath: this.stackConfig.ServicePath,
            enableMigrationWithLiquibase: (this.stackConfig.EnableMigrationWithLiquibase != null) ?  this.stackConfig.EnableMigrationWithLiquibase : false
        });
        Tags.of(infra).add("schedule", "6am-8pm");

        // new EcsCicdConstruct(this, 'MicroserviceCICDConstruct', {
        //     stackName: this.stackName,
        //     projectPrefix: this.projectPrefix,
        //     env: this.commonProps.env!,
        //     stackConfig: this.stackConfig,
        //     variables: this.commonProps.variables,
        //
        //     service: infra.service,
        //     containerName: infra.containerName,
        //     appPath: this.stackConfig.AppPath,
        //     ecrRepo: repositoryNew
        // });

        // new EcsAlbMonitorConstruct(this, 'MicroserviceMonitorConstruct', {
        //     stackName: this.stackName,
        //     projectPrefix: this.projectPrefix,
        //     env: this.commonProps.env!,
        //     stackConfig: this.stackConfig,
        //     variables: this.commonProps.variables,
        //
        //     alb: infra.alb,
        //     ecsSrevice: infra.service,
        //     alarmThreshold: this.stackConfig.AlarmThreshold,
        //     subscriptionEmails: this.stackConfig.SubscriptionEmails,
        //     table: infra.table,
        // });
    }
}
