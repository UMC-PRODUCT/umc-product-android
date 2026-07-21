package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light palette (values/colors.xml)
internal val lightBlack = Color(0xFF000000)
internal val lightWhite = Color(0xFFFFFFFF)
internal val lightIndigo100 = Color(0xFFEBF2FF)
internal val lightIndigo200 = Color(0xFFC7D7FE)
internal val lightIndigo300 = Color(0xFFA4BCFD)
internal val lightIndigo400 = Color(0xFF728EFD)
internal val lightIndigo500 = Color(0xFF5468FC)
internal val lightIndigo600 = Color(0xFF444CE7)
internal val lightIndigo700 = Color(0xFF3538CD)
internal val lightIndigo800 = Color(0xFF2C2FA0)
internal val lightIndigo900 = Color(0xFF222663)
internal val lightGrey000 = Color(0xFFFFFFFF)
internal val lightGrey50 = Color(0xFFF7F8FA)
internal val lightGrey100 = Color(0xFFF2F5F8)
internal val lightGrey200 = Color(0xFFDDE1E9)
internal val lightGrey300 = Color(0xFFC3CBD5)
internal val lightGrey400 = Color(0xFFA6B2BF)
internal val lightGrey500 = Color(0xFF8B97A7)
internal val lightGrey600 = Color(0xFF657081)
internal val lightGrey700 = Color(0xFF4A5464)
internal val lightGrey800 = Color(0xFF202939)
internal val lightGrey900 = Color(0xFF161C27)
internal val lightGrey950 = Color(0xFF0D121C)
internal val lightGreen100 = Color(0xFFE4FCEC)
internal val lightGreen200 = Color(0xFFABEFC6)
internal val lightGreen300 = Color(0xFF75E0A7)
internal val lightGreen400 = Color(0xFF47CD89)
internal val lightGreen500 = Color(0xFF17B26A)
internal val lightGreen600 = Color(0xFF079455)
internal val lightGreen700 = Color(0xFF067647)
internal val lightGreen800 = Color(0xFF085D3A)
internal val lightGreen900 = Color(0xFF053321)
internal val lightYellow100 = Color(0xFFFFF7E0)
internal val lightYellow200 = Color(0xFFFEDF89)
internal val lightYellow300 = Color(0xFFFEC84B)
internal val lightYellow400 = Color(0xFFFDB022)
internal val lightYellow500 = Color(0xFFF79009)
internal val lightYellow600 = Color(0xFFDC6803)
internal val lightYellow700 = Color(0xFFB54708)
internal val lightYellow800 = Color(0xFF93370D)
internal val lightYellow900 = Color(0xFF4E1D09)
internal val lightRed100 = Color(0xFFFEEDEB)
internal val lightRed200 = Color(0xFFFECDCA)
internal val lightRed300 = Color(0xFFFDA29B)
internal val lightRed400 = Color(0xFFF97066)
internal val lightRed500 = Color(0xFFF04438)
internal val lightRed600 = Color(0xFFD92D20)
internal val lightRed700 = Color(0xFFB42318)
internal val lightRed800 = Color(0xFF912018)
internal val lightRed900 = Color(0xFF55160C)
internal val lightKakaoColor = Color(0xFFFEE500)
internal val lightGeofenceFill = Color(0x0D51A2FF)
internal val lightGeofenceStroke = Color(0x4D51A2FF)

