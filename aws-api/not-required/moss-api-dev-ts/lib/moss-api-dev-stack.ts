import {CfnOutput, Duration, Fn, RemovalPolicy, Stack, StackProps} from 'aws-cdk-lib';
import * as s3 from 'aws-cdk-lib/aws-s3'
import {
  BlockPublicAccess,
  Bucket,
  BucketAccessControl,
  BucketEncryption,
  EventType,
  HttpMethods
} from 'aws-cdk-lib/aws-s3'
import {Construct} from 'constructs';
import {AttributeType, BillingMode, ProjectionType, Table} from "aws-cdk-lib/aws-dynamodb";
import {Code, Function, Runtime, Tracing} from "aws-cdk-lib/aws-lambda";
import {RetentionDays} from "aws-cdk-lib/aws-logs";
import {Integration} from "aws-cdk-lib/aws-apigateway";
import {CorsHttpMethod, HttpApi, HttpMethod, PayloadFormatVersion} from "@aws-cdk/aws-apigatewayv2-alpha";
import {HttpLambdaIntegration} from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import {LambdaDestination} from "aws-cdk-lib/aws-s3-notifications";
import {Policy, PolicyStatement} from "aws-cdk-lib/aws-iam";

declare const integration: Integration;

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

export interface AwsApiStackProps extends StackProps {
  readonly stackName: string;
  readonly environment: string;
  readonly prefix: string;
  readonly suffix: string | number;

  readonly project: string;

}
export class AwsApiDevStack extends Stack {


