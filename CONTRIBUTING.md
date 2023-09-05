# Contributing to feign-aws-sigv4

Thank you for investing your time in contributing to our project! Here are a few things you might need to get started.

## Build the code

Clone the repository:

```bash
git clone git@github.com:DarkAtra/feign-aws-sigv4.git
```

Try your first build:

```bash
cd feign-aws-sigv4
mvn clean install
```

## Report issues and ideas

Please use [GitHub Issues](https://github.com/DarkAtra/feign-aws-sigv4/issues) to report issues and ideas that you might work on.

## Executing Integration Tests locally

In order to run Integration Tests locally, you'll have to set up the necessary infrastructure in one of your AWS Accounts.
This can be done by applying the Terraform CDK project found in `integration-tests/infrastructure`. Here's how you do it:

1. Make sure that no state file exists in `/integration-tests/infrastructure`.
   If it exists, see if the state file is empty. If so, delete it. Otherwise, perform Step 7 or 8.
2. Synthesize the Terraform CDK Stack by running:
    ```
    # cd into /integration-tests/infrastructure
    cd integration-tests/infrastructure
    yarn install --immutable --immutable-cache --check-cache
    yarn run synth
    ```
3. Obtain Credentials for your AWS Account. This step highly depends on how the AWS Account is set up. You'll need permissions for API Gateway, Lambda and IAM.
4. Apply the synthesized Terraform CDK Stack by running:
    ```
    # cd into /integration-tests/infrastructure/cdktf.out/stacks/feign-aws-sigv4-integration-tests
    cd cdktf.out/stacks/feign-aws-sigv4-integration-tests
    terraform init
    terraform apply
    ```
5. Confirm with `yes` after checking the Terraform Plan output.
6. Wait until everything is set up. This should only take a few seconds.
7. Execute the Tests by running:
    ```
    export API_GATEWAY_URL=$(terraform output -raw api-gateway-url)
    # cd back to /
    cd ../../../../..
    mvn -B -ntp clean install failsafe:integration-test failsafe:verify -DapiGatewayUrl=$API_GATEWAY_URL
    ```
8. Clean up the necessary infrastructure by running:
    ```
    # cd into /integration-tests/infrastructure/cdktf.out/stacks/feign-aws-sigv4-integration-tests
    cd integration-tests/infrastructure/cdktf.out/stacks/feign-aws-sigv4-integration-tests
    terraform destroy
    ```
9. Confirm with `yes` after checking the Terraform Destroy output.
