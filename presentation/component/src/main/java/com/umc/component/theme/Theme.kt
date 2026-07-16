package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = lightIndigo500,
    onPrimary = lightWhite,
    primaryContainer = lightIndigo100,
    onPrimaryContainer = lightIndigo900,
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
    background = lightGrey000,
    onBackground = lightGrey900,
    surface = lightGrey000,
    onSurface = lightGrey900,
    surfaceVariant = lightGrey100,
    onSurfaceVariant = lightGrey700,
    outline = lightGrey300,
)

private val DarkColors = darkColorScheme(
    primary = darkIndigo600,
    onPrimary = darkGrey000,
    primaryContainer = darkIndigo200,
    onPrimaryContainer = darkIndigo900,
    // accent 팔레트 제거로 yellow(구 warning)로 대체
    secondary = darkYellow500,
    onSecondary = darkGrey000,
    secondaryContainer = darkYellow200,
    onSecondaryContainer = darkYellow900,
    tertiary = darkGreen500,
    onTertiary = darkGrey000,
    tertiaryContainer = darkGreen300,
    onTertiaryContainer = darkGreen900,
    error = darkRed500,
    onError = darkGrey000,
    errorContainer = darkRed300,
    onErrorContainer = darkRed900,
    background = darkGrey000,
    onBackground = darkGrey900,
    surface = darkGrey000,
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
