import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";
import {AwsFrontEndProps} from "./AwsFrontEndConstruct";
import {DomainName, HttpApi} from "aws-cdk-lib/aws-apigatewayv2";
import {aws_route53 as route53, CfnOutput} from "aws-cdk-lib";
import * as acm from "aws-cdk-lib/aws-certificatemanager";
import * as aws_apigatewayv2 from "aws-cdk-lib/aws-apigatewayv2";
import * as route53targets from "aws-cdk-lib/aws-route53-targets";
import {ICertificate} from "aws-cdk-lib/aws-certificatemanager";

interface AwsApiCertProps extends ConstructCommonProps {
    // stage: string,
    certificateArnVariableKey: string

    domainName: string;
    siteSubDomain: string;
}
export class AwsApiCertConstruct extends BaseConstruct {
    private certificate: ICertificate;

    constructor(scope: Construct, id: string, props: AwsApiCertProps) {
        super(scope, id, props);


        const httpId = this.getParameter("apiGateWayHttpId");
        const httpApi = HttpApi.fromHttpApiAttributes(this, "importedHttpAPI", {
            httpApiId: httpId
        })


        // const stage = httpApi.defaultStage?.node.defaultChild as CfnStage
        // stage.accessLogSettings = {
        //     destinationArn: AwsApiGatewaylogGroup.logGroupArn,
        //     format: JSON.stringify({
        //         requestId: '$context.requestId',
        //         userAgent: '$context.identity.userAgent',
        //         sourceIp: '$context.identity.sourceIp',
        //         requestTime: '$context.requestTime',
        //         requestTimeEpoch: '$context.requestTimeEpoch',
        //         httpMethod: '$context.httpMethod',
        //         path: '$context.path',
        //         status: '$context.status',
        //         protocol: '$context.protocol',
        //         responseLength: '$context.responseLength',
        //         domainName: '$context.domainName',
        //         error: '$context.authorizer.error',
        //         claimsProperty: '$context.authorizer.claims.property',
        //         principalId: '$context.authorizer.principalId',
        //         authProperty: '$context.authorizer.property',
        //         cognitoAuthenticationProvider: '$context.identity.cognitoAuthenticationProvider',
        //         cognitoAuthenticationType: '$context.identity.cognitoAuthenticationType',
        //         cognitoIdentityId: '$context.identity.cognitoIdentityId',
        //         cognitoIdentityPoolId: '$context.identity.cognitoIdentityPoolId',
        //         principalOrgId: '$context.identity.principalOrgId',
        //         user: '$context.identity.user'
        //     })
        // }

        const hostedZone = route53.HostedZone.fromLookup(this, `${props.siteSubDomain}-Zone`, { domainName: props.domainName });
        const siteDomain = props.siteSubDomain + '.' + props.domainName;
        new CfnOutput(this, `${props.siteSubDomain} Site`, { value: 'https://' + siteDomain });

        // const certificateArn = this.getParameter(props.certificateArnVariableKey); // "arn:aws:acm:us-east-1:551493771163:certificate/275af1b2-c067-4d3a-a7b0-0258ff7f13ad"
        let certificateArn
        // certificateArn = this.getVariable(props.certificateArnVariableKey);
        certificateArn =  "arn:aws:acm:us-east-1:551493771163:certificate/35393c0c-d8ae-4b10-9796-9e1fc6e82009"

        if (certificateArn != null) {
            this.certificate = acm.Certificate.fromCertificateArn(this, `${props.siteSubDomain}-importedCert`, certificateArn );
            new CfnOutput(this, `${props.siteSubDomain}-Imported Certificate`, { value: this.certificate.certificateArn });
        }

        // Create a new domain name for the API and associate the certificate
        const domain = new aws_apigatewayv2.DomainName(this, 'Domain', {
            certificate: this.certificate,
            domainName: siteDomain, // Replace with the subdomain for your APi
        });

        const stage = new aws_apigatewayv2.HttpStage(this, "AwsHttpStage", {
            httpApi: httpApi
        })

        // Create a custom domain mapping for the HTTP API
        const apiMapping = new aws_apigatewayv2.ApiMapping(this, 'MyApiMapping', {
            api: httpApi,
            domainName: domain,
            stage
        });

        // Create a new A record in Route 53 for the API domain
        new route53.ARecord(this, 'AwsAPICertARecord', {
            recordName: siteDomain, // Replace with the subdomain for your API
            target: route53.RecordTarget.fromAlias(new route53targets.ApiGatewayv2DomainProperties(domain.regionalDomainName, domain.regionalHostedZoneId)),
            zone: hostedZone,
        });



    }

}