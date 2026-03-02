package com.app.minder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ButtonNeutral,
    background = Background,
    surfaceContainer = ContainerBackground,
    onSurface = OnContainer,
    tertiary = Mint,
    onPrimary = OnPrimary

)

@Composable
fun MedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = com.app.minder.presentation.theme.Typography,
//        typography = Typography.copy(
//            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
//                fontWeight = FontWeight.Normal
//            ),
//            titleLarge = MaterialTheme.typography.titleLarge.copy(
//                fontWeight = FontWeight.Medium
//            ),
//            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
//                fontWeight = FontWeight.Medium
//            ),
//            displayLarge = MaterialTheme.typography.displayLarge.copy(
//            )
//        ),
        content = content
    )
}