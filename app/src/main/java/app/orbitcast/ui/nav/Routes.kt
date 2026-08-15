package app.orbitcast.ui.nav

import kotlinx.serialization.Serializable

@Serializable
data object FeedsRoute

@Serializable
data class FeedRoute(val feedId: String)

@Serializable
data class EpisodeRoute(val feedId: String, val episodeId: String)

@Serializable
data class EditorRoute(val feedId: String? = null, val seed: String? = null)

@Serializable
data object SettingsRoute
