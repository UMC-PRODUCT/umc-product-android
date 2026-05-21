package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light palette (values/colors.xml)
internal val lightBlack = Color(0xFF000000)
internal val lightWhite = Color(0xFFFFFFFF)
internal val lightPrimary100 = Color(0xFFEBEFFF)
internal val lightPrimary200 = Color(0xFFCCD6FF)
internal val lightPrimary300 = Color(0xFF99ABFF)
internal val lightPrimary400 = Color(0xFF6683FF)
internal val lightPrimary500 = Color(0xFF4869F0)
internal val lightPrimary600 = Color(0xFF3A5AD9)
internal val lightPrimary700 = Color(0xFF324BB9)
internal val lightPrimary800 = Color(0xFF26398C)
internal val lightPrimary900 = Color(0xFF1D2C6C)
internal val lightAccent100 = Color(0xFFFFF0E5)
internal val lightAccent200 = Color(0xFFFFD0B2)
internal val lightAccent300 = Color(0xFFFFB180)
internal val lightAccent400 = Color(0xFFFF924D)
internal val lightAccent500 = Color(0xFFFF731A)
internal val lightAccent600 = Color(0xFFE55900)
internal val lightAccent700 = Color(0xFFB24600)
internal val lightAccent800 = Color(0xFF803200)
internal val lightAccent900 = Color(0xFF4D1E00)
internal val lightNeutral000 = Color(0xFFFFFFFF)
internal val lightNeutral050 = Color(0xFFF7F8FA)
internal val lightNeutral100 = Color(0xFFF4F5F7)
internal val lightNeutral200 = Color(0xFFE7E8EA)
internal val lightNeutral300 = Color(0xFFCDD1D5)
internal val lightNeutral400 = Color(0xFFB2B8BF)
internal val lightNeutral500 = Color(0xFF8A949E)
internal val lightNeutral600 = Color(0xFF6D7882)
internal val lightNeutral700 = Color(0xFF464C54)
internal val lightNeutral800 = Color(0xFF34363E)
internal val lightNeutral900 = Color(0xFF1F2124)
internal val lightSuccess100 = Color(0xFFE7F8F0)
internal val lightSuccess300 = Color(0xFF99E0BF)
internal val lightSuccess500 = Color(0xFF12B76A)
internal val lightSuccess700 = Color(0xFF0D824B)
internal val lightSuccess900 = Color(0xFF184E3C)
internal val lightWarning100 = Color(0xFFFFFBE5)
internal val lightWarning300 = Color(0xFFFFD180)
internal val lightWarning500 = Color(0xFFFFA500)
internal val lightWarning700 = Color(0xFFB27100)
internal val lightWarning900 = Color(0xFF664100)
internal val lightDanger100 = Color(0xFFFEECEB)
internal val lightDanger300 = Color(0xFFF9AFA9)
internal val lightDanger500 = Color(0xFFF14437)
internal val lightDanger700 = Color(0xFFAB3027)
internal val lightDanger900 = Color(0xFF6C1F19)
internal val lightIndigo600 = Color(0xFF444CE7)

internal val lightKakaoColor = Color(0xFFFEE500)
internal val lightGeofenceFill = Color(0x0D51A2FF)
internal val lightGeofenceStroke = Color(0x4D51A2FF)

// Dark palette (values-night/colors.xml)
internal val darkBlack = Color(0xFF000000)
internal val darkWhite = Color(0xFFFFFFFF)
internal val darkPrimary100 = Color(0xFF1B2965)
internal val darkPrimary200 = Color(0xFF253888)
internal val darkPrimary300 = Color(0xFF3149B5)
internal val darkPrimary400 = Color(0xFF3657D8)
internal val darkPrimary500 = Color(0xFF4264F0)
internal val darkPrimary600 = Color(0xFF617FFF)
internal val darkPrimary700 = Color(0xFF94A7FF)
internal val darkPrimary800 = Color(0xFFC7D2FF)
internal val darkPrimary900 = Color(0xFFE0E7FF)
internal val darkAccent100 = Color(0xFF4D1E00)
internal val darkAccent200 = Color(0xFF7A3000)
internal val darkAccent300 = Color(0xFFA84200)
internal val darkAccent400 = Color(0xFFDB5500)
internal val darkAccent500 = Color(0xFFFF6C0F)
internal val darkAccent600 = Color(0xFFFF924D)
internal val darkAccent700 = Color(0xFFFFAA75)
internal val darkAccent800 = Color(0xFFFFCDAD)
internal val darkAccent900 = Color(0xFFFFEDE0)
internal val darkNeutral000 = Color(0xFF121212)
internal val darkNeutral050 = Color(0xFF0D121C)

