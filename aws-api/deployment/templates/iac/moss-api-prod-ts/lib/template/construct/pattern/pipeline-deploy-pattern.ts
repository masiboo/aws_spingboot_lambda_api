import * as cdk from 'aws-cdk-lib';
import {SecretValue, StageSynthesisOptions} from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as codepipeline from 'aws-cdk-lib/aws-codepipeline';
import * as pipelines from "aws-cdk-lib/pipelines";
import {PipelineType} from 'aws-cdk-lib/aws-codepipeline';
import * as codepipeline_actions from 'aws-cdk-lib/aws-codepipeline-actions';
import * as codecommit from 'aws-cdk-lib/aws-codecommit';
import * as codebuild from 'aws-cdk-lib/aws-codebuild';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as targets from 'aws-cdk-lib/aws-events-targets';
import * as s3 from 'aws-cdk-lib/aws-s3';
import {Bucket} from 'aws-cdk-lib/aws-s3';

import {BaseConstruct, ConstructCommonProps} from '../base/base-construct';
import {AppConfig} from '../../app-config';
import {CloudAssembly} from "aws-cdk-lib/cx-api";
import {PipelineDeployStage} from "../../../../infra/pipeline-deploy-stage";
import {AppContext} from "../../app-context";

const fs = require('fs');

const CDK_VER = '@2';

export interface CodePipelinEnhancedPatternProps extends ConstructCommonProps {
    pipelineName: string;
    // actionFlow: CodeActionProps[];
    // buildPolicies?: iam.PolicyStatement[];
    role?:  iam.IRole;
    region: String,
    account: String,

    appContext: AppContext
}

export interface CodeEventStateLambdaProps {
    FunctionName?: string;
    CodePath: string;
    Runtime: string,
    Handler: string;
}

enum CodeActionKindPrefix {
    Source = 'Source',
    Approve = 'Approve',
    Build = 'Build',
    Deploy = 'Deploy'
}

export enum CodeActionKind {
    SourceCodeCommit = 'SourceCodeCommit',
    SourceS3Bucket = 'SourceS3Bucket',
    ApproveManual = 'ApproveManual',
    BuildCodeBuild = 'BuildCodeBuild',

    SourceGithub = 'SourceGithub',

    SourceCodeStar = 'SourceCodeStar',
    DeployS3Bucket = 'DeployS3Bucket',
}

export interface CodeActionProps {
    Name: string;
    Kind: CodeActionKind;
    Stage: string;
    Enable: boolean;
    Order?: number;
    EventStateLambda?: CodeEventStateLambdaProps;
    Detail:  CodeSourceKindS3BucketProps | CodeSourceKindCodeStarProps | CodeSourceKindGithubProps | CodeApproveKindManualProps | CodeBuildKindCodeBuildProps | CodeDeployKindS3BucketProps;
    RoleArn?: iam.IRole;
}

export interface CodeSourceKindCodeCommitProps {
    RepositoryName: string;
    BranchName: string;
}

export interface CodeSourceKindGithubProps {
    RepositoryName: string;
    BranchName: string;
    Owner: string;
    GithubOauthTokenId: string;

}

export interface CodeSourceKindCodeStarProps {
    RepositoryName: string;
    BranchName: string;
    Owner: string;
    CodeStartConnectionArn: string;

    VariableNameSpace?: string;

}

export interface CodeSourceKindS3BucketProps {
    BucketName: string;
    BucketKey: string;
    Account?: string;
    Region?: string;
}

export interface CodeApproveKindManualProps {
    Description?: string;
}

export interface CodeBuildDeployStacksProps {
    PreCommands?: string[];
    StackNameList: string[];
    PostCommands?: string[];
}

export interface CodeBuildKindCodeBuildProps {
    BuildSpecInline?: string;
    AppConfigFile: string;
    BuildCommands?: string[];
    BuildSpecFile?: string;
    BuildAssumeRoleArn?: string;
    BuildDeployStacks?: CodeBuildDeployStacksProps;
}

export interface CodeDeployKindS3BucketProps {
    BucketName: string;
    Account?: string;
    Region?: string;
}


