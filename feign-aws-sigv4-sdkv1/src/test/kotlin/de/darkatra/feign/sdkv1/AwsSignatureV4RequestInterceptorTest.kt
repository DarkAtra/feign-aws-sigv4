package de.darkatra.feign.sdkv1

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import de.darkatra.feign.common.AwsSignatureV4Constants
import de.darkatra.feign.sdkv2.util.TestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WireMockTest
internal class AwsSignatureV4RequestInterceptorTest {

    companion object {
        private const val ACCESS_KEY = "access-key"
        private const val SECRET_KEY = "secret-key"
        private const val SESSION_TOKEN = "session-token"
        private const val SERVICE = "execute-api"
        private const val REGION = "eu-central-1"
    }

    private val awsCredentialsProvider = AWSStaticCredentialsProvider(BasicSessionCredentials(ACCESS_KEY, SECRET_KEY, SESSION_TOKEN))
    private val awsSignatureV4RequestInterceptor = AwsSignatureV4RequestInterceptor(awsCredentialsProvider, SERVICE, Region.getRegion(Regions.fromName(REGION)))

    @Test
    internal fun shouldSignGetRequestWithQueryParameters(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val testClient = TestClient.create(wireMockRuntimeInfo.httpBaseUrl, awsSignatureV4RequestInterceptor)

        val queryParameter = "query-parameter"
        val expectedResponse = "response-body"

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get(WireMock.urlPathEqualTo("/path"))
                .withQueryParam("query", WireMock.equalTo(queryParameter))
                .withHeader(
                    AwsSignatureV4Constants.AUTHORIZATION,
                    WireMock.matching("AWS4-HMAC-SHA256 Credential=$ACCESS_KEY/[0-9]{8}/$REGION/$SERVICE/aws4_request, SignedHeaders=host;x-amz-date;x-amz-security-token, Signature=[a-z0-9]+")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_DATE,
                    WireMock.matching("[0-9]{8}T[0-9]{6}Z")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_SECURITY_TOKEN,
                    WireMock.equalTo(SESSION_TOKEN)
                )
                .willReturn(
                    WireMock.aResponse()
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
            WireMock.post(WireMock.urlPathEqualTo("/path"))
                .withRequestBody(WireMock.equalTo(body))
                .withHeader(
                    AwsSignatureV4Constants.AUTHORIZATION,
                    WireMock.matching("AWS4-HMAC-SHA256 Credential=$ACCESS_KEY/[0-9]{8}/$REGION/$SERVICE/aws4_request, SignedHeaders=content-length;host;x-amz-date;x-amz-security-token, Signature=[a-z0-9]+")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_DATE,
                    WireMock.matching("[0-9]{8}T[0-9]{6}Z")
                )
                .withHeader(
                    AwsSignatureV4Constants.X_AMZ_SECURITY_TOKEN,
                    WireMock.equalTo(SESSION_TOKEN)
                )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(expectedResponse)
                )
        )

        val actualResponse = testClient.postRequestWithBody(body)

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
