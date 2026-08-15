package app.orbitcast.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlsTest {
    @Test
    fun rssAndNormalize() {
        assertEquals(
            "https://api-13d-8000.sea1.zerops.app/feed/cricket-3ed67d.xml",
            Urls.rss("https://api-13d-8000.sea1.zerops.app/", "cricket-3ed67d"),
        )
        assertEquals(Urls.DEFAULT_API, Urls.normalizeBase("   "))
    }

    @Test
    fun extractsHttpUrls() {
        val text = "See https://example.com/a and also http://foo.test/b."
        assertEquals(
            listOf("https://example.com/a", "http://foo.test/b"),
            Urls.httpUrlsIn(text),
        )
    }
}
