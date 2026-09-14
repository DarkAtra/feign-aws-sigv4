package de.darkatra.feign.sdkv2.util

import feign.DefaultClient
import feign.Feign
import feign.Param
import feign.RequestInterceptor
import feign.RequestLine
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

interface TestClient {

    companion object {
        fun create(url: String, requestInterceptor: RequestInterceptor, trustSelfSignedCerts: Boolean = false): TestClient {
            return Feign.builder()
                .apply {
                    if (trustSelfSignedCerts) {
                        allowSelfSignedCerts()
                    }
                }
                .requestInterceptor(requestInterceptor)
                .target(TestClient::class.java, url)
        }

        private fun Feign.Builder.allowSelfSignedCerts() {
            // trust the self-signed certificate used by WireMock when testing against https
            val trustAllManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf(trustAllManager), SecureRandom())
            }
            client(DefaultClient(sslContext.socketFactory) { _, _ -> true })
        }
    }

    @RequestLine("GET /path?query={query}")
    fun getRequestWithQueryParameter(@Param("query") query: String): String

    @RequestLine("POST /path")
    fun postRequestWithBody(body: String): String
}
