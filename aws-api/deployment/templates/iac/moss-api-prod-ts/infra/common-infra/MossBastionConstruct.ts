import {BaseConstruct, ConstructCommonProps} from "../../lib/template/construct/base/base-construct";
import {Construct} from "constructs";


export interface AwsBastionProps extends ConstructCommonProps {

}


export class AwsBastionConstruct extends BaseConstruct {

    constructor(scope: Construct, id: string, props: AwsBastionProps) {
        super(scope, id, props);

    }

}
