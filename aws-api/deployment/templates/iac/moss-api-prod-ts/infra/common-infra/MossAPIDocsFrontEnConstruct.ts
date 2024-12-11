import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {BlockPublicAccess, Bucket, BucketEncryption, IBucket} from "aws-cdk-lib/aws-s3";
import * as cdk from "aws-cdk-lib";
import {aws_lambda as lambda, aws_route53 as route53, CfnOutput, Duration, Fn, RemovalPolicy} from "aws-cdk-lib";
import * as cloudfront from "aws-cdk-lib/aws-cloudfront";
import {ErrorResponse, HttpVersion, IOriginAccessIdentity, PriceClass} from "aws-cdk-lib/aws-cloudfront";
import {BucketDeployment, Source} from "aws-cdk-lib/aws-s3-deployment";
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import {EdgeFunction} from "aws-cdk-lib/aws-cloudfront/lib/experimental";
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import {ICertificate} from 'aws-cdk-lib/aws-certificatemanager';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import {HttpApi} from "aws-cdk-lib/aws-apigatewayv2";
import {HttpLambdaIntegration} from "aws-cdk-lib/aws-apigatewayv2-integrations";


export interface AwsAPIDocsFrontEndProps extends ConstructCommonProps {
    buildNumber: number;
    buildStage: string,
    project: string,
    domainName: string;
    siteSubDomain: string;
    certificateArnVariableKey: string
    websiteDistSourcePath: string
}


export class AwsAPIDocsFrontEndConstruct extends BaseConstruct {

    private apiCorsLambda: EdgeFunction;
    private staticRewriteLambda: EdgeFunction;

    private certificate: ICertificate;

    constructor(scope: Construct, id: string, props: AwsAPIDocsFrontEndProps) {
        super(scope, id, props);

        const {projectPrefix, buildNumber, buildStage} = props;
        const webACLRef = this.getParameter("webACLWipoIpRef");
        // const webACLRef = this.getVariable("wafAclCloudFrontArnParam");

        //1.
        const siteDomain = props.siteSubDomain + '.' + props.domainName;
        const siteName = `${projectPrefix}-${props.siteSubDomain}`
        new CfnOutput(this, `${props.siteSubDomain}Site`, { value: 'https://' + siteDomain });

        //2.
        // const certificateArn = this.getParameter("certificateArn");
        const certificateArn = this.getVariable(props.certificateArnVariableKey);

        if (certificateArn) {
            this.certificate = acm.Certificate.fromCertificateArn(this, `${props.siteSubDomain}-importedCert`, certificateArn );
            new CfnOutput(this, `${props.siteSubDomain}-Imported Certificate`, { value: this.certificate.certificateArn });
        }

        const apiDocsWebSiteBucket  = new Bucket(scope, 'AwsApiWebSiteBucket', {
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            encryption: BucketEncryption.S3_MANAGED,
            enforceSSL: true,
            versioned: false,
            removalPolicy: RemovalPolicy.DESTROY,
        });

        const orgineAccessIdentiy = new cloudfront.OriginAccessIdentity(this, "Aws-api-docs-orgin-access", {
            comment: `${siteDomain}-OAI`
        })

        const apiEndPoint = this.getParameter('apiGateWayEndPoint');
        const restApi = Fn.select(2, Fn.split("/", apiEndPoint))

        //==== High Level Construct - Start ==== //

        const customErrorResponseProperty: cloudfront.CfnDistribution.CustomErrorResponseProperty = {
            errorCode: 404,
            // the properties below are optional
            errorCachingMinTtl: 100,
            responseCode: 200,
            responsePagePath: '/index.html',
        };

        const errorResponse: cloudfront.ErrorResponse = {
            httpStatus: 404,
            // the properties below are optional
            responseHttpStatus:200,
            responsePagePath: '/index.html',
            ttl: cdk.Duration.minutes(0),
        };

        const cdNewDist = this.createDistributedResource(apiDocsWebSiteBucket,
            orgineAccessIdentiy, props, errorResponse, webACLRef, projectPrefix, buildStage,
            buildNumber, siteDomain, this.certificate);

        //3.
        const zone = route53.HostedZone.fromLookup(this, `${props.siteSubDomain}-Zone`, { domainName: props.domainName });

        const record = new route53.ARecord(this, `${props.siteSubDomain}-SiteAliasRecord-${siteDomain}`, {
            recordName: `${siteDomain}`,
            target: route53.RecordTarget.fromAlias(new targets.CloudFrontTarget(cdNewDist)),
            zone
        });

        new CfnOutput(this, `${props.siteSubDomain}-cfnoutput`, {
            value: record.domainName
        })


        this.putParameter(`${props.siteSubDomain}DistName`,  cdNewDist.distributionDomainName)
        this.putParameter(`${props.siteSubDomain}CloudfrontDistId`,  cdNewDist.distributionId)

    }

