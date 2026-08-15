package app.orbitcast.ui.feeds

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.orbitcast.data.api.models.FeedSummary
import app.orbitcast.ui.components.LoadingState
import app.orbitcast.ui.components.MessageState
import app.orbitcast.ui.components.StatusDot
import app.orbitcast.util.Format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(
    viewModel: FeedListViewModel,
    onFeed: (String) -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OrbitCast") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(Icons.Outlined.Add, contentDescription = "Create feed")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading -> LoadingState()
                state.error != null && state.feeds.isEmpty() -> MessageState(
                    title = "Couldn't load feeds",
                    body = state.error ?: "",
                    actionLabel = "Try again",
                    onAction = { viewModel.refresh(initial = true) },
                )
                state.feeds.isEmpty() -> MessageState(
                    title = "No feeds yet",
                    body = "Create one here, or share an article into OrbitCast from another app.",
                    actionLabel = "New feed",
                    onAction = onCreate,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.error != null) {
                        item {
                            Text(
                                state.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    items(state.feeds, key = { it.id }) { feed ->
                        FeedCard(feed, onClick = { onFeed(feed.id) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FeedCard(feed: FeedSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    feed.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusDot(
                    status = feed.latestStatus,
                    stage = feed.latestStage,
                    active = feed.active,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                feed.topicPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                listOf(
                    Format.cadence(feed.scheduleMinutes),
                    "last ${Format.relative(feed.lastGeneratedAt)}",
                    Format.nextRun(feed.lastGeneratedAt, feed.scheduleMinutes, feed.active),
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
