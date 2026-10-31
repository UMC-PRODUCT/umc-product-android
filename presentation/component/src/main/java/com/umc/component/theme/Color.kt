package com.umc.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light palette (values/colors.xml)
internal val lightBlack = Color(0xFF000000)
internal val lightWhite = Color(0xFFFFFFFF)
internal val lightMain100 = Color(0xFFE8F7F5)
internal val lightMain200 = Color(0xFFC1EBE8)
internal val lightMain300 = Color(0xFF7AC7C1)
internal val lightMain400 = Color(0xFF39A9A1)
internal val lightMain500 = Color(0xFF008A80)
internal val lightMain600 = Color(0xFF02746C)
internal val lightMain700 = Color(0xFF015A54)
internal val lightMain800 = Color(0xFF01423D)
internal val lightMain900 = Color(0xFF013733)
internal val lightGrey50 = Color(0xFFF7F8F8)
internal val lightGrey100 = Color(0xFFF4F5F5)
internal val lightGrey200 = Color(0xFFE5E6E6)
internal val lightGrey300 = Color(0xFFCCCCCC)
internal val lightGrey400 = Color(0xFFADADAD)
internal val lightGrey500 = Color(0xFF7E7F7F)
internal val lightGrey600 = Color(0xFF606262)
internal val lightGrey700 = Color(0xFF3F4141)
internal val lightGrey800 = Color(0xFF282A2A)
internal val lightGrey900 = Color(0xFF0F1010)
internal val lightGrey950 = Color(0xFF070808)
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
internal val lightMint100 = Color(0xFFE5F5F2)
internal val lightTeal600 = Color(0xFF0E8179)
internal val lightGeofenceFill = Color(0x0D51A2FF)
internal val lightGeofenceStroke = Color(0x4D51A2FF)

// Dark palette (values-night/colors.xml)
// white·black은 다크 모드에서 서로 뒤바뀐다 (white는 다크에서 검정, black은 다크에서 흰색)
internal val darkBlack = Color(0xFFFFFFFF)
internal val darkWhite = Color(0xFF000000)
internal val darkMain100 = Color(0xFF013733)
internal val darkMain200 = Color(0xFF01423D)
internal val darkMain300 = Color(0xFF015A54)
internal val darkMain400 = Color(0xFF02746C)
internal val darkMain500 = Color(0xFF008A80)
internal val darkMain600 = Color(0xFF39A9A1)
internal val darkMain700 = Color(0xFF7AC7C1)
internal val darkMain800 = Color(0xFFC1EBE8)
internal val darkMain900 = Color(0xFFE8F7F5)
internal val darkGrey50 = Color(0xFF0F1010)
internal val darkGrey100 = Color(0xFF070808)
internal val darkGrey200 = Color(0xFF282A2A)
internal val darkGrey300 = Color(0xFF3F4141)
internal val darkGrey400 = Color(0xFF606262)
internal val darkGrey500 = Color(0xFF7E7F7F)
internal val darkGrey600 = Color(0xFFADADAD)
internal val darkGrey700 = Color(0xFFCCCCCC)
internal val darkGrey800 = Color(0xFFE5E6E6)
internal val darkGrey900 = Color(0xFFF4F5F5)
internal val darkGrey950 = Color(0xFFF7F8F8)
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
internal val darkMint100 = Color(0xFFE5F5F2)
internal val darkTeal600 = Color(0xFF0E8179)

@Composable
fun black(): Color = if (isSystemInDarkTheme()) darkBlack else lightBlack

@Composable
fun white(): Color = if (isSystemInDarkTheme()) darkWhite else lightWhite

@Composable
fun main100(): Color = if (isSystemInDarkTheme()) darkMain100 else lightMain100

@Composable
fun main200(): Color = if (isSystemInDarkTheme()) darkMain200 else lightMain200

@Composable
fun main300(): Color = if (isSystemInDarkTheme()) darkMain300 else lightMain300

@Composable
fun main400(): Color = if (isSystemInDarkTheme()) darkMain400 else lightMain400

@Composable
fun main500(): Color = if (isSystemInDarkTheme()) darkMain500 else lightMain500

@Composable
fun main600(): Color = if (isSystemInDarkTheme()) darkMain600 else lightMain600

@Composable
fun main700(): Color = if (isSystemInDarkTheme()) darkMain700 else lightMain700

@Composable
fun main800(): Color = if (isSystemInDarkTheme()) darkMain800 else lightMain800

@Composable
fun main900(): Color = if (isSystemInDarkTheme()) darkMain900 else lightMain900

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
fun mint100(): Color = if (isSystemInDarkTheme()) darkMint100 else lightMint100

@Composable
fun teal600(): Color = if (isSystemInDarkTheme()) darkTeal600 else lightTeal600

@Composable
fun geofence_fill(): Color = lightGeofenceFill

@Composable
fun geofence_stroke(): Color = lightGeofenceStroke
