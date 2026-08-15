package app.orbitcast

import android.app.Application
import app.orbitcast.notify.NotificationChannels

class OrbitCastApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.ensure(this)
    }
}
