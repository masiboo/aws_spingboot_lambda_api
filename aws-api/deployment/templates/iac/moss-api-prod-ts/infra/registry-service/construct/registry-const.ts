import * as base from "../../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import * as s3 from "aws-cdk-lib/aws-s3";
import {BlockPublicAccess, Bucket, BucketAccessControl, BucketEncryption, HttpMethods} from "aws-cdk-lib/aws-s3";
import {Duration, RemovalPolicy} from "aws-cdk-lib";
import {AttributeType, BillingMode, ProjectionType, StreamViewType, Table} from "aws-cdk-lib/aws-dynamodb";
import * as iam from "aws-cdk-lib/aws-iam";
import {Role, ServicePrincipal} from "aws-cdk-lib/aws-iam";
import * as sqs from 'aws-cdk-lib/aws-sqs';
import * as sns from 'aws-cdk-lib/aws-sns';
import * as cloudwatch from 'aws-cdk-lib/aws-cloudwatch';
import * as actions from 'aws-cdk-lib/aws-cloudwatch-actions';
import * as events from "aws-cdk-lib/aws-events";
import {EventBus, IEventBus, Match, Rule} from "aws-cdk-lib/aws-events";
import * as targets from "aws-cdk-lib/aws-events-targets"
import {CloudWatchLogGroup} from "aws-cdk-lib/aws-events-targets"
import {LogGroup, RetentionDays} from "aws-cdk-lib/aws-logs";
import {Code, Function, Runtime, StartingPosition, Tracing} from "aws-cdk-lib/aws-lambda";
import {CfnPipe} from "aws-cdk-lib/aws-pipes";
import {EventBridgeWebSocket} from 'cdk-eventbridge-socket';
import {CorsHttpMethod, HttpApi} from "aws-cdk-lib/aws-apigatewayv2";
import {ICertificate} from "aws-cdk-lib/aws-certificatemanager";

export interface RegistryProps extends base.ConstructCommonProps {
    tableName: string;
    s3BucketName: string,
    queueName: string,
    apiGateWayName: string,
    region: string,
    account: string
    stage: string
    auditEventsTableName: string
}
export class RegistryConstruct extends base.BaseConstruct {
    private certificate: ICertificate;

    constructor(scope: Construct, id: string, props: RegistryProps) {
        super(scope, id, props);

        const corsRule: s3.CorsRule = {
            allowedMethods: [s3.HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT ],
            allowedOrigins: ['*'],

            // the properties below are optional
            allowedHeaders: ['*'],
            exposedHeaders: ['exposedHeaders'],
            id: 'madrid-Aws-bucket-cors-rule',
            maxAge: 100,
        };
        const suffix = `${props.stage}-${props.region}-${props.account}`

        const bucketName = this.getName(this.projectPrefix, props.s3BucketName, suffix);
        const registryBucket = new Bucket(this, `madrid-Aws-bucket`, {
            bucketName: bucketName,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            encryption: BucketEncryption.S3_MANAGED,
            enforceSSL: true,
            versioned: true,
            removalPolicy: RemovalPolicy.DESTROY,
            cors: [corsRule],
            eventBridgeEnabled: true,
            accessControl: BucketAccessControl.PRIVATE,
        });

        // dynamodb
        const ddbsuffix = `${props.stage}-${props.region}-${props.account}`
        const registryTableName = this.getName(this.projectPrefix, props.tableName, ddbsuffix);
        const registryTable = new Table(this, "AwsRegistryTableDev", {
            tableName: registryTableName,
            removalPolicy: RemovalPolicy.DESTROY,
            partitionKey: { name: 'PK', type: AttributeType.STRING },
            sortKey: { name: 'SK', type: AttributeType.STRING},
            stream: StreamViewType.NEW_AND_OLD_IMAGES,
            billingMode: BillingMode.PAY_PER_REQUEST,
        })
        registryTable.addGlobalSecondaryIndex({
            indexName: "GSI-Artefact-1",
            partitionKey: {name: 'type', type: AttributeType.STRING},
            sortKey: {name: 'status', type: AttributeType.STRING},
            projectionType: ProjectionType.ALL
        })
        registryTable.addGlobalSecondaryIndex({
            indexName: "GSI-Artefact-2",
            partitionKey: {name: 'mirisDocId', type: AttributeType.STRING},
            sortKey: {name: 'type', type: AttributeType.STRING},
            projectionType: ProjectionType.ALL
        })
        registryTable.addGlobalSecondaryIndex({
            indexName: "GSI-Artefact-3",
            partitionKey: {name: 'requestId', type: AttributeType.STRING},
            sortKey: {name: 'type', type: AttributeType.STRING},
            projectionType: ProjectionType.ALL
        })
        registryTable.addGlobalSecondaryIndex({
            indexName: "GSI-Artefact-4",
            partitionKey: {name: 'type', type: AttributeType.STRING},
            sortKey: {name: 'batchStatus', type: AttributeType.STRING},
            projectionType: ProjectionType.ALL
        })
        registryTable.addGlobalSecondaryIndex({
            indexName: "GSI-Artefact-5",
            partitionKey: {name: 'insertedDate', type: AttributeType.STRING},
            sortKey: {name: 'mirisDocId', type: AttributeType.STRING},
            projectionType: ProjectionType.ALL
        })

        const auditTableName = this.getName(this.projectPrefix, props.auditEventsTableName, ddbsuffix);
        const auditTable = new Table(this, "AwsAuditEventsTable", {
            tableName: auditTableName,
            removalPolicy: RemovalPolicy.DESTROY,
            partitionKey: { name: 'PK', type: AttributeType.STRING },
            sortKey: { name: 'SK', type: AttributeType.STRING},
            stream: StreamViewType.NEW_AND_OLD_IMAGES,
            billingMode: BillingMode.PAY_PER_REQUEST,
        })


        /**
         * EventBridge Permissions
         */
        let eventbridgePutPolicy = new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            resources: ['*'],
            actions: ['events:PutEvents']
        });

        // S3 Events ==> SQS
        const s3EventName = "s3-object-created";
        const dlqThreshold = 10; // Set your threshold value here

