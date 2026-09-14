[![Build](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml)

# feign-aws-sigv4

Provides feign request interceptors to sign http requests using [AWS Signature V4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html).

Include the following dependency in your project:

[//]: # (@formatter:off)
```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv2</artifactId>
    <version>4.0.0</version>
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
