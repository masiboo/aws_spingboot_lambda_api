import * as base from "../../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {RegistryProps} from "../../registry-service/construct/registry-const";
import {BlockPublicAccess, Bucket, BucketAccessControl, BucketEncryption} from "aws-cdk-lib/aws-s3";
import {RemovalPolicy} from "aws-cdk-lib";

export interface PipelineResourceProps extends base.ConstructCommonProps {
    s3BucketName: string,
    region: string,
    account: string,


}


export class PipelineResourceConstruct extends base.BaseConstruct {

    constructor(scope: Construct, id: string, props: PipelineResourceProps) {
        super(scope, id, props);


        const bucketName = this.getBucketame(this.projectPrefix, props.s3BucketName, props.region, props.account);
        const registryBucket = new Bucket(this, `${props.s3BucketName}-bucket`, {
            bucketName: bucketName,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            encryption: BucketEncryption.S3_MANAGED,
            enforceSSL: true,
            versioned: true,
            removalPolicy: RemovalPolicy.DESTROY, // conditional for acc and dev
            eventBridgeEnabled: true,
            accessControl: BucketAccessControl.PRIVATE,
        });

        // SSM
        this.putParameter(`${props.s3BucketName}BucketName`, registryBucket.bucketName);
        this.putParameter(`${props.s3BucketName}BucketArn`, registryBucket.bucketArn);

    }

    // ${NamePrefix}-artifacts-${AWS::Region}-${AWS::AccountId}
    private getBucketame(projectPrefix: string, name: string, region: string, account: string) {
        const suffix = process.env.BUILDNUMBER || 'devBuild'
        return `${projectPrefix}-${name}-${region}-${account}`.toLowerCase();
    }

}
