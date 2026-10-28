package com.umc.presentation.community.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.umc.presentation.community.bottomsheet.create.CommunityCreateMemberBottomSheet
import com.umc.presentation.community.component.CommunityThreadEditorContent
import com.umc.presentation.community.component.CommunityThreadEditorMode
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.persistentListOf

/**
 * 커뮤니티 스레드 생성 화면
 *
 * 챌린저 선택, 제목/특징 입력,
 * AI 카테고리 분류 및 스레드 생성을 위한 UI를 구성합니다.
 * 입력 화면 자체는 수정 화면과 함께 [CommunityThreadEditorContent]를 사용합니다.
 */
@Composable
fun CommunityCreateScreen(
    state: CommunityCreateState,
    onAction: (CommunityCreateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    CommunityThreadEditorContent(
        mode = CommunityThreadEditorMode.Create,
        title = state.title,
        description = state.description,
        aiState = state.aiState,
        classifiedCategory = state.classifiedCategory,
        canRequestClassification = state.canRequestClassification,
        isSubmitEnabled = state.isCompleteEnabled,
        challengers = state.selectedChallengers,
        maxChallengerCount = state.maxChallengerCount,
        onBackClick = {
            onAction(CommunityCreateAction.OnBackClick)
        },
        onSubmitClick = {
            onAction(CommunityCreateAction.OnCompleteClick)
        },
        onTitleChanged = { title ->
            onAction(
                CommunityCreateAction.OnTitleChanged(
                    title = title,
                )
            )
        },
        onDescriptionChanged = { description ->
            onAction(
                CommunityCreateAction.OnDescriptionChanged(
                    description = description,
                )
            )
        },
        onCategorySelected = { category ->
            onAction(
                CommunityCreateAction.OnCategorySelected(
                    category = category,
                )
            )
        },
        onChallengerCardClick = {
            onAction(CommunityCreateAction.OnChallengerCardClick)
        },
        onRequestClassificationClick = {
            onAction(CommunityCreateAction.OnRequestClassificationClick)
        },
        onRetryClassificationClick = {
            onAction(CommunityCreateAction.OnRetryClassificationClick)
        },
        onChangeEmojiClick = {
            onAction(CommunityCreateAction.OnChangeEmojiClick)
        },
        modifier = modifier,
    )

    // 챌린저 선택 BottomSheet
    if (state.showChallengerBottomSheet) {
        CommunityCreateMemberBottomSheet(
            preSelected = state.selectedChallengers,
            maxCount = state.maxChallengerCount,
            onDismissRequest = {
                onAction(
                    CommunityCreateAction.OnDismissChallengerBottomSheet
                )
            },
            onConfirm = { challengers ->
                onAction(
                    CommunityCreateAction.OnChallengersSelected(
                        challengers = challengers,
                    )
                )
            },
            onCsvUploadClick = {
                // TODO CSV 파일 선택 기능 연결
            },
        )
    }
}

/**
 * 스레드 생성 화면 최초 진입 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateScreenPreview() {
    CommunityCreateScreen(
        state = CommunityCreateState(),
        onAction = {},
    )
}

/**
 * 스레드 생성 화면 입력 완료 및 AI 분류 성공 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateScreenFilledPreview() {
    CommunityCreateScreen(
        state = CommunityCreateState(
            title = "안드로이드 스터디",
            description = "매주 과제를 인증하고 코드 리뷰를 주고받아요",
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
            aiState = CommunityAiState.SUCCESS,
            classifiedCategory = CommunityCategory.STUDY,
            myMemberId = 1L,
        ),
        onAction = {},
    )
}