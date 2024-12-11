import {IVpc} from 'aws-cdk-lib/aws-ec2';

import * as base from '../../lib/template/stack/vpc/vpc-base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AlbConstruct} from "./construct/ecs-alb-const";
import {StackConfig} from "../../lib/template/app-config";
import {Override} from "../../lib/template/stack/base/base-stack";

export interface AwsCoreEcsAlbServiceProps extends StackConfig {

}

export class AwsCoreEcsAlbServiceStack extends base.VpcBaseStack {

    constructor(appContext: AppContext, stackConfig: AwsCoreEcsAlbServiceProps) {
        super(appContext, stackConfig);

    }

    @Override
    onLookupLegacyVpc(): base.VpcLegacyLookupProps | undefined {
        return {
            vpcNameLegacy: this.getVariable('VpcName'),
        };
    }

    @Override
    onPostConstructor(baseVpc?: IVpc): void {

        new AlbConstruct(this, `${this.projectPrefix}AlbConstruct`, {
            internetFacing: this.stackConfig.InternetFacing,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            vpc: baseVpc!,

        })
    }


}
