package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = lightMain500,
    onPrimary = lightWhite,
    primaryContainer = lightMain100,
    onPrimaryContainer = lightMain900,
    // accent 팔레트 제거로 yellow(구 warning)로 대체
    secondary = lightYellow500,
    onSecondary = lightWhite,
    secondaryContainer = lightYellow100,
    onSecondaryContainer = lightYellow900,
    tertiary = lightGreen500,
    onTertiary = lightWhite,
    tertiaryContainer = lightGreen100,
    onTertiaryContainer = lightGreen900,
    error = lightRed500,
    onError = lightWhite,
    errorContainer = lightRed100,
    onErrorContainer = lightRed900,
    background = lightGrey50,
    onBackground = lightGrey900,
    surface = lightGrey50,
    onSurface = lightGrey900,
    surfaceVariant = lightGrey100,
    onSurfaceVariant = lightGrey700,
    outline = lightGrey300,
)

private val DarkColors = darkColorScheme(
    primary = darkMain600,
    onPrimary = darkGrey50,
    primaryContainer = darkMain200,
    onPrimaryContainer = darkMain900,
    // accent 팔레트 제거로 yellow(구 warning)로 대체
    secondary = darkYellow500,
    onSecondary = darkGrey50,
    secondaryContainer = darkYellow200,
    onSecondaryContainer = darkYellow900,
    tertiary = darkGreen500,
    onTertiary = darkGrey50,
    tertiaryContainer = darkGreen300,
    onTertiaryContainer = darkGreen900,
    error = darkRed500,
    onError = darkGrey50,
    errorContainer = darkRed300,
    onErrorContainer = darkRed900,
    background = darkGrey50,
    onBackground = darkGrey900,
    surface = darkGrey50,
    onSurface = darkGrey900,
    surfaceVariant = darkGrey100,
    onSurfaceVariant = darkGrey700,
    outline = darkGrey300,
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
