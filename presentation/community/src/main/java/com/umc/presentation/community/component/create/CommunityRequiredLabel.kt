package com.umc.presentation.community.component.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey950
import com.umc.component.theme.red500

/**
 * 필수 입력 항목 라벨 뒤에 red500 `*`를 붙인 텍스트를 만듭니다.
 *
 * Figma 라벨(`제목 *`, `아이콘 *`, `초대할 챌린저 *`)처럼 라벨 색상은 UText의 color를 따르고,
 * `*`만 red500으로 표시합니다. [UText]의 AnnotatedString 오버로드에 그대로 넘겨 사용합니다.
 *
 * @param label 필수 항목 이름
 */
@Composable
fun communityRequiredLabel(label: String): AnnotatedString {
    val asteriskColor = red500()

    return buildAnnotatedString {
        append(label)

        withStyle(SpanStyle(color = asteriskColor)) {
            append(" *")
        }
    }
}

/**
 * 필수 입력 항목 라벨 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityRequiredLabelPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        UText(
            text = communityRequiredLabel("제목"),
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        UText(
            text = communityRequiredLabel("아이콘"),
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        UText(
            text = communityRequiredLabel("초대할 챌린저"),
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )
    }
}
