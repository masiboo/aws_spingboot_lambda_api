
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as sd from 'aws-cdk-lib/aws-servicediscovery'

import * as base from '../../lib/template/stack/vpc/vpc-base-stack';
import { AppContext } from '../../lib/template/app-context';
import { Override } from '../../lib/template/stack/base/base-stack';
import {cloudMapNamespaceArnKey, cloudMapNamespaceIdKey, cloudMapNamespaceNameKey} from "../app-main";
import {AbstractEcsBaseStack} from "./ecs-abstract-base-stack";


export abstract class EcsBaseStack extends AbstractEcsBaseStack {
    abstract onEcsPostConstructor(vpc: ec2.IVpc, cluster: ecs.ICluster, ns: sd.IPrivateDnsNamespace): void;

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);
    }

    @Override
    onLookupLegacyVpc(): base.VpcLegacyLookupProps | undefined {
        return {
            vpcNameLegacy: this.getVariable('VpcName')
        };
    }

    @Override
    onPostConstructor(baseVpc?: ec2.IVpc) {
        const ecsClusterName = this.getParameter('ECSClusterName');
        
        this.commonVpc = baseVpc!;
        this.cloudMapNamespace = this.loadCloudMapNamespace();
        this.ecsCluster = this.loadEcsCluster(ecsClusterName, this.commonVpc, this.cloudMapNamespace);

        this.onEcsPostConstructor(this.commonVpc, this.ecsCluster, this.cloudMapNamespace);
    }

    // private loadCloudMapNamespace(): sd.IPrivateDnsNamespace {
    //     const ns = sd.PrivateDnsNamespace.fromPrivateDnsNamespaceAttributes(this, 'cloud-map', {
    //             namespaceName: this.getParameter(cloudMapNamespaceNameKey),
    //             namespaceArn: this.getParameter(cloudMapNamespaceArnKey),
    //             namespaceId: this.getParameter(cloudMapNamespaceIdKey),
    //         });
    //
    //     return ns;
    // }
    //
    // private loadEcsCluster(clusterName: string, vpc: ec2.IVpc, cloudMapNamespace?: sd.INamespace): ecs.ICluster {
    //     if (this.ecsCluster == undefined) {
    //         this.ecsCluster = ecs.Cluster.fromClusterAttributes(this, 'ecs-cluster', {
    //             vpc,
    //             clusterName,
    //             securityGroups: [],
    //             defaultCloudMapNamespace: cloudMapNamespace
    //         });
    //     }
    //
    //     return this.ecsCluster;
    // }
}
