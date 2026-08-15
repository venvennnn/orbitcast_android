package app.orbitcast.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.orbitcast.ui.components.LoadingState
import app.orbitcast.util.Format

private val CadencePresets = listOf(
    30 to "30m",
    60 to "1h",
    360 to "6h",
    720 to "12h",
    1_440 to "1d",
    10_080 to "1w",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEditorScreen(
    viewModel: FeedEditorViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedFeedId) {
        state.savedFeedId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit feed" else "New feed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.prompt,
                onValueChange = viewModel::onPrompt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("Topic prompt") },
                isError = state.promptError != null,
                supportingText = {
                    Text(state.promptError ?: "This is the standing brief the pipeline researches against.")
                },
            )
            Spacer(Modifier.height(16.dp))
            Text("Cadence", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CadencePresets.forEach { (minutes, label) ->
                    FilterChip(
                        selected = state.scheduleMinutes == minutes,
                        onClick = { viewModel.onCadence(minutes) },
                        label = { Text(label) },
                    )
                }
            }
            Text(
                Format.cadence(state.scheduleMinutes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(16.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Recap previous episode", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Open each new episode with a short spoken recap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = state.recapPrevious, onCheckedChange = viewModel::onRecap)
            }
            if (state.isEdit) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Edits apply to the next scheduled run. They do not regenerate the latest episode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.saving) "Saving…" else if (state.isEdit) "Save changes" else "Create feed")
            }
        }
    }
}
