package app.orbitcast

import android.content.Context
import app.orbitcast.data.api.ApiFactory
import app.orbitcast.data.api.AuthEvent
import app.orbitcast.data.repo.FeedRepository
import app.orbitcast.data.session.SessionStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppContainer(context: Context) {
    val session = SessionStore(context.applicationContext)
    val authEvent = AuthEvent()
    val apiFactory = ApiFactory(
        tokens = { session.token() },
        authEvent = authEvent,
        onUnauthorized = { session.setToken(null) },
    )
    val feeds = FeedRepository(apiFactory, session)

    private val _shareSeeds = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val shareSeeds: SharedFlow<String> = _shareSeeds.asSharedFlow()

    private val _deepLinks = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deepLinks: SharedFlow<String> = _deepLinks.asSharedFlow()

    fun offerShareSeed(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) _shareSeeds.tryEmit(trimmed)
    }

    fun offerDeepLinkFeed(feedId: String) {
        val trimmed = feedId.trim()
        if (trimmed.isNotEmpty()) _deepLinks.tryEmit(trimmed)
    }
}
