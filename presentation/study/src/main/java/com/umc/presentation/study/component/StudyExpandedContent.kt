package com.umc.presentation.study.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.presentation.study.ActStudyItemUiModel

/**
 * 카드 펼쳤을 때 나타나는 콘텐츠
 *
 * 상태별 표시:
 * - IN_PROGRESS + IDLE/READY: 링크 입력 폼
 * - CONFIRMING: 학습 완료 확인 화면
 * - REQUESTED: 대기중 배너
 * - PASS: 통과 배너
 * - FAIL: 실패 배너
 *
 * TODO: 실제 API 연결 후 제출 로직 교체
 */
@Composable
fun StudyExpandedContent(
    item: ActStudyItemUiModel,
    onSubmitClick: (Long, String) -> Unit,
    onConfirmClick: (Long) -> Unit,
) {
    when {
        // 링크 입력 화면
        item.status == StudyStatus.IN_PROGRESS &&
                (item.submitState == SubmitState.IDLE || item.submitState == SubmitState.READY) -> {
            StudyLinkInputSection(item = item, onSubmitClick = onSubmitClick)
        }
        // 제출 확인 화면
        item.submitState == SubmitState.CONFIRMING -> {
            StudyConfirmSection(item = item, onConfirmClick = onConfirmClick)
        }
        // 대기중 배너
        item.submitState == SubmitState.REQUESTED -> {
            StudyStatusBanner(
                iconRes = R.drawable.ic_act_hourglass,
                text = AppStrings.STUDY_STATUS_WAITING,
                textColor = warning700(),
                backgroundColor = warning100(),
            )
        }
        // 통과 배너
        item.status == StudyStatus.PASS -> {
            StudyStatusBanner(
                iconRes = R.drawable.ic_check_success,
                text = AppStrings.STUDY_STATUS_PASS,
                textColor = success700(),
                backgroundColor = success100(),
            )
        }
        // 실패 배너
        item.status == StudyStatus.FAIL -> {
            StudyStatusBanner(
                iconRes = R.drawable.ic_check_failed,
                text = AppStrings.STUDY_STATUS_FAIL,
                textColor = danger700(),
                backgroundColor = danger100(),
            )
        }
    }
}

/** 링크 입력 + 제출 버튼 */
@Composable
private fun StudyLinkInputSection(
    item: ActStudyItemUiModel,
    onSubmitClick: (Long, String) -> Unit,
) {
    var linkText by remember(item.id) { mutableStateOf(item.link) }
    val isEnabled = linkText.isNotBlank() // 링크 입력 여부

    Column {
        UText(
            text = AppStrings.STUDY_SUBMIT_LINK_TITLE,
            style = UmcTypographyTokens.SubheadlineBold,
            color = primary500(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = linkText,
            onValueChange = { linkText = it },
            placeholder = {
                UText(
                    text = AppStrings.STUDY_SUBMIT_LINK_PLACEHOLDER,
                    style = UmcTypographyTokens.Body,
                    color = neutral400(),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primary500(),
                unfocusedBorderColor = neutral300(),
                focusedContainerColor = neutral000(),
                unfocusedContainerColor = neutral000(),
            ),
            singleLine = true,
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { onSubmitClick(item.id, linkText) },
            enabled = isEnabled, // 비활성화
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary500(),
                disabledContainerColor = neutral200(),
                disabledContentColor = neutral400(),
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            contentPadding = PaddingValues(vertical = 13.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_blog_link),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isEnabled) neutral000() else neutral400()
            )
            Spacer(Modifier.width(6.dp))
            UText(
                text = AppStrings.STUDY_SUBMIT_LINK_BUTTON,
                style = UmcTypographyTokens.CalloutBold,
                color = if (isEnabled) neutral000() else neutral400(),
            )
        }
    }
}

/** 학습 완료 확인 화면 */
@Composable
private fun StudyConfirmSection(
    item: ActStudyItemUiModel,
    onConfirmClick: (Long) -> Unit,
) {
    Column {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = neutral100(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UText(
                    text = AppStrings.STUDY_CONFIRM_TITLE,
                    style = UmcTypographyTokens.SubheadlineBold,
                    color = neutral600(),
                )
                Spacer(Modifier.height(10.dp))
                UText(
                    text = AppStrings.STUDY_CONFIRM_DESCRIPTION,
                    style = UmcTypographyTokens.Footnote,
                    color = neutral500(),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onConfirmClick(item.id) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary500()),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            contentPadding = PaddingValues(vertical = 13.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_success),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = neutral000()
            )
            Spacer(Modifier.width(6.dp))
            UText(
                text = AppStrings.STUDY_CONFIRM_BUTTON,
                style = UmcTypographyTokens.CalloutBold,
                color = neutral000(),
            )
        }
    }
}

/** 상태 결과 배너 (대기중 / 통과 / 실패) */
@Composable
private fun StudyStatusBanner(
    iconRes: Int,
    text: String,
    textColor: Color,
    backgroundColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = textColor
            )
            Spacer(Modifier.width(8.dp))
            UText(
                text = text,
                style = UmcTypographyTokens.FootnoteBold,
                color = textColor
            )
        }
    }
}