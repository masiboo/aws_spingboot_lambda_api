
import { Construct } from 'constructs';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as codecommit from 'aws-cdk-lib/aws-codecommit';

import * as base from '../../../lib/template/construct/base/base-construct'


export interface EcsECRProps extends base.ConstructCommonProps {
    shortStackName: string;
}

export class EcsECRConstruct extends base.BaseConstruct {
    public gitRepo: codecommit.Repository;
    public ecrRepo: ecr.Repository;

    constructor(scope: Construct, id: string, props: EcsECRProps) {
        super(scope, id, props);

        const repoSuffix = 'repo';

        this.ecrRepo = new ecr.Repository(this, `${props.stackName}EcrRepository`, {
            repositoryName: `${props.stackName}-${repoSuffix}`.toLowerCase()
        });
        this.exportOutput(`${props.shortStackName}ECRName`, this.ecrRepo.repositoryName);
    }
}