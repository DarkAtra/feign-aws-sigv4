package de.darkatra.feign.sdkv1

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import de.darkatra.feign.sdkv2.util.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AwsSignatureV4RequestInterceptorIT {

    private val awsCredentialsProvider = DefaultAWSCredentialsProviderChain()
    private val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(
        awsCredentialsProvider,
        "execute-api",
        Region.getRegion(Regions.EU_CENTRAL_1)
    )

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
        return "https://hi772yefv9.execute-api.eu-central-1.amazonaws.com/" // System.getProperty("apiGatewayUrl")
    }
}
