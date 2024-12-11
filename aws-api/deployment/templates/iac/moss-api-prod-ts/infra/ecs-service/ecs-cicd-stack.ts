
import * as codecommit from 'aws-cdk-lib/aws-codecommit';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';

import * as base from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';

import { EcsCicdConstruct } from './construct/ecs-cicd-const'

export class EcsCicdStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        const ecsService = ecs.BaseService.fromServiceArnWithCluster(this, 'service', 'your-service-arn');
        const containerName = 'your-container-name';

        const gitRepo = codecommit.Repository.fromRepositoryName(this, 'git', 'your-repo-name');
        const ecrRepo = ecr.Repository.fromRepositoryName(this, 'ecr', 'your-ecr-name');
        const appPath = 'your-app-path';

        new EcsCicdConstruct(this, 'EcsCicdConstrunct', {
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            service: ecsService,
            containerName: containerName,
            appPath: appPath,
            ecrRepo: ecrRepo
        });

    }
}
