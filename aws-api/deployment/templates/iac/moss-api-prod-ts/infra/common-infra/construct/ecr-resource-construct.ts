import * as base from "../../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import * as ecr from "aws-cdk-lib/aws-ecr";

export interface EcrResourceProps extends base.ConstructCommonProps {
    serviceName: string;
    repoSuffix: string;
}

export class EcrResourceConstruct  extends base.BaseConstruct {

    public ecrRepo: ecr.Repository;

    constructor(scope: Construct, id: string, props: EcrResourceProps) {
        super(scope, id, props);

        this.ecrRepo = new ecr.Repository(this, `${props.stackName}`, {
            repositoryName: `${props.projectPrefix}/${props.serviceName}-${props.repoSuffix}`.toLowerCase()
        });

        // SSM
        this.putParameter(`${props.serviceName}EcrRepositoryUri`, this.ecrRepo.repositoryUri);
        this.putParameter(`${props.serviceName}EcrRepositoryName`, this.ecrRepo.repositoryName);

    }

}