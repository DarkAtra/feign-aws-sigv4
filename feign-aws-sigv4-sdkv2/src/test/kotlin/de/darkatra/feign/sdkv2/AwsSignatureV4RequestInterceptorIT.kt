package de.darkatra.feign.sdkv2

import de.darkatra.feign.sdkv2.util.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region

internal class AwsSignatureV4RequestInterceptorIT {

    private val awsCredentialsProvider = DefaultCredentialsProvider.create()
    private val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, "execute-api", Region.EU_CENTRAL_1)

    @Test
    internal fun shouldSignGetRequestWithQueryParameters() {

        val testClient = TestClient.create(getApiGatewayUrl(), awsSignatureV4RequestInterceptor)

        val actualResponse = testClient.getRequestWithQueryParameter("query-parameter")

        assertThat(actualResponse).isEqualTo("Hello")
    }

    @Test
    internal fun shouldSignPostRequestWithBody() {

        val testClient = TestClient.create(getApiGatewayUrl(), awsSignatureV4RequestInterceptor)

        val actualResponse = testClient.postRequestWithBody("request-body")

        assertThat(actualResponse).isEqualTo("Hello")
    }

    private fun getApiGatewayUrl(): String {
        return System.getProperty("apiGatewayUrl")
    }
}
