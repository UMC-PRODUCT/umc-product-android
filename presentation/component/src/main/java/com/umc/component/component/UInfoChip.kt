package com.umc.component.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.neutral050
import com.umc.component.theme.neutral600
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
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

    UInfoChip(
        text = chipText,
        backgroundColor = type.backgroundColor(),
        textColor = type.textColor(),
        modifier = modifier
    )
}

@Composable
fun UInfoChip(
    part: UserPart,
    modifier: Modifier = Modifier,
) {
    UInfoChip(
        text = part.label,
        type = UInfoChipType.PART,
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

@Composable
private fun UInfoChipType.backgroundColor(): Color {
    return when (this) {
        UInfoChipType.SCHOOL -> neutral050()
        UInfoChipType.PART -> primary100()
    }
}

@Composable
private fun UInfoChipType.textColor(): Color {
    return when (this) {
        UInfoChipType.SCHOOL -> neutral600()
        UInfoChipType.PART -> primary500()
    }
}
