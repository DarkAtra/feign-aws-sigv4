[![Build](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml)

# feign-aws-sigv4

Provides feign request interceptors to sign http requests using [AWS Signature V4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html).

## Usage with SDK V1

Include the following dependency in your project:

[//]: # (@formatter:off)
```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv1</artifactId>
    <version>3.0.4</version>
</dependency>
```
[//]: # (@formatter:on)

### Kotlin

[//]: # (@formatter:off)
```kotlin
val awsCredentialsProvider = DefaultAWSCredentialsProviderChain()
val service = "execute-api"
val region = Region.getRegion(Regions.EU_CENTRAL_1)

val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region)

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient::class.java, url)
```
[//]: # (@formatter:on)

### Java

[//]: # (@formatter:off)
```java
final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
final String service = "execute-api";
final Region region = Region.getRegion(Regions.EU_CENTRAL_1);

final RequestInterceptor awsSignatureV4RequestInterceptor = new AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region);

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient.class, url);
```
[//]: # (@formatter:on)

## Usage with SDK V2

Include the following dependency in your project:

[//]: # (@formatter:off)
```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv2</artifactId>
    <version>3.0.4</version>
</dependency>
```
[//]: # (@formatter:on)

### Kotlin

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

### Java

[//]: # (@formatter:off)
```java
final AwsCredentialsProvider awsCredentialsProvider = DefaultCredentialsProvider.create();
final String service = "execute-api";
final Region region = Region.of("eu-central-1");

final RequestInterceptor awsSignatureV4RequestInterceptor = new AwsSignatureV4RequestInterceptor(awsCredentialsProvider, service, region);

Feign.builder()
    .requestInterceptor(awsSignatureV4RequestInterceptor)
    .target(YourClient.class, url);
```
[//]: # (@formatter:on)
