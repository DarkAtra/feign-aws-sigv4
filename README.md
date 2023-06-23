[![Build](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml)

# feign-aws-sigv4

Provides feign request interceptors to sign http requests using [AWS Signature V4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html).

## Usage with SDK V1

Include the following dependency in your project:

```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv1</artifactId>
    <version>2.0.10</version>
</dependency>
```

### Kotlin

```kotlin
val awsCredentialsProvider = DefaultAWSCredentialsProviderChain()
val service = "execute-api"
val region = Region.getRegion(Regions.EU_CENTRAL_1)

val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region)

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient::class.java, url)
```

### Java

```java
final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
final String service = "execute-api";
final Region region = Region.getRegion(Regions.EU_CENTRAL_1);

final RequestInterceptor awsSignatureV4RequestInterceptor = new AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region);

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient.class, url);
```

## Usage with SDK V2

Include the following dependency in your project:

```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv2</artifactId>
    <version>2.0.10</version>
</dependency>
```

### Kotlin

```kotlin
val awsCredentialsProvider = DefaultCredentialsProvider.create()
val service = "execute-api"
val region = Region.of("eu-central-1")

val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region)

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient::class.java, url)
```

### Java

```java
final AwsCredentialsProvider awsCredentialsProvider = DefaultCredentialsProvider.create();
final String service = "execute-api";
final Region region = Region.of("eu-central-1");

final RequestInterceptor awsSignatureV4RequestInterceptor = new AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region);

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient.class, url);
```

## How to build it locally

```
mvn clean install
```

## How to execute Integration Tests locally

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
