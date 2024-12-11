import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {Duration} from "aws-cdk-lib";
import {AwsEventsConstruct} from "../common-infra/AwsEventsConstruct";

export class AwsEventsStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        new AwsEventsConstruct(this, 'AwsEventsConstruct', {
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            lambdaEventFunctions: [
                {
                    functionName:  's3-files-event-trigger-function',
                    functionId: 'SQSEventFunction' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    s3Policy: true,
                    springCloudRouterDefinition: 'SQSS3EventHandler',
                    useFunction: true,
                    s3EventHandler: true,
                    sQsParameterKey: "objectCreatedSQSArn",
                    timeOut: Duration.seconds(900),
                    codePath: '../../../artifacts/lambda/runtimev2.jar'
                },
                {
                    functionName:  'ddb-batch-event-trigger-function',
                    functionId: 'SQSBatchEventFunciton' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    s3Policy: true,
                    springCloudRouterDefinition: 'SQSBatchEventHandler',
                    useFunction: true,
                    s3EventHandler: true,
                    sQsParameterKey: "updatedBatchSqsQueueARN",
                    timeOut: Duration.seconds(900),
                    codePath: '../../../artifacts/lambda/runtimev2.jar'
                }
            ],


        })

    }


}
