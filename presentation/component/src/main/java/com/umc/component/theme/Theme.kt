package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = lightPrimary500,
    onPrimary = lightWhite,
    primaryContainer = lightPrimary100,
    onPrimaryContainer = lightPrimary900,
    secondary = lightAccent500,
    onSecondary = lightWhite,
    secondaryContainer = lightAccent100,
    onSecondaryContainer = lightAccent900,
    tertiary = lightSuccess500,
    onTertiary = lightWhite,
    tertiaryContainer = lightSuccess100,
    onTertiaryContainer = lightSuccess900,
    error = lightDanger500,
    onError = lightWhite,
    errorContainer = lightDanger100,
    onErrorContainer = lightDanger900,
    background = lightNeutral000,
    onBackground = lightNeutral900,
    surface = lightNeutral000,
    onSurface = lightNeutral900,
    surfaceVariant = lightNeutral100,
    onSurfaceVariant = lightNeutral700,
    outline = lightNeutral300,
)

private val DarkColors = darkColorScheme(
    primary = darkPrimary600,
    onPrimary = darkNeutral000,
    primaryContainer = darkPrimary200,
    onPrimaryContainer = darkPrimary900,
    secondary = darkAccent500,
    onSecondary = darkNeutral000,
    secondaryContainer = darkAccent200,
    onSecondaryContainer = darkAccent900,
    tertiary = darkSuccess500,
    onTertiary = darkNeutral000,
    tertiaryContainer = darkSuccess300,
    onTertiaryContainer = darkSuccess900,
    error = darkDanger500,
    onError = darkNeutral000,
    errorContainer = darkDanger300,
    onErrorContainer = darkDanger900,
    background = darkNeutral000,
    onBackground = darkNeutral900,
    surface = darkNeutral000,
    onSurface = darkNeutral900,
    surfaceVariant = darkNeutral100,
    onSurfaceVariant = darkNeutral700,
    outline = darkNeutral300,
)

@Composable
fun UmcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = UmcTypography,
        content = content,
    )
}
