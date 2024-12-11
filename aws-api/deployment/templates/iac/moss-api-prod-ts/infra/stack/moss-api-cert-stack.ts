import * as base from '../../lib/template/stack/base/us-east-base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AwsFrontEndConstruct} from "../common-infra/AwsFrontEndConstruct";
import {AwsApiCertConstruct} from "../common-infra/AwsApiCertConstruct";

export class AwsApiCertStack extends base.CrossRegionBaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        // #todo revert to config file for prod and acc
        const domainName = `Aws.madrid.${appContext.appConfig.Project.Stage}.web1.wipo.int`
        const apiSubDomain = 'api'

        new AwsApiCertConstruct(this, "AwsCertConstruct", {
            certificateArnVariableKey: "certificateArn",
            domainName,
            env: this.commonProps.env!,
            projectPrefix: this.projectPrefix,
            siteSubDomain: apiSubDomain,
            stackConfig: this.stackConfig,
            stackName: this.stackName,
        })

    }




}
