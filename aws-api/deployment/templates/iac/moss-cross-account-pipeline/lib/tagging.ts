
import * as cdk from 'aws-cdk-lib';
import { getLogicalIdPrefix, getResourceNamePrefix } from './configuration';

const COST_CENTER = 'COST_CENTER';
const TAG_ENVIRONMENT = 'TAG_ENVIRONMENT';
const TEAM = 'TEAM';
const APPLICATION = 'APPLICATION';

export function tag(stack: cdk.Stack, targetEnvironment: string): void {
    /**
     * Adds a tag to all constructs in the stack
     *
     * @param stack: The stack to tag
     * @param targetEnvironment: The environment the stack is deployed to
     */
    cdk.Tags.of(stack).add(...getTag(COST_CENTER, targetEnvironment));
    cdk.Tags.of(stack).add(...getTag(TAG_ENVIRONMENT, targetEnvironment));
    cdk.Tags.of(stack).add(...getTag(TEAM, targetEnvironment));
    cdk.Tags.of(stack).add(...getTag(APPLICATION, targetEnvironment));
}

function getTag(tagName: string, targetEnvironment: string): [string, string] {
    /**
     * Get a tag for a given parameter and target environment.
     *
     * @param tagName: The name of the tag
     * @param targetEnvironment: The environment the tag is applied to
     */
    const logicalIdPrefix = getLogicalIdPrefix();
    const resourceNamePrefix = getResourceNamePrefix();

    const tagMap: { [key: string]: [string, string] } = {
        [COST_CENTER]: [
            `${resourceNamePrefix}:cost-center`,
            `${logicalIdPrefix}Infrastructure`,
        ],
        [TAG_ENVIRONMENT]: [
            `${resourceNamePrefix}:environment`,
            targetEnvironment,
        ],
        [TEAM]: [
            `${resourceNamePrefix}:team`,
            `${logicalIdPrefix}Admin`,
        ],
        [APPLICATION]: [
            `${resourceNamePrefix}:application`,
            `${logicalIdPrefix}Infrastructure`,
        ],
    };

    if (!(tagName in tagMap)) {
        throw new Error(`Tag map does not contain a key/value for ${tagName}`);
    }

    return tagMap[tagName];
}