    private getCorsFunction(projectPrefix: string, buildStage: string, buildNumber: number, name: string) {

        const apiCorsLambda = new cloudfront.experimental.EdgeFunction(
            this,
            `${projectPrefix}-${name}-cors-${buildStage}-${buildNumber}`,
            {
                code: lambda.Code.fromAsset("./frontend-assets/cloudfront"),
                handler: "cors.onOriginResponse",
                runtime: lambda.Runtime.NODEJS_18_X,
            }
        );
        return apiCorsLambda;
    }

    private getStaticRewriteFunction(projectPrefix: string, buildStage: string, buildNumber: number, name: string) {

        const staticRewriteLambda = new cloudfront.experimental.EdgeFunction(
            this,
            `${projectPrefix}-${name}rewrite-${buildStage}-${buildNumber}`,
            {
                code: lambda.Code.fromAsset("./frontend-assets/cloudfront"),
                handler: "rewrite.onViewerRequest",
                runtime: lambda.Runtime.NODEJS_18_X,
            }
        );
        return staticRewriteLambda;
    }

    private createDistributedResource(AwsFrontEndBucket: IBucket, originAccessIdentityImported: IOriginAccessIdentity,
                                      props: AwsAPIDocsFrontEndProps, errorResponse: ErrorResponse, webACLRef: string,
                                      projectPrefix: string, buildStage: string, buildNumber: number,
                                      siteDomain: string, certificate: acm.ICertificate) {

        const apiEndPoint = this.getParameter('apiGateWayEndPoint');
        const restApi = Fn.select(2, Fn.split("/", apiEndPoint))

        const distribution = new cloudfront.Distribution(this, `${siteDomain!}-apiDocsDistribution`, {
            defaultBehavior: {
                origin: new origins.S3Origin(AwsFrontEndBucket, {
                    originAccessIdentity: originAccessIdentityImported,
                    originId: `${siteDomain!}-distribution`,
                }),
                // edgeLambdas: [{
                //     functionVersion: this.staticRewriteLambda.latestVersion,
                //     eventType: cloudfront.LambdaEdgeEventType.VIEWER_REQUEST,
                //     includeBody: true
                //
                // }],
                // compress: true,
                allowedMethods: cloudfront.AllowedMethods.ALLOW_ALL,
            },
            // additionalBehaviors: {
            //     '/api/*' : {
            //         origin:  new origins.HttpOrigin(restApi),
            //         edgeLambdas: [{
            //             functionVersion: this.apiCorsLambda.currentVersion,
            //             eventType: cloudfront.LambdaEdgeEventType.ORIGIN_RESPONSE,
            //         }],
            //         allowedMethods: cloudfront.AllowedMethods.ALLOW_ALL,
            //     },
            //
            // },
            errorResponses: [errorResponse],
            defaultRootObject: "index.html",
            enableLogging: true,
            // webAclId: webACLRef,
            priceClass: PriceClass.PRICE_CLASS_ALL,
            httpVersion: HttpVersion.HTTP2,
            enabled: true,
            certificate: certificate,
            domainNames: [siteDomain]
        });

        // Deploy the source code from the /deployment folder
        const deployment = new BucketDeployment(this, `${props.siteSubDomain}-deploybucket-${buildStage}-${buildNumber}`, {
            sources: [Source.asset(props.websiteDistSourcePath)],
            destinationBucket: AwsFrontEndBucket,
            distribution,
        });

        return distribution;
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

    private getName(props: AwsAPIDocsFrontEndProps | undefined, name: string) {
        const suffix = process.env.BUILDNUMBER || 'devBuild'
        return `${props!.projectPrefix}-${name}-${suffix}`.toLowerCase();
    }


    private removeHttp(url: string) {
        return url.replace(/^https?:\/\//, '');
    }

    private createDNSRecordSet(hostedZone: route53.HostedZone, recordTarget: route53.RecordTarget) {

        const recordSet = new route53.RecordSet(this, 'MyRecordSet', {
            recordType: route53.RecordType.A,
            target: recordTarget,
            zone: hostedZone,

            // the properties below are optional
            comment: 'comment',
            deleteExisting: false,
            recordName: 'recordName',
            ttl: cdk.Duration.minutes(30),
        });


    }
}