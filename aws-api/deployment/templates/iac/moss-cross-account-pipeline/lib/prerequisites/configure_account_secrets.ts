import { STSClient, GetCallerIdentityCommand } from '@aws-sdk/client-sts';
import { SecretsManagerClient, CreateSecretCommand } from "@aws-sdk/client-secrets-manager";
import { createInterface } from 'readline';

import { DEPLOYMENT, GITHUB_TOKEN, getAllConfigurations } from "../configuration";

const MY_GITHUB_TOKEN = 'a6b4c3faf68397fd5cd78a1a6e81a6bdfa103459';

async function main() {
    if (!MY_GITHUB_TOKEN) {
        throw new Error(`You must provide a value for: MY_GITHUB_TOKEN`);
    }

    const stsClient = new STSClient({});
    const callerIdentity = await stsClient.send(new GetCallerIdentityCommand({}));
    const accountId = callerIdentity.Account;

    const rl = createInterface({
        input: process.stdin,
        output: process.stdout
    });

    const response = await new Promise<string>((resolve) => {
        rl.question(
            `Are you sure you want to add a secret to AWS Secrets Manager with name ` +
            `${getAllConfigurations()[DEPLOYMENT][GITHUB_TOKEN]} ` +
            `in account: ${accountId}?\n\n` +
            `This should be the Central Deployment Account Id\n\n` +
            `(y/n) `,
            resolve
        );
    });

    rl.close();

    if (response.toLowerCase() === 'y') {
        const secretsManagerClient = new SecretsManagerClient({});
        const secretName = getAllConfigurations()[DEPLOYMENT][GITHUB_TOKEN];
        console.log(`Pushing secret: ${secretName}`);

        await secretsManagerClient.send(new CreateSecretCommand({
            Name: secretName,
            SecretString: MY_GITHUB_TOKEN
        }));
    }
}

main().catch(console.error);