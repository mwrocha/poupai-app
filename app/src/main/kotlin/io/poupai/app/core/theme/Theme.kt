package io.poupai.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    primary = Purple80,
    secondary = Purple60,
    tertiary = PurpleAccent,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
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
    onSurfaceVariant = Color(0xFF49454F), // ← adiciona essa linha
)

@Composable
fun PoupaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoupaiTypography,
        content = content,
    )
}
