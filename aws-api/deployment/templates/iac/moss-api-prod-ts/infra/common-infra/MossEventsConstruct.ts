import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {Bucket, IBucket} from "aws-cdk-lib/aws-s3";
import {Duration} from "aws-cdk-lib";
import {ITable, Table} from "aws-cdk-lib/aws-dynamodb";
import * as iam from "aws-cdk-lib/aws-iam";
import {Policy, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {Code, EventSourceMapping, Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import * as sqs from 'aws-cdk-lib/aws-sqs';


export interface AwsEventsProps extends ConstructCommonProps {
    // suffix: string;
    lambdaEventFunctions: LambdaEventsType[],
}

export type LambdaEventsType = {
    functionName: string,
    functionId: string ,
    handlerPath: string,
    s3Policy: boolean,
    timeOut?: Duration,
    s3EventHandler?: boolean,
    useFunction?: boolean,
    springCloudRouterDefinition?: string,
    vpc?: any,
    sQsParameterKey?: any,
    codePath: string,
}

export class AwsEventsConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsEventsProps) {
        super(scope, id, props);

        const bucketArn = this.getParameter("registryBucketArn");
        const registryBucket = Bucket.fromBucketArn(this, "registry-bucket", bucketArn);

        const tableArn = this.getParameter("registryTableArn");
        const tableName = this.getParameter("registryTableName");

        const auditTableArn = this.getParameter("auditEventsTableArn");

        // const registryTable = Table.fromTableArn(this, "registry-table", tableArn);
        const registryTable = Table.fromTableAttributes(this, "table", {
            globalIndexes: ['GSI-Artefact-1', 'GSI-Artefact-2', 'GSI-Artefact-3', 'GSI-Artefact-4'],
            grantIndexPermissions: true,
            tableArn: tableArn,
        })

        const auditTable = Table.fromTableAttributes(this, "audit-table", {
            grantIndexPermissions: true,
            tableArn: auditTableArn,
        })

        const lambdaList : LambdaEventsType[] = props.lambdaEventFunctions;

        const s3BucketPutPolicyStatement = new PolicyStatement({
            actions: ['s3:ListAllMyBuckets', "s3:PutObject", "s3:GetObject"],
            resources: ["arn:aws:s3:::*"]
        })
        const s3WritePolicy = new Policy( this, "s3-write-policy-red", {
            statements: [s3BucketPutPolicyStatement]
        })

        const lambdaRole = new iam.Role(this, 'QueueConsumerFunctionRole', {
            assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
            managedPolicies: [iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaSQSQueueExecutionRole'),
                iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole')]
        });

        // S3 files Events Handler - start
        lambdaList.map(value => {
            this.createEventFunction(props, value, registryTable, auditTable, registryBucket, s3WritePolicy, true);
        })

        // s3-files events handler end

        // DB Sync
        const indexedArtefactJavaHandler: LambdaEventsType = {
            functionName: `indexed-db-event-trigger-function`,
            functionId: "indexedDBEventId",
            handlerPath: "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest",
            springCloudRouterDefinition: 'SQSDArtefactEventHandler',
            s3Policy: true,
            useFunction: true,
            sQsParameterKey: "updatedArtefactSqsQueueARN", // #todo: p2: derived from SSM parameter
            timeOut: Duration.seconds(900),
            codePath: '../../../artifacts/lambda/runtimev2.jar'
        }

        const javaLambdaFunction = this.createVPCLambdaFunction(
            this.getName(props, indexedArtefactJavaHandler.functionName),
            registryTable,
            auditTable,
            registryBucket,
            indexedArtefactJavaHandler.functionId,
            indexedArtefactJavaHandler.handlerPath,
            indexedArtefactJavaHandler.timeOut ? indexedArtefactJavaHandler.timeOut : Duration.seconds(900),
            indexedArtefactJavaHandler.springCloudRouterDefinition ? indexedArtefactJavaHandler.springCloudRouterDefinition : "getHealth"
        )

        registryBucket.grantReadWrite(javaLambdaFunction);
        registryTable.grantFullAccess(javaLambdaFunction);

        auditTable.grantFullAccess((javaLambdaFunction));


        if (indexedArtefactJavaHandler.sQsParameterKey) {
            this.createSQSEventSource(indexedArtefactJavaHandler, javaLambdaFunction);
        }

        // // Batch SQS Handler
        // const sqsBatchHandler: LambdaEventsType = {
        //     functionName: `BatchArtefactsEeventHandler`,
        //     functionId: "batchedArtefactEventId",
        //     handlerPath: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest',
        //     springCloudRouterDefinition: 'SQSBatchEventHandler',
        //     s3Policy: true,
        //     useFunction: true,
        //     sQsParameterKey: "updatedBatchSqsQueueARN",
        //     codePath: "../../../artifacts/lambda/runtime.zip",

        //     s3EventHandler: true,
        //     timeOut: Duration.seconds(900),
        //     // handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsHealthCheck',
        //     // integerationId: 'healthCheckIntegeration',
        // }
        // this.createEventFunction(props, sqsBatchHandler, registryTable, registryBucket, s3WritePolicy, true);

    }


    private createEventFunction(props: AwsEventsProps, value: LambdaEventsType, registryTable: ITable, auditTable: ITable,
                                registryBucket: IBucket, s3WritePolicy: Policy, v2: boolean) {

        let lambdaFunction: any

        if (v2) {
            lambdaFunction = this.createVPCLambdaFunction(
                this.getName(props, value.functionName),
                registryTable,
                auditTable,
                registryBucket,
                value.functionId,
                value.handlerPath,
                value.timeOut ? value.timeOut : Duration.seconds(900),
                value.springCloudRouterDefinition ? value.springCloudRouterDefinition : "getHealth"
            )
        } else {
            lambdaFunction = this.createV1LambdaFunction(
                this.getName(props, value.functionName),
                registryTable,
                registryBucket,
                value.functionId,
                value.handlerPath,
                value.timeOut ? value.timeOut : Duration.seconds(900),

            )
        }


        if (value.s3Policy) {
            lambdaFunction!.role?.attachInlinePolicy(s3WritePolicy)
        }

        registryBucket.grantReadWrite(lambdaFunction!);
        registryTable.grantFullAccess(lambdaFunction!);
        auditTable.grantFullAccess((lambdaFunction));

        if (value.sQsParameterKey) {
            this.createSQSEventSource(value, lambdaFunction!);
        }
    }

    private creatNodeEvent(props: AwsEventsProps, value: LambdaEventsType, registryTable: ITable, registryBucket: IBucket,
                           s3WritePolicy: Policy) {


        const lambdaFunction = this.createPythonLambdaFunction(
            this.getName(props, value.functionName),
            registryTable,
            registryBucket,
            value.functionId,
            value.handlerPath,
            value.timeOut ? value.timeOut : Duration.seconds(900),
            value.codePath,
        )

        if (value.s3Policy) {
            lambdaFunction.role?.attachInlinePolicy(s3WritePolicy)
        }

        registryBucket.grantReadWrite(lambdaFunction);
        registryTable.grantFullAccess(lambdaFunction);


        if (value.sQsParameterKey) {
            this.createSQSEventSource(value, lambdaFunction);
        }
    }
    private createSQSEventSource(value: LambdaEventsType, lambdaFunction: Function) {
        const parameter = value.sQsParameterKey;
        const sqsArn = this.getParameter(parameter)
        const sQueue = sqs.Queue.fromQueueArn(this, `${value.functionId}-sqs`, sqsArn);

        const SQSPolicyStatement = new PolicyStatement({
            actions: [
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueAttributes"
            ],
            resources: [sqsArn]
        })

        const SQSPolicy = new Policy(this, `${value.functionId}-write`, {
            statements: [SQSPolicyStatement]
        })
        lambdaFunction.role?.attachInlinePolicy(SQSPolicy)

        const eventSourceMapping = new EventSourceMapping(this, `${value.functionId}-event`, {
            target: lambdaFunction,
            batchSize: 10,
            eventSourceArn: sQueue.queueArn
        });
    }

    private createVPCLambdaFunction(functionName: string, registryTable: ITable, auditTable: ITable, registryBucket: IBucket,
                                 functionId: string, handlerPath: string,
                                    timeOut: Duration,  springCloudDefinition: string, iRole?: iam.IRole) {

        // #todo: p1 validate dbservice
        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        const dbAccessService: string = `${this.getParameter("core/dbaccessAlbDnsName")}`;
        // const dbAccessService: string =  "AwsCoreDBAccessStackAlbDnsNameSubtitute";


        return new Function(this, functionId, {

            functionName: functionName,
            runtime: Runtime.JAVA_17, // #todo p2: change to custom runtime and graalvm
            code: Code.fromAsset('../../../artifacts/lambda/runtimev2.jar'),
            handler: handlerPath,
            memorySize: 1024, // #todo p2: change to variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                APP_ENVIRONMENT: 'dev', // #todo p2: change to variable
                spring_cloud_function_definition: springCloudDefinition!,
                MAIN_CLASS: "org.wipo.trademarks.Aws.artefacts.AwsApiApplication",
                Aws_ENVIRONMENT: "DEV",  // #todo ACC, DEV, PROD
                AUDIT_EVENT_TABLE_NAME: auditTable.tableName,
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo p2:change to variable
            timeout: timeOut,
            role: iRole!,
        });
    }

    private createPythonLambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                    functionId: string, handlerPath: string,
                                    timeOut: Duration, codePath: string, iRole?: iam.IRole) {

        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        const dbAccessService: string = "AwsCoreDBAccessStackAlbDnsName";


        return new Function(this, functionId, {

            functionName: functionName,

            runtime: Runtime.PYTHON_3_9,
            code: Code.fromAsset(codePath),
            handler: handlerPath,
            memorySize: 1024, // #todo p2: variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                APP_ENVIRONMENT: 'dev', // #todo p1: variable
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo p2: variable
            timeout: timeOut,
            role: iRole!,
        });
    }



    private createV1LambdaFunction(functionName: string, registryTable: ITable, registryBucket: IBucket,
                                 functionId: string, handlerPath: string, timeOut: Duration, iRole?: iam.IRole) {

        const emailParamkey: string = this.getParameter("AwsCoreEmailSvctackAlbDnsName") ? this.getParameter("AwsCoreEmailSvctackAlbDnsName") : "EmailSVCSubtitute";

        const mediaProcessService = `${this.getParameter("core/mediaprocessAlbDnsName")}`;
        // const mirisProxyService = `http://${this.getParameter("AwsCoreMirisProxyStackAlbDnsName")}`;
        const emailPrivateService = `http://${emailParamkey}`;
        const dbAccessService: string = "AwsCoreDBAccessStackAlbDnsName";

        return new Function(this, functionId, {

            functionName: functionName,
            runtime: Runtime.PROVIDED_AL2,
            code: Code.fromAsset('../../../artifacts/lambda/runtime.zip'),
            handler: handlerPath,
            memorySize: 1024, // #todo p3:  variable
            environment: {
                REGISTRY_TABLE_NAME: registryTable.tableName,
                ARTEFACTS_S3_BUCKET: registryBucket.bucketName,
                CACHE_TABLE: registryTable.tableName,
                Aws_CORE_MEDIA_PROCESS_API_URL: mediaProcessService,
                // Aws_CORE_MIRIS_PROXY_API_URL: mirisProxyService,
                Aws_CORE_EMAIL_SERVICE_API_URL: emailPrivateService,
                Aws_CORE_DB_ACCESS_API_URL: dbAccessService,
                APP_ENVIRONMENT: 'dev', // #todo p3: variable
                API_VERSION: '0.4.0',
                CORE_VERSION: '0.4.0'
            },
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo p3: variable
            timeout: timeOut,
            role: iRole!,
        });
    }

    private getName(props: AwsEventsProps | undefined, name: string) {
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
