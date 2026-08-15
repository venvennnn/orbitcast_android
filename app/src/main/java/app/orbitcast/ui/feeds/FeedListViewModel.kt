package app.orbitcast.ui.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.orbitcast.data.api.models.FeedSummary
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.ui.components.friendlyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedListUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val feeds: List<FeedSummary> = emptyList(),
    val error: String? = null,
)

class FeedListViewModel(
    private val feeds: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedListUiState())
    val state: StateFlow<FeedListUiState> = _state.asStateFlow()

    init {
        refresh(initial = true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = initial && it.feeds.isEmpty(),
                    refreshing = !initial || it.feeds.isNotEmpty(),
                    error = null,
                )
            }
            runCatching { feeds.feeds() }
                .onSuccess { list ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, feeds = list, error = null)
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, error = friendlyError(err))
                    }
                }
        }
    }
}
