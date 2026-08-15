package app.orbitcast.util

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object Format {
    private val displayStamp: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

    fun parseTime(raw: String?): OffsetDateTime? {
        if (raw.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(raw)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    fun relative(raw: String?, now: OffsetDateTime = OffsetDateTime.now()): String {
        val then = parseTime(raw) ?: return "—"
        val seconds = Duration.between(then, now).seconds
        val future = seconds < 0
        val abs = kotlin.math.abs(seconds)
        val label = when {
            abs < 45 -> "just now"
            abs < 90 -> "1 min"
            abs < 3_600 -> "${abs / 60} min"
            abs < 5_400 -> "1 hr"
            abs < 86_400 -> "${abs / 3_600} hr"
            abs < 172_800 -> "1 day"
            abs < 2_592_000 -> "${abs / 86_400} days"
            else -> "${abs / 2_592_000} mo"
        }
        return when {
            label == "just now" -> label
            future -> "in $label"
            else -> "$label ago"
        }
    }

    fun absolute(raw: String?): String {
        val then = parseTime(raw) ?: return "—"
        return then.format(displayStamp)
    }

    fun cadence(scheduleMinutes: Int): String = when {
        scheduleMinutes < 60 -> "every ${scheduleMinutes}m"
        scheduleMinutes % 10_080 == 0 -> {
            val weeks = scheduleMinutes / 10_080
            if (weeks == 1) "weekly" else "every ${weeks}w"
        }
        scheduleMinutes % 1_440 == 0 -> {
            val days = scheduleMinutes / 1_440
            if (days == 1) "daily" else "every ${days}d"
        }
        scheduleMinutes % 60 == 0 -> {
            val hours = scheduleMinutes / 60
            if (hours == 1) "hourly" else "every ${hours}h"
        }
        else -> "every ${scheduleMinutes}m"
    }

    fun nextRun(
        lastGeneratedAt: String?,
        scheduleMinutes: Int,
        active: Boolean,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): String {
        if (!active) return "paused"
        val last = parseTime(lastGeneratedAt) ?: return "unscheduled"
        val next = last.plusMinutes(scheduleMinutes.toLong())
        return if (next.isBefore(now)) "due now" else relative(next.toString(), now)
    }

    fun duration(seconds: Int?): String {
        if (seconds == null) return "—"
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    fun fileSize(bytes: Long?): String {
        if (bytes == null) return "—"
        if (bytes < 1_024) return "$bytes B"
        val kb = bytes / 1_024.0
        if (kb < 1_024) return "%.1f KB".format(Locale.US, kb)
        return "%.1f MB".format(Locale.US, kb / 1_024.0)
    }

    /** First sentence, trimmed, for the skip-reason one-liner. */
    fun oneLine(text: String?, maxChars: Int = 140): String {
        val raw = text?.trim().orEmpty()
        if (raw.isEmpty()) return "Nothing episode-worthy this run."
        val sentence = raw.split(Regex("(?<=[.!?])\\s+"), limit = 2).first()
        return if (sentence.length <= maxChars) sentence else sentence.take(maxChars - 1).trimEnd() + "…"
    }

    fun titleFromPrompt(prompt: String): String {
        val first = prompt.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        if (first.isEmpty()) return "Untitled feed"
        return if (first.length <= 48) first else first.take(47).trimEnd() + "…"
    }
}
