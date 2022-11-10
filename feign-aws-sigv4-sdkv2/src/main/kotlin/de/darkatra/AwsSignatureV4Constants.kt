package de.darkatra

object AwsSignatureV4Constants {

    internal const val AUTHORIZATION = "Authorization"
    internal const val X_AMZ_SECURITY_TOKEN = "X-Amz-Security-Token"
    internal const val X_AMZ_DATE = "X-Amz-Date"

    internal val HEADERS_TO_COPY = setOf(
        AUTHORIZATION,
        X_AMZ_DATE,
        X_AMZ_SECURITY_TOKEN
    )
}
