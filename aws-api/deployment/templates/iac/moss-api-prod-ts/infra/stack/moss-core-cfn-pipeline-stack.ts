import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';
import * as core from 'aws-cdk-lib';

import * as base from '../../lib/template/stack/cfn/cfn-include-stack';
import { Override } from '../../lib/template/stack/base/base-stack';
import { AppContext } from '../../lib/template/app-context';
import { StackConfig } from '../../lib/template/app-config'
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as sd from "aws-cdk-lib/aws-servicediscovery";
import * as pipeline from "../../lib/template/construct/pattern/pipeline-enhanced-pattern";
import * as iam from "aws-cdk-lib/aws-iam";
import {CfnWebACL} from "aws-cdk-lib/aws-waf";
import {CfnRole} from "aws-cdk-lib/aws-iam";

enum BuildKind {
    DEV,
    ACC,
    PRD
}

export class AwsCoreCfnPipelineStack extends base.CfnIncludeStack {

    private role:  iam.IRole;
    private buildKind: BuildKind;
    private buildSpecPath: String;

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);
    }

    @Override
    onLoadTemplateProps(): base.CfnTemplateProps | undefined {

        let buildId = process.env.BUILD_NUMBER

        const allowedEnv = ["dev", "acc", "prod"];
        let stage = this.commonProps.appConfig.Project.Stage;
        const result = allowedEnv.findIndex(item => stage.toUpperCase() === item.toUpperCase());

        if (result !== -1) {
        } else {
            stage = "dev"
        }



        const computedParameters = [
            {
                Key: "NamePrefix",
                Value: this.commonProps.projectPrefix
            },
        ]
        const configParameters : [{ Key: string, Value: string}]  = this.stackConfig.Parameters

        const findParam = configParameters.find(elem => {
            if (elem.Key === "EnableDeploymentToAcc"){
               this.buildKind = elem.Value ? BuildKind.ACC : BuildKind.DEV
            }

            if (elem.Key === "EnableDeploymentToPrd"){
                this.buildKind = elem.Value ? BuildKind.PRD : BuildKind.DEV
            }
        })

        const parameters = configParameters.concat(computedParameters)

        return {
            templatePath: this.stackConfig.TemplatePath,
            parameters: parameters,

        };
    }

    @Override
    onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude) {

        const cfnPipelineRole = cfnTemplate?.getResource('PipelineRole') as CfnRole;
        const pipeRole = iam.Role.fromRoleArn(this, "pipelinerole", cfnPipelineRole.attrArn);

        const account = this.account // (Ref: AWS::AccountId)

        const developmentAccount = cfnTemplate?.getParameter('DevelopmentAccount');
        const acceptanceAccount = cfnTemplate?.getParameter('AcceptanceAccount');
        const productiontAccount = cfnTemplate?.getParameter('ProductionAccount');
        const cicdAccount = cfnTemplate?.getParameter('CICDDeploymentAccount');

        let devActionRole =  iam.Role.fromRoleArn(this, "actionRoleId", `arn:aws:iam::${developmentAccount!.value.toString()}:role/${this.commonProps.projectPrefix}-cicd-role`)

        if (this.buildKind === BuildKind.ACC) {
            devActionRole = iam.Role.fromRoleArn(this, "actionRoleId", `arn:aws:iam::${acceptanceAccount!.value.toString()}:role/${this.commonProps.projectPrefix}-cicd-role`)
        }

        if (this.buildKind === BuildKind.ACC) {
            devActionRole = iam.Role.fromRoleArn(this, "actionRoleId", `arn:aws:iam::${productiontAccount!.value.toString()}:role/${this.commonProps.projectPrefix}-cicd-role`)
        }

        this.createNewPipeline(pipeRole, devActionRole);

    }

    private  createNewPipeline(pipelineRole: iam.IRole, roleArn: iam.IRole) {

        let stage = this.commonProps.appConfig.Project.Stage;

        if (stage === null ) {
            console.log(" String des not exist in the array");
            stage = "dev"
        }

        let deployment = stage == "dev" ? "dev" : "cicd";


            //         EnvironmentVariables:
        //           - Name: PREFIX
        //             Value: !Ref NamePrefix
        //           - Name: ARTIFACTS_ACCOUNT
        //             Value: !Ref AWS::AccountId
        //           - Name: REPOSITORY_NAME
        //             Value: !Ref DocumentAccessRepository
        //           - Name: BUDGET_UNIT_CODE
        //             Value: !Ref BudgetUnitCode
        //           - Name: BUSINESS_IMPACT_LEVEL
        //             Value: !Ref BusinessImpactLevel
        //           - Name: BUSINESS_OWNER
        //             Value: !Ref BusinessOwner
        //           - Name: BUSINESS_UNIT_NAME
        //             Value: !Ref BusinessUnitName
        //           - Name: DATA_CLASSIFICATION
        //             Value: !Ref DataClassification
        //           - Name: TECHNICAL_OWNER
        //             Value: !Ref TechnicalOwner

        new pipeline.PipelineEnhancedPattern(this, 'AwsInfrastructurePipeline', {
            projectPrefix: this.projectPrefix,
            stackConfig: this.stackConfig,
            stackName: this.stackName,
            env: this.commonProps.env!,
            pipelineName: 'AwsInfrastructurePipeline',
            region: this.region,
            account: this.commonProps.appConfig.Project.Account,
            // buildPolicies: [
            //     {}
            // ],
            role: pipelineRole,
            actionFlow: [
                {
                    Name: 'InfraS3SourceClone',
                    Stage: 'InfraSourceStage',
                    Kind: pipeline.ActionKind.SourceS3Bucket,
                    Enable: true,
                    RoleArn: roleArn,
                    Detail: {
                        BucketName: `${this.commonProps.projectPrefix}-artifacts-${this.region}-${this.commonProps.appConfig.Project.Account}`,
                        BucketKey: "package.zip",

                    }
                },
                {
                    Name: 'InfraS3Copy',
                    Stage: 'InfraSourceCopyStage',
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${deployment}.json`,
                        // BuildSpecFile: this.stackConfig.BuildSpecPath,
                        BuildSpecInline: this.stackConfig.BuildSpecInline,
                        // BuildCommands?: string[],
                        // BuildAssumeRoleArn?: string;
                        // BuildDeployStacks: {
                        //     PreCommands?: string[];
                        //     StackNameList: string[];
                        //     PostCommands?: string[];
                        // }
                    }
                },
                // unzip bucket and place in S3


                {
                    Name: 'DeployBucketsECRStacks',
                    Stage: "EECRBucketseployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsInfraCfnBucketsECRStack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployVPCInfraStacks',
                    Stage: "VPCDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
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
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
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
                    Name: 'DeployECSCluster',
                    Stage: "RDSDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Order: 2,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsInfraECSClusterStack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployRegistryStacks',
                    Stage: "RegsitryDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
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
                    Name: 'DeployFrontEndWAF',
                    Stage: "WAFCertsStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 1,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsWafCloudFrontStack",
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployFrontEndCert',
                    Stage: "WAFCertsStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 2,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsUsEastCertificateStack",
                            ]
                        }
                    }
                },


                {
                    Name: 'DeployMicroServiceDBAccessStack',
                    Stage: "MicroServiceDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCoreDBAccessStack",

                            ]
                        }
                    }
                },
                {
                    Name: 'DeployMicroServiceMediaStack',
                    Stage: "MicroServiceDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCoreMediaProcessStack",
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployMicroServiceMirisStack',
                    Stage: "MicroServiceDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: false,
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCoreMirisProxyStack",
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployMicroServiceEmailStack',
                    Stage: "MicroServiceDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: false,
                    RoleArn: roleArn,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsCoreEmailSvctack",
                            ]
                        }
                    }
                },

                {
                    Name: 'DeployAuthStacks',
                    Stage: "AuthStackDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsAuthStack"
                            ]
                        }
                    }
                },

                {
                    Name: 'DeployAPIV1Stacks',
                    Stage: "APIStackDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 1,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsApiV1Stack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployAPIv2Stacks',
                    Stage: "APIStackDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 2,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsApiV2Stack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployEventStacks',
                    Stage: "APIStackDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 3,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsEventsStack"
                            ]
                        }
                    }
                },

                {
                    Name: 'DeployFrontEndStacks',
                    Stage: "FrontEndDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 1,
                    RoleArn: roleArn,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install"
                            ],
                            StackNameList: [
                                "AwsFrontEndStack"
                            ]
                        }
                    }
                },
                {
                    Name: 'DeployAPiDocStacks',
                    Stage: "FrontEndDeployStage",
                    Kind: pipeline.ActionKind.BuildCodeBuild,
                    Enable: true,
                    Order: 2,
                    Detail: {
                        AppConfigFile: `config/app-config-infra-pipeline-${stage}.json`,
                        BuildDeployStacks: {
                            PreCommands:[
                                "ls -al", "aws sts get-caller-identity", "cd templates/iac/Aws-api-prod-ts",  "npm install", "cdk list"
                            ],
                            StackNameList: [
                                "AwsApiDocsStack"
                            ]
                        }
                    }
                },



            ]
        });

    }

}