export class CodePipelineEnhancedPattern extends BaseConstruct {
    public codePipeline: pipelines.CodePipeline;

    private sourceOutput: codepipeline.Artifact;
    private buildOutput: codepipeline.Artifact;
    private stageMap: Map<string, codepipeline.IStage> = new Map();

    constructor(scope: Construct, id: string, props: CodePipelinEnhancedPatternProps) {
        super(scope, id, props);

        const pipelineBaseName: string = props.pipelineName;
        // const actionFlow: CodeActionProps[] = props.actionFlow;
        // const configValid = this.validatePipelineConfig(pipelineBaseName, actionFlow);
        const configValid = true;

        const bucketName = `${this.commonProps.projectPrefix}-artifacts-${props.region}-${props.account}`
        const artifactBucket = Bucket.fromBucketName(this, `${pipelineBaseName}-artifact-bucket`, bucketName);

        if (configValid) {
            // this.codePipeline = new codepipeline.Pipeline(this, 'CICDPipeline', {
            //     pipelineName: `${this.projectPrefix}-${pipelineBaseName}`,
            //     enableKeyRotation: true,
            //     role: props.role, // cross-acount access
            //     artifactBucket: artifactBucket,
            //     pipelineType: PipelineType.V2,
            //     crossAccountKeys: true,
            // });
            // this.codePipeline = new pipelines.CodePipeline(this, 'Pipeline', {
            //     selfMutation: false,
            //     synth: new pipelines.ShellStep('Synth', {
            //         input: pipelines.CodePipelineSource.s3(artifactBucket, "", {
            //
            //         }),
            //         commands: ['npm ci', 'npm run build', 'npx cdk synth'],
            //     }),
            //
            // });

            // const targetAwsEnv: cdk.Environment = "";
            const targetEnvironment: string = "acc";
            const sourceArtifact = new codepipeline.Artifact();
            const cloudAssemblyArtifact = new codepipeline.Artifact();

            this.codePipeline = new pipelines.CodePipeline(
                this,
                `Aws-${targetEnvironment}AwsPipeline`,
                {
                    // synth: undefined,
                    pipelineName: `${targetEnvironment.toLowerCase()}-Aws-datalake-etl-pipeline`,
                    synth: new pipelines.ShellStep('Synth', {
                        input: pipelines.CodePipelineSource.s3(artifactBucket, "package.zip", {

                        }),
                        commands: ['npm ci', 'npm run build', `export ${targetEnvironment} && npx cdk synth`],
                    }),
                    crossAccountKeys: true,
                }
            );

            // const buildPolicies: iam.PolicyStatement[] | undefined = props.buildPolicies;
            // for (const actionProps of actionFlow) {
            //     const actionKind: CodeActionKind = this.findEnumType(CodeActionKind, actionProps.Kind);
            //
            //     if (actionProps.Enable) {
            //         const success = this.registerAction(actionKind, actionProps, buildPolicies, props.projectPrefix);
            //         if (!success) {
            //             break;
            //         }
            //     }
            // }

            const deployStage = new PipelineDeployStage(props.appContext, "stage-id", {
                    projectPrefix: props.projectPrefix,
                    stackConfig: props.stackConfig,
                    stackName: props.stackName,
                    env: props.env
                });

            this.codePipeline.addStage( deployStage) ;

        } else {
            console.info("No source repository, or ActionFlow Config is wrong.");
            throw Error('PipelineConfig is wrong.');
        }
    }

    private validatePipelineConfig(pipelineBaseName: string, actionFlow: CodeActionProps[]): boolean {
        let valid = false;

        if (pipelineBaseName && pipelineBaseName.trim().length > 2) {
            if (actionFlow && actionFlow.length >= 2) {
                let haveSource = false;
                let haveOther = false;

                for (const [index, actionProps] of actionFlow.entries()) {
                    if (index == 0) {
                        const kind = this.findEnumType(CodeActionKind, actionProps.Kind);
                        if (actionProps.Enable && kind.startsWith(CodeActionKindPrefix.Source)) {
                            haveSource = true;
                        }
                    } else {
                        if (actionProps.Enable) {
                            haveOther = true;
                            break;
                        }
                    }
                }

                if (haveSource && haveOther) {
                    valid = true;
                }
            }
        }

        return valid;
    }

