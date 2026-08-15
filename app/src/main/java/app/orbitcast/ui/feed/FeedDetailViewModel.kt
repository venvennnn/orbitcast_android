package app.orbitcast.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.orbitcast.data.api.models.FeedDetail
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.ui.components.friendlyError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FeedDetailUiState(
    val loading: Boolean = true,
    val detail: FeedDetail? = null,
    val rssUrl: String? = null,
    val error: String? = null,
    val busy: Boolean = false,
    val deleted: Boolean = false,
)

class FeedDetailViewModel(
    private val feedId: String,
    private val feeds: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedDetailUiState())
    val state: StateFlow<FeedDetailUiState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var pollJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(3_000)
                load(quiet = true)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun runNow() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { feeds.refresh(feedId) }
                .onSuccess {
                    _messages.tryEmit("Run queued — watch the stage chips.")
                    load()
                }
                .onFailure { _messages.tryEmit(friendlyError(it)) }
            _state.update { it.copy(busy = false) }
        }
    }

    fun retryEpisode(episodeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { feeds.retry(episodeId) }
                .onSuccess {
                    _messages.tryEmit("Retry queued.")
                    load()
                }
                .onFailure { _messages.tryEmit(friendlyError(it)) }
            _state.update { it.copy(busy = false) }
        }
    }

    fun setPaused(paused: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                feeds.patch(feedId, app.orbitcast.data.api.models.PatchFeedBody(active = !paused))
            }.onSuccess {
                load()
            }.onFailure {
                _messages.tryEmit(
                    "Pause/resume isn't on the API yet (B5). The feed is unchanged.",
                )
            }
            _state.update { it.copy(busy = false) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { feeds.delete(feedId) }
                .onSuccess { _state.update { it.copy(deleted = true, busy = false) } }
                .onFailure {
                    _messages.tryEmit(
                        "Delete isn't on the API yet (B5). The feed is unchanged.",
                    )
                    _state.update { it.copy(busy = false) }
                }
        }
    }

    private suspend fun load(quiet: Boolean = false) {
        if (!quiet) _state.update { it.copy(loading = it.detail == null, error = null) }
        runCatching {
            val detail = feeds.feed(feedId)
            val rss = feeds.rssUrl(detail.feed.slug)
            detail to rss
        }.onSuccess { (detail, rss) ->
            _state.update {
                it.copy(loading = false, detail = detail, rssUrl = rss, error = null)
            }
        }.onFailure { err ->
            if (!quiet) {
                _state.update { it.copy(loading = false, error = friendlyError(err)) }
            }
        }
    }
}
