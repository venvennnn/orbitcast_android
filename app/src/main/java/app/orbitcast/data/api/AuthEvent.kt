package app.orbitcast.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide 401 signal. The interceptor emits; navigation collects
 * and returns the user to the token screen.
 */
class AuthEvent {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    fun emitUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
