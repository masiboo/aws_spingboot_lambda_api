import * as base from '../../lib/template/stack/base/base-stack';
import {AppContext} from '../../lib/template/app-context';
import {AwsApiConstruct} from "../common-infra/AwsAPiConstruct";
import {HttpMethod} from "@aws-cdk/aws-apigatewayv2-alpha";
import {Duration} from "aws-cdk-lib";

export class AwsApiStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        new AwsApiConstruct(this, 'AwsApiConstruct', {
            stackName: this.stackName,
            projectPrefix: this.projectPrefix,
            env: this.commonProps.env!,
            stackConfig: this.stackConfig,
            variables: this.commonProps.variables,

            // suffix: this.stackConfig.suffix,
            lambdaFunctions: [
                {
                    functionName: 'healthcheck-function',
                    functionId: 'HealthCheckFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsHealthCheck',
                    integerationId: 'healthCheckIntegeration',
                    path: '/api/healthcheck',
                    methods: [HttpMethod.GET],
                    s3Policy: false,

                },
                {
                    functionName: 'versioncheck-function',
                    functionId: 'VersionCheckFunction',
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.AwsVersionCheck',
                    integerationId: 'versionCheckIntegeration',
                    path: '/api/version',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName: 'signed-url-function',
                    functionId: 'SignedUrlFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactUploadURLRequestHandler',
                    integerationId: 'SignedUrlFunctionIntegration',
                    path: '/api/artefacts/upload',
                    methods: [HttpMethod.POST],
                    s3Policy: true,
                },
                {
                    functionName: 'signed-url-batch-function',
                    functionId: 'SignedUrlBatchFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactBatchUploadURLRequestHandler',
                    integerationId: 'SignedUrlBatchFunctionIntegration',
                    path: '/api/batches/upload/{scannedApp}',
                    methods: [HttpMethod.POST],
                    s3Policy: true,
                    timeOut: Duration.seconds(300),
                },
                {
                    functionName: 'get-all-artefacts-function',
                    functionId: 'GetAllArtefactsFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactRequestHandler',
                    integerationId: 'GetAllArtefactsFunctionIntegration',
                    path: '/api/artefacts',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName: 'get-artefact-by-id',
                    functionId: 'GetbyIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ApiGatewayGetDocumentByDocIdRequestHandler',
                    integerationId: 'GetbyIdFunctionIntegration',
                    path: '/api/artefacts/{artefactId}',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName: 'delete-artefact-by-id',
                    functionId: 'DeleteArtefactByIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDeleteByIdHandler',
                    integerationId: 'DeleteArtefactByIdIntegration',
                    path: '/api/artefacts/delete/{artefactId}',
                    methods: [HttpMethod.PUT],
                    s3Policy: false,
                },
                {
                    functionName: 'artefact-input-validate',
                    functionId: 'ValidateInputFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactInputValidationRequestHandler',
                    integerationId: 'ValidateInputFunctionIntegration',
                    path: '/api/artefacts/validate',
                    methods: [HttpMethod.POST],
                    s3Policy: false,
                },
                {
                    functionName:'get-job-status-by-id',
                    functionId: 'GetJobStatusByIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactJobStatusCheck',
                    integerationId: 'GetJobStatusByIdFunctionIntegration',
                    path: '/api/job/{jobid}/status',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName:  'get-all-job-status-by-requestId',
                    functionId: 'GetAllJobStatusByRequestIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactJobStatusReportHandler',
                    integerationId: 'GetAllJobStatusByRequestIdFunctionIntegration',
                    path: '/api/job/status/{requestId}',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName: 'get-Artefact-ByArtefactId-function',
                    functionId: 'GetArtefactByArtefactIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDownloadUrlByArtefactId',
                    integerationId: 'GetArtefactByArtefactIdIntegration',
                    path: '/api/artefacts/{artefactId}/url',
                    methods: [HttpMethod.GET],
                    s3Policy: true,
                },
                {
                    functionName: 'get-artefact-by-doc-id',
                    functionId: 'GetArtefactByDocIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactsByMirisDocId',
                    integerationId: 'GetArtefactByDocIdIntegration',
                    path: '/api/artefacts/by-doc-id/{mirisDocId}',
                    methods: [HttpMethod.GET],
                    s3Policy: false,
                },
                {
                    functionName: 'get-all-batch-function',
                    functionId: 'GetAllBatchFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.BatchRequestHandler',
                    integerationId: 'GetAllBatchFunctionIntegration',
                    path: '/api/batches',
                    methods: [HttpMethod.GET],
                    s3Policy: true,
                    timeOut: Duration.seconds(300),
                },
                {
                    functionName: 'get-batch-detail-by-id-function',
                    functionId: 'GetBatchDetailbyIdFunction' ,
                    handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.BatchDetailRequestHandler',
                    integerationId: 'GetBatchDetailFunctionIntegration',
                    path: '/api/batch/{batchIdPathParam}',
                    methods: [HttpMethod.GET],
                    s3Policy: true,
                    timeOut: Duration.seconds(300),
                },



            {
                functionName: ('delete-artefact-by-batch-seq'),
                functionId: 'DeleteArtefactByBatchSeqFunction' ,
                handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactDeleteByBatchSeqHandler',
                integerationId: 'DeleteArtefactByBatchSeqIntegration',
                path: '/api/batches/delete/{batchSeq}',
                methods: [HttpMethod.PUT],
                s3Policy: false,
                timeOut: Duration.seconds(300),
            },
            {
                functionName: ('lock-artefact-by-batch-seq'),
                functionId: 'LockArtefactByBatchSeqFunction' ,
                handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactLockByBatchSeqHandler',
                integerationId: 'LockArtefactByBatchSeqIntegration',
                path: '/api/batches/lock/{batchSeq}',
                methods: [HttpMethod.PUT],
                s3Policy: false,
                timeOut: Duration.seconds(300),
            },
            {
                functionName: ('unlock-artefact-by-batch-seq'),
                functionId: 'UnlockArtefactByBatchSeqFunction' ,
                handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ArtefactUnLockByBatchSeqHandler',
                integerationId: 'UnlockArtefactByBatchSeqIntegration',
                path: '/api/batches/unlock/{batchSeq}',
                methods: [HttpMethod.PUT],
                s3Policy: false,
                timeOut: Duration.seconds(300),
            },


            {
                functionName: ('index-artefact-by-id'),
                functionId: 'indexArtefactById' ,
                handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.IndexArtefactByIdHandler',
                integerationId: 'IndexArtefactByIdFunctionIntegration',
                path: '/api/artefacts/index/{artefactId}',
                methods: [HttpMethod.PUT],
                s3Policy: false,
            },

            {
                functionName: ('validate-miris-doc-id'),
                functionId: 'ValidateMirisDocId' ,
                handlerPath: 'org.wipo.trademarks.aws.Aws.artefact.entrypoints.ValidateMirisDocIdHandler',
                integerationId: 'ValidateMirisDocIdIntegration',
                path: '/api/v1/validate/documents/{mirisDocId}',
                methods: [HttpMethod.GET],
                s3Policy: false,
            }
            ]
        })

    }


}
