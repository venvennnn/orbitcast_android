package app.orbitcast.data.api

import app.orbitcast.util.Urls
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(
    private val tokens: TokenProvider,
    private val authEvent: AuthEvent,
    private val onUnauthorized: () -> Unit,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Volatile
    private var cachedBase: String? = null

    @Volatile
    private var cachedApi: OrbitCastApi? = null

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                AuthInterceptor(
                    tokens = tokens,
                    onUnauthorized = {
                        onUnauthorized()
                        authEvent.emitUnauthorized()
                    },
                ),
            )
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                // Method / path / status only — never headers (token leakage).
                android.util.Log.d(
                    "OrbitCast.http",
                    "${request.method} ${request.url.encodedPath} → ${response.code}",
                )
                response
            }
            .build()
    }

    fun api(baseUrl: String): OrbitCastApi {
        val normalized = Urls.normalizeBase(baseUrl) + "/"
        val existing = cachedApi
        if (existing != null && cachedBase == normalized) return existing
        val created = Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OrbitCastApi::class.java)
        cachedBase = normalized
        cachedApi = created
        return created
    }
}
