package de.darkatra

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import de.darkatra.util.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region

@WireMockTest
internal class AwsSignatureV4RequestInterceptorTest {

    companion object {
        private const val ACCESS_KEY = "access-key"
        private const val SECRET_KEY = "secret-key"
        private const val SESSION_TOKEN = "session-token"
        private const val SERVICE = "execute-api"
        private const val REGION = "eu-central-1"
    }

    private val awsCredentialsProvider = StaticCredentialsProvider.create(AwsSessionCredentials.create(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN))
    private val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, SERVICE, Region.of(REGION))

    @Test
    internal fun shouldSignGetRequestWithQueryParameters(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val testClient = TestClient.create(wireMockRuntimeInfo.httpBaseUrl, awsSignatureV4RequestInterceptor)

        val queryParameter = "query-param"
        val expectedResponse = "response-body"

        wireMockRuntimeInfo.wireMock.register(
            get(urlPathEqualTo("/path"))
                .withQueryParam("query", equalTo(queryParameter))
                .withHeader(
                    AwsSignatureV4Constants.AUTHORIZATION,
                    matching("AWS4-HMAC-SHA256 Credential=$ACCESS_KEY/[0-9]{8}/$REGION/$SERVICE/aws4_request, SignedHeaders=host;x-amz-date;x-amz-security-token, Signature=[a-z0-9]+")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_DATE,
                    matching("[0-9]{8}T[0-9]{6}Z")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_SECURITY_TOKEN,
                    equalTo(SESSION_TOKEN)
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(expectedResponse)
                )
        )

        val actualResponse = testClient.getRequestWithQueryParameter(queryParameter)

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    internal fun shouldSignPostRequestWithBody(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val testClient = TestClient.create(wireMockRuntimeInfo.httpBaseUrl, awsSignatureV4RequestInterceptor)

        val body = "request-body"
        val expectedResponse = "response-body"

        wireMockRuntimeInfo.wireMock.register(
            post(urlPathEqualTo("/path"))
                .withRequestBody(equalTo(body))
                .withHeader(
                    AwsSignatureV4Constants.AUTHORIZATION,
                    matching("AWS4-HMAC-SHA256 Credential=$ACCESS_KEY/[0-9]{8}/$REGION/$SERVICE/aws4_request, SignedHeaders=content-length;host;x-amz-date;x-amz-security-token, Signature=[a-z0-9]+")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_DATE,
                    matching("[0-9]{8}T[0-9]{6}Z")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_SECURITY_TOKEN,
                    equalTo(SESSION_TOKEN)
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(expectedResponse)
                )
        )

        val actualResponse = testClient.postRequestWithBody(body)

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