// Create SNS topic
        const alarmTopic = new sns.Topic(this, 'DLQAlarmTopic', {
            topicName: this.getShortName(this.projectPrefix, `${s3EventName}-dlq-alarm-topic`)
        });

// Create Dead Letter Queue
        const deadLetterQueue = new sqs.Queue(this, "s3ObjectCreatedDLQueue", {
            queueName: this.getShortName(this.projectPrefix, `${s3EventName}-dlq`),
            deliveryDelay: Duration.millis(0),
            retentionPeriod: Duration.days(14),
            // ALARMS!!
        });

// Create SQS queue for object created events
        const objectCreatedQueue = new sqs.Queue(this, "s3ObjectCreatedSqsQueue", {
            queueName: this.getShortName(this.projectPrefix, `${s3EventName}-queue`),
            encryption: sqs.QueueEncryption.UNENCRYPTED,
            visibilityTimeout: Duration.minutes(5),
            // fifo: true,
            deadLetterQueue: {
                maxReceiveCount: 2,
                queue: deadLetterQueue
            }
        });

// Create CloudWatch alarm for DLQ
        const dlqAlarm = new cloudwatch.Alarm(this, 'DLQAlarm', {
            metric: deadLetterQueue.metricApproximateNumberOfMessagesVisible(),
            threshold: dlqThreshold,
            evaluationPeriods: 1,
            alarmDescription: 'Alarm when there are messages in the DLQ exceeding the threshold',
            alarmName: this.getShortName(this.projectPrefix, `${s3EventName}-dlq-alarm`)
        });

// Add SNS topic as alarm action
        dlqAlarm.addAlarmAction(new actions.SnsAction(alarmTopic));

// Create EventBridge rule for S3 object created events
        const rule = new events.Rule(this, 's3ObjectCreatedRule', {
            eventPattern: {
                source: ['aws.s3'],
                detailType: [
                    'Object Created'
                ],
                detail: {
                    bucket: {
                        name: [
                            registryBucket.bucketName
                        ]
                    },
                    object: {
                        key: [{
                            prefix: 'Aws' // Replace 'your-folder-prefix/' with your actual folder prefix
                        }]
                    }
                }
            }
        });

