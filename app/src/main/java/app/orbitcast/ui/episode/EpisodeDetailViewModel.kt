package app.orbitcast.ui.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.orbitcast.data.api.models.Episode
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.ui.components.friendlyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EpisodeDetailUiState(
    val loading: Boolean = true,
    val episode: Episode? = null,
    val rssUrl: String? = null,
    val error: String? = null,
)

class EpisodeDetailViewModel(
    private val feedId: String,
    private val episodeId: String,
    private val feeds: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EpisodeDetailUiState())
    val state: StateFlow<EpisodeDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = _state.value.episode == null, error = null)
            runCatching {
                val detail = feeds.feed(feedId)
                val episode = detail.episodes.firstOrNull { it.id == episodeId }
                    ?: error("Episode not in this feed.")
                episode to feeds.rssUrl(detail.feed.slug)
            }.onSuccess { (episode, rss) ->
                _state.value = EpisodeDetailUiState(loading = false, episode = episode, rssUrl = rss)
            }.onFailure { err ->
                _state.value = _state.value.copy(loading = false, error = friendlyError(err))
            }
        }
    }
}
