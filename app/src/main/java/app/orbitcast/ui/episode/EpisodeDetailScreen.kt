package app.orbitcast.ui.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.orbitcast.ui.components.LoadingState
import app.orbitcast.ui.components.MessageState
import app.orbitcast.ui.components.StageChip
import app.orbitcast.util.Format
import app.orbitcast.util.RssIntents
import app.orbitcast.util.Urls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    viewModel: EpisodeDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val episode = state.episode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(episode?.title ?: "Episode") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val share = episode?.audioUrl ?: state.rssUrl ?: return@IconButton
                            RssIntents.shareText(context, share, "Share episode")
                        },
                        enabled = episode != null,
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null && episode == null -> MessageState(
                title = "Couldn't load episode",
                body = state.error ?: "",
                actionLabel = "Try again",
                onAction = viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            episode != null -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { StageChip(episode.status, episode.stage) }
                item {
                    MetaLine(
                        "Generated",
                        Format.absolute(episode.createdAt),
                    )
                }
                item { MetaLine("Duration", Format.duration(episode.durationSeconds)) }
                item { MetaLine("File size", Format.fileSize(episode.audioBytes)) }
                if (episode.isSkip) {
                    item {
                        Section("Skip reason")
                        Text(
                            episode.skipReason ?: "Nothing episode-worthy this run.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                if (episode.isFailed && !episode.error.isNullOrBlank()) {
                    item {
                        Section("Error")
                        Text(
                            episode.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (!episode.description.isNullOrBlank() && !episode.isSkip) {
                    item {
                        Section("Show notes")
                        Text(episode.description ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                val links = Urls.httpUrlsIn(
                    listOfNotNull(episode.description, episode.script).joinToString("\n"),
                )
                if (links.isNotEmpty()) {
                    item {
                        Section("Cited sources")
                        Column {
                            links.forEach { url ->
                                TextButton(onClick = { RssIntents.openHttp(context, url) }) {
                                    Text(url)
                                }
                            }
                        }
                    }
                }
                if (!episode.script.isNullOrBlank()) {
                    item {
                        Section("Script")
                        Text(episode.script ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Text(
        "$label  ·  $value",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
