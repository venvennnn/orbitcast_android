package app.orbitcast.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * M5 channels. FCM emission is backend work (B7); the client still
 * registers the two toggleable channels so a later google-services.json
 * can land without another architecture pass.
 */
object NotificationChannels {
    const val EPISODES = "episodes"
    const val SKIPS = "skips"

    fun ensure(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                EPISODES,
                "Episodes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "A new episode published."
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                SKIPS,
                "Skips",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Nothing episode-worthy happened this run."
            },
        )
    }
}
