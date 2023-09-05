package de.darkatra.feign.sdkv1

import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.HttpMethodName
import com.amazonaws.regions.Region
import de.darkatra.feign.common.AwsSignatureV4Constants
import feign.RequestInterceptor
import feign.RequestTemplate
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.stream.Collectors

/**
 * Feign [RequestInterceptor] that signs http requests with Signature V4. Uses the AWS SDK V1.
 *
 * @param awsCredentialsProvider the [AWSCredentialsProvider] used to sign the request
 * @param service the service to sign the requests for. Use 'execute-api' if you're issuing requests against AWS API Gateway.
 * @param region the region
 */
class AwsSignatureV4RequestInterceptor(
    private val awsCredentialsProvider: AWSCredentialsProvider,
    private val service: String,
    private val region: Region
) : RequestInterceptor {

    private val aws4Signer: AWS4Signer = AWS4Signer().apply {
        serviceName = service
        regionName = region.name
    }

    override fun apply(template: RequestTemplate) {

        val body = template.body()
        val url = template.feignTarget().url()
        val path = template.path()

        // convert the RequestTemplate to an SdkHttpFullRequest to delegate the signing process to the AWS SDK
        val request = DefaultRequest<Unit>(service).apply {
            endpoint = URI.create(
                when {
                    url.endsWith("/") -> url
                    else -> "$url/"
                }
            )
            resourcePath = when {
                path.startsWith("/") -> path.removePrefix("/")
                else -> path
            }
            parameters = convertQueryParameters(template.queries())
            httpMethod = HttpMethodName.fromValue(template.method())
            headers = convertHeaders(template.headers())
            content = body?.let { ByteArrayInputStream(it) }
        }

        aws4Signer.sign(request, awsCredentialsProvider.credentials)

        // copy amazon specific headers over to the request template
        request.headers.entries.stream()
            .filter { header -> AwsSignatureV4Constants.HEADERS_TO_COPY.contains(header.key) }
            .forEach { header -> template.header(header.key, header.value) }
    }

    private fun convertQueryParameters(queryParameters: Map<String, Collection<String>>): MutableMap<String, List<String>> {
        return queryParameters.entries.stream()
            .map { header -> header.key to header.value.toList() }
            .collect(Collectors.toMap(Pair<String, List<String>>::first, Pair<String, List<String>>::second))
    }

    private fun convertHeaders(headers: Map<String, Collection<String>>): Map<String, String> {
        return headers.entries.stream()
            .map { header -> header.key to header.value.toList() }
            .collect(Collectors.toMap(Pair<String, List<String>>::first) { header ->
                header.second.joinToString(",")
            })
    }
}
