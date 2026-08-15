package app.orbitcast.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.orbitcast.data.api.models.CreateFeedBody
import app.orbitcast.data.api.models.PatchFeedBody
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.ui.components.friendlyError
import app.orbitcast.util.Format
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedEditorUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val isEdit: Boolean = false,
    val title: String = "",
    val prompt: String = "",
    val scheduleMinutes: Int = 60,
    val recapPrevious: Boolean = true,
    val titleError: String? = null,
    val promptError: String? = null,
    val error: String? = null,
    val savedFeedId: String? = null,
)

class FeedEditorViewModel(
    private val feedId: String?,
    seed: String?,
    private val feeds: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        FeedEditorUiState(
            isEdit = feedId != null,
            prompt = seed.orEmpty(),
            title = if (seed.isNullOrBlank()) "" else Format.titleFromPrompt(seed),
        ),
    )
    val state: StateFlow<FeedEditorUiState> = _state.asStateFlow()

    init {
        if (feedId != null) load(feedId)
    }

    fun onTitle(value: String) = _state.update { it.copy(title = value, titleError = null) }
    fun onPrompt(value: String) = _state.update { it.copy(prompt = value, promptError = null) }
    fun onCadence(minutes: Int) = _state.update { it.copy(scheduleMinutes = minutes) }
    fun onRecap(value: Boolean) = _state.update { it.copy(recapPrevious = value) }

    fun save() {
        val current = _state.value
        val title = current.title.trim().ifBlank { Format.titleFromPrompt(current.prompt) }
        val prompt = current.prompt.trim()
        val titleError = if (title.isBlank()) "Give the feed a title." else null
        val promptError = if (prompt.isBlank()) "The topic prompt is the feed." else null
        if (titleError != null || promptError != null) {
            _state.update { it.copy(titleError = titleError, promptError = promptError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, title = title) }
            val result = if (feedId == null) {
                runCatching {
                    feeds.create(
                        CreateFeedBody(
                            title = title,
                            topicPrompt = prompt,
                            scheduleMinutes = current.scheduleMinutes,
                            recapPrevious = current.recapPrevious,
                        ),
                    ).id
                }
            } else {
                runCatching {
                    feeds.patch(
                        feedId,
                        PatchFeedBody(
                            title = title,
                            topicPrompt = prompt,
                            scheduleMinutes = current.scheduleMinutes,
                            recapPrevious = current.recapPrevious,
                        ),
                    )
                    feedId
                }
            }
            result.onSuccess { id ->
                _state.update { it.copy(saving = false, savedFeedId = id) }
            }.onFailure { err ->
                val message = if (feedId != null) {
                    "Edits aren't on the API yet (B5). Create a new feed instead, or use the web dashboard."
                } else {
                    friendlyError(err)
                }
                _state.update { it.copy(saving = false, error = message) }
            }
        }
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching { feeds.feed(id).feed }
                .onSuccess { feed ->
                    _state.update {
                        it.copy(
                            loading = false,
                            title = feed.title,
                            prompt = feed.topicPrompt,
                            scheduleMinutes = feed.scheduleMinutes,
                            recapPrevious = feed.recapPrevious,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(loading = false, error = friendlyError(err)) }
                }
        }
    }
}
