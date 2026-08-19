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

/**
 * 관리자 제출 상세 BottomSheet의 베스트 워크북 탭 콘텐츠
 *
 * 주요 기능
 * - 베스트 워크북 등록
 * - 베스트 선정 사유 입력
 * - 기존 선정 사유 수정
 * - 베스트 워크북 등록 취소
 * - 등록 및 취소 확인 Dialog 표시
 */
@Composable
fun AdminSubmitBestWorkbookContent(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit,
) {
    // 베스트 워크북 선정 확인 Dialog
    if (state.showBestConfirmDialog) {
        UDialog(
            title = "선정하기",
            content = "베스트 워크북으로 선정하시겠습니까?",
            isTwoButton = true,
            positiveText = "선정하기",
            negativeText = "취소",
            negativeBackgroundColor = grey100(),
            negativeTextColor = grey800(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = indigo500(),
            positiveTextColor = grey000(),
            positiveBorderColor = indigo500(),
            onPositive = {
                onAction(AdminSubmitAction.ConfirmBest)
            },
            onNegative = {
                onAction(AdminSubmitAction.DismissBestDialog)
            },
            onDismissRequest = {
                onAction(AdminSubmitAction.DismissBestDialog)
            }
        )
    }

    // 베스트 워크북 선정 취소 확인 Dialog
    if (state.showBestCancelDialog) {
        UDialog(
            title = "취소하기",
            content = "베스트 워크북 선정을 취소하시겠습니까?",
            isTwoButton = true,
            positiveText = "취소하기",
            negativeText = "취소",
            negativeBackgroundColor = grey100(),
            negativeTextColor = grey800(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveTextColor = red500(),
            positiveBorderColor = red100(),
            onPositive = {
                onAction(AdminSubmitAction.ConfirmCancelBest)
            },
            onNegative = {
                onAction(AdminSubmitAction.DismissBestDialog)
            },
            onDismissRequest = {
                onAction(AdminSubmitAction.DismissBestDialog)
            }
        )
    }

    // 베스트 워크북 등록 안내 배너
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(yellow100())
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_error_filled
            ),
            contentDescription = null,
            tint = yellow500(),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        UText(
            text = "우수한 스터디 제출물을 커뮤니티 '명예의 전당'에 등록될 수 있습니다.",
            style = Subheadline,
            color = yellow700()
        )
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    // 베스트 선정 사유 입력 영역
    UText(
        text = "추천사(커뮤니티 공개용)",
        style = SubheadlineBold,
        color = grey800()
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    /**
     * 베스트 선정 사유 입력창
     *
     * 이미 베스트로 등록된 경우에는 읽기 전용이며,
     * 수정하기 버튼을 누른 경우에만 다시 입력할 수 있습니다.
     */
    BasicTextField(
        value = state.bestCommentDraft,
        onValueChange = {
            onAction(
                AdminSubmitAction.OnBestCommentChanged(it)
            )
        },
        enabled =
            !state.isBestRegistered ||
                    state.isEditingBest,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                grey000(),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                grey300(),
                RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        textStyle = Callout.copy(
            color = grey800()
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.TopStart
            ) {
                if (state.bestCommentDraft.isEmpty()) {
                    UText(
                        text = "챌린저에게 전달할 피드백을 입력하세요.",
                        style = Callout,
                        color = grey400()
                    )
                }

                innerTextField()
            }
        }
    )

    /**
     * 아직 베스트 등록 전
     * → 등록하기 버튼 표시
     */
    if (!state.isBestRegistered) {
        Spacer(
            modifier = Modifier.height(110.dp)
        )

        UButton(
            text = "등록하기",
            onClick = {
                onAction(
                    AdminSubmitAction.RegisterBest
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled =
                state.bestCommentDraft.isNotBlank(),
            backgroundColor =
                if (state.bestCommentDraft.isNotBlank()) {
                    indigo500()
                } else {
                    grey200()
                },
            textColor =
                if (state.bestCommentDraft.isNotBlank()) {
                    grey000()
                } else {
                    grey300()
                },
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
        )
    }

    /**
     * 베스트 선정 사유 수정 중
     * → 완료하기 버튼 표시
     */
    else if (state.isEditingBest) {
        Spacer(
            modifier = Modifier.height(110.dp)
        )

        UButton(
            text = "완료하기",
            onClick = {
                onAction(
                    AdminSubmitAction.CompleteBest
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled =
                state.bestCommentDraft.isNotBlank(),
            backgroundColor =
                if (state.bestCommentDraft.isNotBlank()) {
                    indigo500()
                } else {
                    indigo500()
                },
            textColor =
                if (state.bestCommentDraft.isNotBlank()) {
                    grey000()
                } else {
                    grey300()
                },
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
        )
    }

    /**
     * 이미 베스트 등록 완료
     * → 등록 취소 / 수정하기 버튼 표시
     */
    else {
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        UButton(
            text = "등록 취소하기",
            onClick = {
                onAction(
                    AdminSubmitAction.CancelBest
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            backgroundColor = grey000(),
            textColor = red500(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            borderWidth = 1.dp,
            borderColor = red500(),
            prevIcon = painterResource(
                R.drawable.ic_check_failed
            ),
            prevIconTint = red500(),
            contentPadding = PaddingValues(
                horizontal = 13.dp,
                vertical = 12.dp,
            ),
        )

        Spacer(
            modifier = Modifier.height(55.dp)
        )

        UButton(
            text = "수정하기",
            onClick = {
                onAction(
                    AdminSubmitAction.EditBest
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            backgroundColor = grey000(),
            textColor = grey800(),
            textStyle = HeadlineBold,
            cornerRadius = 8.dp,
            borderWidth = 1.dp,
            borderColor = grey300(),
            contentPadding = PaddingValues(
                horizontal = 13.dp,
                vertical = 12.dp,
            ),
        )
    }
}