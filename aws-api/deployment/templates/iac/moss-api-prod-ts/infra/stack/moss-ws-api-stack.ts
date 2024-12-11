import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AwsWsApiConstruct} from "../common-infra/AwsWsApiConstruct";

export class AwsWsApiStack extends base.BaseStack {
    
    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        new AwsWsApiConstruct(this,'AwsWsApiConstruct',{
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,
            lambdaWSFunctions: [
                {
                    functionName:  'ws-connection-function',
                    functionId: 'WSConnectionFunction' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    integerationId: 'WSConnectionIntegeration',
                    springCloudRouterDefinition: 'WSConnectionHandler',
                    useFunction: true,
                    routeKey: '$connect'
                },
                {
                    functionName:  'ws-disconnection-function',
                    functionId: 'WSDisonnectionFunction' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    integerationId: 'WSDisconnectionIntegeration',
                    springCloudRouterDefinition: 'WSDisconnectionHandler',
                    useFunction: true,
                    routeKey: '$disconnect'
                },
                {
                    functionName:  'ws-default-message-function',
                    functionId: 'WSDefaultMessageFunction' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    integerationId: 'WSDefaultMessageIntegeration',
                    springCloudRouterDefinition: 'WSDefaultMessageHandler',
                    useFunction: true,
                    routeKey: '$default'
                },
                {
                    functionName:  'ws-message-function',
                    functionId: 'WSMessageFunction' ,
                    handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
                    integerationId: 'WSMessageIntegeration',
                    springCloudRouterDefinition: 'WSMessageHandler',
                    useFunction: true,
                    routeKey: 'sendMessage',
                    executeApiPolicy: true
                }
            ]
        })
    }
}