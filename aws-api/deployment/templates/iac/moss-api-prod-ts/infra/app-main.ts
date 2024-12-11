#!/usr/bin/env node
import {AppContext, AppContextError, ProjectPrefixType} from '../lib/template/app-context';
import {AwsApiStack} from "./stack/Aws-api-stack";
import {AwsCoreCfnVpcStack} from "./stack/Aws-core-cfn-vpc-stack";
import {AwsCoreVpcRdsStack} from "./stack/Aws-core-vpc-rds-stack";
import {AwsRegistryInfraStack} from "./registry-service/Aws-registry-infra-stack";
import {AwsFrontEndStack} from "./stack/Aws-front-end-stack";
import {EcsAlbServiceStack} from "./ecs-service/ecs-alb-service-stack";
import {AwsCoreCfnWafStack} from "./stack/Aws-core-cfn-waf-stack";
import {AwsCoreCfnPipelineStack} from "./stack/Aws-core-cfn-pipeline-stack";
import {AwsUsEastCertificateStack} from "./stack/Aws-us-east-certificate-stack";
import {AwsCoreCfnPreReqStack} from "./stack/Aws-core-cfn-prereqs-stack";
import {AwsCfnECRBucketStack} from "./stack/Aws-infra-cfn-ecr-bucket-stack";
import {AwsApiV2Stack} from "./stack/Aws-api-v2-stack";
import {AwsAuthStack} from "./stack/Aws-auth-stack";
import {AwsCoreVpcEcsClusterStack} from "./stack/Aws-core-vpc-ecs-cluster-stack";
import {AwsEventsStack} from "./stack/Aws-events-stack";
import {AwsWsApiStack} from './stack/Aws-ws-api-stack';
import {Tags} from "aws-cdk-lib";
import {AwsApiDocsStack} from "./stack/Aws-api-docs-stack";
import {AwsWafCloudFrontStack} from "./stack/Aws-waf-stack";
import {AwsAuthEventsStack} from "./stack/Aws-auth-trigger-stack";
import {AwsApiCertStack} from "./stack/Aws-api-cert-stack";
import {AwsCoreEcsAlbServiceStack} from "./ecs-service/ecs-alb-stack";
import {PipelineStack} from "../lib/template/stack/devops/pipeline-stack";


export const vpcNameKey = "vpcName";
export const vpcIdKey = "vpcId";
export const privateSubnet1Key = "privateSubnet1";
export const privateSubnet2Key = "privateSubnet2";
export const privateSubnet3Key = "privateSubnet3";
export const wafIpRefKey = "wafIpRef";
export const wafRefRegionalKey = "wafRefRegional";
export const cloudMapNamespaceNameKey = "privateDnsNamespaceName";
export const cloudMapNamespaceArnKey = "privateDnsNamespaceArn";
export const cloudMapNamespaceIdKey = "privateDnsNamespaceId";
export const DatabaseHostNameKey = "databaseHostName";
export const DatabaseNameKey = "databaseName";
export const DatabaseSecretArnKey = "databaseSecretArn";

// #todo make this config item
const suffixStageInName = false;
const budgetUnitCode = "madrid-Aws"

