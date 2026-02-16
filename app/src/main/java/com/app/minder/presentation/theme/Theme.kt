package com.app.minder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val LightColorScheme = lightColorScheme(
    primary = ButtonNeutral,
    onPrimary = Background,
    background = Background,
    surfaceContainer = ContainerBackground,
    onSurface = OnContainer

)

@Composable
fun MedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium
            )
        ),
        content = content
    )
}