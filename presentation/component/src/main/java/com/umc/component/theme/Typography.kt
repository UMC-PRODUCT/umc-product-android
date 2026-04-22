package com.umc.component.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.umc.component.R

private val pretendardRegular = FontFamily(Font(R.font.pretendard_regular, FontWeight.Normal))
private val pretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))

object UmcTypographyTokens {
    val LargeTitle = TextStyle(
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = 0.01.sp,
        fontFamily = pretendardRegular,
    )

    val Title1 = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.01.sp,
        fontFamily = pretendardRegular,
    )

    val Title2 = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.015.sp,
        fontFamily = pretendardRegular,
    )

    val Title3 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.019.sp,
        fontFamily = pretendardRegular,
    )

    val Headline = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardRegular,
    )

    val Body = TextStyle(
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardRegular,
    )

    val Callout = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardRegular,
    )

    val Subheadline = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.0016).sp,
        fontFamily = pretendardRegular,
    )

    val Footnote = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.006).sp,
        fontFamily = pretendardRegular,
    )

    val Caption1 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        fontFamily = pretendardRegular,
    )

    val Caption2 = TextStyle(
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.0005.sp,
        fontFamily = pretendardRegular,
    )

    val LargeTitleBold = TextStyle(
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = 0.01.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val Title1Bold = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.01.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val Title2Bold = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.015.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val Title3Bold = TextStyle(
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.019.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val HeadlineBold = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val BodyBold = TextStyle(
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val CalloutBold = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.02).sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val SubheadlineBold = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.0016).sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val FootnoteBold = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.006).sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val Caption1Bold = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )

    val Caption2Bold = TextStyle(
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.0005.sp,
        fontFamily = pretendardSemiBold,
        fontWeight = FontWeight.SemiBold,
    )
}

val UmcTypography = Typography(
    displayLarge = UmcTypographyTokens.LargeTitle,
    displayMedium = UmcTypographyTokens.Title1,
    displaySmall = UmcTypographyTokens.Title2,
    headlineLarge = UmcTypographyTokens.Title3,
    headlineMedium = UmcTypographyTokens.Headline,
    headlineSmall = UmcTypographyTokens.Subheadline,
    titleLarge = UmcTypographyTokens.Body,
    titleMedium = UmcTypographyTokens.Callout,
    titleSmall = UmcTypographyTokens.Footnote,
    bodyLarge = UmcTypographyTokens.Body,
    bodyMedium = UmcTypographyTokens.Callout,
    bodySmall = UmcTypographyTokens.Caption1,
    labelLarge = UmcTypographyTokens.HeadlineBold,
    labelMedium = UmcTypographyTokens.SubheadlineBold,
    labelSmall = UmcTypographyTokens.Caption2,
)