internal val darkNeutral100 = Color(0xFF28292F)
internal val darkNeutral200 = Color(0xFF3A3F46)
internal val darkNeutral300 = Color(0xFF626C75)
internal val darkNeutral400 = Color(0xFF7C8792)
internal val darkNeutral500 = Color(0xFFA6ADB5)
internal val darkNeutral600 = Color(0xFFC2C7CC)
internal val darkNeutral700 = Color(0xFFDCDDE0)
internal val darkNeutral800 = Color(0xFFEBEDEF)
internal val darkNeutral900 = Color(0xFFF7F7F7)
internal val darkSuccess100 = Color(0xFF174A39)
internal val darkSuccess300 = Color(0xFF0D824B)
internal val darkSuccess500 = Color(0xFF12B76A)
internal val darkSuccess700 = Color(0xFF99E0BF)
internal val darkSuccess900 = Color(0xFFE7F8F0)
internal val darkWarning100 = Color(0xFF5C3A00)
internal val darkWarning300 = Color(0xFFA86B00)
internal val darkWarning500 = Color(0xFFF59E00)
internal val darkWarning700 = Color(0xFFFFCD75)
internal val darkWarning900 = Color(0xFFFFFAE0)
internal val darkDanger100 = Color(0xFF6C1F19)
internal val darkDanger300 = Color(0xFFAB3027)
internal val darkDanger500 = Color(0xFFF14437)
internal val darkDanger700 = Color(0xFFF9AFA9)
internal val darkDanger900 = Color(0xFFFEECEB)
internal val darkIndigo600 = Color(0xFF728EFD)
internal val darkKakaoColor = Color(0xFFFEE500)

@Composable
fun black(): Color = if (isSystemInDarkTheme()) darkBlack else lightBlack

@Composable
fun white(): Color = if (isSystemInDarkTheme()) darkWhite else lightWhite

@Composable
fun primary100(): Color = if (isSystemInDarkTheme()) darkPrimary100 else lightPrimary100

@Composable
fun primary200(): Color = if (isSystemInDarkTheme()) darkPrimary200 else lightPrimary200

@Composable
fun primary300(): Color = if (isSystemInDarkTheme()) darkPrimary300 else lightPrimary300

@Composable
fun primary400(): Color = if (isSystemInDarkTheme()) darkPrimary400 else lightPrimary400

@Composable
fun primary500(): Color = if (isSystemInDarkTheme()) darkPrimary500 else lightPrimary500

@Composable
fun primary600(): Color = if (isSystemInDarkTheme()) darkPrimary600 else lightPrimary600

@Composable
fun primary700(): Color = if (isSystemInDarkTheme()) darkPrimary700 else lightPrimary700

@Composable
fun primary800(): Color = if (isSystemInDarkTheme()) darkPrimary800 else lightPrimary800

@Composable
fun primary900(): Color = if (isSystemInDarkTheme()) darkPrimary900 else lightPrimary900

@Composable
fun accent100(): Color = if (isSystemInDarkTheme()) darkAccent100 else lightAccent100

@Composable
fun accent200(): Color = if (isSystemInDarkTheme()) darkAccent200 else lightAccent200

@Composable
fun accent300(): Color = if (isSystemInDarkTheme()) darkAccent300 else lightAccent300

@Composable
fun accent400(): Color = if (isSystemInDarkTheme()) darkAccent400 else lightAccent400

