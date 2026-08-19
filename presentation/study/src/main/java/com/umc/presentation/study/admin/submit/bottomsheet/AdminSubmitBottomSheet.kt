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

/**
 * 관리자 스터디 제출 상세 BottomSheet
 *
 * 주요 기능
 * - 제출 URL 및 제출 내용 확인
 * - 관리자 피드백 작성
 * - PASS / FAIL 승인 및 반려
 * - 기존 피드백 및 제출 상태 수정
 * - 베스트 워크북 등록/수정 탭 제공
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubmitBottomSheet(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    val item = state.bottomSheetItem ?: return

    ModalBottomSheet(
        onDismissRequest = { onAction(AdminSubmitAction.CloseBottomSheet) },
        containerColor = grey000(),
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
                color = grey800(),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grey100(), RoundedCornerShape(1000.dp))
                    .padding(4.dp)
            ) {
                listOf("검토", "베스트 워크북").forEachIndexed { index, title ->
                    val isSelected = state.reviewTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) grey000() else grey100(),
                                RoundedCornerShape(1000.dp)
                            )
                            .clickable { onAction(AdminSubmitAction.OnReviewTabChanged(index)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UText(
                            text = title,
                            style = HeadlineBold,
                            color = if (isSelected) grey800() else grey400()
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


/**
 * 챌린저가 제출한 URL을 표시하는 영역
 *
 * 제출 URL과 링크 바로가기 버튼을 제공합니다.
 */
@Composable
private fun SubmitUrlSection(item: AdminSubmitItemUiModel) {
    UText(text = "제출 URL", style = SubheadlineBold, color = grey800())
    Spacer(Modifier.height(8.dp))

    // url
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey100(), RoundedCornerShape(8.dp))
            .border(1.dp, grey200(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        UText(
            text = item.submitUrl.ifBlank { "http://github.com" },
            style = UmcTypographyTokens.Callout,
            color = if (item.submitUrl.isBlank()) grey400() else grey800()
        )
    }

    Spacer(Modifier.height(8.dp))

    // 링크 바로가기
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey100(), RoundedCornerShape(8.dp))
            .clickable { }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_link),
            contentDescription = null,
            tint = grey600(),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        UText(text = "링크 바로가기", style = SubheadlineBold, color = grey600())
    }
}

/**
 * 아직 검토되지 않은 제출물의 피드백 작성 영역
 *
 * 피드백을 작성한 뒤 제출을 통과 또는 반려할 수 있습니다.
 */
@Composable
private fun ReviewContent(
    item: AdminSubmitItemUiModel,
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    SubmitUrlSection(item = item)

    Spacer(Modifier.height(24.dp))

    UText(text = "피드백", style = SubheadlineBold, color = grey800())
    Spacer(Modifier.height(8.dp))


    BasicTextField(
        value = state.feedback,
        onValueChange = { onAction(AdminSubmitAction.OnFeedbackChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(grey000(), RoundedCornerShape(8.dp))
            .border(1.dp, grey300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = UmcTypographyTokens.Callout.copy(color = grey800()),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (state.feedback.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력하세요.",
                        style = UmcTypographyTokens.Callout,
                        color = grey400()
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
            backgroundColor = if (state.isSubmitEnabled) red100() else grey100(),
            textColor = if (state.isSubmitEnabled) red500() else grey300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_check_failed),
            prevIconTint = if (state.isSubmitEnabled) red500() else grey300(),
        )
        UButton(
            text = "통과",
            onClick = { onAction(AdminSubmitAction.SubmitReview(true)) },
            modifier = Modifier.weight(1f).height(52.dp),
            enabled = state.isSubmitEnabled,
            backgroundColor = if (state.isSubmitEnabled) green100() else grey100(),
            textColor = if (state.isSubmitEnabled) green500() else grey300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_check_success),
            prevIconTint = if (state.isSubmitEnabled) green500() else grey300(),
        )
    }
}

/**
 * 이미 검토가 완료된 제출물의 피드백 수정 영역
 *
 * 기존 피드백을 수정하고 PASS / FAIL 상태를 변경할 수 있습니다.
 */
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
        UText(text = "피드백", style = SubheadlineBold, color = grey800())
        Spacer(Modifier.weight(1f))


        Icon(
            painter = painterResource(R.drawable.ic_setting),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(2.dp))
        UText(text = "현황 변경", style = Caption1, color = grey500())
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
                isActive && status == "PASS" -> green100()
                isActive && status == "FAIL" -> red100()
                else -> grey100()
            }
            val textColor = when {
                isActive && status == "PASS" -> green700()
                isActive && status == "FAIL" -> red700()
                else -> grey400()
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
            .background(grey000(), RoundedCornerShape(8.dp))
            .border(1.dp, grey300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = UmcTypographyTokens.Callout.copy(color = grey800()),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (state.feedback.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력하세요.",
                        style = UmcTypographyTokens.Callout,
                        color = grey400()
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
        backgroundColor = if (state.isSubmitEnabled) green500() else grey200(),
        textColor = if (state.isSubmitEnabled) grey000() else grey300(),
        textStyle = HeadlineBold,
        cornerRadius = 8.dp,
    )
}