package app.orbitcast.data.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun attachesBearerWhenTokenPresent() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor({ "secret-token" }, onUnauthorized = {}))
            .build()
        client.newCall(Request.Builder().url(server.url("/feeds")).build()).execute()
        val recorded = server.takeRequest()
        assertEquals("Bearer secret-token", recorded.getHeader("Authorization"))
        server.shutdown()
    }

    @Test
    fun omitsHeaderWhenTokenBlank() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor({ "  " }, onUnauthorized = {}))
            .build()
        client.newCall(Request.Builder().url(server.url("/feeds")).build()).execute()
        assertNull(server.takeRequest().getHeader("Authorization"))
        server.shutdown()
    }

    @Test
    fun unauthorizedClearsSession() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("no"))
        server.start()
        var cleared = false
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor({ "stale" }, onUnauthorized = { cleared = true }))
            .build()
        val response = client.newCall(Request.Builder().url(server.url("/feeds")).build()).execute()
        assertEquals(401, response.code)
        assertTrue(cleared)
        server.shutdown()
    }
}
