export interface ProjectConfig {
    Name: string,
    Stage: string,
    Account: string,
    Region: string,
    Profile: string,
    BusinessUnit: string
}

export interface StackConfig {
    Name: string,
    UpdateRegionName?: string,
    [name: string]: any
}

export interface AppConfig {
    Project: ProjectConfig;
    Global: any;
    Stack: StackConfig;
}
