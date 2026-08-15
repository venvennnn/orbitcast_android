package app.orbitcast.data.api

import okhttp3.Interceptor
import okhttp3.Response

fun interface TokenProvider {
    fun token(): String?
}

/**
 * Attaches `Authorization: Bearer <token>` when a token is stored.
 * A 401 clears the token (via [onUnauthorized]) and signals the UI.
 *
 * Never log the token. The request header is not copied into any message.
 */
class AuthInterceptor(
    private val tokens: TokenProvider,
    private val onUnauthorized: () -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokens.token()?.trim().orEmpty()
        val request = if (token.isEmpty()) {
            original
        } else {
            original.newBuilder()
                .header(HEADER, "$SCHEME $token")
                .build()
        }
        val response = chain.proceed(request)
        if (response.code == 401) {
            onUnauthorized()
        }
        return response
    }

    companion object {
        const val HEADER = "Authorization"
        const val SCHEME = "Bearer"
    }
}
