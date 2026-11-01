package com.umc.component.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.green100
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.main100
import com.umc.component.theme.main600
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow600
import com.umc.component.theme.white
import com.umc.component.theme.green500
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow400
import com.umc.component.theme.yellow800
import com.umc.component.theme.mint100
import com.umc.component.theme.tagColors
import com.umc.component.theme.teal600
import com.umc.domain.model.enums.UserPart

private const val INFRA_CHIP_LABEL = "Infra"

enum class UInfoChipType {
    SCHOOL,
    PART
}

/**
 * 챌린저의 학교 또는 파트를 표시하는 정보 칩입니다.
 *
 * 학교명은 회색 칩으로, 파트는 [partChipColors] 의 파트별 색으로 표시합니다.
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
    val colors = when (type) {
        UInfoChipType.SCHOOL -> schoolChipColors()
        UInfoChipType.PART -> partChipColors(UserPart.from(chipText))
    }

    UInfoChip(
        text = chipText,
        backgroundColor = colors.background,
        textColor = colors.text,
        borderColor = colors.border,
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
    val colors = partChipColors(part)

    UInfoChip(
        text = part.label,
        backgroundColor = colors.background,
        textColor = colors.text,
        borderColor = colors.border,
        modifier = modifier
    )
}

/**
 * 인프라를 겸하는 챌린저에게 파트 칩과 함께 붙이는 정보 칩입니다.
 *
 * 인프라는 파트가 아니라 챌린저마다 따로 내려오는 여부 값이라 [UserPart] 를 받지 않습니다.
 *
 * @param modifier 크기와 배치를 지정하는 Modifier
 */
@Composable
fun UInfraChip(
    modifier: Modifier = Modifier,
) {
    UInfoChip(
        text = INFRA_CHIP_LABEL,
        backgroundColor = yellow100(),
        textColor = yellow600(),
        modifier = modifier
    )
}

/**
 * 호출부에서 배경색과 텍스트 색상을 직접 지정할 수 있는 기본 정보 칩입니다.
 *
 * @param borderColor 1dp 테두리 색. null 이면 테두리를 그리지 않습니다
 */
@Composable
fun UInfoChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
) {
    UChip(
        text = text,
        modifier = modifier,
        backgroundColor = backgroundColor,
        textColor = textColor,
        textStyle = Caption1Bold,
        cornerRadius = 4.dp,
        contentPadding = PaddingValues(horizontal = 8.dp),
        minHeight = 24.dp,
        borderWidth = if (borderColor != null) 1.dp else 0.dp,
        borderColor = borderColor ?: Color.Transparent,
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

/**
 * 칩 한 개의 색
 *
 * @param border 1dp 테두리 색. null 이면 테두리 없이 그린다
 */
@Immutable
data class UPartChipColors(
    val background: Color,
    val text: Color,
    val border: Color? = null,
)

@Composable
private fun schoolChipColors() = UPartChipColors(grey800(), white())

/**
 * 파트 칩 색
 *
 * PM · Design · Web PE · Mobile PE 는 디자인 시스템 Tag/Part 의 테두리 형태(50 배경 · 100 테두리 · 600 글자)를 쓴다.
 * 지난 기수 파트와 미정은 시안에 색이 없어 예전 색(옅은 배경 · 진한 글자, 테두리 없음)을 그대로 둔다
 */
@Composable
fun partChipColors(part: UserPart): UPartChipColors {
    part.tagColors()?.let { return UPartChipColors(background = it.light, text = it.strong, border = it.base) }
    return when (part) {
        UserPart.ADMIN -> UPartChipColors(main100(), main600())
        UserPart.WEB -> UPartChipColors(yellow100(), yellow800())
        UserPart.ANDROID -> UPartChipColors(mint100(), teal600())
        UserPart.IOS -> UPartChipColors(yellow100(), yellow500())
        UserPart.SPRINGBOOT -> UPartChipColors(green100(), green500())
        UserPart.NODEJS -> UPartChipColors(yellow100(), yellow400())
        else -> UPartChipColors(grey100(), grey600())
    }
}

@Preview(showBackground = true)
@Composable
private fun UPartChipPreview() {
    UmcTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 11기 파트 (테두리 형태)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserPart.current.forEach { UInfoChip(part = it) }
            }
            // 학교 · 인프라 · 지난 기수 파트
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UInfoChip(text = "중앙대학교", type = UInfoChipType.SCHOOL)
                UInfraChip()
                UInfoChip(part = UserPart.ANDROID)
            }
        }
    }
}
