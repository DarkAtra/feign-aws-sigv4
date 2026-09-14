package de.darkatra.feign.sdkv2

import de.darkatra.feign.common.AwsSignatureV4Constants
import feign.RequestInterceptor
import feign.RequestTemplate
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.auth.aws.signer.AwsV4FamilyHttpSigner
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner
import software.amazon.awssdk.regions.Region
import java.net.URI
import java.util.stream.Collectors

/**
 * Feign [RequestInterceptor] that signs http requests with Signature V4. Uses the AWS SDK V2.
 *
 * @param awsCredentialsProvider the [AwsCredentialsProvider] used to sign the request
 * @param service the service to sign the requests for. Use 'execute-api' if
 * you're issuing requests against AWS API Gateway or 'vpc-lattice-svcs' for AWS VPC Lattice.
 * @param region the region
 * @param payloadSigningEnabled whether to sign the request payload. Set this to `false` when
 * talking to AWS VPC Lattice, which does not support payload signing and requires the
 * `x-amz-content-sha256` header to be set to `UNSIGNED-PAYLOAD`. Note that unsigned payloads
 * are only permitted over https.
 */
class AwsSignatureV4RequestInterceptor(
    private val awsCredentialsProvider: AwsCredentialsProvider,
    private val service: String,
    private val region: Region,
    private val payloadSigningEnabled: Boolean = true
) : RequestInterceptor {

    private val aws4Signer: AwsV4HttpSigner = AwsV4HttpSigner.create()

    override fun apply(template: RequestTemplate) {

        val body = template.body()

        val signedRequest = aws4Signer.sign { request ->
            // convert the RequestTemplate to an SdkHttpFullRequest to delegate the signing process to the AWS SDK
            request
                .putProperty(AwsV4FamilyHttpSigner.PAYLOAD_SIGNING_ENABLED, payloadSigningEnabled)
                .identity(awsCredentialsProvider.resolveCredentials())
                .request(
                    SdkHttpFullRequest.builder()
                        .uri(getRequestUri(template))
                        .method(SdkHttpMethod.fromValue(template.method()))
                        .headers(convertHeaders(template.headers()))
                        .build()
                )
                .payload(
                    when {
                        body != null -> RequestBody.fromBytes(body).contentStreamProvider()
                        else -> RequestBody.empty().contentStreamProvider()
                    }
                )
                .putProperty(AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, service)
                .putProperty(AwsV4HttpSigner.REGION_NAME, region.id())
        }.request()

        // copy amazon specific headers over to the request template
        signedRequest.headers().entries.stream()
            .filter { header -> AwsSignatureV4Constants.HEADERS_TO_COPY.contains(header.key) }
            .forEach { header -> template.header(header.key, header.value) }
    }

    private fun convertHeaders(headers: Map<String, Collection<String>>): Map<String, List<String>> {
        return headers.entries.stream()
            .map { header -> header.key to header.value.toList() }
            .collect(Collectors.toMap(Pair<String, List<String>>::first, Pair<String, List<String>>::second))
    }

    private fun getRequestUri(template: RequestTemplate): URI {
        val url = template.feignTarget().url()
        val pathWithParams = template.url()
        return URI.create(
            when {
                url.endsWith("/") -> url
                else -> "$url/"
            }
        ).resolve(
            when {
                pathWithParams.startsWith("/") -> pathWithParams.removePrefix("/")
                else -> pathWithParams
            }
        )
    }
}
