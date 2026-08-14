package com.umc.component.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.green100
import com.umc.component.theme.green600
import com.umc.component.theme.green700
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo600
import com.umc.component.theme.indigo700
import com.umc.component.theme.red100
import com.umc.component.theme.red600
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow600
import com.umc.component.theme.yellow700
import com.umc.component.theme.white
import com.umc.domain.model.enums.UserPart

enum class UInfoChipType {
    SCHOOL,
    PART
}

/**
 * 챌린저의 학교 또는 파트를 표시하는 정보 칩입니다.
 *
 * 학교명은 문자열 해시를 기반으로 공용 팔레트 색상을 안정적으로 선택하고,
 * 파트는 [UserPart]별 의미 색상을 사용합니다.
 *
 * @param text 학교명 또는 파트명
 * @param type 정보 종류
 * @param modifier 크기와 배치를 지정하는 Modifier
 */
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
        UInfoChipType.SCHOOL -> schoolChipStyle()
        UInfoChipType.PART -> partChipStyle(UserPart.from(chipText))
    }

    UInfoChip(
        text = chipText,
        backgroundColor = style.backgroundColor,
        textColor = style.textColor,
        modifier = modifier
    )
}

/**
 * [UserPart]를 직접 받아 파트 라벨과 색상을 표시하는 정보 칩입니다.
 *
 * @param part 표시할 파트
 * @param modifier 크기와 배치를 지정하는 Modifier
 */
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

/**
 * 호출부에서 배경색과 텍스트 색상을 직접 지정할 수 있는 기본 정보 칩입니다.
 */
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

@Composable
private fun schoolChipStyle() = UInfoChipStyle(grey800(), white())

@Composable
private fun partChipStyle(part: UserPart): UInfoChipStyle {
    return when (part) {
        UserPart.PLAN -> UInfoChipStyle(indigo100(), indigo600())
        UserPart.DESIGN -> UInfoChipStyle(red100(), red600())
        UserPart.WEB -> UInfoChipStyle(green100(), green600())
        UserPart.IOS -> UInfoChipStyle(indigo100(), indigo700())
        UserPart.ANDROID -> UInfoChipStyle(green100(), green700())
        UserPart.SPRINGBOOT -> UInfoChipStyle(yellow100(), yellow600())
        UserPart.NODEJS -> UInfoChipStyle(yellow100(), yellow700())
        UserPart.ADMIN,
        UserPart.UNKNOWN -> UInfoChipStyle(grey100(), grey600())
    }
}
