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

To run Integration Tests locally, you'll have to set up the necessary infrastructure in one of your AWS Accounts.
This can be done by applying the Terraform configuration found in `integration-tests/infrastructure`. Here's how you do it:

1. Make sure that no state file exists in `/integration-tests/infrastructure`.
   If it exists, see if the state file is empty. If so, delete it. Otherwise, perform Step 7 or 8.
2. Obtain Credentials for your AWS Account. This step highly depends on how the AWS Account is set up. You'll need permissions for API Gateway, Lambda and IAM.
3. Initialize and apply the Terraform stack by running:
    ```
    cd integration-tests/infrastructure
    terraform init
    terraform apply
    ```
4. Confirm with `yes` after checking the Terraform Plan output.
5. Wait until everything is set up. This should only take a few seconds.
6. Execute the Tests by running:
    ```
    export API_GATEWAY_URL=$(terraform output -raw api-gateway-url)
    # cd back to the repository root
    cd ../..
    mvn -B -ntp clean install failsafe:integration-test failsafe:verify -DapiGatewayUrl=$API_GATEWAY_URL
    ```
7. Clean up the necessary infrastructure by running:
    ```
    cd integration-tests/infrastructure
    terraform destroy
    ```
8. Confirm with `yes` after checking the Terraform Destroy output.
