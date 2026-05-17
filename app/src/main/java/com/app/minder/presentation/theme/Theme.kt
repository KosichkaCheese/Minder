package com.app.minder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = Background,
    surfaceContainer = ContainerBackground,
    onSurface = OnContainer,
    tertiary = Tertiary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onTertiary = onTertiary
)

@Composable
fun MedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}