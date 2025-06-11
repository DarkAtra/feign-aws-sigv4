package de.darkatra.feign.common

/**
 * This class is considered internal. Please do not use it in your code.
 */
object AwsSignatureV4Constants {

    const val AUTHORIZATION = "Authorization"
    const val X_AMZ_CONTENT_SHA256 = "x-amz-content-sha256"
    const val X_AMZ_DATE = "X-Amz-Date"
    const val X_AMZ_SECURITY_TOKEN = "X-Amz-Security-Token"

    val HEADERS_TO_COPY = setOf(
        AUTHORIZATION,
        X_AMZ_CONTENT_SHA256,
        X_AMZ_DATE,
        X_AMZ_SECURITY_TOKEN,
    )
}