    private registerAction(actionKind: CodeActionKind, actionProps: CodeActionProps, buildPolicies?: iam.PolicyStatement[], prefix?: string, actionRole?: iam.IRole): boolean {
        let success = true;

        if (actionKind.startsWith(CodeActionKindPrefix.Source)) {
            if (actionKind == CodeActionKind.SourceCodeCommit) {
                const props = actionProps.Detail as CodeSourceKindCodeCommitProps;
                const stage = this.addStage(actionProps.Stage);

                stage.addAction(this.createActionSourceCodeCommit(actionProps.Name, props, actionProps.Order));
            } else if (actionKind == CodeActionKind.SourceS3Bucket) {
                const props = actionProps.Detail as CodeSourceKindS3BucketProps;
                const stage = this.addStage(actionProps.Stage);

                stage.addAction(this.createActionSourceS3Bucket(actionProps.Name, props, actionProps.Order, actionRole));
            } else if  (actionKind == CodeActionKind.SourceGithub) {

                const props = actionProps.Detail as CodeSourceKindGithubProps;
                const stage = this.addStage(actionProps.Stage);
                stage.addAction(this.createActionSourceGithubAction(actionProps.Name, props, actionProps.Order))
            } else if  (actionKind == CodeActionKind.SourceCodeStar) {

            const props = actionProps.Detail as CodeSourceKindCodeStarProps;
            const stage = this.addStage(actionProps.Stage);
            stage.addAction(this.createActionSourceCodeStar(actionProps.Name, props, actionProps.Order))
        }

            else {
                console.error('[ERROR] not supported SourceKind', actionProps.Kind);
                success = false;
            }
        }
        else if (actionKind.startsWith(CodeActionKindPrefix.Approve)) {
            if (actionKind == CodeActionKind.ApproveManual) {
                const props = actionProps.Detail as CodeApproveKindManualProps;
                const stage = this.addStage(actionProps.Stage);

                stage.addAction(this.createActionApproveManual(actionProps.Name, props, actionProps.Order));
            } else {
                console.error('[ERROR] not supported ApproveKind', actionProps.Kind);
                success = false;
            }
        }
        else if (actionKind.startsWith(CodeActionKindPrefix.Build)) {
            if (actionKind == CodeActionKind.BuildCodeBuild) {
                const props = actionProps.Detail as CodeBuildKindCodeBuildProps;
                const stage = this.addStage(actionProps.Stage);
                const action = this.createActionBuildCodeBuild(actionProps, props, buildPolicies, prefix, actionRole);

                if (action) {
                    stage.addAction(action);
                    this.registerEventLambda(actionProps, action);
                } else {
                    console.error('[ERROR] fail to create build-action', actionProps.Name);
                    success = false;
                }
            } else {
                console.error('[ERROR] not supported BuildKind', actionProps.Kind);
                success = false;
            }
        }

        else if (actionKind.startsWith(CodeActionKindPrefix.Deploy)) {
            if (actionKind == CodeActionKind.DeployS3Bucket) {
                const props = actionProps.Detail as CodeDeployKindS3BucketProps;
                const stage = this.addStage(actionProps.Stage);
                const action = this.createActionDeployS3Bucket(actionProps.Name, props, actionProps.Order);

                if (action) {
                    stage.addAction(action);
                    this.registerEventLambda(actionProps, action);
                } else {
                    console.error('[ERROR] fail to create deploy-action', actionProps.Name);
                    success = false;
                }
            } else {
                console.error('[ERROR] not supported DeployKind', actionProps.Kind);
                success = false;
            }
        }

        return success;
    }

    private addStage(stageName: string): any {
        let stage: undefined = undefined;

        // if (this.stageMap.has(stageName)) {
        //     stage = this.stageMap.get(stageName);
        // } else {
        //     stage = this.codePipeline.addStage({
        //         _assemblyBuilder: undefined,
        //         createBuilder: undefined,
        //         node: undefined,
        //         policyValidationBeta1: [],
        //         get artifactId(): string {
        //             return "";
        //         },
        //         get assetOutdir(): string {
        //             return "";
        //         },
        //         get outdir(): string {
        //             return "";
        //         },
        //         synth(options?: StageSynthesisOptions): CloudAssembly {
        //             return undefined;
        //         },
        //         toString(): string {
        //             return "";
        //         },
        //         stageName: stageName });
        //     this.stageMap.set(stageName, stage);
        // }

        return stage!;
    }

