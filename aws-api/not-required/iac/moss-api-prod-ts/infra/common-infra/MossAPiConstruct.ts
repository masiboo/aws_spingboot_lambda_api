import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import * as s3 from "aws-cdk-lib/aws-s3";
import {
    BlockPublicAccess,
    Bucket,
    BucketAccessControl,
    BucketEncryption,
    EventType,
    HttpMethods, IBucket
} from "aws-cdk-lib/aws-s3";
import {CfnOutput, Duration, RemovalPolicy} from "aws-cdk-lib";
import {AttributeType, BillingMode, ITable, ProjectionType, Table} from "aws-cdk-lib/aws-dynamodb";
import {Policy, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {LambdaDestination} from "aws-cdk-lib/aws-s3-notifications";
import {Code, EventSourceMapping, Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import {
    CorsHttpMethod,
    HttpApi,
    HttpMethod,
    HttpRoute, HttpRouteKey,
    IHttpApi,
    PayloadFormatVersion
} from "@aws-cdk/aws-apigatewayv2-alpha";
import {HttpLambdaIntegration} from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import * as sqs from 'aws-cdk-lib/aws-sqs';
import * as iam from 'aws-cdk-lib/aws-iam';

export interface AwsApiProps extends ConstructCommonProps {

    // suffix: string;
    lambdaFunctions: LambdaListType[]
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
    s3EventHandler?: boolean
}

export class AwsApiConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsApiProps) {
        super(scope, id, props);

        const bucketArn = this.getParameter("registryBucketArn");
        const registryBucket = Bucket.fromBucketArn(this, "registry-bucket", bucketArn);

        const tableArn = this.getParameter("registryTableArn");
        const tableName = this.getParameter("registryTableName");

        // const registryTable = Table.fromTableArn(this, "registry-table", tableArn);
        const registryTable = Table.fromTableAttributes(this, "table", {
            globalIndexes: ['GSI-Artefact-1', 'GSI-Artefact-2', 'GSI-Artefact-3', 'GSI-Artefact-4'],
            grantIndexPermissions: true,
            tableArn: tableArn,
        })

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
                value.timeOut ? value.timeOut : Duration.seconds(3)
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

        const s3eventHandler : LambdaListType =   {
            s3Policy: false,
            functionName: this.getName(props, 's3-input-trigger-function'),
            functionId: 'S3EventFunction' ,
            handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.S3EventTriggerHandler',
            integerationId: undefined,
            path: undefined,
            methods: [],
            s3EventHandler: true
        }

        const sQseventHandler : LambdaListType =   {
            s3Policy: false,
            functionName: this.getName(props, 'sqs-event-trigger-function'),
            functionId: 'SQSEventFunction' ,
            handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.SQSEventTriggerHandler',
            integerationId: undefined,
            path: undefined,
            methods: [],
            s3EventHandler: true
        }

        const sqsArn = this.getParameter("objectCreatedSQSArn")
        const sQueue = sqs.Queue.fromQueueArn(this, "imported-sqs", sqsArn);

        const lambdaRole = new iam.Role(this, 'QueueConsumerFunctionRole', {
            assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
            managedPolicies: [iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaSQSQueueExecutionRole'),
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole')]
        });

        const s3lambdaFunction = this.createLambdaFunction(sQseventHandler.functionName,
            registryTable,
            registryBucket,
            sQseventHandler.functionId,
            sQseventHandler.handlerPath,
            sQseventHandler.timeOut ? sQseventHandler.timeOut : Duration.seconds(300),
            lambdaRole
        )

        const eventSourceMapping = new EventSourceMapping(this, 'QueueObjectCreatedSQSEvent', {
            target: s3lambdaFunction,
            batchSize: 10,
            eventSourceArn: sQueue.queueArn
        });

        registryBucket.grantReadWrite(s3lambdaFunction);
        registryTable.grantFullAccess(s3lambdaFunction);

    }


    private createHttpRoute(httpApi: IHttpApi, path: string, integration: HttpLambdaIntegration, method: any, id: string) {

        const httpRouteKey = HttpRouteKey.with(path, /* all optional props */ method);

        new HttpRoute(this, id,
        {
            httpApi: httpApi,
            routeKey: httpRouteKey,
            integration
        });

    }

    private createLambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                 functionId: string, handlerPath: string, timeOut: Duration, iRole?: iam.IRole) {
        return new Function(this, functionId, {
            functionName: functionName,
            runtime: Runtime.PROVIDED_AL2,
            code: Code.fromAsset('../runtime.zip'),
            handler: handlerPath,
            memorySize: 512,
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                APP_ENVIRONMENT: 'dev'
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.DISABLED,
            timeout: timeOut,
            role: iRole!,
        });
    }

    private getName(props: AwsApiProps | undefined, name: string) {
        const suffix = process.env.BUILDNUMBER || 'devBuild'
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