
// Environments (targeted at accounts)
export const DEPLOYMENT = 'Deployment';
export const DEV = 'Dev';
export const ACC = 'Acc';
export const PROD = 'Prod';

export const ENVIRONMENT = 'environment';
export const ACCOUNT_ID = 'account_id';
export const REGION = 'region';
export const VPC_CIDR = 'vpc_cidr';

// Manual Inputs
export const LOGICAL_ID_PREFIX = 'logical_id_prefix';
export const RESOURCE_NAME_PREFIX = 'resource_name_prefix';

// Secrets Manager Inputs
export const GITHUB_TOKEN = 'github_token';

// Used in Automated Outputs
export const VPC_ID = 'vpc_id';
export const AVAILABILITY_ZONE_1 = 'availability_zone_1';
export const AVAILABILITY_ZONE_2 = 'availability_zone_2';
export const AVAILABILITY_ZONE_3 = 'availability_zone_3';
export const SUBNET_ID_1 = 'subnet_id_1';
export const SUBNET_ID_2 = 'subnet_id_2';
export const SUBNET_ID_3 = 'subnet_id_3';
export const ROUTE_TABLE_1 = 'route_table_1';
export const ROUTE_TABLE_2 = 'route_table_2';
export const ROUTE_TABLE_3 = 'route_table_3';
export const SHARED_SECURITY_GROUP_ID = 'shared_security_group_id';
export const S3_KMS_KEY = 's3_kms_key';
export const S3_ACCESS_LOG_BUCKET = 's3_access_log_bucket';
export const S3_RAW_BUCKET = 's3_raw_bucket';
export const S3_CONFORMED_BUCKET = 's3_conformed_bucket';
export const S3_PURPOSE_BUILT_BUCKET = 's3_purpose_built_bucket';

interface Configuration {
    [key: string]: any;
}

export function getLocalConfiguration(environment: string): Configuration {
    /**
     * Provides manually configured variables that are validated for quality and safety.
     *
     * @param environment string: The environment used to retrieve corresponding configuration
     * @throws Error: Throws an exception if the resource_name_prefix does not conform
     * @throws Error: Throws an exception if the requested environment does not exist
     * @returns Configuration:
     */
    const localMapping: Configuration = {
        [DEPLOYMENT]: {
            [ACCOUNT_ID]: '173148697964', // replace with CICD account!
            [REGION]: 'eu-central-1',
            [LOGICAL_ID_PREFIX]: 'AwsApp',
            [RESOURCE_NAME_PREFIX]: 'cdkAws',
        },
        [DEV]: {
            [ACCOUNT_ID]: '551493771163',
            [REGION]: 'eu-central-1',
            [VPC_CIDR]: '10.20.0.0/24',
        },
        [ACC]: {
            [ACCOUNT_ID]: '489962969526',
            [REGION]: 'eu-central-1',
            [VPC_CIDR]: '10.10.0.0/24',
        },
        [PROD]: {
            [ACCOUNT_ID]: '489962969526',
            [REGION]: 'eu-central-1',
            [VPC_CIDR]: '10.0.0.0/24',
        },
    };

    // const resourcePrefix = localMapping[DEPLOYMENT][RESOURCE_NAME_PREFIX];
    // if (!/^[a-z0-9-]+$/.test(resourcePrefix) || resourcePrefix.endsWith('-') || resourcePrefix.startsWith('-')) {
    //     throw new Error('Resource names may only contain lowercase alphanumeric characters and hyphens and cannot contain leading or trailing hyphens');
    // }

    if (!localMapping[environment]) {
        throw new Error(`The requested environment: ${environment} does not exist in local mappings`);
    }

    return localMapping[environment];
}

export function getEnvironmentConfiguration(environment: string): Configuration {
    /**
     * Provides all configuration values for the given target environment
     *
     * @param environment string: The environment used to retrieve corresponding configuration
     * @returns Configuration:
     */
    const cloudformationOutputMapping: Configuration = {
        [ENVIRONMENT]: environment,
        [VPC_ID]: `${environment}VpcId`,
        [AVAILABILITY_ZONE_1]: `${environment}AvailabilityZone1`,
        [AVAILABILITY_ZONE_2]: `${environment}AvailabilityZone2`,
        [AVAILABILITY_ZONE_3]: `${environment}AvailabilityZone3`,
        [SUBNET_ID_1]: `${environment}SubnetId1`,
        [SUBNET_ID_2]: `${environment}SubnetId2`,
        [SUBNET_ID_3]: `${environment}SubnetId3`,
        [ROUTE_TABLE_1]: `${environment}RouteTable1`,
        [ROUTE_TABLE_2]: `${environment}RouteTable2`,
        [ROUTE_TABLE_3]: `${environment}RouteTable3`,
        [SHARED_SECURITY_GROUP_ID]: `${environment}SharedSecurityGroupId`,
        [S3_KMS_KEY]: `${environment}S3KmsKeyArn`,
        [S3_ACCESS_LOG_BUCKET]: `${environment}S3AccessLogBucket`,
        [S3_RAW_BUCKET]: `${environment}RawBucketName`,
        [S3_CONFORMED_BUCKET]: `${environment}ConformedBucketName`,
        [S3_PURPOSE_BUILT_BUCKET]: `${environment}PurposeBuiltBucketName`,
    };

    return { ...cloudformationOutputMapping, ...getLocalConfiguration(environment) };
}

export function getAllConfigurations(): Configuration {
    /**
     * Returns a dict mapping of configurations for all environments.
     * These keys correspond to static values, CloudFormation outputs, and Secrets Manager (passwords only) records.
     *
     * @returns Configuration:
     */
    return {
        [DEPLOYMENT]: {
            [ENVIRONMENT]: DEPLOYMENT,
            [GITHUB_TOKEN]: '/DataLake/GitHubToken',
            ...getLocalConfiguration(DEPLOYMENT),
        },
        [DEV]: getEnvironmentConfiguration(DEV),
        [ACC]: getEnvironmentConfiguration(ACC),
        [PROD]: getEnvironmentConfiguration(PROD),
    };
}

export function getLogicalIdPrefix(): string {
    /**
     * Returns the logical id prefix to apply to all CloudFormation resources
     *
     * @returns string:
     */
    return getLocalConfiguration(DEPLOYMENT)[LOGICAL_ID_PREFIX];
}

export function getResourceNamePrefix(): string {
    /**
     * Returns the resource name prefix to apply to all resources names
     *
     * @returns string:
     */
    return getLocalConfiguration(DEPLOYMENT)[RESOURCE_NAME_PREFIX];
}