    private createActionSourceCodeCommit(actionName: string, props: CodeSourceKindCodeCommitProps, runOrder?: number): codepipeline.IAction {
        const repo = codecommit.Repository.fromRepositoryName(
            this,
            'CodeCommit-Repository',
            props.RepositoryName,
        );

        this.sourceOutput = new codepipeline.Artifact('SourceOutput')
        const action = new codepipeline_actions.CodeCommitSourceAction({
            actionName: actionName,
            repository: repo,
            output: this.sourceOutput,
            branch: props.BranchName,
            codeBuildCloneOutput: true,
            runOrder: runOrder
        });

        return action;
    }

    private createActionSourceGithubAction(actionName: string, props: CodeSourceKindGithubProps, runOrder?: number): codepipeline.IAction {

        this.sourceOutput = new codepipeline.Artifact('SourceOutput')
        const sourceActionGithub = new codepipeline_actions.GitHubSourceAction({
            actionName: actionName,
            owner: props.Owner,
            repo: props.RepositoryName,
            oauthToken: SecretValue.secretsManager(props.GithubOauthTokenId),
            output: this.sourceOutput,
            branch: props.BranchName
        });

        return sourceActionGithub;
    }

    private createActionSourceCodeStar(actionName: string, props: CodeSourceKindCodeStarProps, runOrder?: number): codepipeline.IAction {

        this.sourceOutput = new codepipeline.Artifact('SourceOutput')
        const sourceActonCodeStar = new codepipeline_actions.CodeStarConnectionsSourceAction({
            actionName: actionName,
            owner: props.Owner,
            repo: props.RepositoryName,
            connectionArn: props.CodeStartConnectionArn,
            variablesNamespace: props.VariableNameSpace,
            output: this.sourceOutput,
            branch: props.BranchName
        });

        return sourceActonCodeStar;
    }

    private createActionSourceS3Bucket(actionName: string, props: CodeSourceKindS3BucketProps, runOrder?: number, actionRole?: iam.IRole): codepipeline.IAction {
        const bucket = s3.Bucket.fromBucketAttributes(this, `${actionName}SourceS3Bucket`, {
            bucketName: props.BucketName,
            account: props.Account,
            region: props.Region
        });

        this.sourceOutput = new codepipeline.Artifact('SourceOutput')
        const action = new codepipeline_actions.S3SourceAction({
            actionName: actionName,
            bucket,
            bucketKey: props.BucketKey,
            output: this.sourceOutput,
            runOrder: runOrder,
            role: actionRole,
        });

        return action;
    }

    private createActionApproveManual(actionName: string, props: CodeApproveKindManualProps, runOrder?: number, actionRole?: iam.IRole): codepipeline.IAction {
        return new codepipeline_actions.ManualApprovalAction({
            actionName: actionName,
            additionalInformation: props.Description,
            runOrder: runOrder,
        })
    }

