import * as cdk from 'aws-cdk-lib';
import * as wafv2 from 'aws-cdk-lib/aws-wafv2';
import * as base from "../../lib/template/stack/base/us-east-base-stack";
import {AppContext} from "../../lib/template/app-context";

type listOfRules = {
    name: string;
    priority: number;
    overrideAction: string;
    excludedRules: string[];
};

export class AwsWafCloudFrontStack extends base.CrossRegionBaseStack {

    /**
     * Take in list of rules
     * Create output for use in WAF config
     */
    protected makeRules(listOfRules: listOfRules[] = []) {
        const rules: wafv2.CfnRuleGroup.RuleProperty[] = [];
        listOfRules.forEach(function (r) {
            const mrgsp: wafv2.CfnWebACL.ManagedRuleGroupStatementProperty = {
                name: r['name'],
                vendorName: "AWS",
                excludedRules: []
            };

            var stateProp: wafv2.CfnWebACL.StatementProperty = {
                managedRuleGroupStatement: {
                    name: r['name'],
                    vendorName: "AWS",
                }
            };
            var overrideAction: wafv2.CfnWebACL.OverrideActionProperty = { none: {} }

            var rule: wafv2.CfnWebACL.RuleProperty = {
                name: r['name'],
                priority: r['priority'],
                overrideAction: overrideAction,
                statement: stateProp,
                visibilityConfig: {
                    sampledRequestsEnabled: true,
                    cloudWatchMetricsEnabled: true,
                    metricName: r['name']
                },
            };
            rules.push(rule);
        }); // forEach


        const ruleGeoMatch: wafv2.CfnWebACL.RuleProperty = {
            name: 'GeoMatch',
            priority: 0,
            action: {
                block: {} // To disable, change to *count*
            },
            statement: {
                notStatement: {
                    statement: {
                        geoMatchStatement: {
                            // block connection if source not in the below country list
                            countryCodes: [
                                "CH", // SWitzerland
                                "DE", // Germany
                            ]
                        }

                    }
                }
            },
            visibilityConfig: {
                sampledRequestsEnabled: true,
                cloudWatchMetricsEnabled: true,
                metricName: 'GeoMatch'
            }
        }; // GeoMatch
        rules.push(ruleGeoMatch);

        const cfnIPSet = new wafv2.CfnIPSet(this, 'AwsCfnWafIPSet', {
            addresses: ["193.5.93.0/24", "3.126.58.0/24"],
            ipAddressVersion: 'IPV4',
            scope: 'CLOUDFRONT',

            // the properties below are optional
            description: 'Aws Ip Whitelist',
            name: 'AwsIPWhitelist',
            tags: [{
                key: 'application',
                value: 'Aws',
            }],
        });

        const ruleIPMatch: wafv2.CfnWebACL.RuleProperty = {
            name: 'IPWhitelistMatch',
            priority: 1,
            action: {
                allow: {} // To disable, change to *count*
            },
            statement: {
                notStatement: {
                    statement: {
                        ipSetReferenceStatement: {
                            arn: cfnIPSet.attrArn
                        }
                    }
                }
            },
            visibilityConfig: {
                sampledRequestsEnabled: true,
                cloudWatchMetricsEnabled: true,
                metricName: 'IPSetMatch'
            }
        }; // IPMatch
        rules.push(ruleIPMatch);


        /**
         * The rate limit is the maximum number of requests from a
         * single IP address that are allowed in a five-minute period.
         * This value is continually evaluated,
         * and requests will be blocked once this limit is reached.
         * The IP address is automatically unblocked after it falls below the limit.
         */
        // const ruleLimitRequests1000: wafv2.CfnWebACL.RuleProperty = {
        //     name: 'LimitRequests1000',
        //     priority: 2,
        //     action: {
        //         block: {} // To disable, change to *count*
        //     },
        //     statement: {
        //         rateBasedStatement: {
        //             limit: 1000,
        //             aggregateKeyType: "IP"
        //         }
        //     },
        //     visibilityConfig: {
        //         sampledRequestsEnabled: true,
        //         cloudWatchMetricsEnabled: true,
        //         metricName: 'LimitRequests1000'
        //     }
        // }; // limit requests to 100
        // rules.push(ruleLimitRequests1000);

        return rules;
    } // function makeRules

    constructor(appContext: AppContext, stackConfig: any) {
        super(appContext, stackConfig);

        /**
         * List available Managed Rule Groups using AWS CLI
         * aws wafv2 list-available-managed-rule-groups --scope CLOUDFRONT
         */
        const managedRules: listOfRules[] = [
            {
            "name": "AWSManagedRulesCommonRuleSet",
            "priority": 10,
            "overrideAction": "none",
            "excludedRules": []
            },
            {
            "name": "AWSManagedRulesAmazonIpReputationList",
            "priority": 20,
            "overrideAction": "none",
            "excludedRules": []
            },
            {
            "name": "AWSManagedRulesKnownBadInputsRuleSet",
            "priority": 30,
            "overrideAction": "none",
            "excludedRules": []
            },
            {
            "name": "AWSManagedRulesAnonymousIpList",
            "priority": 40,
            "overrideAction": "none",
            "excludedRules": []
            },
            {
            "name": "AWSManagedRulesLinuxRuleSet",
            "priority": 50,
            "overrideAction": "none",
            "excludedRules": []
            },
            {
            "name": "AWSManagedRulesUnixRuleSet",
            "priority": 60,
            "overrideAction": "none",
            "excludedRules": [],
            }
            ];

        // WAF - CloudFront
        const wafAclCloudFront = new wafv2.CfnWebACL(this, "WafCloudFront", {
            defaultAction: { allow : {} },
            /**
             * The scope of this Web ACL.
             * Valid options: CLOUDFRONT, REGIONAL.
             * For CLOUDFRONT, you must create your WAFv2 resources
             * in the US East (N. Virginia) Region, us-east-1
             */
            scope: "CLOUDFRONT",
            // Defines and enables Amazon CloudWatch metrics and web request sample collection.
            visibilityConfig: {
                cloudWatchMetricsEnabled: true,
                metricName: `${this.projectPrefix}-wafv2-acl-metric`,
                sampledRequestsEnabled: true
            },
            description: "Aws WAFv2 ACL for CloudFront",
            name: `${this.projectPrefix}-wafv2-acl`,
            rules: this.makeRules(managedRules),

        }); // wafv2.CfnWebACL

        // cdk.Tags.of(wafAclCloudFront).add("Name", "waf-cloudfront", { "priority": 300 });
        // cdk.Tags.of(wafAclCloudFront).add("Purpose", "Aws CloudFront", { "priority": 300 });
        // cdk.Tags.of(wafAclCloudFront).add("CreatedBy", "CloudFormation", { "priority": 300 });

        this.putVariable("WafAclCloudFrontRefIdParamBA", wafAclCloudFront.attrId)
        this.putVariable("wafAclCloudFrontArnParamBC", wafAclCloudFront.attrArn)

    } // constructor
} // class