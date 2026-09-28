package com.example.iotsimulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Custom Material 3 Light Scheme with Soft Warm Pastel Accents
private val PastelLightColorScheme = lightColorScheme(
    primary = SageTeal,
    onPrimary = CardWhite,
    primaryContainer = CardWhite,
    onPrimaryContainer = TextPrimaryDark,
    secondary = BlushPink,
    onSecondary = TextPrimaryDark,
    tertiary = GoldenYellow,
    background = BgCreamLight,
    onBackground = TextPrimaryDark,
    surface = CardWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSecondaryCream,
    onSurfaceVariant = TextSecondaryMuted,
    outline = CardBorder,
    error = CoralRed
)

@Composable
fun IOTSimulatorTheme(
    content: @Composable () -> Unit
) {
    // Force light theme and disable dynamic colors
    MaterialTheme(
        colorScheme = PastelLightColorScheme,
        typography = Typography,
        content = content
    )
}