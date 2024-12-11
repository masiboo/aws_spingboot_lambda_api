import * as base from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';

import * as pipeline from '../../lib/template/construct/pattern/pipeline-enhanced-pattern';


export class AwsApiInfrastructrePipelineStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        let stage = this.commonProps.appConfig.Project.Stage;

        if (stage === null ) {
            console.log(" String des not exist in the array");
            stage = "dev"
        }


        new pipeline.PipelineEnhancedPattern(this, 'AwsApiInfrastructurePipeline', {
            projectPrefix: this.projectPrefix,
            stackConfig: this.stackConfig,
            stackName: this.stackName,
            env: this.commonProps.env!,
            pipelineName: 'AwsAPIInfrastructurePipeline',
            actionFlow: [
                {
                    Name: 'APIInfraS3SourceClone',
                    Stage: 'APIInfraSourceStage',
                    Kind: pipeline.ActionKind.SourceS3Bucket,
                    Enable: true,
                    Detail: {
                        BucketName: `${this.commonProps.projectPrefix}-artifacts-${this.region}-${this.commonProps.appConfig.Project.Account}`,
                        BucketKey: "package.zip"
                    }
                },
                {
                    Name: 'DeployAPIStacks',
                    Stage: "APIStackDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsApiStack"
                            ]
                        }
                    }
                },

                ]
        });
    }
}
