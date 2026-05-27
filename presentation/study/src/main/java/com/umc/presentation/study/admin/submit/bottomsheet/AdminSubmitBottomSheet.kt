package com.umc.presentation.study.admin.submit.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.presentation.study.admin.submit.AdminSubmitAction
import com.umc.presentation.study.admin.submit.AdminSubmitItemUiModel
import com.umc.presentation.study.admin.submit.AdminSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubmitBottomSheet(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    val item = state.bottomSheetItem ?: return

    ModalBottomSheet(
        onDismissRequest = { onAction(AdminSubmitAction.CloseBottomSheet) },
        containerColor = neutral000(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            UText(
                text = "스터디 피드백 하기",
                style = Title3Bold,
                color = neutral800(),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(neutral100(), RoundedCornerShape(1000.dp))
                    .padding(4.dp)
            ) {
                listOf("검토", "베스트 워크북").forEachIndexed { index, title ->
                    val isSelected = state.reviewTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) neutral000() else neutral100(),
                                RoundedCornerShape(1000.dp)
                            )
                            .clickable { onAction(AdminSubmitAction.OnReviewTabChanged(index)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UText(
                            text = title,
                            style = HeadlineBold,
                            color = if (isSelected) neutral800() else neutral400()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.reviewTabIndex == 0) {
                if (state.isReviewed) {
                    ReviewedContent(item = item, state = state, onAction = onAction)
                } else {
                    ReviewContent(item = item, state = state, onAction = onAction)
                }
            } else {
                AdminSubmitBestWorkbookContent(
                    state = state,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun SubmitUrlSection(item: AdminSubmitItemUiModel) {
    UText(text = "제출 URL", style = SubheadlineBold, color = neutral800())
    Spacer(Modifier.height(8.dp))

    // url
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral100(), RoundedCornerShape(8.dp))
            .border(1.dp, neutral200(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        UText(
            text = item.submitUrl.ifBlank { "http://github.com" },
            style = UmcTypographyTokens.Callout,
            color = if (item.submitUrl.isBlank()) neutral400() else neutral800()
        )
    }

    Spacer(Modifier.height(8.dp))

    // 링크 바로가기
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral100(), RoundedCornerShape(8.dp))
            .clickable { }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_go_website),
            contentDescription = null,
            tint = neutral600(),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        UText(text = "링크 바로가기", style = SubheadlineBold, color = neutral600())
    }
}

@Composable
private fun ReviewContent(
    item: AdminSubmitItemUiModel,
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    SubmitUrlSection(item = item)

    Spacer(Modifier.height(24.dp))

    UText(text = "피드백", style = SubheadlineBold, color = neutral800())
    Spacer(Modifier.height(8.dp))


    BasicTextField(
        value = state.feedback,
        onValueChange = { onAction(AdminSubmitAction.OnFeedbackChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(neutral000(), RoundedCornerShape(8.dp))
            .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = UmcTypographyTokens.Callout.copy(color = neutral800()),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (state.feedback.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력하세요.",
                        style = UmcTypographyTokens.Callout,
                        color = neutral400()
                    )
                }
                innerTextField()
            }
        }
    )

    Spacer(Modifier.height(16.dp))

    // 반려,통과 버튼
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UButton(
            text = "반려",
            onClick = { onAction(AdminSubmitAction.SubmitReview(false)) },
            modifier = Modifier.weight(1f).height(52.dp),
            enabled = state.isSubmitEnabled,
            backgroundColor = if (state.isSubmitEnabled) danger100() else neutral100(),
            textColor = if (state.isSubmitEnabled) danger500() else neutral300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_check_failed),
            prevIconTint = if (state.isSubmitEnabled) danger500() else neutral300(),
        )
        UButton(
            text = "통과",
            onClick = { onAction(AdminSubmitAction.SubmitReview(true)) },
            modifier = Modifier.weight(1f).height(52.dp),
            enabled = state.isSubmitEnabled,
            backgroundColor = if (state.isSubmitEnabled) success100() else neutral100(),
            textColor = if (state.isSubmitEnabled) success500() else neutral300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_check_success),
            prevIconTint = if (state.isSubmitEnabled) success500() else neutral300(),
        )
    }
}

@Composable
private fun ReviewedContent(
    item: AdminSubmitItemUiModel,
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    SubmitUrlSection(item = item)

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(text = "피드백", style = SubheadlineBold, color = neutral800())
        Spacer(Modifier.weight(1f))


        Icon(
            painter = painterResource(R.drawable.ic_setting),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(2.dp))
        UText(text = "현황 변경", style = Caption1, color = neutral500())
        Spacer(Modifier.width(8.dp))


        listOf("PASS", "FAIL").forEach { status ->
            val currentItemStatus = item.markStatus
            val pendingStatus = state.pendingStatus


            val isActive = if (pendingStatus != null) {
                pendingStatus == status
            } else {
                currentItemStatus == status
            }

            val bgColor = when {
                isActive && status == "PASS" -> success100()
                isActive && status == "FAIL" -> danger100()
                else -> neutral100()
            }
            val textColor = when {
                isActive && status == "PASS" -> success700()
                isActive && status == "FAIL" -> danger700()
                else -> neutral400()
            }

            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(4.dp))
                    .clickable { onAction(AdminSubmitAction.ChangeStatus(status)) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                UText(
                    text = if (status == "PASS") "Pass" else "Fail",
                    style = Caption1Bold,
                    color = textColor
                )
            }
            Spacer(Modifier.width(4.dp))
        }
    }

    Spacer(Modifier.height(8.dp))

    //입력창
    BasicTextField(
        value = state.feedback,
        onValueChange = { onAction(AdminSubmitAction.OnFeedbackChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(neutral000(), RoundedCornerShape(8.dp))
            .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = UmcTypographyTokens.Callout.copy(color = neutral800()),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (state.feedback.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력된 상태",
                        style = UmcTypographyTokens.Callout,
                        color = neutral400()
                    )
                }
                innerTextField()
            }
        }
    )

    Spacer(Modifier.height(16.dp))


    UButton(
        text = "완료하기",
        onClick = { onAction(AdminSubmitAction.CompleteChange) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = state.isSubmitEnabled,
        backgroundColor = if (state.isSubmitEnabled) primary500() else neutral200(),
        textColor = if (state.isSubmitEnabled) neutral000() else neutral300(),
        textStyle = HeadlineBold,
        cornerRadius = 8.dp,
    )
}