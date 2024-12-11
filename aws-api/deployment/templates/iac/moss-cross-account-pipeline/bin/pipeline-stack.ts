// !/usr/bin/env node

import * as cdk from 'aws-cdk-lib';
import * as process from 'process';
import { PipelineStack } from '../lib/pipeline-stack';
import { EmptyStack } from '../lib/empty-stack';
import {
    ACCOUNT_ID, DEPLOYMENT, DEV, ACC, PROD, REGION,
    getLogicalIdPrefix, getAllConfigurations
} from '../lib/configuration';
import { tag } from '../lib/tagging';

const app = new cdk.App();

if (process.env.IS_BOOTSTRAP) {
    new EmptyStack(app, 'StackStub');
} else {
    const rawMappings = getAllConfigurations();

    const deploymentAccount = rawMappings[DEPLOYMENT][ACCOUNT_ID];
    const deploymentRegion = rawMappings[DEPLOYMENT][REGION];
    const deploymentAwsEnv = {
        account: deploymentAccount,
        region: deploymentRegion,
    };
    const logicalIdPrefix = getLogicalIdPrefix();

    if (process.env.ENV === DEV || process.env.ENV === undefined) {

        const targetEnvironment = DEV;
        const devAccount = rawMappings[DEV][ACCOUNT_ID];
        const devRegion = rawMappings[DEV][REGION];
        const devAwsEnv = {
            account: devAccount,
            region: devRegion,
        };
        const devPipelineStack = new PipelineStack(
            app,
            `${targetEnvironment}${logicalIdPrefix}InfrastructurePipeline`,
            DEV,
            'main',
            devAwsEnv,
            deploymentAwsEnv
        );
        tag(devPipelineStack, DEPLOYMENT);
    }

    if (process.env.ENV === ACC) {
        console.log("test active")

        const targetEnvironment = ACC;
        const testAccount = rawMappings[ACC][ACCOUNT_ID];
        const testRegion = rawMappings[ACC][REGION];
        const testAwsEnv = {
            account: testAccount,
            region: testRegion,
        };
        const testPipelineStack = new PipelineStack(
            app,
            `${targetEnvironment}${logicalIdPrefix}InfrastructurePipeline`,
            ACC,
            'test',
            testAwsEnv,
            deploymentAwsEnv
        );
        tag(testPipelineStack, DEPLOYMENT);
    }

    if (process.env.ENV === PROD) {
        console.log("PROD active")

        const targetEnvironment = PROD;
        const prodAccount = rawMappings[PROD][ACCOUNT_ID];
        const prodRegion = rawMappings[PROD][REGION];
        const prodAwsEnv = {
            account: prodAccount,
            region: prodRegion,
        };
        const prodPipelineStack = new PipelineStack(
            app,
            `${targetEnvironment}${logicalIdPrefix}InfrastructurePipeline`,
             PROD,
            'prod',
             prodAwsEnv,
             deploymentAwsEnv
        );
        tag(prodPipelineStack, DEPLOYMENT);
    }
}

app.synth();