try {

    const buildNumber = process.env.BUILDNUMBER || "364970d";
    const version = process.env.VERSION || "0.5.0";

    const appContext = new AppContext({
        appConfigFileKey: 'APP_CONFIG',
        projectPrefixType: suffixStageInName ? ProjectPrefixType.NameStage : ProjectPrefixType.Name
    });

    // Pre-Reqs
    if (appContext.appConfig.Stack.AwsCfnPreRequisite) {
        const preReqStack = new AwsCoreCfnPreReqStack(appContext,
            appContext.appConfig.Stack.AwsCfnPreRequisite)
        Tags.of(preReqStack).add("budget-unit-code", `${budgetUnitCode}`);
    }


    if (appContext.appConfig.Stack.AwsFrontEndCertificate){
        const certStack = new AwsUsEastCertificateStack(appContext, appContext.appConfig.Stack.AwsFrontEndCertificate)
        Tags.of(certStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    // Import or Create new VPC/DNS
    if (appContext.appConfig.Stack.AwsInfraCfnVpcDnsWaf) {
        const vpcStack = new AwsCoreCfnVpcStack(appContext, appContext.appConfig.Stack.AwsInfraCfnVpcDnsWaf);
        Tags.of(vpcStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    // Pre-Reqs Buckets and Repos
    if (appContext.appConfig.Stack.AwsInfraCfnBucketsECR) {
        const bucketStack = new AwsCfnECRBucketStack(appContext,
            appContext.appConfig.Stack.AwsInfraCfnBucketsECR);
        Tags.of(bucketStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    // lambda functions
    if (appContext.appConfig.Stack.AwsApiV1) {
        const apiV1 = new AwsApiStack(appContext, appContext.appConfig.Stack.AwsApiV1)
        Tags.of(apiV1).add("git-rev-number", `${buildNumber}`);
        Tags.of(apiV1).add("budget-unit-code", `${budgetUnitCode}`);

    }

    if (appContext.appConfig.Stack.AwsApiV2) {
        const apiV2 = new AwsApiV2Stack(appContext, appContext.appConfig.Stack.AwsApiV2)
        Tags.of(apiV2).add("git-rev-number", `${buildNumber}`);
        Tags.of(apiV2).add("budget-unit-code", `${budgetUnitCode}`);
    }

    // Cognito Stack
    if (appContext.appConfig.Stack.AwsAuth) {
        const authstack = new AwsAuthStack(appContext, appContext.appConfig.Stack.AwsAuth)
        Tags.of(authstack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    // SQS and Lambda Handlers
    if (appContext.appConfig.Stack.AwsEvents) {
        const eventStack = new AwsEventsStack(appContext, appContext.appConfig.Stack.AwsEvents)
        Tags.of(eventStack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    if (appContext.appConfig.Stack.AwsAuthTrigger) {
        const authTriggerStack = new AwsAuthEventsStack(appContext, appContext.appConfig.Stack.AwsAuthTrigger)
        Tags.of(authTriggerStack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    if (appContext.appConfig.Stack.AwsWsApi) {
        const apiStack = new AwsWsApiStack(appContext, appContext.appConfig.Stack.AwsWsApi)
        Tags.of(apiStack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    if (appContext.appConfig.Stack.AwsInfraRds) {
        const rdsStack = new AwsCoreVpcRdsStack(appContext, appContext.appConfig.Stack.AwsInfraRds);
        Tags.of(rdsStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    if (appContext.appConfig.Stack.AwsCoreAlbService) {
        const albStack = new AwsCoreEcsAlbServiceStack(appContext, appContext.appConfig.Stack.AwsCoreAlbService);
        Tags.of(albStack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    if (appContext.appConfig.Stack.AwsInfraECSCluster) {
        const ecsClusterStack = new AwsCoreVpcEcsClusterStack(appContext, appContext.appConfig.Stack.AwsInfraECSCluster);
        Tags.of(ecsClusterStack).add("budget-unit-code", `${budgetUnitCode}`);
        Tags.of(ecsClusterStack).add("schedule", "6am-8pm");
    }

    // Begin stage infra
    if (appContext.appConfig.Stack.AwsInfraRegistryInfra) {
        const regStack = new AwsRegistryInfraStack(appContext, appContext.appConfig.Stack.AwsInfraRegistryInfra);
        Tags.of(regStack).add("budget-unit-code", `${budgetUnitCode}`);
    }

    // waf + apwsigateway (restapigateway)

    if (appContext.appConfig.Stack.AwsCfnWafCdn) {
        const wafStack = new AwsCoreCfnWafStack(appContext, appContext.appConfig.Stack.AwsCfnWafCdn)
        Tags.of(wafStack).add("budget-unit-code", `${budgetUnitCode}`);

    }


    if (appContext.appConfig.Stack.AwsWafCloudFront){
        const wafStack = new AwsWafCloudFrontStack(appContext, appContext.appConfig.Stack.AwsWafCloudFront)
        Tags.of(wafStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    if (appContext.appConfig.Stack.AwsFrontEnd) {
        const frontEndStack = new AwsFrontEndStack(appContext, appContext.appConfig.Stack.AwsFrontEnd)
        Tags.of(frontEndStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    if (appContext.appConfig.Stack.AwsApiCert) {
        const apiCertStack = new AwsApiCertStack(appContext, appContext.appConfig.Stack.AwsApiCert)
        Tags.of(apiCertStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    if (appContext.appConfig.Stack.AwsApiDocs) {
        const apiDocStack = new AwsApiDocsStack(appContext, appContext.appConfig.Stack.AwsApiDocs)
        Tags.of(apiDocStack).add("budget-unit-code", `${budgetUnitCode}`);

    }

    // Micro-services - DBAccess
    if (appContext.appConfig.Stack.AwsCoreDBAccess) {
        const dbstack = new EcsAlbServiceStack(appContext, appContext.appConfig.Stack.AwsCoreDBAccess);
        Tags.of(dbstack).add("Aws-app", "Aws-db-core");
        Tags.of(dbstack).add("git-rev-number", `${buildNumber}`);
        Tags.of(dbstack).add("budget-unit-code", `${budgetUnitCode}`);
        Tags.of(dbstack).add("schedule", "6am-8pm");


    }

    if (appContext.appConfig.Stack.AwsCoreMediaProcess) {
        const mediaStack = new EcsAlbServiceStack(appContext, appContext.appConfig.Stack.AwsCoreMediaProcess);
        Tags.of(mediaStack).add("Aws-app", "Aws-media-process");
        Tags.of(mediaStack).add("git-rev-number", `${buildNumber}`);
        Tags.of(mediaStack).add("budget-unit-code", `${budgetUnitCode}`);
        Tags.of(mediaStack).add("schedule", "6am-8pm");



    }

    if (appContext.appConfig.Stack.AwsCoreMirisProxy) {
        const mirisproxy = new EcsAlbServiceStack(appContext, appContext.appConfig.Stack.AwsCoreMirisProxy);
        Tags.of(mirisproxy).add("Aws-app", "Aws-miris-proxy");
        Tags.of(mirisproxy).add("git-rev-number", `${buildNumber}`);
        Tags.of(mirisproxy).add("budget-unit-code", `${budgetUnitCode}`);
        Tags.of(mirisproxy).add("schedule", "6am-8pm");


    }

    if (appContext.appConfig.Stack.AwsCoreEmailSvcProcess) {
        const emailservice = new EcsAlbServiceStack(appContext, appContext.appConfig.Stack.AwsCoreEmailSvcProcess);
        Tags.of(emailservice).add("Aws-app", "Aws-email-service");
        Tags.of(emailservice).add("budget-unit-code", `${budgetUnitCode}`);
        Tags.of(emailservice).add("schedule", "6am-8pm");


    }

} catch (error) {
    if (error instanceof AppContextError) {
        console.error('[AppContextError]:', error.message);
    } else {
        console.error('[Error]: not-handled-error', error);
    }
}
