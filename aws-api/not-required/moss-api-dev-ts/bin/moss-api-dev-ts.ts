#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { AwsApiDevStack } from '../lib/Aws-api-dev-stack';

const app = new cdk.App();

const project = "Aws-api";
const environment = process.env.BUILDSTAGE || 'dev';
const prefix = process.env.DEVELOPER || 'demo';
const suffix = process.env.BUILDNUMBER && parseInt(process.env.BUILDNUMBER, 10) || '1';

new AwsApiDevStack(app, 'AwsApiDevTsStack', {
    stackName: `${project}-${environment}-${prefix}-${suffix}`,
    project,
    environment,
    prefix,
    suffix,

    /* If you don't specify 'env', this stack will be environment-agnostic.
 * Account/Region-dependent features and context lookups will not work,
 * but a single synthesized template can be deployed anywhere. */

    /* Uncomment the next line to specialize this stack for the AWS Account
     * and Region that are implied by the current CLI configuration. */
    // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

    /* Uncomment the next line if you know exactly what Account and Region you
     * want to deploy the stack to. */
    // env: { account: '123456789012', region: 'us-east-1' },
});
