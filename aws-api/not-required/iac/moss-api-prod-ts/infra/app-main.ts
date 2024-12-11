#!/usr/bin/env node
import {AppContext, AppContextError} from '../lib/template/app-context';
import {AwsApiPipelineStack} from "./stack/Aws-api-pipeline-stack";
import {AwsApiStack} from "./stack/Aws-api-stack";

try {
    const appContext = new AppContext({
        appConfigFileKey: 'APP_CONFIG',
    });

    if (appContext.appConfig.Stack.AwsApiPipeline) {
        new AwsApiPipelineStack(appContext, appContext.appConfig.Stack.AwsApiPipeline);
    }

    new AwsApiStack(appContext, appContext.appConfig.Stack.AwsApi)

} catch (error) {
    if (error instanceof AppContextError) {
        console.error('[AppContextError]:', error.message);
    } else {
        console.error('[Error]: not-handled-error', error);
    }
}
