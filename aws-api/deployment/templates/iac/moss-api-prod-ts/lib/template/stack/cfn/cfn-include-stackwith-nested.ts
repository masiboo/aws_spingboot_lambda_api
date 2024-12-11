import * as cfn_inc from 'aws-cdk-lib/cloudformation-include';

import * as base from '../base/base-stack';
import { AppContext } from '../../app-context';
import { StackConfig } from '../../app-config'


export interface CfnTemplateNestedProps {
    templatePath: string;
    parameters?: any;
    dnsTemplatePath: string;
    infrastructureTemplatePath: string,
    certTemplatePath: string,
    initTemplatePath: string,
}

export abstract class CfnIncludeStackNested extends base.BaseStack {
    private cfnTemplate?: cfn_inc.CfnInclude;


    abstract onLoadTemplateProps(): CfnTemplateNestedProps | undefined;
    abstract onPostConstructor(cfnTemplate?: cfn_inc.CfnInclude): void;

    constructor(appContext: AppContext, stackConfig: StackConfig) {
        super(appContext, stackConfig);

        const props = this.onLoadTemplateProps();

        if (props != undefined) {
            this.cfnTemplate = this.loadTemplate(props);
        } else {
            this.cfnTemplate = undefined;
        }

        this.onPostConstructor(this.cfnTemplate);
    }

    private loadTemplate(props: CfnTemplateNestedProps): cfn_inc.CfnInclude {

        const cfnTemplate = new cfn_inc.CfnInclude(this, 'cfn-include-template', {
            templateFile: props.templatePath,
            loadNestedStacks: {
                'Init': {
                    templateFile: props.initTemplatePath,
                },
                'Infrastructure': {
                    templateFile: props.infrastructureTemplatePath,
                },
                'Dns': {
                    templateFile: props.dnsTemplatePath,
                }

            },
        });

        // set default values
        if (props.parameters != undefined) {
            for(let param of props.parameters) {
                const paramEnv = cfnTemplate.getParameter(param.Key);
                paramEnv.default = param.Value;
            }
        }

        // set other CFN Parameters here e.g. BuildID.

        return cfnTemplate;
    }
}
