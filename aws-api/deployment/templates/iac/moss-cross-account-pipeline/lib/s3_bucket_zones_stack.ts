
import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as kms from 'aws-cdk-lib/aws-kms';
import * as s3 from 'aws-cdk-lib/aws-s3';
import {
    PROD, S3_ACCESS_LOG_BUCKET, S3_CONFORMED_BUCKET, S3_KMS_KEY, S3_PURPOSE_BUILT_BUCKET, S3_RAW_BUCKET, ACC,
    getEnvironmentConfiguration, getLogicalIdPrefix, getResourceNamePrefix
} from './configuration';

export class S3BucketZonesStack extends cdk.Stack {
    private readonly targetEnvironment: string;
    private removalPolicy: cdk.RemovalPolicy;

    constructor(scope: Construct, id: string, targetEnvironment: string, deploymentAccountId: string, ...rest: any[]) {
        super(scope, id, ...rest);

        this.targetEnvironment = targetEnvironment;
        const mappings = getEnvironmentConfiguration(targetEnvironment);
        const logicalIdPrefix = getLogicalIdPrefix();
        const resourceNamePrefix = getResourceNamePrefix();

        this.removalPolicy = cdk.RemovalPolicy.DESTROY;
        if (targetEnvironment === PROD || targetEnvironment === ACC) {
            this.removalPolicy = cdk.RemovalPolicy.RETAIN;
        }

        const s3KmsKey = this.createKmsKey(this.account, logicalIdPrefix, resourceNamePrefix);
        const accessLogsBucket = this.createAccessLogsBucket(
            `${targetEnvironment}${logicalIdPrefix}AccessLogsBucket`,
            `${targetEnvironment.toLowerCase()}-${resourceNamePrefix}-${this.account}-${this.region}-access-logs`,
            s3KmsKey
        );

        const rawBucket = this.createDataLakeZoneBucket(
            `${targetEnvironment}${logicalIdPrefix}RawBucket`,
            `${targetEnvironment.toLowerCase()}-${resourceNamePrefix}-${this.account}-${this.region}-raw`,
            accessLogsBucket,
            s3KmsKey
        );

        const conformedBucket = this.createDataLakeZoneBucket(
            `${targetEnvironment}${logicalIdPrefix}ConformedBucket`,
            `${targetEnvironment.toLowerCase()}-${resourceNamePrefix}-${this.account}-${this.region}-conformed`,
            accessLogsBucket,
            s3KmsKey
        );

        const purposeBuiltBucket = this.createDataLakeZoneBucket(
            `${targetEnvironment}${logicalIdPrefix}PurposeBuiltBucket`,
            `${targetEnvironment.toLowerCase()}-${resourceNamePrefix}-${this.account}-${this.region}-purpose-built`,
            accessLogsBucket,
            s3KmsKey
        );

        // Stack Outputs that are programmatically synchronized
        new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}KmsKeyArn`, {
            value: s3KmsKey.keyArn,
            exportName: mappings[S3_KMS_KEY],
        });

        new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}AccessLogsBucketName`, {
            value: accessLogsBucket.bucketName,
            exportName: mappings[S3_ACCESS_LOG_BUCKET],
        });

        new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}RawBucketName`, {
            value: rawBucket.bucketName,
            exportName: mappings[S3_RAW_BUCKET],
        });

        new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}ConformedBucketName`, {
            value: conformedBucket.bucketName,
            exportName: mappings[S3_CONFORMED_BUCKET],
        });

        new cdk.CfnOutput(this, `${targetEnvironment}${logicalIdPrefix}PurposeBuiltBucketName`, {
            value: purposeBuiltBucket.bucketName,
            exportName: mappings[S3_PURPOSE_BUILT_BUCKET],
        });
    }

    private createKmsKey(deploymentAccountId: string, logicalIdPrefix: string, resourceNamePrefix: string): kms.Key {
        const s3KmsKey = new kms.Key(this, `${this.targetEnvironment}${logicalIdPrefix}KmsKey`, {
            admins: [new iam.AccountPrincipal(this.account)],
            description: 'Key used for encrypting Data Lake S3 Buckets',
            removalPolicy: this.removalPolicy,
            alias: `${this.targetEnvironment.toLowerCase()}-${resourceNamePrefix}-kms-key`,
        });

        s3KmsKey.addToResourcePolicy(new iam.PolicyStatement({
            principals: [
                new iam.AccountPrincipal(this.account),
                new iam.AccountPrincipal(deploymentAccountId),
            ],
            actions: [
                'kms:Encrypt',
                'kms:Decrypt',
                'kms:ReEncrypt*',
                'kms:GenerateDataKey*',
                'kms:DescribeKey',
            ],
            resources: ["*"],
        }));

        return s3KmsKey;
    }

    private createDataLakeZoneBucket(
        logicalId: string,
        bucketName: string,
        accessLogsBucket: s3.Bucket,
        s3KmsKey: kms.Key
    ): s3.Bucket {
        let lifecycleRules: s3.LifecycleRule[] = [
            {
                enabled: true,
                expiration: cdk.Duration.days(60),
                noncurrentVersionExpiration: cdk.Duration.days(30),
            }
        ];

        if (this.targetEnvironment === PROD) {
            lifecycleRules = [
                {
                    enabled: true,
                    expiration: cdk.Duration.days(2555),
                    noncurrentVersionExpiration: cdk.Duration.days(90),
                    transitions: [
                        {
                            storageClass: s3.StorageClass.GLACIER,
                            transitionAfter: cdk.Duration.days(365),
                        }
                    ]
                }
            ];
        }

        const bucket = new s3.Bucket(this, logicalId, {
            accessControl: s3.BucketAccessControl.PRIVATE,
            blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
            bucketKeyEnabled: true,
            bucketName: bucketName,
            encryption: s3.BucketEncryption.KMS,
            encryptionKey: s3KmsKey,
            lifecycleRules: lifecycleRules,
            publicReadAccess: false,
            removalPolicy: this.removalPolicy,
            versioned: true,
            objectOwnership: s3.ObjectOwnership.OBJECT_WRITER,
            serverAccessLogsBucket: accessLogsBucket,
            serverAccessLogsPrefix: bucketName,
        });

        const policyDocumentStatements: iam.PolicyStatement[] = [
            new iam.PolicyStatement({
                sid: 'OnlyAllowSecureTransport',
                effect: iam.Effect.DENY,
                principals: [new iam.AnyPrincipal()],
                actions: [
                    's3:GetObject',
                    's3:PutObject',
                ],
                resources: [`${bucket.bucketArn}/*`],
                conditions: { 'Bool': { 'aws:SecureTransport': 'false' } },
            })
        ];

        if (this.targetEnvironment === PROD || this.targetEnvironment === ACC) {
            policyDocumentStatements.push(
                new iam.PolicyStatement({
                    sid: 'BlockUserDeletionOfBucket',
                    effect: iam.Effect.DENY,
                    principals: [new iam.AnyPrincipal()],
                    actions: ['s3:DeleteBucket'],
                    resources: [bucket.bucketArn],
                    conditions: { 'StringLike': { 'aws:userId': `arn:aws:iam::${this.account}:user/*` } },
                })
            );
        }

        policyDocumentStatements.forEach(statement => {
            bucket.addToResourcePolicy(statement);
        });

        return bucket;
    }

    private createAccessLogsBucket(logicalId: string, bucketName: string, s3KmsKey: kms.Key): s3.Bucket {
        return new s3.Bucket(this, logicalId, {
            accessControl: s3.BucketAccessControl.LOG_DELIVERY_WRITE,
            blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
            bucketKeyEnabled: true,
            bucketName: bucketName,
            encryption: s3.BucketEncryption.KMS,
            encryptionKey: s3KmsKey,
            publicReadAccess: false,
            removalPolicy: cdk.RemovalPolicy.RETAIN,
            versioned: true,
            objectOwnership: s3.ObjectOwnership.BUCKET_OWNER_PREFERRED,
        });
    }
}
