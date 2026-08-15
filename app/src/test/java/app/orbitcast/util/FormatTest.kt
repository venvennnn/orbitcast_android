package app.orbitcast.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FormatTest {
    private val now = OffsetDateTime.of(2026, 8, 15, 12, 0, 0, 0, ZoneOffset.UTC)

    @Test
    fun cadencePresets() {
        assertEquals("every 30m", Format.cadence(30))
        assertEquals("hourly", Format.cadence(60))
        assertEquals("every 6h", Format.cadence(360))
        assertEquals("daily", Format.cadence(1_440))
        assertEquals("weekly", Format.cadence(10_080))
    }

    @Test
    fun relativePastAndFuture() {
        assertEquals("3 hr ago", Format.relative("2026-08-15T09:00:00Z", now))
        assertEquals("in 2 hr", Format.relative("2026-08-15T14:00:00Z", now))
        assertEquals("just now", Format.relative("2026-08-15T12:00:20Z", now))
    }

    @Test
    fun nextRunDueAndPaused() {
        assertEquals("paused", Format.nextRun("2026-08-15T10:00:00Z", 60, active = false, now))
        assertEquals("due now", Format.nextRun("2026-08-15T10:00:00Z", 60, active = true, now))
        assertEquals("in 1 hr", Format.nextRun("2026-08-15T11:30:00Z", 90, active = true, now))
    }

    @Test
    fun durationAndSize() {
        assertEquals("2:22", Format.duration(142))
        assertEquals("—", Format.duration(null))
        assertEquals("830.7 KB", Format.fileSize(850_608))
        assertEquals("1.0 MB", Format.fileSize(1_048_576))
    }

    @Test
    fun skipReasonOneLiner() {
        val long = "Nothing changed. A second sentence should be dropped."
        assertEquals("Nothing changed.", Format.oneLine(long))
        assertEquals("Nothing episode-worthy this run.", Format.oneLine("  "))
        val huge = "A".repeat(200)
        assertTrue(Format.oneLine(huge).endsWith("…"))
        assertTrue(Format.oneLine(huge).length <= 140)
    }

    @Test
    fun titleFromPrompt() {
        assertEquals("Untitled feed", Format.titleFromPrompt("  \n  "))
        assertEquals("Weekend tech news", Format.titleFromPrompt("Weekend tech news\nMore detail"))
    }

    @Test
    fun parsesApiTimestamps() {
        val parsed = Format.parseTime("2026-08-13T20:15:27.702320+00:00")
        assertEquals(2026, parsed?.year)
        assertEquals(13, parsed?.dayOfMonth)
    }
}
