import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {Duration} from "aws-cdk-lib";
import {AwsEventsConstruct} from "../common-infra/AwsEventsConstruct";
import {AwsAuthEventsConstruct} from "../common-infra/AwsAuthEventsConstruct";

export class AwsAuthEventsStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        new AwsAuthEventsConstruct(this, 'AwsAuthEventsConstruct', {
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            lambdaEventFunctions: [
            ],


        })

    }


}
