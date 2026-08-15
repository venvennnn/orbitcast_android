package app.orbitcast.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.net.toUri

object RssIntents {
    fun copy(context: Context, rssUrl: String) {
        val clipboard = context.getSystemService<ClipboardManager>() ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("OrbitCast RSS", rssUrl))
        Toast.makeText(context, "RSS URL copied", Toast.LENGTH_SHORT).show()
    }

    /** Hand the public RSS URL to whatever can view it — typically a podcast app. */
    fun open(context: Context, rssUrl: String) {
        val https = Intent(Intent.ACTION_VIEW, rssUrl.toUri())
        try {
            context.startActivity(Intent.createChooser(https, "Open RSS"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No app can open this RSS URL", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAntennaPod(context: Context, rssUrl: String) {
        val bare = rssUrl.replace(Regex("^https?://", RegexOption.IGNORE_CASE), "")
        val intent = Intent(Intent.ACTION_VIEW, "pcast://$bare".toUri())
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            open(context, rssUrl)
        }
    }

    fun shareText(context: Context, text: String, title: String = "Share") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun openHttp(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Can't open link", Toast.LENGTH_SHORT).show()
        }
    }
}