    private createActionBuildCodeBuild(actionProps: CodeActionProps, buildProps: CodeBuildKindCodeBuildProps,
                                       buildPolicies?: iam.PolicyStatement[], prefix?: string, actionRole?: iam.IRole ): codepipeline.IAction | undefined {
        let appConfig: AppConfig = JSON.parse(fs.readFileSync(buildProps.AppConfigFile).toString());

        let buildSpec = undefined;

        const assumeRoleEnable = buildProps.BuildAssumeRoleArn ? true : false;
        if (buildProps.BuildCommands && buildProps.BuildCommands.length > 0) {

            buildSpec = this.createBuildSpecUsingCommands(buildProps.BuildCommands, assumeRoleEnable);
        } else if (buildProps.BuildDeployStacks && buildProps.BuildDeployStacks.StackNameList.length > 0) {

            buildSpec = this.createBuildSpecUsingStackName(buildProps.BuildDeployStacks, assumeRoleEnable, prefix);
        } else if (buildProps.BuildSpecFile && buildProps.BuildSpecFile.length > 3) {
            buildSpec = codebuild.BuildSpec.fromSourceFilename(buildProps.BuildSpecFile);

        } else if (buildProps.BuildSpecInline && buildProps.BuildSpecInline.length > 3) {
            // buildspec object
            buildSpec = this.createBuildSpecInline("", appConfig.Project.Account, buildProps.BuildSpecInline, assumeRoleEnable);
        } else {
            console.error('[ERROR] not supported CodeBuild - BuildSpecType');
        }

        let buildAction = undefined;
        if (buildSpec) {
            let project: codebuild.IProject = new codebuild.PipelineProject(this, `${actionProps.Stage}-${actionProps.Name}-Project`, {
                environment: {
                    buildImage: codebuild.LinuxBuildImage.STANDARD_5_0,
                    computeType: codebuild.ComputeType.MEDIUM,
                    privileged: true,
                },
                environmentVariables: {
                    ACCOUNT: { value: `${appConfig.Project.Account}` },
                    REGION: { value: `${appConfig.Project.Region}` },
                    PROJECT_NAME: { value: `${appConfig.Project.Name}` },
                    PROJECT_STAGE: { value: `${appConfig.Project.Stage}` },
                    PROJECT_PREFIX: { value: `${this.projectPrefix}` },
                    APP_CONFIG: { value: buildProps.AppConfigFile },
                    ASSUME_ROLE_ARN: { value: buildProps.BuildAssumeRoleArn ? buildProps.BuildAssumeRoleArn : '' },
                    ON_PIPELINE: { value: 'YES' },
                    BUILDNUMBER: { value: 'devbuild'}
                },
                buildSpec: buildSpec,
                timeout: cdk.Duration.minutes(60)
            });

            project.addToRolePolicy(this.getDeployCommonPolicy());
            if (buildPolicies) {
                buildPolicies.forEach(policy => project.addToRolePolicy(policy));
            } else {
                project.role?.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AdministratorAccess'));
            }

            // this.buildOutput = new codepipeline.Artifact(`${actionProps.Name}BuildOutput`);

            buildAction = new codepipeline_actions.CodeBuildAction({
                actionName: actionProps.Name,
                project,
                input: this.sourceOutput,
                // outputs: [this.buildOutput],
                runOrder: actionProps.Order,
                role: actionRole,
            });
        }

        return buildAction;
    }

    private createBuildSpecUsingCommands(buildCommands: string[], assumeRoleEnable: boolean): codebuild.BuildSpec {
        const buildSpec = codebuild.BuildSpec.fromObject(
            {
                version: "0.2",
                phases: {
                    install: {
                        // https://docs.aws.amazon.com/codebuild/latest/userguide/runtime-versions.html
                        'runtime-versions': {
                            nodejs: 18
                        },
                        commands: this.createInstallCommands(assumeRoleEnable, false)
                    },
                    pre_build: {
                        commands: [
                            'pwd',
                            'ls -l'
                        ]
                    },
                    build: {
                        commands: buildCommands
                    },
                    post_build: {
                        commands: [
                            'pwd',
                            'ls -l'
                        ]
                    }
                },
                artifacts: {
                    files: [
                        '**/*'
                    ],
                    'exclude-paths': [
                        'cdk.out/',
                        'node_modules/',
                        '.git/'
                    ]
                }
            }
        );
        return buildSpec;
    }

    // https://docs.aws.amazon.com/codebuild/latest/userguide/build-spec-ref.html
    private createBuildSpecUsingStackName(props: CodeBuildDeployStacksProps, assumeRoleEnable: boolean, prefix?: string): codebuild.BuildSpec {
        const cdkDeployStacksCommands = props.StackNameList.map(stackName => {
            const args = stackName.trim().split(' ');
            const pureStackName = args[0];
            args[0] = `cdk deploy ${prefix!}-${pureStackName} --require-approval never`;
            return args.join(' ');
        });
        
        const buildSpec = codebuild.BuildSpec.fromObject(
            {
                version: "0.2",
                phases: {
                    install: {
                        'runtime-versions': {
                            nodejs: 14
                        },
                        commands: this.createInstallCommands(assumeRoleEnable, true)
                    },
                    pre_build: {
                        commands: props.PreCommands
                    },
                    build: {
                        commands: cdkDeployStacksCommands
                    },
                    post_build: {
                        commands: props.PostCommands
                    }
                },
                artifacts: {
                    files: [
                        '**/*'
                    ],
                    'exclude-paths': [
                        'cdk.out/',
                        'node_modules/',
                        '.git/'
                    ]
                }
            });
        return buildSpec;
    }

