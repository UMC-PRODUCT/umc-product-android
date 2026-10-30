package com.umc.presentation.community.component.create

import com.umc.presentation.community.model.communityCharacterCount
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey950
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

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 스레드 제목 입력 영역
        CommunityThreadFormLabel(
            label = communityRequiredLabel("제목"),
            count = titleCount,
            maxCount = 80,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 최대 글자 수를 넘는 입력은 받지 않음 (지우는 입력은 허용)
        UTextField(
            value = title,
            onValueChange = onTitleChanged,
            acceptInput = {
                // 80자 제한
                val count = communityCharacterCount(it)
                count <= 80 || count < titleCount
            },
            placeholder = "제목을 입력하세요",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // AI 카테고리 분류 등에 사용되는 스레드 특징 입력 영역
        CommunityThreadFormLabel(
            label = AnnotatedString("스레드 특징"),
            count = descriptionCount,
            maxCount = 500,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 최대 글자 수를 넘는 입력은 받지 않음 (지우는 입력은 허용)
        UTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            acceptInput = {
                // 500자 제한
                val count = communityCharacterCount(it)
                count <= 500 || count < descriptionCount
            },
            placeholder = "상세 내용을 입력하세요",
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default,
            ),
            verticalAlignment = Alignment.Top,
        )
    }
}

/**
 * 입력 항목 라벨과 글자 수 카운터
 *
 * 라벨은 왼쪽, 현재 글자 수/최대 글자 수는 오른쪽 끝에 표시합니다.
 */
@Composable
private fun CommunityThreadFormLabel(
    label: AnnotatedString,
    count: Int,
    maxCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        UText(
            text = "$count/$maxCount",
            style = UmcTypographyTokens.Subheadline,
            color = grey400(),
        )
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
 * 제목이 최대 글자 수(80자)까지 입력된 상태 Preview
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityThreadFormMaxLengthPreview() {
    CommunityThreadForm(
        title = "가".repeat(80),
        description = "안드로이드 파트 스터디 과제 인증과 코드 리뷰를 나누는 스레드예요.",
        onTitleChanged = {},
        onDescriptionChanged = {},
        modifier = Modifier.padding(16.dp),
    )
}
