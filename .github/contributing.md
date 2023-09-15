# Contributing to feign-aws-sigv4

feign-aws-sigv4 is released under the MIT license.

## Code of Conduct

This project adheres to the Contributor Covenant [code of conduct](code_of_conduct.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to darkatra@gmail.com.

## GitHub Issues

We use GitHub issues to track bugs and enhancements.
If you are reporting a bug, please help to speed up problem diagnosis by providing as much information as possible.
Ideally, that would include a small sample project that reproduces the issue.

## Code Conventions and Housekeeping

None of these is essential for a pull request, but they will all help.
They can also be added after the original pull request but before a merge.

* We use the [Official Kotlin Code Style](https://kotlinlang.org/docs/coding-conventions.html).
  Please refer to the documentation to learn how to properly configure it in your IDE.
  In addition, some rules are enforced via the [.editorconfig](../.editorconfig).
* A few unit tests would help a lot as well -- someone has to do it.
* Verification tasks, including tests, can be executed by running `mvn clean install` from the project root.
* If no-one else is using your branch, please rebase it against the current `main` branch.
* When writing a commit message please follow [these conventions](https://www.conventionalcommits.org).

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
