import * as base from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';

import * as pipeline from '../../lib/template/construct/pattern/pipeline-enhanced-pattern';


export class AwsInfrastructrePipelineStack extends base.BaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        const account = this.account // (Ref: AWS::AccountId)

        // bucket read policy

        // kms key policy

        // ECR read policy

        // buildD roles

        // pipeline role

        new pipeline.PipelineEnhancedPattern(this, 'AwsInfrastructurePipeline', {
            projectPrefix: this.projectPrefix,
            stackConfig: this.stackConfig,
            stackName: this.stackName,
            env: this.commonProps.env!,
            pipelineName: 'AwsInfrastructurePipeline',
            actionFlow: [
                {
                    Name: 'InfraS3SourceClone',
                    Stage: 'InfraSourceStage',
                    Kind: pipeline.ActionKind.SourceS3Bucket,
                    Enable: true,
                    Detail: {
                        BucketName: `${this.commonProps.projectPrefix}-artifacts-${this.region}-${this.commonProps.appConfig.Project.Account}`,
                        BucketKey: "package.zip"
                    }
                },
                {
                    Name: 'DeployVPCInfraStacks',
                    Stage: "VPCDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: "config/app-config-infra-pipeline-dev.json",
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCfnInfraVpcStack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployRDSStacks',
                    Stage: "RDSDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: "config/app-config-infra-pipeline-dev.json",
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsInfraRdsStack",
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployRegistryStacks',
                    Stage: "RegsitryDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: "config/app-config-infra-pipeline-dev.json",
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsInfraRegistryStack",

                            ]
                        }
                    }
                },
                {
                    Name: 'DeployFrontEndStacks',
                    Stage: "FrontEndDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: "config/app-config-infra-pipeline-dev.json",
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCfnWafCdnStack",
                                "AwsFrontEndStack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployMicroServiceStacks',
                    Stage: "MicroServiceDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Detail: {
                        AppConfigFile: "config/app-config-infra-pipeline-dev.json",
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCoreDBAccessStack",
                                "AwsCoreMediaProcessStack",
                                "AwsCoreMirisProxyStack",
                                "AwsCoreEmailSvctack",

                            ]
                        }
                    }
                }
            ]
        });


    }
}
