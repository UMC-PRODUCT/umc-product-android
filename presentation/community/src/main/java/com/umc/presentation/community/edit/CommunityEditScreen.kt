package com.umc.presentation.community.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberBottomSheet
import com.umc.presentation.community.component.create.CommunityAiCard
import com.umc.presentation.community.component.create.CommunityChallengerCard
import com.umc.presentation.community.component.create.CommunityCreateTopBar
import com.umc.presentation.community.component.create.CommunityDeleteButton
import com.umc.presentation.community.component.create.CommunityThreadForm

@Composable
fun CommunityEditScreen(
    state: CommunityEditState,
    onAction: (CommunityEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(grey000())
            .navigationBarsPadding(),
    ) {
        CommunityCreateTopBar(
            title = "스레드 수정하기",
            actionText = "완료",
            isActionEnabled = state.isSaveEnabled,
            onBackClick = {
                onAction(
                    CommunityEditAction.OnBackClick
                )
            },
            onActionClick = {
                onAction(
                    CommunityEditAction.OnSaveClick
                )
            },
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 240.dp,
                    ),
            ) {

                UText(
                    text = "챌린저 명단",
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                CommunityChallengerCard(
                    challengers = state.selectedChallengers,
                    maxCount = state.maxChallengerCount,
                    currentCount = state.currentChallengerCount,
                    onClick = {
                        onAction(
                            CommunityEditAction.OnChallengerCardClick
                        )
                    },
                )

                Spacer(
                    modifier = Modifier.height(32.dp),
                )

                CommunityThreadForm(
                    title = state.title,
                    description = state.description,
                    onTitleChanged = { title ->
                        onAction(
                            CommunityEditAction.OnTitleChanged(
                                title = title,
                            )
                        )
                    },
                    onDescriptionChanged = { description ->
                        onAction(
                            CommunityEditAction.OnDescriptionChanged(
                                description = description,
                            )
                        )
                    },
                )

                Spacer(
                    modifier = Modifier.height(32.dp),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    ),
            ) {
                CommunityAiCard(
                    aiState = state.aiState,
                    classifiedCategory = state.classifiedCategory,
                    canRequestClassification =
                        state.canRequestClassification,
                    onRequestClassificationClick = {
                        onAction(
                            CommunityEditAction
                                .OnRetryClassificationClick
                        )
                    },
                    onRetryClassificationClick = {
                        onAction(
                            CommunityEditAction
                                .OnRetryClassificationClick
                        )
                    },
                    onChangeEmojiClick = {
                        onAction(
                            CommunityEditAction.OnChangeEmojiClick
                        )
                    },
                )

                Spacer(
                    modifier = Modifier.height(16.dp),
                )

                CommunityDeleteButton(
                    onClick = {
                        onAction(
                            CommunityEditAction.OnDeleteThreadClick
                        )
                    },
                )
            }
        }
    }

    if (state.showDeleteDialog) {
        UBasicDialog(
            title = "스레드를 삭제하시겠습니까?",
            content = "이 작업은 되돌릴 수 없어요.",
            negativeText = "취소",
            positiveText = "삭제하기",
            type = DialogType.ERROR,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey600(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onNegative = {
                onAction(
                    CommunityEditAction.OnDismissDeleteDialog
                )
            },
            onPositive = {
                onAction(
                    CommunityEditAction.OnConfirmDeleteClick
                )
            },
            onDismissRequest = {
                onAction(
                    CommunityEditAction.OnDismissDeleteDialog
                )
            },
        )
    }

    if (
        state.showChallengerBottomSheet &&
        state.threadId.isNotBlank()
    ) {
        CommunityMemberBottomSheet(
            threadId = state.threadId,
            onDismissRequest = {
                onAction(
                    CommunityEditAction.OnDismissChallengerBottomSheet
                )
            },
            onInviteSuccess = {
                onAction(
                    CommunityEditAction.OnMemberInviteSuccess
                )
            },
        )
    }
}
