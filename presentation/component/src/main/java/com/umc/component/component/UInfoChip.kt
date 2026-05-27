package com.umc.component.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.domain.model.enums.UserPart

enum class UInfoChipType {
    SCHOOL,
    PART
}

@Composable
fun UInfoChip(
    text: String,
    type: UInfoChipType,
    modifier: Modifier = Modifier,
) {
    val chipText = when (type) {
        UInfoChipType.SCHOOL -> text.ifBlank { "알수없음" }
        UInfoChipType.PART -> text.toPartLabel()
    }
    val style = when (type) {
        UInfoChipType.SCHOOL -> schoolChipStyle(chipText)
        UInfoChipType.PART -> partChipStyle(UserPart.from(chipText))
    }

    UInfoChip(
        text = chipText,
        backgroundColor = style.backgroundColor,
        textColor = style.textColor,
        modifier = modifier
    )
}

@Composable
fun UInfoChip(
    part: UserPart,
    modifier: Modifier = Modifier,
) {
    val style = partChipStyle(part)

    UInfoChip(
        text = part.label,
        backgroundColor = style.backgroundColor,
        textColor = style.textColor,
        modifier = modifier
    )
}

@Composable
fun UInfoChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    UChip(
        text = text,
        modifier = modifier,
        backgroundColor = backgroundColor,
        textColor = textColor,
        textStyle = Caption1Bold,
        cornerRadius = 4.dp,
        contentPadding = PaddingValues(horizontal = 8.dp),
        minHeight = 24.dp
    )
}

private fun String.toPartLabel(): String {
    val part = UserPart.from(this)
    return if (part == UserPart.UNKNOWN) {
        ifBlank { part.label }
    } else {
        part.label
    }
}

private data class UInfoChipStyle(
    val backgroundColor: Color,
    val textColor: Color,
)

private fun schoolChipStyle(schoolName: String): UInfoChipStyle {
    val normalizedName = schoolName.normalizedKey()
    val index = (normalizedName.hashCode() and Int.MAX_VALUE) % schoolPalette.size
    return schoolPalette[index]
}

private fun partChipStyle(part: UserPart): UInfoChipStyle {
    return when (part) {
        UserPart.PLAN -> UInfoChipStyle(Color(0xFFEFF6FF), Color(0xFF2563EB))
        UserPart.DESIGN -> UInfoChipStyle(Color(0xFFFDF2F8), Color(0xFFDB2777))
        UserPart.WEB -> UInfoChipStyle(Color(0xFFECFDF5), Color(0xFF059669))
        UserPart.IOS -> UInfoChipStyle(Color(0xFFF5F3FF), Color(0xFF7C3AED))
        UserPart.ANDROID -> UInfoChipStyle(Color(0xFFF0FDF4), Color(0xFF16A34A))
        UserPart.SPRINGBOOT -> UInfoChipStyle(Color(0xFFFFF7ED), Color(0xFFEA580C))
        UserPart.NODEJS -> UInfoChipStyle(Color(0xFFFEFCE8), Color(0xFFCA8A04))
        UserPart.ADMIN,
        UserPart.UNKNOWN -> UInfoChipStyle(Color(0xFFF4F5F7), Color(0xFF6D7882))
    }
}

private fun String.normalizedKey(): String {
    return trim()
        .lowercase()
        .replace(" ", "")
}

private val schoolPalette = listOf(
    UInfoChipStyle(Color(0xFFFFF1F2), Color(0xFFE11D48)),
    UInfoChipStyle(Color(0xFFFFF7ED), Color(0xFFEA580C)),
    UInfoChipStyle(Color(0xFFFFFBEB), Color(0xFFD97706)),
    UInfoChipStyle(Color(0xFFFEFCE8), Color(0xFFCA8A04)),
    UInfoChipStyle(Color(0xFFF7FEE7), Color(0xFF65A30D)),
    UInfoChipStyle(Color(0xFFF0FDF4), Color(0xFF16A34A)),
    UInfoChipStyle(Color(0xFFECFDF5), Color(0xFF059669)),
    UInfoChipStyle(Color(0xFFF0FDFA), Color(0xFF0D9488)),
    UInfoChipStyle(Color(0xFFECFEFF), Color(0xFF0891B2)),
    UInfoChipStyle(Color(0xFFF0F9FF), Color(0xFF0284C7)),
    UInfoChipStyle(Color(0xFFEFF6FF), Color(0xFF2563EB)),
    UInfoChipStyle(Color(0xFFEEF2FF), Color(0xFF4F46E5)),
    UInfoChipStyle(Color(0xFFF5F3FF), Color(0xFF7C3AED)),
    UInfoChipStyle(Color(0xFFFAF5FF), Color(0xFF9333EA)),
    UInfoChipStyle(Color(0xFFFDF4FF), Color(0xFFC026D3)),
    UInfoChipStyle(Color(0xFFFDF2F8), Color(0xFFDB2777)),
    UInfoChipStyle(Color(0xFFFFF1F2), Color(0xFFBE123C)),
    UInfoChipStyle(Color(0xFFFFF4E6), Color(0xFFC2410C)),
    UInfoChipStyle(Color(0xFFFFF8DB), Color(0xFFA16207)),
    UInfoChipStyle(Color(0xFFF2FCE2), Color(0xFF4D7C0F)),
    UInfoChipStyle(Color(0xFFE9FBF0), Color(0xFF15803D)),
    UInfoChipStyle(Color(0xFFE6FAF5), Color(0xFF047857)),
    UInfoChipStyle(Color(0xFFE5FAF8), Color(0xFF0F766E)),
    UInfoChipStyle(Color(0xFFE6F8FB), Color(0xFF0E7490)),
    UInfoChipStyle(Color(0xFFE8F4FF), Color(0xFF0369A1)),
    UInfoChipStyle(Color(0xFFEAF0FF), Color(0xFF1D4ED8)),
    UInfoChipStyle(Color(0xFFEDEBFF), Color(0xFF4338CA)),
    UInfoChipStyle(Color(0xFFF1EAFF), Color(0xFF6D28D9)),
    UInfoChipStyle(Color(0xFFFFECF7), Color(0xFFBE185D)),
)