@Composable
fun accent500(): Color = if (isSystemInDarkTheme()) darkAccent500 else lightAccent500

@Composable
fun accent600(): Color = if (isSystemInDarkTheme()) darkAccent600 else lightAccent600

@Composable
fun accent700(): Color = if (isSystemInDarkTheme()) darkAccent700 else lightAccent700

@Composable
fun accent800(): Color = if (isSystemInDarkTheme()) darkAccent800 else lightAccent800

@Composable
fun accent900(): Color = if (isSystemInDarkTheme()) darkAccent900 else lightAccent900

@Composable
fun neutral000(): Color = if (isSystemInDarkTheme()) darkNeutral000 else lightNeutral000

@Composable
fun neutral050(): Color = if (isSystemInDarkTheme()) darkNeutral050 else lightNeutral050

@Composable
fun neutral100(): Color = if (isSystemInDarkTheme()) darkNeutral100 else lightNeutral100

@Composable
fun neutral200(): Color = if (isSystemInDarkTheme()) darkNeutral200 else lightNeutral200

@Composable
fun neutral300(): Color = if (isSystemInDarkTheme()) darkNeutral300 else lightNeutral300

@Composable
fun neutral400(): Color = if (isSystemInDarkTheme()) darkNeutral400 else lightNeutral400

@Composable
fun neutral500(): Color = if (isSystemInDarkTheme()) darkNeutral500 else lightNeutral500

@Composable
fun neutral600(): Color = if (isSystemInDarkTheme()) darkNeutral600 else lightNeutral600

@Composable
fun neutral700(): Color = if (isSystemInDarkTheme()) darkNeutral700 else lightNeutral700

@Composable
fun neutral800(): Color = if (isSystemInDarkTheme()) darkNeutral800 else lightNeutral800

@Composable
fun neutral900(): Color = if (isSystemInDarkTheme()) darkNeutral900 else lightNeutral900

@Composable
fun success100(): Color = if (isSystemInDarkTheme()) darkSuccess100 else lightSuccess100

@Composable
fun success300(): Color = if (isSystemInDarkTheme()) darkSuccess300 else lightSuccess300

@Composable
fun success500(): Color = if (isSystemInDarkTheme()) darkSuccess500 else lightSuccess500

@Composable
fun success700(): Color = if (isSystemInDarkTheme()) darkSuccess700 else lightSuccess700

@Composable
fun success900(): Color = if (isSystemInDarkTheme()) darkSuccess900 else lightSuccess900

@Composable
fun warning100(): Color = if (isSystemInDarkTheme()) darkWarning100 else lightWarning100

@Composable
fun warning300(): Color = if (isSystemInDarkTheme()) darkWarning300 else lightWarning300

@Composable
fun warning500(): Color = if (isSystemInDarkTheme()) darkWarning500 else lightWarning500

@Composable
fun warning700(): Color = if (isSystemInDarkTheme()) darkWarning700 else lightWarning700

@Composable
fun warning900(): Color = if (isSystemInDarkTheme()) darkWarning900 else lightWarning900

@Composable
fun danger100(): Color = if (isSystemInDarkTheme()) darkDanger100 else lightDanger100

@Composable
fun danger300(): Color = if (isSystemInDarkTheme()) darkDanger300 else lightDanger300

@Composable
fun danger500(): Color = if (isSystemInDarkTheme()) darkDanger500 else lightDanger500

@Composable
fun danger700(): Color = if (isSystemInDarkTheme()) darkDanger700 else lightDanger700

@Composable
fun danger900(): Color = if (isSystemInDarkTheme()) darkDanger900 else lightDanger900

@Composable
fun indigo600(): Color = if (isSystemInDarkTheme()) darkIndigo600 else lightIndigo600


@Composable
fun kakaoColor(): Color = if (isSystemInDarkTheme()) darkKakaoColor else lightKakaoColor

@Composable
fun geofence_fill(): Color = lightGeofenceFill

@Composable
fun geofence_stroke(): Color = lightGeofenceStroke
