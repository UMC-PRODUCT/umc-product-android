package com.umc.presentation.community.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberBottomSheet
import com.umc.presentation.community.component.create.CommunityThreadEditorContent
import com.umc.presentation.community.component.create.CommunityThreadEditorMode
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.persistentListOf

/**
 * 커뮤니티 스레드 수정 화면
 *
 * 생성 화면과 같은 [CommunityThreadEditorContent]를 수정 모드로 사용합니다.
 * 챌린저 관리, 제목/특징 수정, AI 카테고리 재분류,
 * 스레드 수정(저장) 및 삭제 기능을 제공합니다.
 */
@Composable
fun CommunityEditScreen(
    state: CommunityEditState,
    onAction: (CommunityEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    CommunityThreadEditorContent(
        mode = CommunityThreadEditorMode.Edit(
            canDelete = state.canEdit,
        ),
        title = state.title,
        description = state.description,
        aiState = state.aiState,
        classifiedCategory = state.classifiedCategory,
        canRequestClassification = state.canRequestClassification,
        isSubmitEnabled = state.isSaveEnabled,
        challengers = state.selectedChallengers,
        maxChallengerCount = state.maxChallengerCount,
        currentChallengerCount = state.currentChallengerCount,
        isLoading = state.isLoading,
        onBackClick = {
            onAction(CommunityEditAction.OnBackClick)
        },
        onSubmitClick = {
            onAction(CommunityEditAction.OnSaveClick)
        },
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
        onCategorySelected = { category ->
            onAction(
                CommunityEditAction.OnCategorySelected(
                    category = category,
                )
            )
        },
        // 클릭 시 챌린저 관리 BottomSheet 표시
        onChallengerCardClick = {
            onAction(CommunityEditAction.OnChallengerCardClick)
        },
        onRequestClassificationClick = {
            onAction(CommunityEditAction.OnRetryClassificationClick)
        },
        onRetryClassificationClick = {
            onAction(CommunityEditAction.OnRetryClassificationClick)
        },
        onChangeEmojiClick = {
            onAction(CommunityEditAction.OnChangeEmojiClick)
        },
        onDeleteClick = {
            onAction(CommunityEditAction.OnDeleteThreadClick)
        },
        modifier = modifier,
    )

    // 스레드 삭제 확인 Dialog
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

    // 챌린저 추가/삭제를 위한 멤버 관리 BottomSheet
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

/**
 * 스레드 상세 조회 중 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityEditScreenLoadingPreview() {
    UmcTheme {
        CommunityEditScreen(
            state = CommunityEditState(
                isLoading = true,
            ),
            onAction = {},
        )
    }
}

/**
 * 스레드 상세 조회 완료 Preview (저장 버튼 + 하단 삭제 버튼)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityEditScreenLoadedPreview() {
    UmcTheme {
        CommunityEditScreen(
            state = CommunityEditState(
                threadId = "1",
                title = "알고리즘 스터디",
                description = "매주 코딩테스트 문제를 함께 풉니다.",
                selectedChallengers = persistentListOf(
                    CommunityChallengerUiModel(
                        memberId = 2L,
                        name = "김챌린저",
                        nickname = "챌린",
                        school = "UMC대학교",
                        generation = 9L,
                        partLabel = "Android",
                        profileImage = "",
                    ),
                ),
                currentChallengerCount = 1,
                isLoading = false,
                canEdit = true,
                aiState = CommunityAiState.SUCCESS,
                classifiedCategory = CommunityCategory.STUDY,
            ),
            onAction = {},
        )
    }
}
