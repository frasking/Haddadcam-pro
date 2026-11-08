package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SurveillanceDarkColorScheme = darkColorScheme(
    primary = SyrianGreenLight,
    onPrimary = SurveillanceBgDark,
    primaryContainer = SyrianGreenDark,
    onPrimaryContainer = Color.White,
    secondary = SecurityShieldBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF034A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = AlertRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF670005),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = SurveillanceBgDark,
    onBackground = TextPrimaryDark,
    surface = SurveillanceSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurveillanceSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = SurveillanceSurfaceContainer,
    outline = SurveillanceBorder,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Surveillance monitors look best in tactical dark mode
    dynamicColor: Boolean = false, // Keep the signature Syrian green surveillance aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SurveillanceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
