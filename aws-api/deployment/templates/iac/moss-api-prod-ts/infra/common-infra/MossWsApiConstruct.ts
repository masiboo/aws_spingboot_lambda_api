import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Code,Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import {Construct} from "constructs";
import {Duration} from "aws-cdk-lib";
import * as iam from 'aws-cdk-lib/aws-iam';
import { PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Policy } from "aws-cdk-lib/aws-iam";
import {WebSocketLambdaIntegration} from "aws-cdk-lib/aws-apigatewayv2-integrations";
import {WebSocketApi} from "aws-cdk-lib/aws-apigatewayv2";


export interface AwsWsApiProps extends ConstructCommonProps {

   lambdaWSFunctions: LambdaListType[],
}

export type LambdaListType = {
    functionName: string,
    functionId: string ,
    handlerPath: string,
    integerationId: string | undefined,
    useFunction?: boolean,
    springCloudRouterDefinition?: string,
    routeKey?:string,
    executeApiPolicy?: boolean
}

export class AwsWsApiConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsWsApiProps) {
        super(scope, id, props);
        const webSocketApi = new WebSocketApi(this, 'Awswsapi');

        const apigwExecApiStatement = new PolicyStatement({
            actions: ['execute-api:ManageConnections'],
            resources: ["*"]
        });

        const executeApigwPolicy = new Policy( this, "api-execute-policy", {
            statements: [apigwExecApiStatement]
        })

        const lambdaRole = new iam.Role(this, 'SendMessageRole', {
            assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
            managedPolicies: [iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole')]
        });
       
        const lambdaWSList : LambdaListType[] = props.lambdaWSFunctions;
        lambdaWSList.map(functionValue => {
            const enabled = !!functionValue.useFunction
            if (enabled) {

                const lambdaFunction = this.createV2LambdaFunction(
                    this.getName(props, `${functionValue.functionName}-v2`),
                    `${functionValue.functionId}V2`,
                    'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',            
                    Duration.seconds(100),
                    functionValue.springCloudRouterDefinition ? functionValue.springCloudRouterDefinition : "getHealth",
                    functionValue.executeApiPolicy? lambdaRole: undefined
                )
                if (functionValue.integerationId != null && functionValue.routeKey != null){
                    this.createWebSocketRoute(webSocketApi,functionValue.routeKey
                        ,new WebSocketLambdaIntegration (functionValue.integerationId,lambdaFunction));
                }

                if (functionValue.executeApiPolicy) {
                    lambdaFunction.role?.attachInlinePolicy(executeApigwPolicy)
                }

            }

        });
    }
        
    // https://docs.aws.amazon.com/cdk/api/v2/docs/@aws-cdk_aws-apigatewayv2-alpha.WebSocketApi.html
    private createWebSocketRoute(webSocketApi:WebSocketApi, routeKey: string,lambdaIntegration: WebSocketLambdaIntegration){
        webSocketApi.addRoute(routeKey,{ integration :lambdaIntegration})

    }

    private createV2LambdaFunction(functionName: string, functionId: string,handlerPath: string, timeOut: Duration, springCloudDefinition?: string,
                                   iRole?: iam.IRole,
                                ) {

        return new Function(this, functionId, {
            functionName: functionName,
            runtime: Runtime.JAVA_17, // #todo change to custom runtime and graalvm
            code: Code.fromAsset('../../../artifacts/lambda/runtimev2.jar'),
            handler: handlerPath,
            memorySize: 512, // #todo variable
            environment: {              
                APP_ENVIRONMENT: 'dev', // #todo variable
                spring_cloud_function_definition: springCloudDefinition!,
                MAIN_CLASS: "org.wipo.trademarks.Aws.artefacts.AwsApiApplication",
                Aws_ENVIRONMENT: "DEV",  // #todo ACC, DEV, PROD
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.DISABLED, // #todo variable
            timeout: timeOut,
            role: iRole!,
        });
    }


    private getName(props: AwsWsApiProps | undefined, name: string) {
        // console.log("suffix: " + process.env.BUILDNUMBER)
        const suffix = process.env.BUILDNUMBER ? process.env.BUILDNUMBER : 'devBuild'
        return `${props!.projectPrefix}-${name}-${suffix}`.toLowerCase();
    }
}