package app.orbitcast.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Health(
    val ok: Boolean = false,
    val db: Boolean? = null,
    val error: String? = null,
    @SerialName("queue_depth") val queueDepth: Int? = null,
    @SerialName("public_api_url") val publicApiUrl: String? = null,
)

@Serializable
data class FeedSummary(
    val id: String,
    val title: String,
    @SerialName("topic_prompt") val topicPrompt: String = "",
    val slug: String,
    @SerialName("schedule_minutes") val scheduleMinutes: Int = 60,
    val active: Boolean = true,
    @SerialName("last_generated_at") val lastGeneratedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("recap_previous") val recapPrevious: Boolean = true,
    @SerialName("episode_count") val episodeCount: Int = 0,
    @SerialName("completed_count") val completedCount: Int = 0,
    @SerialName("latest_status") val latestStatus: String? = null,
    @SerialName("latest_stage") val latestStage: String? = null,
    @SerialName("latest_error") val latestError: String? = null,
    @SerialName("latest_episode_id") val latestEpisodeId: String? = null,
)

@Serializable
data class Feed(
    val id: String,
    val title: String,
    @SerialName("topic_prompt") val topicPrompt: String = "",
    val slug: String,
    @SerialName("schedule_minutes") val scheduleMinutes: Int = 60,
    val active: Boolean = true,
    @SerialName("last_generated_at") val lastGeneratedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("recap_previous") val recapPrevious: Boolean = true,
)

@Serializable
data class FeedDetail(
    val feed: Feed,
    val episodes: List<Episode> = emptyList(),
)

@Serializable
data class Episode(
    val id: String,
    @SerialName("feed_id") val feedId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val script: String? = null,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("audio_bytes") val audioBytes: Long? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val status: String = "queued",
    val stage: String? = null,
    val error: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val isSkip: Boolean get() = status.equals("skipped", ignoreCase = true)
    val isFailed: Boolean get() = status.equals("failed", ignoreCase = true)
    val isInFlight: Boolean
        get() = status.equals("queued", ignoreCase = true) ||
            status.equals("processing", ignoreCase = true)

    val skipReason: String? get() = if (isSkip) error ?: description else null
}

@Serializable
data class CreateFeedBody(
    val title: String,
    @SerialName("topic_prompt") val topicPrompt: String,
    @SerialName("schedule_minutes") val scheduleMinutes: Int = 60,
    @SerialName("recap_previous") val recapPrevious: Boolean = true,
)

@Serializable
data class PatchFeedBody(
    val title: String? = null,
    @SerialName("topic_prompt") val topicPrompt: String? = null,
    @SerialName("schedule_minutes") val scheduleMinutes: Int? = null,
    @SerialName("recap_previous") val recapPrevious: Boolean? = null,
    val active: Boolean? = null,
)
