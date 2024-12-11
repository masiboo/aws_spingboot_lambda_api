import * as base from "../../lib/template/stack/base/base-stack";
import {AppContext} from "../../lib/template/app-context";
import {EcrResourceConstruct} from "./construct/ecr-resource-construct";

export class EcrResourceStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        const dbAccessRepo = new EcrResourceConstruct(this, "db-access", {
            serviceName: this.stackConfig.DbAccessServiceName,
            stackName: this.stackName,
             projectPrefix: this.projectPrefix,
             env: this.commonProps.env!,
             stackConfig: this.stackConfig,
             variables: this.commonProps.variables,

            repoSuffix: this.commonProps.appConfig.Project.Stage,

        })

        const mediaProcess = new EcrResourceConstruct(this, "media-Access", {
            serviceName: this.stackConfig.MediaProcessServiceName,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            repoSuffix: this.commonProps.appConfig.Project.Stage,

        })

        const mirisProxy = new EcrResourceConstruct(this, "miris-proxy", {
            serviceName: this.stackConfig.MirisProxyServiceName,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            repoSuffix: this.commonProps.appConfig.Project.Stage,

        })
    }

}