// Dark palette (values-night/colors.xml)
internal val darkBlack = Color(0xFF000000)
internal val darkWhite = Color(0xFFFFFFFF)
internal val darkIndigo100 = Color(0xFF1F235B)
internal val darkIndigo200 = Color(0xFF2A2D98)
internal val darkIndigo300 = Color(0xFF3538CD)
internal val darkIndigo400 = Color(0xFF444CE7)
internal val darkIndigo500 = Color(0xFF5E71FD)
internal val darkIndigo600 = Color(0xFF728EFD)
internal val darkIndigo700 = Color(0xFFA4BCFD)
internal val darkIndigo800 = Color(0xFFC3D4FE)
internal val darkIndigo900 = Color(0xFFDBE8FF)
internal val darkGrey000 = Color(0xFF121212)
internal val darkGrey50 = Color(0xFF0D121C)
internal val darkGrey100 = Color(0xFF1C2331)
internal val darkGrey200 = Color(0xFF333D4D)
internal val darkGrey300 = Color(0xFF485161)
internal val darkGrey400 = Color(0xFF657081)
internal val darkGrey500 = Color(0xFF8B97A7)
internal val darkGrey600 = Color(0xFFA0ADBB)
internal val darkGrey700 = Color(0xFFBDC6D1)
internal val darkGrey800 = Color(0xFFDDE1E9)
internal val darkGrey900 = Color(0xFFF2F5F8)
internal val darkGrey950 = Color(0xFFF7F8FA)
internal val darkGreen100 = Color(0xFF042A1B)
internal val darkGreen200 = Color(0xFF085D3A)
internal val darkGreen300 = Color(0xFF067647)
internal val darkGreen400 = Color(0xFF079455)
internal val darkGreen500 = Color(0xFF17B26A)
internal val darkGreen600 = Color(0xFF47CD89)
internal val darkGreen700 = Color(0xFF75E0A7)
internal val darkGreen800 = Color(0xFFABEFC6)
internal val darkGreen900 = Color(0xFFDBFAE5)
internal val darkYellow100 = Color(0xFF451908)
internal val darkYellow200 = Color(0xFF93370D)
internal val darkYellow300 = Color(0xFFB54708)
internal val darkYellow400 = Color(0xFFDC6803)
internal val darkYellow500 = Color(0xFFF79009)
internal val darkYellow600 = Color(0xFFFDB022)
internal val darkYellow700 = Color(0xFFFEC84B)
internal val darkYellow800 = Color(0xFFFEDF89)
internal val darkYellow900 = Color(0xFFFEF4D7)
internal val darkRed100 = Color(0xFF4C140B)
internal val darkRed200 = Color(0xFF912018)
internal val darkRed300 = Color(0xFFB42318)
internal val darkRed400 = Color(0xFFD92D20)
internal val darkRed500 = Color(0xFFF04438)
internal val darkRed600 = Color(0xFFF97066)
internal val darkRed700 = Color(0xFFFDA29B)
internal val darkRed800 = Color(0xFFFECDCA)
internal val darkRed900 = Color(0xFFFEE5E2)
internal val darkKakaoColor = Color(0xFFFEE500)

@Composable
fun black(): Color = if (isSystemInDarkTheme()) darkBlack else lightBlack

@Composable
fun white(): Color = if (isSystemInDarkTheme()) darkWhite else lightWhite

@Composable
fun indigo100(): Color = if (isSystemInDarkTheme()) darkIndigo100 else lightIndigo100

@Composable
fun indigo200(): Color = if (isSystemInDarkTheme()) darkIndigo200 else lightIndigo200

@Composable
fun indigo300(): Color = if (isSystemInDarkTheme()) darkIndigo300 else lightIndigo300

@Composable
fun indigo400(): Color = if (isSystemInDarkTheme()) darkIndigo400 else lightIndigo400

@Composable
fun indigo500(): Color = if (isSystemInDarkTheme()) darkIndigo500 else lightIndigo500

@Composable
fun indigo600(): Color = if (isSystemInDarkTheme()) darkIndigo600 else lightIndigo600

@Composable
fun indigo700(): Color = if (isSystemInDarkTheme()) darkIndigo700 else lightIndigo700

@Composable
fun indigo800(): Color = if (isSystemInDarkTheme()) darkIndigo800 else lightIndigo800

@Composable
fun indigo900(): Color = if (isSystemInDarkTheme()) darkIndigo900 else lightIndigo900

@Composable
fun grey000(): Color = if (isSystemInDarkTheme()) darkGrey000 else lightGrey000

@Composable
fun grey50(): Color = if (isSystemInDarkTheme()) darkGrey50 else lightGrey50

@Composable
fun grey100(): Color = if (isSystemInDarkTheme()) darkGrey100 else lightGrey100

@Composable
fun grey200(): Color = if (isSystemInDarkTheme()) darkGrey200 else lightGrey200

