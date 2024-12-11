import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {BlockPublicAccess, Bucket, BucketEncryption, IBucket} from "aws-cdk-lib/aws-s3";
import * as cdk from "aws-cdk-lib";
import {aws_iam as iam, aws_lambda as lambda, CfnOutput, Duration, Fn, RemovalPolicy} from "aws-cdk-lib";
import * as cloudfront from "aws-cdk-lib/aws-cloudfront";
import {
    ErrorResponse,
    HttpVersion,
    IOriginAccessIdentity,
    IOriginRequestPolicy, OriginRequestPolicy,
    PriceClass
} from "aws-cdk-lib/aws-cloudfront";
import {BucketDeployment, Source} from "aws-cdk-lib/aws-s3-deployment";
import { aws_route53 as route53 } from 'aws-cdk-lib';
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import {EdgeFunction} from "aws-cdk-lib/aws-cloudfront/lib/experimental";
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import {ICertificate} from "aws-cdk-lib/aws-certificatemanager";
import {HttpApi} from "aws-cdk-lib/aws-apigatewayv2";
import {HttpLambdaIntegration} from "aws-cdk-lib/aws-apigatewayv2-integrations";

export interface AwsFrontEndProps extends ConstructCommonProps {
    buildNumber: number;
    buildStage: string,
    project: string,
    domainName: string;
    siteSubDomain: string;
    certificateArnVariableKey: string
    lambdaRewrite: boolean
    bucketSSMKey: string
    createS3Bucket: boolean
    websiteDistSourcePath: string
}


export class AwsFrontEndConstruct extends BaseConstruct {

    private apiCorsLambda: EdgeFunction;
    private staticRewriteLambda: EdgeFunction;

    private certificate: ICertificate;


