import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {Bucket, IBucket} from "aws-cdk-lib/aws-s3";
import {Duration} from "aws-cdk-lib";
import {ITable, Table} from "aws-cdk-lib/aws-dynamodb";
import * as iam from "aws-cdk-lib/aws-iam";
import {Policy, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {Code, Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import {IUserPoolClient, UserPool, UserPoolClient} from 'aws-cdk-lib/aws-cognito'
import {
    HttpApi,
    HttpMethod,
    HttpRoute,
    HttpRouteKey,
    IHttpApi,
    PayloadFormatVersion
} from "aws-cdk-lib/aws-apigatewayv2";
import {HttpUserPoolAuthorizer} from "aws-cdk-lib/aws-apigatewayv2-authorizers";
import {HttpLambdaIntegration} from "aws-cdk-lib/aws-apigatewayv2-integrations";


export interface AwsApiProps extends ConstructCommonProps {

    // suffix: string;
    lambdaFunctions: LambdaListType[],
    lambdaV2Functions: LambdaListType[],
    useCustomRuntime?: boolean;
}

export type LambdaListType = {
    functionName: string,
    functionId: string ,
    handlerPath: string,
    integerationId: string | undefined,
    path: string | undefined,
    methods: HttpMethod[],
    s3Policy: boolean,
    timeOut?: Duration,
    s3EventHandler?: boolean,
    useFunction?: boolean,
    springCloudRouterDefinition?: string,
    secured?: boolean

    invoker?: boolean
}

export class AwsApiConstruct extends BaseConstruct {

    private props: AwsApiProps

    constructor(scope: Construct, id: string, props: AwsApiProps) {
        super(scope, id, props);

        this.props = props;
        const bucketArn = this.getParameter("registryBucketArn");
        const registryBucket = Bucket.fromBucketArn(this, "registry-bucket", bucketArn);

        const reportsBucketArn = this.getParameter("LogsBucketArn");
        const reportsBucket = Bucket.fromBucketArn(this, "ImportedReportBucket", reportsBucketArn);

        const tableArn = this.getParameter("registryTableArn");
        const tableName = this.getParameter("registryTableName");

        // const registryTable = Table.fromTableArn(this, "registry-table", tableArn);
        const registryTable = Table.fromTableAttributes(this, "table", {
            globalIndexes: ['GSI-Artefact-1', 'GSI-Artefact-2', 'GSI-Artefact-3', 'GSI-Artefact-4'],
            grantIndexPermissions: true,
            tableArn: tableArn,
        })

        const userPoolId = this.getParameter('userPoolID');
        const userPoolClientId = this.getParameter("auth/appClientId");
        const userPool = UserPool.fromUserPoolId(this, 'importedUserPool', userPoolId)
        const userPoolClient = UserPoolClient.fromUserPoolClientId(this, "importedUserPoolClient", userPoolClientId);
        const authorizer = new HttpUserPoolAuthorizer('AwsAuthorizer', userPool, {
            userPoolClients: [userPoolClient]
        });

        // cognito-auth-end


        const lambdaList : LambdaListType[] = props.lambdaFunctions;

        const httpId = this.getParameter("apiGateWayHttpId");
        const httpApi = HttpApi.fromHttpApiAttributes(this, "importedHttpAPI", {
            httpApiId: httpId
        })

        const s3BucketPutPolicyStatement = new PolicyStatement({
            actions: ['s3:ListAllMyBuckets', "s3:PutObject", "s3:GetObject"],
            resources: ["arn:aws:s3:::*"]
        })

        const s3WritePolicy = new Policy( this, "s3-write-policy-red", {
            statements: [s3BucketPutPolicyStatement]
        })


        lambdaList.map(value => {
            const lambdaFunction = this.createLambdaFunction(
                this.getName(props, value.functionName),
                registryTable,
                registryBucket,
                value.functionId,
                value.handlerPath,
                value.timeOut ? value.timeOut : Duration.seconds(300)
            )

            registryTable.grantFullAccess(lambdaFunction);


            if (value.integerationId != null && value.path != null) {
                const integration = new HttpLambdaIntegration(value.integerationId, lambdaFunction, {
                    payloadFormatVersion: PayloadFormatVersion.VERSION_1_0
                })
                this.createHttpRoute(httpApi, value.path, integration, value.methods, `${value.functionId}-id`);
            }

            if (value.s3Policy){
                lambdaFunction.role?.attachInlinePolicy(s3WritePolicy)
            }

        })

        // start-v2
        const lambdaV2List : LambdaListType[] = props.lambdaV2Functions;
        lambdaV2List.map(functorValue => {

            const enabled = !!functorValue.useFunction
            // console.log(`${functorValue.functionName} check is ${enabled}`)

            if (enabled) {
                const lambdaFunction = this.createV2LambdaFunction(
                    this.getName(props, `${functorValue.functionName}-v2`),
                    registryTable,
                    registryBucket,
                    reportsBucket,
                    `${functorValue.functionId}V2`,
                    functorValue.handlerPath,
                    functorValue.timeOut ? functorValue.timeOut : Duration.seconds(300),
                    functorValue.springCloudRouterDefinition ? functorValue.springCloudRouterDefinition : "getHealth"
                )


                registryTable.grantFullAccess(lambdaFunction);

                if (functorValue.integerationId != null && functorValue.path != null) {
                    const integration = new HttpLambdaIntegration(functorValue.integerationId, lambdaFunction, {
                        payloadFormatVersion: PayloadFormatVersion.VERSION_1_0
                    })
                    const v2Path = `/api/v2/${functorValue.path}`
                    this.createHttpRoute(httpApi, v2Path, integration, functorValue.methods, `${functorValue.functionId}-v2-id`, functorValue.secured ? authorizer : undefined);
                }

                if (functorValue.s3Policy) {
                    lambdaFunction.role?.attachInlinePolicy(s3WritePolicy)
                }

                // #redo architecture
                if (functorValue.invoker) {
                    const signedUrlName = this.getName(this.props, `signed-url-function`);
                    const signedfunction = Function.fromFunctionName(this, `${functorValue.functionName}function-import`, signedUrlName);
                    const invokePolicy = this.createInvokePolicy(signedfunction.functionArn, functorValue.functionName);
                    lambdaFunction.role?.attachInlinePolicy(invokePolicy);
                }


            } else {
                // console.log("function disabled")
            }
        })
        // end-V2

    }


    private createHttpRoute(httpApi: IHttpApi, path: string, integration: HttpLambdaIntegration,
                            method: any, id: string, authorizer?: HttpUserPoolAuthorizer, authorizationScopes?: string[]) {

        const httpRouteKey = HttpRouteKey.with(path, /* all optional props */ method);

        new HttpRoute(this, id,
            {
                httpApi: httpApi,
                routeKey: httpRouteKey,
                integration,
                authorizer,
                authorizationScopes
            });

    }

    private createLambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                 functionId: string, handlerPath: string, timeOut: Duration, iRole?: iam.IRole) {

        const emailParamkey: string = this.getParameter("AwsCoreEmailSvctackAlbDnsName") ? this.getParameter("AwsCoreEmailSvctackAlbDnsName") : "EmailSVCSubtitute";
        // const mirisProxyKey: string = this.getParameter("AwsCoreMirisProxyStackAlbDnsName") ? this.getParameter("AwsCoreMirisProxyStackAlbDnsName") : "MirisProxySubtitute";

        const dbAccessService: string = `${this.getParameter("core/dbaccessAlbDnsName")}`;
        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        // const mirisProxyService = `http://${mirisProxyKey}`;
        const emailPrivateService = `http://${emailParamkey}`;

        const buildNumber = process.env.BUILDNUMBER || "devBuild";
        const version = process.env.VERSION || "0.9.0";
        const stage = process.env.PROJECT_STAGE || "dev";

        return new Function(this, functionId, {

            functionName: functionName,
            runtime: Runtime.JAVA_17, // #todo change to custom runtime and graalvm
            code: Code.fromAsset('../../../artifacts/lambda/runtime.jar'),
            handler: handlerPath,
            memorySize: 2048, // #todo variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                // Aws_CORE_MIRIS_PROXY_API_URL: mirisProxyService,
                Aws_CORE_EMAIL_SERVICE_API_URL: emailPrivateService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                APP_ENVIRONMENT: `${stage}`,
                API_VERSION: `${version}-${buildNumber}`,
                CORE_VERSION: `${version}-${buildNumber}`,
                ARTEFACT_SERVICE_INTERFACE: "NONE",
                Aws_ENVIRONMENT: "DEV",  // #todo ACC, DEV, PROD
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE,
            timeout: timeOut,
            role: iRole!,

        });
    }


    private createV2LambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                   reportsBucket: IBucket, functionId: string, handlerPath: string, timeOut: Duration,
                                   springCloudDefinition?: string, iRole?: iam.IRole,
    ) {

        const emailParamkey: string = this.getParameter("AwsCoreEmailSvctackAlbDnsName") ? this.getParameter("AwsCoreEmailSvctackAlbDnsName") : "EmailSVCSubtitute";
        const mirisProxyKey: string = this.getParameter("core/mediaprocessAlbDnsName") ? this.getParameter("core/mediaprocessAlbDnsName") : "MirisProxySubtitute";
        // const dbAccessUrl: string = this.getParameter("AwsCoreDBAccessStackAlbDnsName") ? this.getParameter("AwsCoreDBAccessStackAlbDnsName") : "AwsCoreDBAccessStackAlbDnsNameSubtitute";

        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        const mirisProxyService = `http://${mirisProxyKey}`;
        const emailPrivateService = `http://${emailParamkey}`;
        // const dbAccessService = `http://${dbAccessUrl}`;
        const dbAccessService: string =  "AwsCoreDBAccessStackAlbDnsNameSubtitute";

        const signedUrlName = this.getName(this.props, `signed-url-function`);
        const buildNumber = process.env.BUILDNUMBER || "devBuild";
        const version = process.env.VERSION || "0.10.graal-";
        const stage = process.env.PROJECT_STAGE || "dev";

        return new Function(this, functionId, {

            functionName: functionName,
            runtime: this.props.useCustomRuntime ? Runtime.PROVIDED_AL2 : Runtime.JAVA_17,
            code: this.props.useCustomRuntime
                ? Code.fromAsset('../../../artifacts/lambda/runtime2.zip')
                : Code.fromAsset('../../../artifacts/lambda/runtimev2.jar'),
            handler: handlerPath,
            memorySize: 2048, // #todo variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                // Aws_CORE_MIRIS_PROXY_API_URL: mirisProxyService,
                Aws_CORE_EMAIL_SERVICE_API_URL: emailPrivateService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                spring_cloud_function_definition: springCloudDefinition!,
                MAIN_CLASS: "org.wipo.trademarks.Aws.artefacts.AwsApiApplication",
                APP_ENVIRONMENT: `${stage}`,
                API_VERSION: `${version}-${buildNumber}`,
                CORE_VERSION: `${version}-${buildNumber}`,
                Aws_API_SIGNED_URL_NAME: signedUrlName,
                BATCH_S3_REPORTS_BUCKET: reportsBucket.bucketName,
                ARTEFACT_SERVICE_INTERFACE: "NONE",
                Aws_ENVIRONMENT: "DEV",  // #todo ACC, DEV, PROD
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo variable
            timeout: timeOut,
            role: iRole!,
        });
    }


    private getName(props: AwsApiProps | undefined, name: string) {
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

    private createInvokePolicy( functionArn: string, invoker: string) {
        const lambdaV1InvokePolicyStatement = new PolicyStatement( {
            effect: iam.Effect.ALLOW,
            actions: [ 'lambda:InvokeFunction' ],
            resources: [ functionArn ]
        })
        const lambdaFunctionPolicy = new Policy(this, `${invoker}-lambda-policy`, {
            statements: [lambdaV1InvokePolicyStatement]
        })

        return lambdaFunctionPolicy;
    }
}
