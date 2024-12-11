import * as base from '../../lib/template/stack/base/us-east-base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AwsAPIDocsFrontEndConstruct} from "../common-infra/AwsAPIDocsFrontEnConstruct";

export class AwsApiDocsStack extends base.CrossRegionBaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        // #todo revert to config file for prod and acc
        const domainName = `Aws.madrid.${appContext.appConfig.Project.Stage}.web1.aws.int`
        const siteSubDomain = "docs"

        new AwsAPIDocsFrontEndConstruct(this, 'AwsAPIDocsFrontEndConstruct', {
            buildNumber: 0,
            buildStage: this.commonProps.projectPrefix,
            project: this.projectPrefix,
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,
            domainName,
            siteSubDomain,
            certificateArnVariableKey: "docsCertificateVarArn",
            websiteDistSourcePath: "../../../artifacts/lambda/doc.zip"
        })

    }


}
