package com.umc.presentation.community.component.create

import com.umc.presentation.community.model.communityCharacterCount
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey950
import com.umc.component.theme.red400
import com.umc.component.theme.grey300
import com.umc.component.theme.indigo500
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview

/**
 * 스레드 생성/수정 화면의 기본 정보 입력 폼
 *
 * 스레드 제목과 특징(상세 내용)을 입력받습니다.
 */
@Composable
fun CommunityThreadForm(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleCount = communityCharacterCount(title)
    val descriptionCount = communityCharacterCount(description)
    var titleInputRejected by remember(title) { mutableStateOf(false) }
    var descriptionInputRejected by remember(description) { mutableStateOf(false) }
    val titleError = titleInputRejected || titleCount > 80
    val descriptionError = descriptionInputRejected || descriptionCount > 500

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 스레드 제목 입력 영역
        UText(
            text = "제목 ($titleCount/80)",
            style = UmcTypographyTokens.HeadlineBold,
            color = if (titleError) red400() else grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = title,
            onValueChange = onTitleChanged,
            acceptInput = {
                val count = communityCharacterCount(it)
                count <= 80 || count < titleCount
            },
            onInputRejected = { titleInputRejected = true },
            strokeColor = if (titleError) red400() else grey300(),
            focusStrokeColor = if (titleError) red400() else indigo500(),
            placeholder = "제목을 입력하세요",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        if (titleError) {
            Spacer(modifier = Modifier.height(4.dp))
            UText(
                text = "제목은 최대 80자까지 입력할 수 있어요.",
                style = UmcTypographyTokens.Footnote,
                color = red400(),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // AI 카테고리 분류 등에 사용되는 스레드 특징 입력 영역
        UText(
            text = "스레드 특징 ($descriptionCount/500)",
            style = UmcTypographyTokens.HeadlineBold,
            color = if (descriptionError) red400() else grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            acceptInput = {
                val count = communityCharacterCount(it)
                count <= 500 || count < descriptionCount
            },
            onInputRejected = { descriptionInputRejected = true },
            strokeColor = if (descriptionError) red400() else grey300(),
            focusStrokeColor = if (descriptionError) red400() else indigo500(),
            placeholder = "상세 내용을 입력하세요",
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default,
            ),
            verticalAlignment = Alignment.Top,
        )

        if (descriptionError) {
            Spacer(modifier = Modifier.height(4.dp))
            UText(
                text = "스레드 특징은 최대 500자까지 입력할 수 있어요.",
                style = UmcTypographyTokens.Footnote,
                color = red400(),
            )
        }
    }
}

/**
 * 입력 전 빈 폼 Preview
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityThreadFormEmptyPreview() {
    CommunityThreadForm(
        title = "",
        description = "",
        onTitleChanged = {},
        onDescriptionChanged = {},
        modifier = Modifier.padding(16.dp),
    )
}

/**
 * 제목 글자 수 초과로 오류가 표시되는 상태 Preview
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityThreadFormErrorPreview() {
    CommunityThreadForm(
        title = "가".repeat(81),
        description = "안드로이드 파트 스터디 과제 인증과 코드 리뷰를 나누는 스레드예요.",
        onTitleChanged = {},
        onDescriptionChanged = {},
        modifier = Modifier.padding(16.dp),
    )
}
