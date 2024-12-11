import * as AWS from 'aws-sdk';
import { DEPLOYMENT, GITHUB_TOKEN, getAllConfigurations } from '../configuration';

let MY_GITHUB_TOKEN: string = '';

if (!MY_GITHUB_TOKEN) {
    throw new Error(`You must provide a value for: ${MY_GITHUB_TOKEN}`);
}

const response: string | null = prompt(
    `Are you sure you want to add a secret to AWS Secrets Manager with name ` +
    `${getAllConfigurations()[DEPLOYMENT][GITHUB_TOKEN]} ` +
    `in account: ${new AWS.STS().getCallerIdentity().promise().then(identity => identity.Account)}?\n\n` +
    `This should be the Central Deployment Account Id\n\n(y/n)`
);

if (response && response.toLowerCase() === 'y') {
    const secretsManagerClient = new AWS.SecretsManager();
    const secretName = getAllConfigurations()[DEPLOYMENT][GITHUB_TOKEN];
    console.log(`Pushing secret: ${secretName}`);
    secretsManagerClient.createSecret({ Name: secretName, SecretString: MY_GITHUB_TOKEN }).promise().then(() => {
        console.log('Secret successfully pushed.');
    }).catch((err: any) => {
        console.error('Error pushing secret:', err);
    });
}
