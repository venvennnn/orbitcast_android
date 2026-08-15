package app.orbitcast.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.orbitcast.data.api.models.Episode
import app.orbitcast.data.api.models.Feed
import app.orbitcast.ui.components.LoadingState
import app.orbitcast.ui.components.MessageState
import app.orbitcast.ui.components.StageChip
import app.orbitcast.util.Format
import app.orbitcast.util.RssIntents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailScreen(
    viewModel: FeedDetailViewModel,
    onBack: () -> Unit,
    onEpisode: (String) -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.startPolling() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.stopPolling() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    val feed = state.detail?.feed
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(feed?.title ?: "Feed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        val rss = state.rssUrl
                        DropdownMenuItem(
                            text = { Text("Copy RSS") },
                            onClick = {
                                menuOpen = false
                                rss?.let { RssIntents.copy(context, it) }
                            },
                            enabled = rss != null,
                        )
                        DropdownMenuItem(
                            text = { Text("Open RSS") },
                            onClick = {
                                menuOpen = false
                                rss?.let { RssIntents.open(context, it) }
                            },
                            enabled = rss != null,
                        )
                        DropdownMenuItem(
                            text = { Text("Open in AntennaPod") },
                            onClick = {
                                menuOpen = false
                                rss?.let { RssIntents.openAntennaPod(context, it) }
                            },
                            enabled = rss != null,
                        )
                        DropdownMenuItem(
                            text = { Text("Run now") },
                            onClick = {
                                menuOpen = false
                                viewModel.runNow()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(if (feed?.active == false) "Resume" else "Pause") },
                            onClick = {
                                menuOpen = false
                                viewModel.setPaused(feed?.active != false)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null && state.detail == null -> MessageState(
                title = "Couldn't load this feed",
                body = state.error ?: "",
                actionLabel = "Try again",
                onAction = viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            state.detail != null -> {
                val detail = state.detail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { FeedHeader(detail.feed, state.rssUrl) }
                    if (detail.episodes.isEmpty()) {
                        item {
                            Text(
                                "No runs yet. Use Run now to queue the first episode.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(detail.episodes, key = { it.id }) { episode ->
                        TimelineRow(
                            episode = episode,
                            onClick = { onEpisode(episode.id) },
                            onRetry = { viewModel.retryEpisode(episode.id) },
                        )
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this feed?") },
            text = { Text("This is a destructive API call. The current backend may not support it yet.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun FeedHeader(feed: Feed, rssUrl: String?) {
    val context = LocalContext.current
    Column {
        Text(feed.topicPrompt, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            listOf(
                Format.cadence(feed.scheduleMinutes),
                if (feed.recapPrevious) "recap on" else "recap off",
                if (feed.active) "active" else "paused",
            ).joinToString("  ·  "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (rssUrl != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { RssIntents.openAntennaPod(context, rssUrl) }) {
                Icon(Icons.Outlined.Podcasts, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Hand RSS to a podcast app")
            }
        }
    }
}

@Composable
private fun TimelineRow(
    episode: Episode,
    onClick: () -> Unit,
    onRetry: () -> Unit,
) {
    val skip = episode.isSkip
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (skip) 0.72f else 1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (skip) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (skip) {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (skip) "Skipped" else (episode.title ?: "Untitled"),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                StageChip(episode.status, episode.stage)
            }
            Spacer(Modifier.height(6.dp))
            if (skip) {
                Text(
                    Format.oneLine(episode.skipReason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    listOfNotNull(
                        Format.relative(episode.createdAt),
                        episode.durationSeconds?.let { Format.duration(it) },
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (episode.isFailed && !episode.error.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    episode.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (episode.isFailed || skip) {
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}
