package app.orbitcast.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.orbitcast.BuildConfig
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.data.session.SessionStore
import app.orbitcast.ui.components.friendlyError
import app.orbitcast.util.Urls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiBaseUrl: String = Urls.DEFAULT_API,
    val token: String = "",
    val tokenPresent: Boolean = false,
    val episodesNotifications: Boolean = true,
    val skipsNotifications: Boolean = true,
    val testing: Boolean = false,
    val testResult: String? = null,
    val versionName: String = BuildConfig.VERSION_NAME,
    val versionCode: Int = BuildConfig.VERSION_CODE,
)

class SettingsViewModel(
    private val session: SessionStore,
    private val feeds: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        SettingsUiState(
            token = session.token().orEmpty(),
            tokenPresent = !session.token().isNullOrBlank(),
        ),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            session.apiBaseUrl.collect { url ->
                _state.update { it.copy(apiBaseUrl = url) }
            }
        }
        viewModelScope.launch {
            session.episodesNotifications.collect { on ->
                _state.update { it.copy(episodesNotifications = on) }
            }
        }
        viewModelScope.launch {
            session.skipsNotifications.collect { on ->
                _state.update { it.copy(skipsNotifications = on) }
            }
        }
    }

    fun onApiBaseUrl(value: String) = _state.update { it.copy(apiBaseUrl = value, testResult = null) }
    fun onToken(value: String) = _state.update { it.copy(token = value, testResult = null) }

    fun saveBaseUrl() {
        viewModelScope.launch { session.setApiBaseUrl(_state.value.apiBaseUrl) }
    }

    fun saveToken() {
        session.setToken(_state.value.token)
        _state.update { it.copy(tokenPresent = session.token() != null, testResult = "Token saved.") }
    }

    fun clearToken() {
        session.setToken(null)
        _state.update { it.copy(token = "", tokenPresent = false, testResult = "Token cleared.") }
    }

    fun setEpisodesNotifications(on: Boolean) {
        viewModelScope.launch { session.setEpisodesNotifications(on) }
    }

    fun setSkipsNotifications(on: Boolean) {
        viewModelScope.launch { session.setSkipsNotifications(on) }
    }

    fun testConnection() {
        viewModelScope.launch {
            session.setApiBaseUrl(_state.value.apiBaseUrl)
            session.setToken(_state.value.token.ifBlank { session.token() })
            _state.update { it.copy(testing = true, testResult = null, tokenPresent = session.token() != null) }
            runCatching {
                val health = feeds.health()
                val list = feeds.feeds()
                if (!health.ok) error(health.error ?: "Health check failed.")
                "Interceptor ok — ${list.size} feeds, queue ${health.queueDepth ?: 0}."
            }.onSuccess { msg ->
                _state.update { it.copy(testing = false, testResult = msg) }
            }.onFailure { err ->
                _state.update { it.copy(testing = false, testResult = friendlyError(err)) }
            }
        }
    }
}
