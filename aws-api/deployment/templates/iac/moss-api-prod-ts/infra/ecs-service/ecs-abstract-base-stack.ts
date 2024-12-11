import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as sd from 'aws-cdk-lib/aws-servicediscovery'

import * as base from '../../lib/template/stack/vpc/vpc-base-stack';
import { AppContext } from '../../lib/template/app-context';
import { Override } from '../../lib/template/stack/base/base-stack';
import {cloudMapNamespaceArnKey, cloudMapNamespaceIdKey, cloudMapNamespaceNameKey} from "../app-main";


export abstract class AbstractEcsBaseStack extends base.VpcBaseStack {
    protected commonVpc: ec2.IVpc;
    protected ecsCluster: ecs.ICluster;
    protected cloudMapNamespace: sd.IPrivateDnsNamespace;



    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);
    }

    protected getVpc(): ec2.IVpc {
        return this.commonVpc;
    }

    protected getCluster(): ecs.ICluster {
        return this.ecsCluster;
    }

    protected getNamespace(): sd.IPrivateDnsNamespace {
        return this.cloudMapNamespace;
    }


    protected loadCloudMapNamespace(): sd.IPrivateDnsNamespace {
        const ns = sd.PrivateDnsNamespace.fromPrivateDnsNamespaceAttributes(this, 'cloud-map', {
                namespaceName: this.getParameter(cloudMapNamespaceNameKey),
                namespaceArn: this.getParameter(cloudMapNamespaceArnKey),
                namespaceId: this.getParameter(cloudMapNamespaceIdKey),
            });

        return ns;
    }

    protected loadEcsCluster(clusterName: string, vpc: ec2.IVpc, cloudMapNamespace?: sd.INamespace): ecs.ICluster {
        if (this.ecsCluster == undefined) {
            this.ecsCluster = ecs.Cluster.fromClusterAttributes(this, 'ecs-cluster', {
                vpc,
                clusterName,
                securityGroups: [],
                defaultCloudMapNamespace: cloudMapNamespace
            });
        }

        return this.ecsCluster;
    }
}
