import os
import json
import argparse
import subprocess

def run_command(command):
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    output, error = process.communicate()
    if process.returncode != 0:
        raise Exception("Command failed: {}".format(error.decode('utf-8')))
    return output.decode('utf-8')

def package_cdk_output(cdk_out_dir, s3_bucket, output_dir, selected_template_file=None):
    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)

    # Read the manifest to get all synthesized stacks
    with open(os.path.join(cdk_out_dir, 'manifest.json'), 'r') as f:
        manifest = json.load(f)

    for stack_name, assembly in manifest.get('artifacts', {}).items():
        if assembly.get('type') == 'aws:cloudformation:stack':
            stack_template = assembly['properties']['templateFile']

            # Check if the current stack_template matches the selected_template_file
            if selected_template_file and stack_template != selected_template_file:
                continue

            input_template = os.path.join(cdk_out_dir, stack_template)
            output_template = os.path.join(output_dir, f"{stack_name}-packaged.yaml")

            print(f"Packaging template for stack: {stack_name}")
            package_command = f"aws cloudformation package " \
                              f"--template-file {input_template} " \
                              f"--s3-bucket {s3_bucket} " \
                              f"--output-template-file {output_template}"

            run_command(package_command)
            print(f"Packaged template saved to: {output_template}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Package CDK output using CloudFormation package')
    parser.add_argument('--cdk-out', dest='cdk_out_dir', required=True,
                        help='Directory containing CDK output')
    parser.add_argument('--bucket', dest='s3_bucket', required=True,
                        help='S3 bucket name for uploading assets')
    parser.add_argument('--output', dest='output_dir', required=True,
                        help='Directory to save packaged templates')
    parser.add_argument('--template-file', dest='template_file', required=False,
                        help='Specific template file to package (e.g., Aws-AwsCfnPreRequisiteStack.template.json)')

    args = parser.parse_args()

    package_cdk_output(args.cdk_out_dir, args.s3_bucket, args.output_dir, args.template_file)
