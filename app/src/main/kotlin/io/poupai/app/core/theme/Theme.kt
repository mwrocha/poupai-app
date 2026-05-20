package io.poupai.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// ─── Paleta Poupaí (extraída do protótipo) ───
val Purple80 = Color(0xFF513174)
val Purple60 = Color(0xFF9B7FD4)
val Purple40 = Color(0xFF503173)
val Purple20 = Color(0xFF3D2472)

val PurpleLight = Color(0xFFE8DEF8)
val PurpleAccent = Color(0xFF7C4DFF)
val PurpleDark = Color(0xFF381E72)

val GrayLight = Color(0xFFF5F5F5)
val GrayMedium = Color(0xFFBDBDBD)
val GrayDark = Color(0xFF424242)

val GreenPositive = Color(0xFF4CAF50)
val RedNegative = Color(0xFFE53935)

// ─── Color Schemes ───
private val DarkColorScheme = darkColorScheme(
    primary = Purple60,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A3F6B),
    onPrimaryContainer = Color(0xFFF2EEFB),
    secondary = Purple60,
    onSecondary = Color.White,
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = Color(0xFF221C2E),
    onBackground = Color(0xFFF2EEFB),
    surface = Color(0xFF2E2740),
    onSurface = Color(0xFFF2EEFB),
    surfaceVariant = Color(0xFF3A3350),
    onSurfaceVariant = Color(0xFFB8B0CC),
    outline = Color(0xFF4A3F6B),
    outlineVariant = Color(0xFF3A3350),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = Purple60,
    tertiary = PurpleAccent,
    background = Color.White,
    surface = GrayLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
)

@Composable
fun PoupaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val tokens = if (darkTheme) DarkTokens else LightTokens

    CompositionLocalProvider(LocalPoupaiTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PoupaiTypography,
            content = content,
        )
    }
}
