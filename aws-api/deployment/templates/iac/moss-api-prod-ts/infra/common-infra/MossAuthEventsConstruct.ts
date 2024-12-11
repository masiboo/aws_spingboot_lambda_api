import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {IBucket} from "aws-cdk-lib/aws-s3";
import {Duration} from "aws-cdk-lib";
import {ITable} from "aws-cdk-lib/aws-dynamodb";
import * as iam from "aws-cdk-lib/aws-iam";
import * as Iam from "aws-cdk-lib/aws-iam";
import {Code, Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import * as CustomResources from 'aws-cdk-lib/custom-resources';
import {UserPool} from "aws-cdk-lib/aws-cognito";
import {HttpUserPoolAuthorizer} from "aws-cdk-lib/aws-apigatewayv2-authorizers";

export interface AwsAuthEventsProps extends ConstructCommonProps {
    // suffix: string;
    lambdaEventFunctions: LambdaAuthEventsType[],
}

export type LambdaAuthEventsType = {
    functionName: string,
    functionId: string ,
    handlerPath: string,
    timeOut?: Duration,
    useFunction?: boolean,
    springCloudRouterDefinition: string,
    vpc?: any,
    codePath: string,
}

export class AwsAuthEventsConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsAuthEventsProps) {
        super(scope, id, props);

        const userPoolId = this.getParameter('userPoolID');
        const userPool = UserPool.fromUserPoolId(this, 'importedUserPool', userPoolId)

        // cognito-auth-end
        const lambdaList: LambdaAuthEventsType[] = props.lambdaEventFunctions;

        // start-trigger - pre-auth-function
        const preAuthTrigger: LambdaAuthEventsType = {
            codePath: '../../../artifacts/lambda/runtimev2.jar',
            functionId: "preAuthTrigger-func-id",
            functionName: "pre-auth-trigger",
            handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
            springCloudRouterDefinition: "",
        }

        const preAuthTriggerFunction = this.createV2LambdaFunction(
            this.getName(props, `${preAuthTrigger.functionName}-v2`),
            `${preAuthTrigger.functionId}V2`,
            preAuthTrigger.handlerPath,
            preAuthTrigger.timeOut ? preAuthTrigger.timeOut : Duration.seconds(300),
            preAuthTrigger.springCloudRouterDefinition ? preAuthTrigger.springCloudRouterDefinition : "getHealth"
        )

        new CustomResources.AwsCustomResource(this, "UpdateUserPool", {
            resourceType: "Custom::UpdateUserPoolWithTrigger",
            onCreate: {
                region: this.stackConfig.region,
                service: "CognitoIdentityServiceProvider",
                action: "updateUserPool",
                parameters: {
                    UserPoolId: userPool.userPoolId,
                    LambdaConfig: {
                        // PreSignUp: preAuthTriggerFunction.functionArn,
                        PreAuthentication: preAuthTriggerFunction.functionArn,
                        PostAuthentication: preAuthTriggerFunction.functionArn,
                    },
                },
                physicalResourceId: CustomResources.PhysicalResourceId.of(userPool.userPoolId),
            },
            onUpdate: {
                region: this.stackConfig.region,
                service: "CognitoIdentityServiceProvider",
                action: "updateUserPool",
                parameters: {
                    UserPoolId: userPool.userPoolId,
                    LambdaConfig: {
                        // PreSignUp: preAuthTriggerFunction.functionArn,
                        PreAuthentication: preAuthTriggerFunction.functionArn,
                        PostAuthentication: preAuthTriggerFunction.functionArn,
                    },
                },
                physicalResourceId: CustomResources.PhysicalResourceId.of(userPool.userPoolId),
            },
            policy: CustomResources.AwsCustomResourcePolicy.fromSdkCalls({resources: CustomResources.AwsCustomResourcePolicy.ANY_RESOURCE}),
        });

        const invokeCognitoTriggerPermission = {
            principal: new Iam.ServicePrincipal('cognito-idp.amazonaws.com'),
            sourceArn: userPool.userPoolArn
        }
        preAuthTriggerFunction.addPermission('InvokePreSignUpHandlerPermission', invokeCognitoTriggerPermission)

    }

    private createSQSV2LambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                 functionId: string, handlerPath: string,
                                    timeOut: Duration,  springCloudDefinition: string, iRole?: iam.IRole) {

        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        const dbAccessService: string = "AwsCoreDBAccessStackAlbDnsName";

        return new Function(this, functionId, {

            functionName: functionName,
            runtime: Runtime.JAVA_17, // #todo change to custom runtime and graalvm
            code: Code.fromAsset('../../../artifacts/lambda/runtimev2.jar'),
            handler: handlerPath,
            memorySize: 1024, // #todo variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
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

    private createV2LambdaFunction(functionName: string,
                                   functionId: string, handlerPath: string, timeOut: Duration, springCloudDefinition?: string,
                                   iRole?: iam.IRole,
    ) {

        const emailParamkey: string = this.getParameter("AwsCoreEmailSvctackAlbDnsName") ? this.getParameter("AwsCoreEmailSvctackAlbDnsName") : "EmailSVCSubtitute";
        const mirisProxyKey: string = this.getParameter("core/mediaprocessAlbDnsName") ? this.getParameter("core/mediaprocessAlbDnsName") : "MirisProxySubtitute";
        const dbAccessUrl: string = "AwsCoreDBAccessStackAlbDnsName";

        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        const mirisProxyService = `http://${mirisProxyKey}`;
        const emailPrivateService = `http://${emailParamkey}`;
        const dbAccessService = `http://${dbAccessUrl}`;

        return new Function(this, functionId, {

            functionName: functionName,
            runtime: Runtime.JAVA_17, // #todo change to custom runtime and graalvm
            code: Code.fromAsset('../../../artifacts/lambda/runtimev2.jar'),
            handler: handlerPath,
            memorySize: 1024, // #todo variable
            environment: {
                // REGISTRY_TABLE_NAME: registryTable.tableName,
                // ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                // CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                // Aws_CORE_MIRIS_PROXY_API_URL: mirisProxyService,
                Aws_CORE_EMAIL_SERVICE_API_URL: emailPrivateService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                APP_ENVIRONMENT: 'dev', // #todo variable
                spring_cloud_function_definition: springCloudDefinition!,
                MAIN_CLASS: "org.wipo.trademarks.Aws.artefacts.AwsApiApplication",
                API_VERSION: '0.4.0', // #todo extract build variable
                CORE_VERSION: '0.4.0' // #todo extract build variable
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo variable
            timeout: timeOut,
            role: iRole!,
        });
    }

    private getName(props: AwsAuthEventsProps | undefined, name: string) {
        // console.log("suffix: " + process.env.BUILDNUMBER)
        const suffix = process.env.BUILDNUMBER ? process.env.BUILDNUMBER : 'devBuild'
        return `${props!.projectPrefix}-${name}-${suffix}`.toLowerCase();
    }

    private createEnvironment(targetServiceStackName: string): any {
        const env: any = {
            'Namespace': `${this.projectPrefix}-NS`,
            'TargetServiceName': targetServiceStackName,
            'AlbDnsName': this.getParameter(`${targetServiceStackName}AlbDnsName`),
        }

        for (let item in this.stackConfig.Environment) {
            env[item] = String(this.stackConfig.Environment[item]);
        }

        return env;
    }
    private removeHttp(url: string) {
        return url.replace(/^https?:\/\//, '');
    }
}
