import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {RegistryConstruct} from "./construct/registry-const";

export class AwsRegistryInfraStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        const domainName = `Aws.madrid.${appContext.appConfig.Project.Stage}.web1.wipo.int`
        const siteSubDomain = "api"

        const regsitry = new RegistryConstruct(this, 'RegistryConstruct', {
            stage: this.commonProps.appConfig.Project.Stage,
            account: this.commonProps.appConfig.Project.Account,
            region: this.commonProps.appConfig.Project.Region,
            env: this.commonProps.env!,
            projectPrefix: this.projectPrefix,
            stackConfig: this.stackConfig,
            stackName:  this.stackName,
            tableName: this.stackConfig.DynamoDBTableName,
            queueName: this.stackConfig.ObjectQueueName,
            s3BucketName: this.stackConfig.S3RegistryName,
            apiGateWayName: this.stackConfig.ApiGateWayName,
            auditEventsTableName: this.stackConfig.AuditEventsDDBTableName
        })

    }

}
