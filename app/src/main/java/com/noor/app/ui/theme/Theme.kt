package com.noor.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Gold,
    onSecondary = InkText,
    background = MintSurface,
    onBackground = InkText,
    surface = CardGreen,
    onSurface = InkText,
    surfaceVariant = CardGreen,
    primaryContainer = DeepGreen,
    onPrimaryContainer = SoftGold
)

private val DarkColors = darkColorScheme(
    primary = SageGreen,
    onPrimary = DeepGreen,
    secondary = Gold,
    onSecondary = InkText,
    background = DeepGreen,
    onBackground = MintSurface,
    surface = Emerald,
    onSurface = MintSurface,
    primaryContainer = Emerald,
    onPrimaryContainer = SoftGold
)

@Composable
fun NoorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
