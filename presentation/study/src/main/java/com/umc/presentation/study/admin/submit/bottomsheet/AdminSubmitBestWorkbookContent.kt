package com.umc.presentation.study.admin.submit.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.presentation.study.admin.submit.AdminSubmitAction
import com.umc.presentation.study.admin.submit.AdminSubmitState

@Composable
fun AdminSubmitBestWorkbookContent(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    // 선정하기 다이얼로그
    if (state.showBestConfirmDialog) {
        UDialog(
            title = "선정하기",
            content = "베스트 워크북으로 선정하시겠습니까?",
            isTwoButton = true,
            positiveText = "선정하기",
            negativeText = "취소",
            onPositive = { onAction(AdminSubmitAction.ConfirmBest) },
            onNegative = { onAction(AdminSubmitAction.DismissBestDialog) },
            onDismissRequest = { onAction(AdminSubmitAction.DismissBestDialog) }
        )
    }

    // 취소하기 다이얼로그
    if (state.showBestCancelDialog) {
        UDialog(
            title = "취소하기",
            content = "베스트 워크북 선정을 취소하시겠습니까?",
            isTwoButton = true,
            positiveText = "취소하기",
            negativeText = "취소",
            onPositive = { onAction(AdminSubmitAction.ConfirmCancelBest) },
            onNegative = { onAction(AdminSubmitAction.DismissBestDialog) },
            onDismissRequest = { onAction(AdminSubmitAction.DismissBestDialog) }
        )
    }

    // 안내 배너
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(warning100())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_filled),
            contentDescription = null,
            tint = warning500(),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(Modifier.width(8.dp))
        UText(
            text = "우수한 스터디 제출물을 커뮤니티 '명예의 전당'에 등록될 수 있습니다.",
            style = Subheadline,
            color = warning700()
        )
    }

    Spacer(Modifier.height(16.dp))

    // 추천사
    UText(text = "추천사(커뮤니티 공개용)", style = SubheadlineBold, color = neutral800())
    Spacer(Modifier.height(8.dp))

    // 입력창
    BasicTextField(
        value = state.bestComment,
        onValueChange = { onAction(AdminSubmitAction.OnBestCommentChanged(it)) },
        enabled = !state.isBestRegistered || state.isEditingBest,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(neutral000(), RoundedCornerShape(8.dp))
            .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = Callout.copy(color = neutral800()),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (state.bestComment.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력하세요.",
                        style = Callout,
                        color = neutral400()
                    )
                }
                innerTextField()
            }
        }
    )

    if (!state.isBestRegistered) {
        Spacer(Modifier.height(110.dp))
        UButton(
            text = "등록하기",
            onClick = { onAction(AdminSubmitAction.RegisterBest) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.bestComment.isNotBlank(),
            backgroundColor = if (state.bestComment.isNotBlank()) primary500() else neutral200(),
            textColor = if (state.bestComment.isNotBlank()) neutral000() else neutral300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
        )
    } else if (state.isEditingBest) {
        Spacer(Modifier.height(110.dp))
        UButton(
            text = "완료하기",
            onClick = { onAction(AdminSubmitAction.CompleteBest) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.bestComment.isNotBlank(),
            backgroundColor = if (state.bestComment.isNotBlank()) primary500() else neutral200(),
            textColor = if (state.bestComment.isNotBlank()) neutral000() else neutral300(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
        )
    } else {
        Spacer(Modifier.height(16.dp))
        UButton(
            text = "등록 취소하기",
            onClick = { onAction(AdminSubmitAction.CancelBest) },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            backgroundColor = neutral000(),
            textColor = danger500(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            borderWidth = 1.dp,
            borderColor = danger500(),
            prevIcon = painterResource(R.drawable.ic_check_failed),
            prevIconTint = danger500(),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(55.dp))
        UButton(
            text = "수정하기",
            onClick = { onAction(AdminSubmitAction.EditBest) },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            backgroundColor = neutral000(),
            textColor = neutral800(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            borderWidth = 1.dp,
            borderColor = neutral300(),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp),
        )
    }
}