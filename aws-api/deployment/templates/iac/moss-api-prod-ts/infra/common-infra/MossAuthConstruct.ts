import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {UserPool} from 'aws-cdk-lib/aws-cognito'
import {HttpApi} from "aws-cdk-lib/aws-apigatewayv2";
import * as cognito from 'aws-cdk-lib/aws-cognito';
import {CfnOutput, Duration, RemovalPolicy} from "aws-cdk-lib";

export interface AwsAuthProps extends ConstructCommonProps {

}

export class AwsAuthConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsAuthProps) {
        super(scope, id, props);

        // #fixme dummy test
        this.putParameter("AwsCoreEmailSvctackAlbDnsName", "dunmmyEmailServiceUrl")

        const httpId = this.getParameter("apiGateWayHttpId");
        const httpApi = HttpApi.fromHttpApiAttributes(this, "importedHttpAPI", {
            httpApiId: httpId
        })

        // cognito-auth-start
        // Cognito User Pool with Email Sign-in Type.
        const userPool = new UserPool(this, 'AwsUserPool', {
            // userPoolName: this.getName(props, 'user-pool'), #todo move to own stack and construct
            signInAliases: {
                email: true
            },

            // selfSignUpEnabled: true,
            // autoVerify: {
            //     email: true,
            // },
            // userVerification: {
            //     emailSubject: 'You need to verify your email',
            //     emailBody: 'Thanks for signing up Your verification code is {####}', // # This placeholder is a must if code is selected as preferred verification method
            //     emailStyle: cognito.VerificationEmailStyle.CODE,
            // },
            // standardAttributes: {
            //     familyName: {
            //         mutable: false,
            //         required: true,
            //     },
            //     address: {
            //         mutable: true,
            //         required: false,
            //     },
            // },
            //
            // customAttributes: {
            //     'tenantId': new cognito.StringAttribute({
            //         mutable: false,
            //         minLen: 10,
            //         maxLen: 15,
            //     }),
            //     'createdAt': new cognito.DateTimeAttribute(),
            //     'employeeId': new cognito.NumberAttribute({
            //         mutable: false,
            //         min: 1,
            //         max: 100,
            //     }),
            //     'isAdmin': new cognito.BooleanAttribute({
            //         mutable: false,
            //     }),
            // },
            // passwordPolicy: {
            //     minLength: 8,
            //     requireLowercase: true,
            //     requireUppercase: true,
            //     requireDigits: true,
            //     requireSymbols: false,
            // },
            // accountRecovery: cognito.AccountRecovery.EMAIL_ONLY,
            // removalPolicy: RemovalPolicy.DESTROY,
        })

        const env = 'dev';
        const callBack = "https://admin.Aws.madrid.dev.web1.wipo.int"

        const callbackUrls = new Array();
        callbackUrls.push('https://' + "admin.Aws.madrid" + env + '.web1.wipo.int/login');
        if (env=='dev') {
            callbackUrls.push('http://localhost:4000');
        }

        const appClient = userPool.addClient('awesome-app-client', {
            userPoolClientName: 'Aws-app-angular-client',
            authFlows: {
                userPassword: true,
                userSrp: true,
            },
            oAuth: {
                flows: {
                    authorizationCodeGrant: true
                },
                callbackUrls: callbackUrls,
            },
            // scopes: [ cognito.OAuthScope.OPENID ],
            // callbackUrls: [ 'https://my-app-domain.com/welcome' ],
            // logoutUrls: [ 'https://my-app-domain.com/signin' ],
            idTokenValidity: Duration.hours(8),
            accessTokenValidity: Duration.hours(8),
            generateSecret: false
        });


        this.putParameter('userPoolARN', userPool.userPoolArn)
        this.putParameter('userPoolID', userPool.userPoolId)
        this.putParameter('auth/appClientId', appClient.userPoolClientId)
        this.putParameter('auth/appClientName', appClient.userPoolClientName)

        // cognito-auth-end

        new CfnOutput(this, 'App ClientId', { value: appClient.userPoolClientId});
        new CfnOutput(this, 'UserPoolID', { value: userPool.userPoolId});


    }

    private getName(props: AwsAuthProps | undefined, name: string) {
        // console.log("suffix: " + process.env.BUILDNUMBER)
        const suffix = process.env.BUILDNUMBER ? process.env.BUILDNUMBER : 'devBuild'
        return `${props!.projectPrefix}-${name}-${suffix}`.toLowerCase();
    }


}
