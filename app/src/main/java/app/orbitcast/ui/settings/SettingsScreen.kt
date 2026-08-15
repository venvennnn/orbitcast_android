package app.orbitcast.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    lockBack: Boolean = false,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    if (!lockBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("API", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.apiBaseUrl,
                onValueChange = viewModel::onApiBaseUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Base URL") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = viewModel::saveBaseUrl, modifier = Modifier.fillMaxWidth()) {
                Text("Save URL")
            }
            Spacer(Modifier.height(20.dp))
            Text("Bearer token", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Stored in an EncryptedFile sealed by the Android Keystore. " +
                    "A 401 clears it and returns you here. The live API does not " +
                    "validate tokens yet (B1) — paste any value to prove the interceptor, " +
                    "or leave blank to call the open API.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.token,
                onValueChange = viewModel::onToken,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Token") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = viewModel::saveToken, modifier = Modifier.weight(1f)) {
                    Text("Save token")
                }
                Spacer(Modifier.padding(6.dp))
                OutlinedButton(onClick = viewModel::clearToken, modifier = Modifier.weight(1f)) {
                    Text("Clear")
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::testConnection,
                enabled = !state.testing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.testing) "Testing…" else "Test connection")
            }
            if (state.testResult != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.testResult ?: "", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Two channels, separately muteable. FCM emission is backend work (B7); " +
                    "toggles are stored so they survive that landing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                title = "Episodes",
                body = "A new episode published.",
                checked = state.episodesNotifications,
                onChecked = viewModel::setEpisodesNotifications,
            )
            ToggleRow(
                title = "Skips",
                body = "Nothing episode-worthy happened.",
                checked = state.skipsNotifications,
                onChecked = viewModel::setSkipsNotifications,
            )
            Spacer(Modifier.height(24.dp))
            Text("Build", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "OrbitCast ${state.versionName} (${state.versionCode}) — control plane, not a player.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    body: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
