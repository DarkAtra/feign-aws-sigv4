[![Build](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml/badge.svg)](https://github.com/DarkAtra/feign-aws-sigv4/actions/workflows/build.yml)

# feign-aws-sigv4

Provides feign request interceptors to sign http requests using [AWS Signature V4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html).

| Artifact Id             | AWS SDK Version | Status              |
|-------------------------|-----------------|---------------------|
| `feign-aws-sigv4-sdkv1` | V1              | ❌ To be implemented |
| `feign-aws-sigv4-sdkv2` | V2              | ✅ Fully implemented |

## Usage with SDK V2

Include the following dependency in your project:

```xml
<dependency>
    <groupId>de.darkatra</groupId>
    <artifactId>feign-aws-sigv4-sdkv2</artifactId>
    <version>1.0.0</version>
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
