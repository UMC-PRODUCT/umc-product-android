package com.umc.component.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.green100
import com.umc.component.theme.green700
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo600
import com.umc.component.theme.red100
import com.umc.component.theme.red600
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow600
import com.umc.component.theme.white
import com.umc.component.theme.green500
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow400
import com.umc.component.theme.indigo800
import com.umc.component.theme.yellow800
import com.umc.component.theme.mint100
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
        // 지난 기수 파트. 디자인 지정색(Admin 보라 · PM 마젠타 · Web 브라운 · Android 시안 …)에
        // 가장 가까운 팔레트 계열을 쓰고, 칩 규칙대로 배경은 옅은 단계 · 글자는 진한 단계로 둔다.
        UserPart.ADMIN -> UInfoChipStyle(indigo100(), indigo600())
        UserPart.PLAN -> UInfoChipStyle(indigo100(), indigo800())
        UserPart.DESIGN -> UInfoChipStyle(red100(), red600())
        UserPart.WEB -> UInfoChipStyle(yellow100(), yellow800())
        UserPart.ANDROID -> UInfoChipStyle(mint100(), teal600())
        UserPart.IOS -> UInfoChipStyle(yellow100(), yellow500())
        UserPart.SPRINGBOOT -> UInfoChipStyle(green100(), green500())
        UserPart.NODEJS -> UInfoChipStyle(yellow100(), yellow400())

        // 11기에 생긴 파트
        UserPart.WEB_PRODUCT_ENGINEER -> UInfoChipStyle(yellow100(), yellow600())
        UserPart.MOBILE_PRODUCT_ENGINEER -> UInfoChipStyle(green100(), green700())

        UserPart.UNKNOWN -> UInfoChipStyle(grey100(), grey600())
    }
}