@Composable
fun grey300(): Color = if (isSystemInDarkTheme()) darkGrey300 else lightGrey300

@Composable
fun grey400(): Color = if (isSystemInDarkTheme()) darkGrey400 else lightGrey400

@Composable
fun grey500(): Color = if (isSystemInDarkTheme()) darkGrey500 else lightGrey500

@Composable
fun grey600(): Color = if (isSystemInDarkTheme()) darkGrey600 else lightGrey600

@Composable
fun grey700(): Color = if (isSystemInDarkTheme()) darkGrey700 else lightGrey700

@Composable
fun grey800(): Color = if (isSystemInDarkTheme()) darkGrey800 else lightGrey800

@Composable
fun grey900(): Color = if (isSystemInDarkTheme()) darkGrey900 else lightGrey900

@Composable
fun grey950(): Color = if (isSystemInDarkTheme()) darkGrey950 else lightGrey950

@Composable
fun green100(): Color = if (isSystemInDarkTheme()) darkGreen100 else lightGreen100

@Composable
fun green200(): Color = if (isSystemInDarkTheme()) darkGreen200 else lightGreen200

@Composable
fun green300(): Color = if (isSystemInDarkTheme()) darkGreen300 else lightGreen300

@Composable
fun green400(): Color = if (isSystemInDarkTheme()) darkGreen400 else lightGreen400

@Composable
fun green500(): Color = if (isSystemInDarkTheme()) darkGreen500 else lightGreen500

@Composable
fun green600(): Color = if (isSystemInDarkTheme()) darkGreen600 else lightGreen600

@Composable
fun green700(): Color = if (isSystemInDarkTheme()) darkGreen700 else lightGreen700

@Composable
fun green800(): Color = if (isSystemInDarkTheme()) darkGreen800 else lightGreen800

@Composable
fun green900(): Color = if (isSystemInDarkTheme()) darkGreen900 else lightGreen900

@Composable
fun yellow100(): Color = if (isSystemInDarkTheme()) darkYellow100 else lightYellow100

@Composable
fun yellow200(): Color = if (isSystemInDarkTheme()) darkYellow200 else lightYellow200

@Composable
fun yellow300(): Color = if (isSystemInDarkTheme()) darkYellow300 else lightYellow300

@Composable
fun yellow400(): Color = if (isSystemInDarkTheme()) darkYellow400 else lightYellow400

@Composable
fun yellow500(): Color = if (isSystemInDarkTheme()) darkYellow500 else lightYellow500

@Composable
fun yellow600(): Color = if (isSystemInDarkTheme()) darkYellow600 else lightYellow600

@Composable
fun yellow700(): Color = if (isSystemInDarkTheme()) darkYellow700 else lightYellow700

@Composable
fun yellow800(): Color = if (isSystemInDarkTheme()) darkYellow800 else lightYellow800

@Composable
fun yellow900(): Color = if (isSystemInDarkTheme()) darkYellow900 else lightYellow900

@Composable
fun red100(): Color = if (isSystemInDarkTheme()) darkRed100 else lightRed100

@Composable
fun red200(): Color = if (isSystemInDarkTheme()) darkRed200 else lightRed200

@Composable
fun red300(): Color = if (isSystemInDarkTheme()) darkRed300 else lightRed300

@Composable
fun red400(): Color = if (isSystemInDarkTheme()) darkRed400 else lightRed400

@Composable
fun red500(): Color = if (isSystemInDarkTheme()) darkRed500 else lightRed500

@Composable
fun red600(): Color = if (isSystemInDarkTheme()) darkRed600 else lightRed600

@Composable
fun red700(): Color = if (isSystemInDarkTheme()) darkRed700 else lightRed700

@Composable
fun red800(): Color = if (isSystemInDarkTheme()) darkRed800 else lightRed800

@Composable
fun red900(): Color = if (isSystemInDarkTheme()) darkRed900 else lightRed900

@Composable
fun kakaoColor(): Color = if (isSystemInDarkTheme()) darkKakaoColor else lightKakaoColor

@Composable
fun geofence_fill(): Color = lightGeofenceFill

@Composable
fun geofence_stroke(): Color = lightGeofenceStroke
