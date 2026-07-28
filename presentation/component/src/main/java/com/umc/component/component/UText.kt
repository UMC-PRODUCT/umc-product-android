package com.umc.component.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo600

/**
 * 시스템 글자 크기 설정과 관계없이 디자인 스펙의 글자 크기를 유지하는 공용 텍스트입니다.
 *
 * 활동 화면의 목록, 상태 카드, 다이얼로그에서 Compose [Text] 대신 사용합니다.
 * 색상을 지정하지 않으면 전달된 [style]의 색상을 그대로 사용합니다.
 *
 * @param text 표시할 문자열
 * @param modifier 크기와 배치를 지정하는 Modifier
 * @param color 텍스트 색상
 * @param textDecoration 텍스트 장식
 * @param textAlign 텍스트 정렬
 * @param overflow 영역을 벗어난 텍스트 처리 방식
 * @param softWrap 자동 줄바꿈 여부
 * @param maxLines 최대 줄 수
 * @param minLines 최소 줄 수
 * @param onTextLayout 텍스트 레이아웃 완료 콜백
 * @param style 텍스트 스타일
 */
@Composable
fun UText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val currentDensity = LocalDensity.current
    val fixedFontScaleDensity = remember(currentDensity) {
        Density(density = currentDensity.density, fontScale = 1f)
    }

    CompositionLocalProvider(LocalDensity provides fixedFontScaleDensity) {
        Text(
            text = text,
            style = style,
            color = color,
            modifier = modifier,
            textDecoration = textDecoration,
            textAlign = textAlign,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout ?: {}
        )
    }
}

@Composable
fun HuggText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val currentDensity = LocalDensity.current
    val fixedFontScaleDensity = remember(currentDensity) {
        Density(density = currentDensity.density, fontScale = 1f)
    }

    CompositionLocalProvider(LocalDensity provides fixedFontScaleDensity) {
        Text(
            text = text,
            style = style,
            color = color,
            modifier = modifier,
            textDecoration = textDecoration,
            textAlign = textAlign,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout ?: {}
        )
    }
}

/**
 * 홈 프로필 카드에서 활동 일수를 강조한 문장을 생성합니다.
 *
 * @param day 누적 활동 일수
 */
@Composable
fun getGrowthText(day: Int): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = indigo600())) {
        append("${day}일째")
    }
    withStyle(style = SpanStyle(color = grey800())) {
        append(" 성장하고 있어요")
    }
}
