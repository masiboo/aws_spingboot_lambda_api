
import * as iam from 'aws-cdk-lib/aws-iam';

import * as base from '../base/base-stack';
import {AppContext} from '../../app-context';
import {StackConfig} from '../../app-config'
import * as pipeline from '../../construct/pattern/pipeline-deploy-pattern';


export class PipelineStack extends base.BaseStack {

    private simplePipeline: pipeline.CodePipelineEnhancedPattern;

    // abstract onPipelineName(): string;
    // abstract onActionFlow(): pipeline.ActionProps[];
    // abstract onPostConstructor(pipeline: pipeline.PipelineEnhancedPattern): void;
    // protected onBuildPolicies(): iam.PolicyStatement[]|undefined {
    //     return undefined
    // }

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);

        // const pipelineName = this.onPipelineName();
        // const actionFlow = this.onActionFlow();

        // this.simplePipeline = new pipeline.PipelineEnhancedPattern(this, 'AwsPipeline', {
        //     pipelineName,
        //     actionFlow,
        //     stackConfig,
        //     projectPrefix: this.projectPrefix,
        //     stackName: this.stackName,
        //     env: this.commonProps.env!,
        //     variables: this.commonProps.variables
        // });

        this.simplePipeline = new pipeline.CodePipelineEnhancedPattern(this, 'AwsPipeline', {
            account: this.account,
            pipelineName: "",
            region: this.region,
            // pipelineName,
            // actionFlow,
            stackConfig,
            projectPrefix: this.projectPrefix,
            stackName: this.stackName,
            env: this.commonProps.env!,
            variables: this.commonProps.variables,
            appContext
        });

        // this.onPostConstructor(this.simplePipeline);
    }
}