  constructor(scope: Construct, id: string, props?: AwsApiStackProps) {
    super(scope, id, props);

 // S3-stack
    const corsRule: s3.CorsRule = {
      allowedMethods: [s3.HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT ],
      allowedOrigins: ['*'],

      // the properties below are optional
      allowedHeaders: ['*'],
      exposedHeaders: ['exposedHeaders'],
      id: 'madrid-Aws-bucket-cors-rule',
      maxAge: 100,
    };
    const bucketName = this.getName(props, 'registry-bucket');
    const registryBucket = new Bucket(this, 'madrid-Aws-bucket', {
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
    const registryTableName = this.getName(props, 'Aws-table');
    const registryTable = new Table(this, "AwsRegistryTableDev", {
      tableName: registryTableName,
      removalPolicy: RemovalPolicy.DESTROY,
      partitionKey: { name: 'PK', type: AttributeType.STRING },
      sortKey: { name: 'SK', type: AttributeType.STRING},
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
    // functions
    const lambdaList: LambdaListType[] = [{
      functionName: this.getName(props, 'healthcheck-function'),
      functionId: 'HealthCheckFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsHealthCheck',
      integerationId: 'healthCheckIntegeration',
      path: '/api/healthcheck',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
      {
        functionName: this.getName(props, 'versioncheck-function'),
        functionId: 'VersionCheckFunction',
        handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsVersionCheck',
        integerationId: 'versionCheckIntegeration',
        path: '/api/version',
        methods: [HttpMethod.GET],
        s3Policy: false,
      },
      {
      functionName: this.getName(props, 'signed-url-function'),
      functionId: 'SignedUrlFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactUploadURLRequestHandler',
      integerationId: 'SignedUrlFunctionIntegration',
      path: '/api/artefacts/upload',
      methods: [HttpMethod.POST],
      s3Policy: true,
    },
      {
      functionName: this.getName(props, 'signed-url-batch-function'),
      functionId: 'SignedUrlBatchFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactBatchUploadURLRequestHandler',
      integerationId: 'SignedUrlBatchFunctionIntegration',
      path: '/api/batches/upload/{scannedApp}',
      methods: [HttpMethod.POST],
      s3Policy: true,
        timeOut: Duration.seconds(300),
    },

      {
        functionName: this.getName(props, 'get-all-batch-function'),
        functionId: 'GetAllBatchFunction' ,
        handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.BatchRequestHandler',
        integerationId: 'GetAllBatchFunctionIntegration',
        path: '/api/batches',
        methods: [HttpMethod.GET],
        s3Policy: true,
        timeOut: Duration.seconds(300),
      },
      {
        functionName: this.getName(props, 'get-batch-detail-by-id-function'),
        functionId: 'GetBatchDetailbyIdFunction' ,
        handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.BatchDetailRequestHandler',
        integerationId: 'GetBatchDetailFunctionIntegration',
        path: '/api/batch/{batchIdPathParam}',
        methods: [HttpMethod.GET],
        s3Policy: true,
        timeOut: Duration.seconds(300),
      },
      {
       functionName: this.getName(props, 'delete-artefact-by-batch-seq'),
       functionId: 'DeleteArtefactByBatchSeqFunction' ,
       handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDeleteByBatchSeqHandler',
       integerationId: 'DeleteArtefactByBatchSeqIntegration',
       path: '/api/batches/delete/{batchSeq}',
       methods: [HttpMethod.PUT],
       s3Policy: false,
       timeOut: Duration.seconds(300),
      },
      {
       functionName: this.getName(props, 'lock-artefact-by-batch-seq'),
       functionId: 'LockArtefactByBatchSeqFunction' ,
       handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactLockByBatchSeqHandler',
       integerationId: 'LockArtefactByBatchSeqIntegration',
       path: '/api/batches/lock/{batchSeq}',
       methods: [HttpMethod.PUT],
       s3Policy: false,
       timeOut: Duration.seconds(300),
      },
      {
       functionName: this.getName(props, 'unlock-artefact-by-batch-seq'),
       functionId: 'UnlockArtefactByBatchSeqFunction' ,
       handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactUnLockByBatchSeqHandler',
       integerationId: 'UnlockArtefactByBatchSeqIntegration',
       path: '/api/batches/unlock/{batchSeq}',
       methods: [HttpMethod.PUT],
       s3Policy: false,
       timeOut: Duration.seconds(300),
      },
      {
      functionName: this.getName(props, 'get-all-artefacts-function'),
      functionId: 'GetAllArtefactsFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactRequestHandler',
      integerationId: 'GetAllArtefactsFunctionIntegration',
      path: '/api/artefacts',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
      {
      functionName: this.getName(props, 'get-artefact-by-id'),
      functionId: 'GetbyIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ApiGatewayGetDocumentByDocIdRequestHandler',
      integerationId: 'GetbyIdFunctionIntegration',
      path: '/api/artefacts/{artefactId}',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
        {
          functionName: this.getName(props, 'index-artefact-by-id'),
          functionId: 'indexArtefactById' ,
          handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.IndexArtefactByIdHandler',
          integerationId: 'IndexArtefactByIdFunctionIntegration',
          path: '/api/artefacts/index/{artefactId}',
          methods: [HttpMethod.PUT],
          s3Policy: false,
    },
      {
       functionName: this.getName(props, 'delete-artefact-by-id'),
       functionId: 'DeleteArtefactByIdFunction' ,
       handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDeleteByIdHandler',
       integerationId: 'DeleteArtefactByIdIntegration',
       path: '/api/artefacts/delete/{artefactId}',
       methods: [HttpMethod.PUT],
       s3Policy: false,
    },
      {
      functionName: this.getName(props, 'artefact-input-validate'),
      functionId: 'ValidateInputFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactInputValidationRequestHandler',
      integerationId: 'ValidateInputFunctionIntegration',
      path: '/api/artefacts/validate',
      methods: [HttpMethod.POST],
      s3Policy: false,
    },
      {
      functionName: this.getName(props, 'get-job-status-by-id'),
      functionId: 'GetJobStatusByIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactJobStatusCheck',
      integerationId: 'GetJobStatusByIdFunctionIntegration',
      path: '/api/job/{jobid}/status',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
    {
      functionName: this.getName(props, 'get-all-job-status-by-requestId'),
      functionId: 'GetAllJobStatusByRequestIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactJobStatusReportHandler',
      integerationId: 'GetAllJobStatusByRequestIdFunctionIntegration',
      path: '/api/job/status/{requestId}',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
    {
      functionName: this.getName(props, 'get-Artefact-ByArtefactId-function'),
      functionId: 'GetArtefactByArtefactIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDownloadUrlByArtefactId',
      integerationId: 'GetArtefactByArtefactIdIntegration',
      path: '/api/artefacts/{artefactId}/url',
      methods: [HttpMethod.GET],
      s3Policy: true,
    },
    {
      functionName: this.getName(props, 'get-Artefact-info-function'),
      functionId: 'GetArtefactInfoByArtefactIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactInfoHandler',
      integerationId: 'GetArtefactInfoByArtefactIdIntegration',
      path: '/api/artefacts/{artefactId}/info',
      methods: [HttpMethod.GET],
      s3Policy: true,
    },
    {
      functionName: this.getName(props, 'get-artefact-by-doc-id'),
      functionId: 'GetArtefactByDocIdFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactsByMirisDocId',
      integerationId: 'GetArtefactByDocIdIntegration',
      path: '/api/artefacts/by-doc-id/{mirisDocId}',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
    {
      functionName: this.getName(props, 'validate-miris-doc-id'),
      functionId: 'ValidateMirisDocId' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ValidateMirisDocIdHandler',
      integerationId: 'ValidateMirisDocIdIntegration',
      path: '/api/v1/validate/documents/{mirisDocId}',
      methods: [HttpMethod.GET],
      s3Policy: false,
    },
    {
      functionName: this.getName(props, 'artefact-note-creation'),
      functionId: 'ArtefactNoteCreation' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactNoteCreationHandler',
      integerationId: 'ArtefactNoteCreationHandler',
      path: '/api/v1/artefact-notes',
      methods: [HttpMethod.POST],
      s3Policy: false,
    },
    {
      functionName: this.getName(props, 'artefact-note-delete-by-id'),
      functionId: 'ArtefactNoteDeleteById' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactNoteDeleteByIdHandler',
      integerationId: 'ArtefactNoteDeleteByIdHandler',
      path: '/api/v1/artefact-notes/{id}',
      methods: [HttpMethod.DELETE],
      s3Policy: false,
      }    ];

    // api GATEway - HTTP
    const apiName = this.getName(props, 'Awsapi');
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
      }
    });

    const s3BucketPutPolicyStatement = new PolicyStatement({
      actions: ['s3:ListAllMyBuckets', "s3:PutObject", "s3:GetObject"],
      resources: ["arn:aws:s3:::*"]
    })

    const s3WritePolicy = new Policy( this, "s3-write-policy-343red", {
      statements: [s3BucketPutPolicyStatement]
    })


    lambdaList.map(value => {
      const lambdaFunction = this.createLambdaFunction(value.functionName,
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
        this.createHttpRoute(httpApi, value.path, integration, value.methods);
      }

      if (value.s3Policy){
        lambdaFunction.role?.attachInlinePolicy(s3WritePolicy)
      }

    })

    const s3eventHandler : LambdaListType =   {
      s3Policy: false,
      functionName: this.getName(props, 's3--input-trigger-'),
      functionId: 'S3TriggerFunction' ,
      handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.S3EventTriggerHandler',
      integerationId: undefined,
      path: undefined,
      methods: [],
      s3EventHandler: true
    }

    const s3lambdaFunction = this.createLambdaFunction(s3eventHandler.functionName,
        registryTable,
        registryBucket,
        s3eventHandler.functionId,
        s3eventHandler.handlerPath,
        s3eventHandler.timeOut ? s3eventHandler.timeOut : Duration.seconds(3)
    )

    registryBucket.grantReadWrite(s3lambdaFunction);
//        registryBucket.addObjectCreatedNotification(EventType.OBJECT_CREATED, new LambdaDestination(lambdaFunction));
    registryBucket.addEventNotification(EventType.OBJECT_CREATED, new LambdaDestination(s3lambdaFunction));
    registryTable.grantFullAccess(s3lambdaFunction);

    const apiUrlName = this.getName(props, 'api-url')
    const apiUrl = new CfnOutput(this, 'ApiUrl', {
      exportName: apiUrlName,
      value: httpApi.apiEndpoint,
    })
  }

  private createHttpRoute(httpApi: HttpApi, path: string, healthCheckIntegration: HttpLambdaIntegration, method: any) {
    httpApi.addRoutes({
      path: path,
      authorizationScopes: [],
      authorizer: undefined,
      integration: healthCheckIntegration,
      methods: method
    })
  }

  private createLambdaFunction(functionName: string, registryTable: Table, registryBucket: Bucket,
                               functionId: string, handlerPath: string, timeOut: Duration) {
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
      tracing: Tracing.ACTIVE,
      timeout: timeOut,
    });
  }

  private getName(props: AwsApiStackProps | undefined, name: string) {

    return `${props!.project}-${name}-${props!.environment}-${props!.prefix}-${props!.suffix}`;
  }

  private removeHttp(url: string) {
    return url.replace(/^https?:\/\//, '');
  }
}
