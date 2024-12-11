import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';

export class EmptyStack extends cdk.Stack {
    constructor(scope: Construct, id: string, ...rest: any[]) {
        /**
         * This stack is intentionally left empty. This is used during bootstrap to prevent synth of
         * stacks that are dependent on configuration.
         *
         * @param scope Construct: Parent of this stack, usually an App or a Stage, but could be any construct.
         * @param id string: The construct ID of this stack. If stackName is not explicitly defined,
         *                   this id (and any parent IDs) will be used to determine the physical ID of the stack.
         * @param rest any[]: Additional arguments passed to the parent class.
         */
        super(scope, id, ...rest);
        // Intentionally left empty
    }
}
