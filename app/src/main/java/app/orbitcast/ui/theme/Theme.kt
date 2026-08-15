package app.orbitcast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Sky,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6F8),
    onPrimaryContainer = Ink,
    secondary = Ink80,
    onSecondary = Color.White,
    background = Ice,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8F0F7),
    onSurfaceVariant = Color(0xFF3E5366),
    outline = Mist,
    error = Bad,
)

private val DarkColors = darkColorScheme(
    primary = SkySoft,
    onPrimary = Ink,
    primaryContainer = Color(0xFF1B3A5A),
    onPrimaryContainer = SkySoft,
    secondary = SkySoft,
    onSecondary = Ink,
    background = Color(0xFF07131E),
    onBackground = Color(0xFFE6EEF6),
    surface = Color(0xFF0F2132),
    onSurface = Color(0xFFE6EEF6),
    surfaceVariant = Color(0xFF1A3146),
    onSurfaceVariant = Color(0xFFB7C7D6),
    outline = Color(0xFF3A5166),
    error = Color(0xFFFF8A80),
)

@Composable
fun OrbitCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
