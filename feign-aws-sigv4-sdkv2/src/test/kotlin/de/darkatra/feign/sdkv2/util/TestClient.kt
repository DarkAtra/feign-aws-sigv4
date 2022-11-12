package de.darkatra.feign.sdkv2.util

import feign.Feign
import feign.Param
import feign.RequestInterceptor
import feign.RequestLine

interface TestClient {

    companion object {
        fun create(url: String, requestInterceptor: RequestInterceptor): TestClient {
            return Feign.builder()
                .requestInterceptor(requestInterceptor)
                .target(TestClient::class.java, url)
        }
    }

    @RequestLine("GET /path?query={query}")
    fun getRequestWithQueryParameter(@Param("query") query: String): String

    @RequestLine("POST /path")
    fun postRequestWithBody(body: String): String
}
