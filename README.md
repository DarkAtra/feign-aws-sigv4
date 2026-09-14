[![Build](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml)

# feign-aws-sigv4

Provides feign request interceptors to sign http requests using [AWS Signature V4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html).

Include the following dependency in your project:

[//]: # (@formatter:off)
```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv2</artifactId>
    <version>5.0.0</version>
</dependency>
```
[//]: # (@formatter:on)

## Usage with AWS API Gateway

[//]: # (@formatter:off)
```kotlin
val awsCredentialsProvider = DefaultCredentialsProvider.create()
val service = "execute-api"
val region = Region.of("eu-central-1")

val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region)

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient::class.java, url)
```
[//]: # (@formatter:on)

## Usage with AWS VPC Lattice

[AWS VPC Lattice](https://docs.aws.amazon.com/vpc-lattice/latest/ug/what-is-vpc-lattice.html) does not support payload signing. Requests must be signed with the
`x-amz-content-sha256` header set to `UNSIGNED-PAYLOAD` instead, and the service name must be `vpc-lattice-svcs`. Pass `signPayload = false` to the interceptor
to enable this behavior. Note that AWS only permits unsigned payloads over `https`.

[//]: # (@formatter:off)
```kotlin
val awsCredentialsProvider = DefaultCredentialsProvider.create()
val service = "vpc-lattice-svcs"
val region = Region.of("eu-central-1")
val signPayload = false

val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region, signPayload)

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient::class.java, url)
```
[//]: # (@formatter:on)