    constructor(scope: Construct, id: string, props: AwsFrontEndProps) {
        super(scope, id, props);

        const {projectPrefix, buildNumber, buildStage} = props;

        // #todo move these outside constuct
        const apiEndPoint = this.getParameter('apiGateWayEndPoint');
        // const webACLRef = this.getParameter("webACLWipoIpRef");
        // const webACLRef = this.getVariable("wafAclCloudFrontArnParamBC");
        const webACLRef = "dummyId";
        const cloudfrontOAIRef: string = this.getParameter("cloudfrontOAI");
        const bucketDomain: string = this.getParameter("AwsFrontendDomain");
        const bucketName: string = this.getParameter("AwsFrontendBucketName");
        const logsBucketArn: string =  this.getParameter("LogsBucketArn");

        let AwsFrontEndBucket;
        if (props.createS3Bucket) {
            AwsFrontEndBucket = new Bucket(scope, `${props.siteSubDomain}-Bucket`, {
                blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
                encryption: BucketEncryption.S3_MANAGED,
                enforceSSL: true,
                versioned: true,
                removalPolicy: RemovalPolicy.DESTROY,
            });
        } else {
            AwsFrontEndBucket  = Bucket.fromBucketName(this, 'imported-Aws-bucket', bucketName);
        }

        //1.
        const zone = route53.HostedZone.fromLookup(this, `${props.siteSubDomain}-Zone`, { domainName: props.domainName });
        const siteDomain = props.siteSubDomain + '.' + props.domainName;
        new CfnOutput(this, `${props.siteSubDomain} Site`, { value: 'https://' + siteDomain });

        //2.
        // const certificateArn = this.getParameter("certificateArn");
        const certificateArn = this.getVariable(props.certificateArnVariableKey);

        if (certificateArn) {
            this.certificate = acm.Certificate.fromCertificateArn(this, `${props.siteSubDomain}-importedCert`, certificateArn );
            new CfnOutput(this, `${props.siteSubDomain}-Imported Certificate`, { value: this.certificate.certificateArn });
        }

        const logBucket = Bucket.fromBucketArn(this, `${props.siteSubDomain}-imported-log-bucket`, logsBucketArn);

        const originAccessIdentityImported = cloudfront.OriginAccessIdentity
            .fromOriginAccessIdentityId( this, `${props.siteSubDomain}-imported-OAI`, cloudfrontOAIRef);

        // const orgineAccessIdentiy = new cloudfront.OriginAccessIdentity(this, "orgin-access", {
        //
        // })
        //
        // // Replacement OAI
        // const cfnOriginAccessControl = new cloudfront.CfnOriginAccessControl(this, 'MyCfnOriginAccessControl', {
        //     originAccessControlConfig: {
        //         name: 'name',
        //         originAccessControlOriginType: 'originAccessControlOriginType',
        //         signingBehavior: 'signingBehavior',
        //         signingProtocol: 'signingProtocol',
        //
        //         // the properties below are optional
        //         description: 'description',
        //     },
        // });


        // Add a Lambda@Edge to add CORS headers to the API.
        this.apiCorsLambda = this.getCorsFunction(projectPrefix, buildStage, buildNumber, props.siteSubDomain );


        // Add a Lambda@Edge to rewrite paths and add redirects headers to the static site.
        if (props.lambdaRewrite) {
            this.staticRewriteLambda = this.getStaticRewriteFunction(projectPrefix, buildStage, buildNumber, props.siteSubDomain);
        }

        const customErrorResponseProperty: cloudfront.CfnDistribution.CustomErrorResponseProperty = {
            errorCode: 404,
            // the properties below are optional
            errorCachingMinTtl: 100,
            responseCode: 200,
            responsePagePath: '/index.html',
        };

        // todo: add more error responses
        const errorResponse: cloudfront.ErrorResponse = {
            httpStatus: 404,
            // the properties below are optional
            responseHttpStatus:200,
            responsePagePath: '/index.html',
            ttl: cdk.Duration.minutes(0),
        };

        const AwsCachePolicy = new cloudfront.CachePolicy(this, `${projectPrefix}CachePolicy`, {
            cachePolicyName: `${projectPrefix}-custom-cache-policy`,
            comment: 'Aws Default Cache policy',
            // defaultTtl: Duration.days(2),
            // minTtl: Duration.minutes(1),
            // maxTtl: Duration.days(10),
            // cookieBehavior: cloudfront.CacheCookieBehavior.all(),
            headerBehavior: cloudfront.CacheHeaderBehavior.allowList('Authorization'),
            // queryStringBehavior: cloudfront.CacheQueryStringBehavior.denyList('username'),
            enableAcceptEncodingGzip: true,
            enableAcceptEncodingBrotli: true,
        });

        // Create the CloudFront Web Distribution
        const cfWebDist = new cloudfront.CloudFrontWebDistribution(this, ` ${projectPrefix}-webdist`, {

            originConfigs: [
                {

                    customOriginSource: {
                        domainName: Fn.select(2, Fn.split("/", apiEndPoint)),
                        originPath: '',

                    },

                    behaviors: [
                        {
                            lambdaFunctionAssociations: [
                                {
                                    lambdaFunction: this.apiCorsLambda,
                                    eventType: cloudfront.LambdaEdgeEventType.ORIGIN_RESPONSE,
                                },
                            ],

                            allowedMethods: cloudfront.CloudFrontAllowedMethods.ALL,
                            pathPattern: "api/**",
                            maxTtl: Duration.millis(0),
                            minTtl: Duration.millis(0),
                            defaultTtl: Duration.millis(0),

                            forwardedValues: {
                                queryString: true,
                                headers: ['Authorization'],
                            },
                        },

                    ],
                },

                {
                    s3OriginSource: {
                        s3BucketSource: AwsFrontEndBucket,
                        originAccessIdentity: originAccessIdentityImported,

                    },
                    behaviors: [
                        { lambdaFunctionAssociations: [
                                {
                                    lambdaFunction: this.staticRewriteLambda,
                                    eventType: cloudfront.LambdaEdgeEventType.VIEWER_REQUEST,
                                },
                            ],
                            isDefaultBehavior: true
                        }
                    ],
                },
            ],
            errorConfigurations: [customErrorResponseProperty],
            // webACLId: webACLRef,
            priceClass: PriceClass.PRICE_CLASS_ALL,
            httpVersion: HttpVersion.HTTP2,
            enabled: true,
            defaultRootObject: "index.html",
            viewerCertificate: {
                aliases: [siteDomain],
                props: {
                    acmCertificateArn: certificateArn,
                    sslSupportMethod: "sni-only",
                },
            },
        });

        const cfDistdeployment = new BucketDeployment(this, `${projectPrefix}-${props.siteSubDomain}-deploybucket-${buildStage}-${buildNumber}`, {
            sources: [Source.asset(props.websiteDistSourcePath)],
            destinationBucket: AwsFrontEndBucket,
            distribution: cfWebDist,
        });

        // // todo - new distribution api to be tested
        const cdNewDist = this.createDistributedResource(AwsFrontEndBucket,
            originAccessIdentityImported, props, errorResponse, webACLRef, projectPrefix, buildStage,
            buildNumber, siteDomain, this.certificate);

        new CfnOutput(this, `${props.siteSubDomain}-cfnoutput-${buildStage}-${buildNumber}`, {
            value: cfWebDist.distributionDomainName,
        });

        this.putParameter('AwsAdminWeb',  cfWebDist.distributionDomainName)
        this.putParameter('adminCloudfrontDistId',  cfWebDist.distributionId)

        //3.
        new route53.ARecord(this, `${props.siteSubDomain}-SiteAliasRecord`, {
            recordName: siteDomain,
            target: route53.RecordTarget.fromAlias(new targets.CloudFrontTarget(cfWebDist)),
            zone
        });
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
                                      props: AwsFrontEndProps, errorResponse: ErrorResponse, webACLRef: string,
                                      projectPrefix: string, buildStage: string, buildNumber: number,
                                      siteDomain?: string, certificate?: acm.ICertificate) {

        const apiEndPoint = this.getParameter('apiGateWayEndPoint');
        const restApi = Fn.select(2, Fn.split("/", apiEndPoint))

        const originRequestPolicy: IOriginRequestPolicy = new OriginRequestPolicy(this, "AwsOriginRequestPolicy", {
            originRequestPolicyName: 'AwsPolicy',
            comment: 'A default policy',
            cookieBehavior: cloudfront.OriginRequestCookieBehavior.all(),
            headerBehavior: cloudfront.OriginRequestHeaderBehavior.all(), // add custom secret headers here
            queryStringBehavior: cloudfront.OriginRequestQueryStringBehavior.all(),
        })

        const distribution = new cloudfront.Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new origins.S3Origin(AwsFrontEndBucket, {
                    originAccessIdentity: originAccessIdentityImported,
                    originId: `${props.projectPrefix}-origin-distribution`,
                    customHeaders: {
                        Foo: 'bar',
                    },

                }),
                edgeLambdas: [{
                    functionVersion: this.staticRewriteLambda.currentVersion,
                    eventType: cloudfront.LambdaEdgeEventType.VIEWER_REQUEST,
                    includeBody: true

                }],
                // compress: true,
                allowedMethods: cloudfront.AllowedMethods.ALLOW_ALL,
                // viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
            },
            additionalBehaviors: {
                '/api/**' : {
                    origin:  new origins.HttpOrigin(restApi),
                    originRequestPolicy: originRequestPolicy,
                    edgeLambdas: [{
                        functionVersion: this.apiCorsLambda.currentVersion,
                        eventType: cloudfront.LambdaEdgeEventType.ORIGIN_RESPONSE,
                    }],
                    allowedMethods: cloudfront.AllowedMethods.ALLOW_ALL,
                },

            },
            errorResponses: [errorResponse],
            defaultRootObject: "index.html",
            // domainNames: [siteDomain],
            // certificate: certificate,
            enableLogging: true,
            // logBucket: logBucket,
            // webAclId: webACLRef,
            priceClass: PriceClass.PRICE_CLASS_ALL,
            httpVersion: HttpVersion.HTTP2,
            enabled: true,

        });

        // Deploy the source code from the /deployment folder
        const deployment = new BucketDeployment(this, `${projectPrefix}-${props.siteSubDomain}-deploybucket-v2-${buildStage}-${buildNumber}`, {
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

    private getName(props: AwsFrontEndProps | undefined, name: string) {
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