import * as base from "../../lib/template/stack/base/base-stack";
import {AppContext} from "../../lib/template/app-context";
import * as ssm from 'aws-cdk-lib/aws-ssm';

export class AwsCoreVpcImportStack extends base.BaseStack {

    public hostedZoneId: string;

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        const prefix = 'Awsdev'
        const cfnVpcId =  ssm.StringParameter.valueForStringParameter(
            this, `/${prefix}/vpcId`);
        this.putParameter('VpcId', cfnVpcId);

        const inputString = "subnet-053c61254e2b4d71f,subnet-07fffb6efa66fa475,subnet-04eb4fd5e9513f5eb"
        const resultArray = inputString.split(","); // Splitting the string by comma

        resultArray.forEach((value, index) => {
            console.log(value)
            this.putParameter(`privateSubnet${index}`, value);
        })

        const privateDnsNameSpaceArn = ssm.StringParameter.valueForStringParameter(
            this, `/${prefix}/privateDnsNamespaceArn`);
        this.putParameter('privateDnsNameSpaceArn', privateDnsNameSpaceArn);

        const privateDnsNameSpaceId = ssm.StringParameter.valueForStringParameter(
            this, `/${prefix}/privateDnsNamespaceId`);
        this.putParameter('privateDnsNameSpaceId', privateDnsNameSpaceId);

        const privateDnsNameSpaceName = ssm.StringParameter.valueForStringParameter(
            this, `/${prefix}/privateDnsNamespaceName`);
        this.putParameter('privateDnsNameSpaceName', privateDnsNameSpaceName);

        const publicDnsNameSpaceId = ssm.StringParameter.valueForStringParameter(
            this, `/${prefix}/publicHostedZoneId`);
        this.putParameter('publicDnsNameSpaceId', publicDnsNameSpaceId);
        this.putVariable('publicDnsNameSpaceId', publicDnsNameSpaceId);

        this.hostedZoneId = publicDnsNameSpaceId;

    }

}
