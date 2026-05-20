package io.poupai.app.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tokens semânticos do Poupaí.
 *
 * Em vez de hard-coding `Color(0xFFF5F5F7)` para fundo ou `Color.White` para card,
 * cada tela referencia `PoupaiTheme.tokens.bg`, `PoupaiTheme.tokens.surface`, etc.
 * Os valores trocam automaticamente entre light/dark.
 *
 * Tons de "destaque sobre gradiente roxo" (como texto em cima do header) seguem
 * sendo `Color.White` literal — eles são intencionalmente fixos.
 */
@Immutable
data class PoupaiTokens(
    val bg: Color,
    val surface: Color,
    val surfaceAlt: Color,         // categorias, chips de fundo claro, segmented bg
    val surfaceSunken: Color,      // bg de barras (categorias, progresso, etc.)
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val accentBright: Color,       // chips selecionados, ícones de seção
    val accentSoft: Color,         // ícones em chip suave (PurpleLight no light)
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val isDark: Boolean,
) {
    val heroGradient: Brush
        get() = Brush.linearGradient(listOf(heroStart, heroMid, heroEnd))

    val headerGradient: Brush
        get() = Brush.verticalGradient(listOf(heroStart, heroMid))
}

internal val LightTokens = PoupaiTokens(
    bg = Color(0xFFF5F5F7),
    surface = Color.White,
    surfaceAlt = Color(0xFFF5F5F5),
    surfaceSunken = Color(0xFFF0F0F0),
    textPrimary = Color(0xFF1C1B1F),
    textSecondary = Color(0xFF6B6B6B),
    textMuted = Color(0xFF9E9E9E),
    divider = Color(0xFFEDEAF2),
    accentBright = Purple40,
    accentSoft = PurpleLight,
    heroStart = PurpleDark,
    heroMid = Purple40,
    heroEnd = Color(0xFF6B4396),
    isDark = false,
)

internal val DarkTokens = PoupaiTokens(
    // Fundo deep purple-slate (inspirado em apps fintech modernos)
    bg = Color(0xFF221C2E),
    surface = Color(0xFF2E2740),
    surfaceAlt = Color(0xFF3A3350),
    surfaceSunken = Color(0xFF1A1525),
    textPrimary = Color(0xFFF2EEFB),
    textSecondary = Color(0xFFB8B0CC),
    textMuted = Color(0xFF7E7590),
    divider = Color(0xFF3A3350),
    // No dark, Purple60 (lavanda) é mais legível como accent
    accentBright = Purple60,
    accentSoft = Color(0xFF4A3F6B),
    // Hero card no dark: gradiente roxo um pouco mais vibrante p/ destacar
    heroStart = Color(0xFF3B1F6E),
    heroMid = Color(0xFF5A3C8F),
    heroEnd = Color(0xFF7C5AB5),
    isDark = true,
)

val LocalPoupaiTokens = staticCompositionLocalOf { LightTokens }

object PoupaiTheme {
    val tokens: PoupaiTokens
        @Composable @ReadOnlyComposable
        get() = LocalPoupaiTokens.current
}
