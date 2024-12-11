import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as codecommit from 'aws-cdk-lib/aws-codecommit';
import * as codebuild from 'aws-cdk-lib/aws-codebuild';
import * as codepipeline from 'aws-cdk-lib/aws-codepipeline';
import * as actions from 'aws-cdk-lib/aws-codepipeline-actions';
import * as pipelines from 'aws-cdk-lib/pipelines';

import * as base from '../../../lib/template/construct/base/base-construct'
import {SecretValue} from "aws-cdk-lib";
export interface EcsCicdProps extends base.ConstructCommonProps {
    service: ecs.IBaseService;
    containerName: string;
    ecrRepo: ecr.IRepository;
    appPath?: string;
    dockerfileName?: string;
    buildCommands?: string[];
    enableKeyRotation?: boolean
}

export class EcsCicdConstruct extends base.BaseConstruct {

    constructor(scope: Construct, id: string, props: EcsCicdProps) {
        super(scope, id, props);

        const sourceOutput = new codepipeline.Artifact();

        const ecrSourceInput = new actions.EcrSourceAction({
            actionName: 'ECR_Source_Action',
            repository: props.ecrRepo,
            imageTag: 'latest', // optional, default: 'latest'
            output: sourceOutput,
        });

        const buildOutput = new codepipeline.Artifact();
        const buildAction = new actions.CodeBuildAction({
            actionName: 'CodeBuild_ImageDefinitionBuild',
            project: this.createImageDefinitions(props.ecrRepo, props),
            input: sourceOutput,
            outputs: [buildOutput],
        });

        const deployAction = new actions.EcsDeployAction({
            actionName: 'ECS_ContainerDeploy',
            input: buildOutput,
            service: props.service,
            // moved to build action now creates the image file
            // imageFile: new codepipeline.ArtifactPath(buildOutput, ( props.appPath ? `${props.appPath}/imagedefinitions.json` : 'imagedefinitions.json')),
            deploymentTimeout: cdk.Duration.minutes(60)
        });

        new codepipeline.Pipeline(this, 'ECSServicePipeline', {
            pipelineName: `${props.stackName}-Pipeline`,
            enableKeyRotation: props.enableKeyRotation ? props.enableKeyRotation : true,
            stages: [
                {
                    stageName: 'Source',
                    actions: [ecrSourceInput],
                },
                {
                    stageName: 'Build',
                    actions: [buildAction],
                },
                {
                    stageName: 'Deploy',
                    actions: [deployAction],
                }
            ]
        });
    }

    private createBuildProject(ecrRepo: ecr.IRepository, props: EcsCicdProps): codebuild.Project {
        const buildCommandsBefore = [
            'echo "In Build Phase"',
            'cd $APP_PATH',
            'ls -l',
        ];
        const buildCommandsAfter = [
            '$(aws ecr get-login --no-include-email)',
            `docker build -f ${props.dockerfileName ? props.dockerfileName : 'Dockerfile'} -t $ECR_REPO_URI:$TAG .`,
            'docker push $ECR_REPO_URI:$TAG'
        ];

        const appPath = props.appPath ? `${props.appPath}` : '.';

        const project = new codebuild.Project(this, 'DockerBuild', {
            projectName: `${props.stackName}DockerBuild`,
            environment: {
                buildImage: codebuild.LinuxBuildImage.AMAZON_LINUX_2_3,
                computeType: codebuild.ComputeType.SMALL,
                privileged: true
            },
            environmentVariables: {
                'ECR_REPO_URI': {
                    value: `${ecrRepo.repositoryUri}`
                },
                'CONTAINER_NAME': {
                    value: `${props.containerName}`
                },
                'APP_PATH': {
                    value: appPath
                }
            },
            buildSpec: codebuild.BuildSpec.fromObject({
                version: "0.2",
                phases: {
                    pre_build: {
                        commands: [
                            'echo "In Pre-Build Phase"',
                            'export TAG=latest',
                            'echo $TAG'
                        ]
                    },
                    build: {
                        commands: [
                            ...buildCommandsBefore,
                            ...(props.buildCommands ? props.buildCommands : []),
                            ...buildCommandsAfter
                        ]
                    },
                    post_build: {
                        commands: [
                            'echo "In Post-Build Phase"',
                            'pwd',
                            "printf '[{\"name\":\"%s\",\"imageUri\":\"%s\"}]' $CONTAINER_NAME $ECR_REPO_URI:$TAG > imagedefinitions.json",
                            "pwd; ls -al; cat imagedefinitions.json"
                        ]
                    }
                },
                artifacts: {
                    files: [
                        `${appPath}/imagedefinitions.json`
                    ]
                }
            }),
        });

        ecrRepo.grantPullPush(project.role!);
        this.appendEcrReadPolicy('build-policy', project.role!);

        return project;
    }

    private createImageDefinitions(ecrRepo: ecr.IRepository, props: EcsCicdProps) {
       const project = new codebuild.Project(this, 'GenerateImageDefinitions', {
           projectName: `${props.stackName}DockerBuild`,
           environment: {
               buildImage: codebuild.LinuxBuildImage.AMAZON_LINUX_2_3,
               computeType: codebuild.ComputeType.SMALL,
               privileged: true
           },
           environmentVariables: {
               'ECR_REPO_URI': {
                   value: `${ecrRepo.repositoryUri}`
               },
               'CONTAINER_NAME': {
                   value: `${props.containerName}`
               },
           },
           //  //https://stackoverflow.com/a/57015190
            buildSpec: codebuild.BuildSpec.fromObject({
                version: '0.2',
                phases: {
                    build: {
                        commands: [
                            'printenv',
                            `TAG=$(cat imageDetail.json | python -c "import sys, json; print(json.load(sys.stdin)['ImageTags'][0])")`,
                            'echo "${ECR_REPO_URI}:${TAG}"',
                            "printf '[{\"name\":\"%s\",\"imageUri\":\"%s\"}]' $CONTAINER_NAME $ECR_REPO_URI:$TAG > imagedefinitions.json",
                            'pwd; ls -al; cat imagedefinitions.json',
                        ],
                    },
                },
                artifacts: { files: ['imagedefinitions.json'] },
            })
    })

        ecrRepo.grantPullPush(project.role!);
        this.appendEcrReadPolicy('build-policy', project.role!);

        return project;
    }

    private appendEcrReadPolicy(baseName: string, role: iam.IRole) {
        const statement = new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            resources: ['*'],
            actions: [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage"
            ]
        });

        const policy = new iam.Policy(this, baseName);
        policy.addStatements(statement);

        role.attachInlinePolicy(policy);
    }
}