// Set SQS queue as destination of event bus
        rule.addTarget(new targets.SqsQueue(objectCreatedQueue));


        const apiGatewaySuffix  = `${props.stage}-${props.region}`
        const apiName = this.getName(this.projectPrefix, props.apiGateWayName, ddbsuffix);



        const AwsApiGatewaylogGroup = new LogGroup(this, 'AwsApiGatewayAccessLogGroup', {
            removalPolicy: RemovalPolicy.DESTROY,
            logGroupName: `/aws/apigateway/${apiName}`,
        });

        const httpApi = new HttpApi(this, 'AwsAdminApi', {
            apiName,
            corsPreflight: {
                allowHeaders: ['Authorization', '*'],
                allowMethods: [
                    CorsHttpMethod.GET,
                    CorsHttpMethod.HEAD,
                    CorsHttpMethod.OPTIONS,
                    CorsHttpMethod.POST,
                ],
                allowOrigins: ['*'],
                maxAge: Duration.days(10),
            },
            // accessLogDestination: new LogGroupLogDestination(AwsApiGatewaylogGroup),
            // accessLogFormat: AccessLogFormat.jsonWithStandardFields(),
        })


        // Stream Targets
        /** ------------------ EventBus Definition ------------------ */

        const customEventBus = new EventBus(this, `${props.stackName}AwsEventBus`, {
            eventBusName: `${props.projectPrefix}-${props.stackName}EventBus`,
        });

        /* LOGGING */
        const eventLoggerRule = new Rule(this, `${props.stackName}EventLoggerRule`, {
            description: 'Log all events',
            ruleName: 'catchall',
            eventPattern: {
                source:  Match.exists()
            },
            eventBus: customEventBus,
        });
        const logGroup = new LogGroup(this, `${props.stackName}EventLogGroup`, {
            logGroupName: `/aws/events/${props.stackName}BusCustom`,
            retention: RetentionDays.ONE_WEEK,
            removalPolicy: RemovalPolicy.DESTROY // acc + prod
        });
        eventLoggerRule.addTarget(new CloudWatchLogGroup(logGroup));

        const eventBridgeRole = new Role(this, `${props.stackName}-events-role`, {
            assumedBy: new ServicePrincipal('events.amazonaws.com'),
        });
        logGroup.grantWrite(eventBridgeRole);

        const artefactPipeRole = new Role(this, `${props.stackName}artefact-pipe-role`, {
            assumedBy: new ServicePrincipal('pipes.amazonaws.com'),
        });

        const batchPipeRole = new Role(this, `${props.stackName}batch-pipe-role`, {
            assumedBy: new ServicePrincipal('pipes.amazonaws.com'),
        });

        // #todo conditional check if local or pipeline

        // const artefactDDBEventsEnricherFunction  = new NodejsFunction(this, `${props.stackName}lambda-function-splitter`, {
        //     functionName: `${props.stackName}Aws-stream-nodeFnInLine`,
        //     memorySize: 1024,
        //     runtime: Runtime.NODEJS_18_X,
        //     handler: 'handler',
        //     entry: path.join(__dirname, '../../../../../../../Aws-api-functions/streamTargetTs/src', 'enricher.ts'),
        // });

        // artefacts DDB Events enricher
        const artefactDDBEventsEnricherFunction =  new Function(this, `${props.stackName}lambda-function-splitter`, {
            functionName: `${props.stackName}artefact-stream-nodeFn`,
            runtime: Runtime.NODEJS_18_X,
            code: Code.fromAsset('../../../artifacts/lambda/streamdeploy.zip'),
            handler: 'enricher.handler', // Aws-api-functions/streamTargetTs/src/enricher.ts
            memorySize: 1024, // #todo variable
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo variable
        });

        // artefacts DDB Events enricher
        const batchDDBEventsEnricherFunction =  new Function(this, `${props.stackName}batch-function-enricher`, {
            functionName: `${props.stackName}batch-stream-nodeFn`,
            runtime: Runtime.NODEJS_18_X,
            code: Code.fromAsset('../../../artifacts/lambda/streamdeploy.zip'),
            handler: 'batchEnricher.handler', // Aws-api-functions/streamTargetTs/src/enricher.ts
            memorySize: 1024, // #todo variable
            logRetention: RetentionDays.ONE_WEEK,
            tracing: Tracing.ACTIVE, // #todo - variable
        });

        registryTable.grantStreamRead(artefactDDBEventsEnricherFunction);
        registryTable.grantStreamRead(batchDDBEventsEnricherFunction);

        registryTable.grantStreamRead(artefactPipeRole);
        customEventBus.grantPutEventsTo(artefactPipeRole);

        artefactDDBEventsEnricherFunction.grantInvoke(artefactPipeRole);
        // batchDDBEventsEnricherFunction.grantInvoke(artefactPipeRole);

        registryTable.grantStreamRead(batchPipeRole);
        customEventBus.grantPutEventsTo(batchPipeRole);

        // artefactDDBEventsEnricherFunction.grantInvoke(batchPipeRole);
        batchDDBEventsEnricherFunction.grantInvoke(batchPipeRole);

        // pipe for new and INSERTed artefacts + eventbridge rule + sqs  //
        // ?? - indexedArtefactSqsQueueARN - ??
        const insertedPipeSource = "Aws.artefact.source";
        const insertedPipeDetailType = "ArtefactCreated";
        const insertedRuleName = "indexedArtefact";
        const insertedPipe = new CfnPipe(this, `${props.stackName}artefact-inserted-pipe`, {
            roleArn: artefactPipeRole.roleArn,
            //@ts-ignore
            source: registryTable.tableStreamArn,
            sourceParameters: {
                dynamoDbStreamParameters: {
                    startingPosition: StartingPosition.LATEST,
                    batchSize: 1,
                },
                filterCriteria: {
                    filters: [
                        {
                            pattern: '{"eventName" : ["INSERT"] }',
                        },
                    ],
                },
            },
            enrichment: artefactDDBEventsEnricherFunction.functionArn, // #todo dedicated inserted enricher
            target: customEventBus.eventBusArn,
            targetParameters: {
                eventBridgeEventBusParameters: {
                    detailType: insertedPipeDetailType,
                    source: insertedPipeSource,
                },
            },
        });
        this.createRuleSQSTarget(insertedRuleName, customEventBus, insertedPipeSource, insertedPipeDetailType);

        // pipe for new and INDEXed artefacts + eventbridge rule + sqs
        // s3-files-event-trigger-function - updatedArtefactSqsQueueARN - SQSS3EventHandler
        const updatedPipeSource = "Aws.artefact.source";
        const updatedPipeDetailType = "ArtefactUpdated";
        const updatedRuleName = "updatedArtefact";
        const updatedPipe = new CfnPipe(this, `${props.stackName}artefact-updated-pipe`, {
            roleArn: artefactPipeRole.roleArn,
            //@ts-ignore
            source: registryTable.tableStreamArn,
            sourceParameters: {
                dynamoDbStreamParameters: {
                    startingPosition: StartingPosition.LATEST,
                    batchSize: 1,
                },
                filterCriteria: {
                    filters: [
                        {
                            pattern: '{"eventName" : ["MODIFY"] }',
                        },
                    ],
                },
            },
            enrichment: artefactDDBEventsEnricherFunction.functionArn, // #todo dedicated updated enricher
            target: customEventBus.eventBusArn,
            targetParameters: {
                eventBridgeEventBusParameters: {
                    detailType: updatedPipeDetailType,
                    source: updatedPipeSource,
                },
            },
        });
        this.createRuleSQSTarget(updatedRuleName, customEventBus, updatedPipeSource, updatedPipeDetailType);

        // #todo pipe for new and unindexed-batch and bulk artefacts + eventbridge rule + sqs  //
        // #todo pipe for new and indexed-batch and bulk artefacts + eventbridge rule + sqs  //

        // pipe for new and INSERTed batch + eventbridge rule + sqs
        // ?? - batchInsertedSqsQueueARN - ??
        const insertedBatchPipeSource = "Aws.batch.inserted";
        const insertedBatchPipeDetailType = "BatchCreated";
        const insertedBatchRuleName = "batchInserted";
        const insertedBatchPipe = new CfnPipe(this, `${props.stackName}batch-inserted-pipe`, {
            roleArn: batchPipeRole.roleArn,
            //@ts-ignore
            source: registryTable.tableStreamArn,
            sourceParameters: {
                dynamoDbStreamParameters: {
                    startingPosition: StartingPosition.LATEST,
                    batchSize: 1,
                },
                filterCriteria: {
                    filters: [
                        {
                            pattern: '{"eventName" : ["INSERT"] }',
                        },
                    ],
                },
            },
            enrichment: batchDDBEventsEnricherFunction.functionArn, // #todo dedicated inserted enricher
            target: customEventBus.eventBusArn,
            targetParameters: {
                eventBridgeEventBusParameters: {
                    detailType: insertedBatchPipeDetailType,
                    source: insertedBatchPipeSource,
                },
            },
        });
        this.createRuleSQSTarget(insertedBatchRuleName, customEventBus, insertedBatchPipeSource, insertedBatchPipeDetailType);

        // pipe for batch updated artefacts + eventbridge rule + sqs  //
        // ddb-batch-event-trigger-function -  updatedBatchSqsQueueARN - SQSBatchEventHandler
        const updatedBatchPipeSource = "Aws.batch.updated";
        const updatedBatchPipeDetailType = "BatchUpdated";
        const updatedBatchRuleName = "updatedBatch";
        const updatedBatchPipe = new CfnPipe(this, `${props.stackName}batch-updated-pipe`, {
            roleArn: batchPipeRole.roleArn,
            //@ts-ignore
            source: registryTable.tableStreamArn,
            sourceParameters: {
                dynamoDbStreamParameters: {
                    startingPosition: StartingPosition.LATEST,
                    batchSize: 1,
                },
                filterCriteria: {
                    filters: [
                        {
                            pattern: '{"eventName" : ["MODIFY"] }',
                        },
                    ],
                },
            },
            enrichment: batchDDBEventsEnricherFunction.functionArn, // #todo dedicated updated enricher
            target: customEventBus.eventBusArn,
            targetParameters: {
                eventBridgeEventBusParameters: {
                    detailType: updatedBatchPipeDetailType,
                    source: updatedBatchPipeSource,
                },
            },
        });
        this.createRuleSQSTarget(updatedBatchRuleName, customEventBus, updatedBatchPipeSource, updatedBatchPipeDetailType);

        // #todo - change this to authenticated and ssm paramter
        const stageing = props.stage;
        // console.log("staging "+stageing);
        if (stageing === 'dev') {
            const evWeb = new EventBridgeWebSocket(this, `${props.stackName}sockets`, {
                bus: customEventBus.eventBusName,
                eventPattern: {
                    source:  Match.exists()
                },
                stage: stageing,
            });
        }


        this.putParameter("registryBucketName", registryBucket.bucketName);
        this.putParameter("registryBucketArn", registryBucket.bucketArn);

        this.putParameter("registryTableName", registryTable.tableName);
        this.putParameter("registryTableArn", registryTable.tableArn);
        this.putParameter("registryTableStreamArn", registryTable.tableStreamArn!);

        this.putParameter("auditEventsTableName", auditTable.tableName);
        this.putParameter("auditEventsTableArn", auditTable.tableArn);

        this.putParameter('objectCreatedSQSArn', objectCreatedQueue.queueArn);
        this.putParameter('objectCreatedSQSUrl', objectCreatedQueue.queueUrl);

        this.putParameter('apiGateWayEndPoint', httpApi.apiEndpoint);
        this.putParameter('apiGateWayApiId', httpApi.apiId);
        this.putParameter('apiGateWayHttpId', httpApi.httpApiId);

    }

    private createRuleSQSTarget(ruleName: string, customEventBus: IEventBus, source: string, detailType: string) {
        // Create rule
        const rule = new events.Rule(this, `${ruleName}-eventRule`, {
            ruleName:  this.getShortName(this.projectPrefix, `${ruleName}eventRule`) ,
            eventBus: customEventBus
        });

        // Add event pattern to rule
        rule.addEventPattern({
            source: [source],
            detailType: [detailType]
        });

        // Create SQS queue;

        const quename =this.getShortName(this.projectPrefix, `${ruleName}-queue`);

        const deadLetterQueue = new sqs.Queue(this, `${ruleName}-dlq`, {
            queueName: `${quename}-dlq`,
            deliveryDelay: Duration.millis(0),
            retentionPeriod: Duration.days(14),
        });

        const customQueue = new sqs.Queue(this, `${ruleName}-queue`, {
            queueName:  quename,
            encryption: sqs.QueueEncryption.UNENCRYPTED,
            visibilityTimeout: Duration.minutes(5),
            deadLetterQueue: {
                maxReceiveCount: 5,
                queue: deadLetterQueue
            }
        });

        // Set SQS queue as destination of event bus
        rule.addTarget(new targets.SqsQueue(customQueue));

        this.putParameter(`${ruleName}EventBusName`,  customEventBus.eventBusName);
        this.putParameter( `${ruleName}RuleName`, rule.ruleName );
        this.putParameter( `${ruleName}SqsQueueName`, customQueue.queueName );
        this.putParameter( `${ruleName}SqsQueueARN`, customQueue.queueArn );

    }

    private getName(projectPrefix: string, name: string, suffix: string) {
        // const suffix = process.env.BUILDNUMBER || 'devBuild'
        return `${projectPrefix}-${name}-${suffix}`.toLowerCase();
    }

    private getShortName(projectPrefix: string, name: string) {
        
        return `${projectPrefix}-${name}`.toLowerCase().substring(0, 70)
    }


}