import * as base from '../../lib/template/stack/base/us-east-base-stack';
import {AppContext} from '../../lib/template/app-context';
import {aws_route53 as route53, CfnOutput} from "aws-cdk-lib";
import * as acm from "aws-cdk-lib/aws-certificatemanager";

export class AwsUsEastCertificateStack extends base.CrossRegionBaseStack {

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        // todo: make these unto configurable constructs
        const baseUrlDomainName = `Aws.madrid.${appContext.appConfig.Project.Stage}.web1.wipo.int`
        const siteSubDomain = "admin"
        const docsSubDomian = "docs"
        const operationSubDomain = "operations"
        const apiSubDomain = "api"

        //1.
        const zone = route53.HostedZone.fromLookup(this, 'Zone', { domainName: baseUrlDomainName });
        const siteDomain = siteSubDomain + '.' + baseUrlDomainName;
        new CfnOutput(this, 'Admin Site', { value: 'https://' + siteDomain });

        const docsDomain = docsSubDomian + '.' + baseUrlDomainName;
        new CfnOutput(this, 'Docs Site', { value: 'https://' + docsDomain });

        const operationsDomain = operationSubDomain + '.' + baseUrlDomainName;
        new CfnOutput(this, 'Operation Site', { value: 'https://' + operationsDomain });

        const apiDomain = apiSubDomain + '.' + baseUrlDomainName;
        new CfnOutput(this, 'API Site', { value: 'https://' + apiDomain });

        //2.
        const adminCertificate = new acm.Certificate(this, 'SiteCertificate', {
            domainName: siteDomain,
            validation: acm.CertificateValidation.fromDns(zone),

        });

        const docsCertificate = new acm.Certificate(this, 'DocsSiteCertificate', {
            domainName: docsDomain,
            validation: acm.CertificateValidation.fromDns(zone),

        });

        const operationsCertificate = new acm.Certificate(this, 'OperationsSiteCertificate', {
            domainName: operationsDomain,
            validation: acm.CertificateValidation.fromDns(zone),

        });

        const apiCertificate = new acm.Certificate(this, 'ApiSiteCertificate', {
            domainName: apiDomain,
            validation: acm.CertificateValidation.fromDns(zone),
        });


        // this.putParameter("certificateArn", adminCertificate.certificateArn);
        this.putVariable("certificateArn", adminCertificate.certificateArn)
        new CfnOutput(this, 'Certificate', { value: adminCertificate.certificateArn });

        // this.putParameter("docsCertificateSSMArn", docsCertificate.certificateArn);
        this.putVariable("docsCertificateVarArn", docsCertificate.certificateArn)
        new CfnOutput(this, 'DocsCertificate', { value: docsCertificate.certificateArn });

        // this.putParameter("opsCertificateArn", operationsCertificate.certificateArn);
        this.putVariable("opsCertificateArn", operationsCertificate.certificateArn)
        new CfnOutput(this, 'OpsCertificate', { value: operationsCertificate.certificateArn });

        // this.putParameter("apiCertificateArn", apiCertificate .certificateArn);
        this.putVariable("apiCertificateArn", apiCertificate.certificateArn)
        new CfnOutput(this, 'ApiCertificate', { value: apiCertificate.certificateArn });

    }


}
