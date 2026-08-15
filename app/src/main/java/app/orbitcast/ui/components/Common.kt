package app.orbitcast.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.orbitcast.ui.theme.Bad
import app.orbitcast.ui.theme.Ok
import app.orbitcast.ui.theme.Skip
import app.orbitcast.ui.theme.Warn

fun statusColor(status: String?, stage: String? = null, active: Boolean = true): Color {
    if (!active) return Skip
    val key = (stage ?: status).orEmpty().lowercase()
    return when (key) {
        "completed", "published" -> Ok
        "failed" -> Bad
        "skipped" -> Skip
        "queued", "researching", "writing", "voicing", "publishing", "processing" -> Warn
        else -> Skip
    }
}

fun stageLabel(status: String?, stage: String?): String {
    if (status.equals("completed", ignoreCase = true)) return "published"
    return (stage ?: status ?: "—").lowercase()
}

@Composable
fun StatusDot(
    status: String?,
    stage: String? = null,
    active: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val color = statusColor(status, stage, active)
    val inFlight = active && (
        status.equals("queued", true) ||
            status.equals("processing", true) ||
            stage in listOf("researching", "writing", "voicing", "publishing", "queued")
        )
    val alpha = if (inFlight) {
        val t = rememberInfiniteTransition(label = "dot")
        val a by t.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "dotAlpha",
        )
        a
    } else {
        1f
    }
    Box(
        modifier
            .size(10.dp)
            .alpha(alpha)
            .background(color, CircleShape),
    )
}

@Composable
fun StageChip(status: String?, stage: String?, modifier: Modifier = Modifier) {
    val color = statusColor(status, stage)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = stageLabel(status, stage),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageState(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

fun friendlyError(throwable: Throwable): String {
    val raw = throwable.message.orEmpty()
    return when {
        raw.contains("Unable to resolve host", ignoreCase = true) ->
            "Can't reach the API. Check the base URL and your connection."
        raw.contains("timeout", ignoreCase = true) ->
            "The API timed out. Pull to try again."
        raw.contains("401") || raw.contains("Unauthorized", ignoreCase = true) ->
            "The API rejected the token. Paste a new one in Settings."
        raw.contains("404") ->
            "The API doesn't know that resource. It may not be implemented yet."
        raw.contains("405") ->
            "This action isn't on the API yet."
        raw.isBlank() -> "Something went wrong talking to the API."
        else -> raw
    }
}
