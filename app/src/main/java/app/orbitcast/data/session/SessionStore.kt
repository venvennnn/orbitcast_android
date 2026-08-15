package app.orbitcast.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import app.orbitcast.util.Urls
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * Non-secrets live in DataStore. The bearer token lives in an EncryptedFile
 * sealed by a Keystore-backed [MasterKey] (D3).
 */
class SessionStore(private val context: Context) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val tokenFile: File
        get() = File(context.filesDir, TOKEN_FILE)

    @Volatile
    private var cachedToken: String? = UNLOADED

    val apiBaseUrl: Flow<String> = context.settingsDataStore.data.map { prefs ->
        Urls.normalizeBase(prefs[KEY_API_BASE] ?: Urls.DEFAULT_API)
    }

    val episodesNotifications: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIF_EPISODES] ?: true
    }

    val skipsNotifications: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIF_SKIPS] ?: true
    }

    suspend fun setApiBaseUrl(url: String) {
        context.settingsDataStore.edit { it[KEY_API_BASE] = Urls.normalizeBase(url) }
    }

    suspend fun setEpisodesNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIF_EPISODES] = enabled }
    }

    suspend fun setSkipsNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIF_SKIPS] = enabled }
    }

    fun token(): String? {
        val cached = cachedToken
        if (cached !== UNLOADED) return cached
        val loaded = readTokenFile()
        cachedToken = loaded
        return loaded
    }

    @Synchronized
    fun setToken(value: String?) {
        val file = tokenFile
        if (file.exists()) {
            file.delete()
        }
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            cachedToken = null
            return
        }
        encrypted().openFileOutput().use { it.write(trimmed.toByteArray(Charsets.UTF_8)) }
        cachedToken = trimmed
    }

    @Synchronized
    private fun readTokenFile(): String? {
        val file = tokenFile
        if (!file.exists()) return null
        return try {
            encrypted().openFileInput().use { input ->
                input.readBytes().toString(Charsets.UTF_8).trim().ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encrypted(): EncryptedFile =
        EncryptedFile.Builder(
            context,
            tokenFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()

    companion object {
        private const val TOKEN_FILE = "token.enc"
        private val UNLOADED = String()
        private val KEY_API_BASE = stringPreferencesKey("api_base_url")
        private val KEY_NOTIF_EPISODES = booleanPreferencesKey("notif_episodes")
        private val KEY_NOTIF_SKIPS = booleanPreferencesKey("notif_skips")
    }
}