    private createBuildSpecInline(props: any, deploymentAccount: string, buildSpecInline: any, assumeRoleEnable: boolean ): codebuild.BuildSpec {

        const account = deploymentAccount;

        const buildSpec = codebuild.BuildSpec.fromObject({
            version: '0.2',
            phases: {
                install: {
                    'runtime-versions': {
                        python: '3.11',
                    },
                    commands: [
                        'pip install --upgrade pip',
                        'pip install awscli',
                    ],
                },
                pre_build: {
                    commands: [
                        'echo Retrieving information from artifacts...',
                        'DATE=$(date \'+%y-%m-%d\')',
                        'ls -al $CODEBUILD_SRC_DIR',
                        'COMMIT="0"',
                        'PREFIX=$PROJECT_PREFIX',
                        `ARTIFACTS_ACCOUNT=${account}`,
                        'if [ -f "cicd-package.json" ]; then COMMIT=$(jq -r \'.build.number\' cicd-package.json | sed \'s/null/0/g\' | sed \'s/BUILD_NUMBER/0/g\'); fi',
                        'echo commit $COMMIT',
                        'SHORT_SOURCE_VERSION=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | head -c 8)',
                        'echo $SHORT_SOURCE_VERSION',
                        'BUILD_ID=$DATE.$COMMIT.$SHORT_SOURCE_VERSION',
                    ],
                },
                build: {
                    commands: [
                        'echo Building the parameters file...',
                        'touch templates/iac/master-parameters.json',
                        'ARTIFACTS_BUCKET=${PREFIX}-artifacts-${AWS_REGION}-${ARTIFACTS_ACCOUNT}',
                        'printf \'{"Parameters":{\'  templates/iac/master-parameters.json',
                        'printf \'"BuildId":"%s",\' $BUILD_ID  templates/iac/master-parameters.json',
                        'printf \'"BusinessUnitCodeName":"%s",\' "${BUDGET_UNIT_CODE}"  templates/iac/master-parameters.json',
                        'printf \'"AppId":"%s",\' "${APP_ID}"  templates/iac/master-parameters.json',
                        'printf \'"BusinessImpactLevelName":"%s",\' "${BUSINESS_IMPACT_LEVEL}"  templates/iac/master-parameters.json',
                        'printf \'"BusinessOwnerName":"%s",\' "${BUSINESS_OWNER}"  templates/iac/master-parameters.json',
                        'printf \'"BusinessUnitNameName":"%s",\' "${BUSINESS_UNIT_NAME}"  templates/iac/master-parameters.json',
                        'printf \'"DataClassificationName":"%s",\' "${DATA_CLASSIFICATION}"  templates/iac/master-parameters.json',
                        'printf \'"TechnicalOwnerName":"%s"\' "${TECHNICAL_OWNER}"  templates/iac/master-parameters.json',
                        'printf \'},\'  templates/iac/master-parameters.json',
                        'printf \'"Tags":{\'  templates/iac/master-parameters.json',
                        'printf \'"budget-unit-code":"%s",\' "${BUDGET_UNIT_CODE}"  templates/iac/master-parameters.json',
                        'printf \'"business-impact-level":"%s",\' "${BUSINESS_IMPACT_LEVEL}"  templates/iac/master-parameters.json',
                        'printf \'"business-owner":"%s",\' "${BUSINESS_OWNER}"  templates/iac/master-parameters.json',
                        'printf \'"business-unit-name":"%s",\' "${BUSINESS_UNIT_NAME}"  templates/iac/master-parameters.json',
                        'printf \'"data-classification":"%s",\' "${DATA_CLASSIFICATION}"  templates/iac/master-parameters.json',
                        'printf \'"service":"%s",\' "${PREFIX}"  templates/iac/master-parameters.json',
                        'printf \'"technical-owner":"%s"}}\' "${TECHNICAL_OWNER}"  templates/iac/master-parameters.json',
                        'cat templates/iac/master-parameters.json',
                    ],
                },
                post_build: {
                    commands: [
                        'echo Copying application, database and cloudFormation templates to S3',
                        'zip templates/iac/master.zip templates/iac/master.yml templates/iac/master-parameters.json',
                        'aws s3 cp --recursive artifacts s3://$ARTIFACTS_BUCKET/builds/$BUILD_ID/artifacts/',
                        'aws s3 cp --recursive templates s3://$ARTIFACTS_BUCKET/builds/$BUILD_ID/templates/',
                    ],
                },
            },
            artifacts: {
                'secondary-artifacts': {
                    BuildArtifact: {
                        files: [
                            'templates/iac/master.yml',
                            'templates/iac/master-parameters.json',
                            'templates/iac/master*.yml',
                        ],
                    },
                },
            },
        });

        return buildSpec;
    }
    private createActionDeployS3Bucket(actionName: string, props: CodeDeployKindS3BucketProps, runOrder?: number): codepipeline.IAction {
        const bucket = s3.Bucket.fromBucketAttributes(this, `${actionName}DeployS3Bucket`, {
            bucketName: props.BucketName,
            account: props.Account,
            region: props.Region
        });

        const action = new codepipeline_actions.S3DeployAction({
            actionName: actionName,
            input: this.buildOutput,
            bucket
        });

        return action;
    }

