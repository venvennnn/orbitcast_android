package app.orbitcast

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.orbitcast.ui.nav.OrbitCastNav
import app.orbitcast.ui.theme.OrbitCastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            OrbitCastTheme {
                OrbitCastNav()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val container = (application as OrbitCastApp).container
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
                if (!text.isNullOrBlank()) container.offerShareSeed(text)
            }
            Intent.ACTION_VIEW -> {
                val data = intent.data ?: return
                if (data.scheme == "orbitcast" && data.host == "feeds") {
                    val id = data.pathSegments.firstOrNull()
                    if (!id.isNullOrBlank()) container.offerDeepLinkFeed(id)
                }
            }
        }
    }
}