    private createInstallCommands(assumeRoleEnable: boolean, setupEnable: boolean): string[] {
        let commands: string[] = [];

        const assumeRoleCommands = [
            'creds=$(mktemp -d)/creds.json',
            'aws sts assume-role --role-arn $ASSUME_ROLE_ARN --role-session-name assume_role > $creds',
            `export AWS_ACCESS_KEY_ID=$(cat $creds | grep "AccessKeyId" | cut -d '"' -f 4)`,
            `export AWS_SECRET_ACCESS_KEY=$(cat $creds | grep "SecretAccessKey" | cut -d '"' -f 4)`,
            `export AWS_SESSION_TOKEN=$(cat $creds | grep "SessionToken" | cut -d '"' -f 4)`,
        ]

        const setupInstallCommands = [
            `npm install -g aws-cdk${CDK_VER}`,
        ]

        if (assumeRoleEnable) {
            commands = commands.concat(assumeRoleCommands);
        }
        if (setupEnable) {
            commands = commands.concat(setupInstallCommands);
        }

        return commands;
    }

    private getDeployCommonPolicy(): iam.PolicyStatement {
        const statement = new iam.PolicyStatement();
        statement.addActions(
            "cloudformation:*",
            "lambda:*",
            "s3:*",
            "ssm:*",
            "iam:PassRole",
            "kms:*",
            "events:*",
            "sts:AssumeRole"
        );
        statement.addResources("*");
        return statement;
    }

    private registerEventLambda(actionProps: CodeActionProps, action: codepipeline.IAction) {
        if (actionProps.EventStateLambda
            && actionProps.EventStateLambda.CodePath && actionProps.EventStateLambda.CodePath.length > 0
            && actionProps.EventStateLambda.Handler && actionProps.EventStateLambda.Handler.length > 0) {

            action?.onStateChange(
                `${actionProps.Stage}-${actionProps.Name}-EventState`,
                new targets.LambdaFunction(this.createEventStateLambda(`${actionProps.Stage}-${actionProps.Name}-EventStateLambda`,
                    actionProps.EventStateLambda)));
        }
    }

    private createEventStateLambda(baseName: string, props: CodeEventStateLambdaProps): lambda.Function {
        const func = new lambda.Function(this, baseName, {
            functionName: `${this.projectPrefix}-${baseName}`,
            runtime: new lambda.Runtime(props.Runtime),
            code: lambda.Code.fromAsset(props.CodePath),
            handler: props.Handler
        })

        return func;
    }
}
