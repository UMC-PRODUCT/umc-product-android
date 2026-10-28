package com.umc.presentation.community.component.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.presentation.community.R
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * 스레드 생성/수정 화면이 함께 쓰는 입력 화면의 모드
 */
sealed interface CommunityThreadEditorMode {

    /** 스레드 생성 */
    data object Create : CommunityThreadEditorMode

    /**
     * 스레드 수정
     *
     * @param canDelete 하단 스레드 삭제 버튼 표시 여부
     */
    data class Edit(
        val canDelete: Boolean,
    ) : CommunityThreadEditorMode
}

/**
 * 스레드 생성/수정 공용 입력 화면
 *
 * 상단바, 미리보기, 제목/특징 입력, 아이콘 선택, 챌린저 영역, AI 카테고리 카드를 구성합니다.
 * 수정 모드에서는 상단 버튼이 "저장"으로 바뀌고 스크롤 영역 맨 아래에 삭제 버튼이 붙습니다.
 * 상태와 BottomSheet·Dialog는 각 화면(생성/수정)이 소유합니다.
 */
@Composable
fun CommunityThreadEditorContent(
    mode: CommunityThreadEditorMode,
    title: String,
    description: String,
    aiState: CommunityAiState,
    classifiedCategory: CommunityCategory?,
    canRequestClassification: Boolean,
    isSubmitEnabled: Boolean,
    challengers: ImmutableList<CommunityChallengerUiModel>,
    maxChallengerCount: Int,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    onChallengerCardClick: () -> Unit,
    onRequestClassificationClick: () -> Unit,
    onRetryClassificationClick: () -> Unit,
    onChangeEmojiClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentChallengerCount: Int = challengers.size,
    isLoading: Boolean = false,
    onDeleteClick: () -> Unit = {},
) {
    val isEditMode = mode is CommunityThreadEditorMode.Edit

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(grey000())
            .navigationBarsPadding(),
    ) {
        CommunityCreateTopBar(
            title = if (isEditMode) "스레드 수정하기" else "스레드 만들기",
            actionText = if (isEditMode) "저장" else "완료",
            isActionEnabled = isSubmitEnabled,
            onBackClick = onBackClick,
            onActionClick = onSubmitClick,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        )

        if (isLoading) {
            // 스레드 상세 조회가 끝나기 전에는 빈 입력칸과 기본 카테고리 카드를 보여주지 않는다.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = indigo500(),
                )
            }
            return@Column
        }

        // 탑 바를 제외한 스크롤 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            CommunityThreadEditorFields(
                mode = mode,
                title = title,
                description = description,
                classifiedCategory = classifiedCategory,
                challengers = challengers,
                maxChallengerCount = maxChallengerCount,
                currentChallengerCount = currentChallengerCount,
                onTitleChanged = onTitleChanged,
                onDescriptionChanged = onDescriptionChanged,
                onCategorySelected = onCategorySelected,
                onChallengerCardClick = onChallengerCardClick,
                onDeleteClick = onDeleteClick,
            )

            /**
             * AI 카테고리 분류 카드
             *
             * 입력 상태에 따라 안내, 로딩,
             * 분류 결과 및 재분류 상태를 표시합니다.
             */
            CommunityAiCard(
                aiState = aiState,
                classifiedCategory = classifiedCategory,
                canRequestClassification = canRequestClassification,
                onRequestClassificationClick = onRequestClassificationClick,
                onRetryClassificationClick = onRetryClassificationClick,
                onChangeEmojiClick = onChangeEmojiClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .then(
                        if (aiState == CommunityAiState.GUIDE) {
                            // 입력 중 키보드에 AI 카드가 가려지지 않도록 처리
                            Modifier
                                .imePadding()
                                .padding(bottom = 8.dp)
                        } else {
                            Modifier.padding(bottom = 16.dp)
                        }
                    ),
            )
        }
    }
}

/**
 * AI 카드 아래로 스크롤되는 입력 영역
 */
@Composable
private fun CommunityThreadEditorFields(
    mode: CommunityThreadEditorMode,
    title: String,
    description: String,
    classifiedCategory: CommunityCategory?,
    challengers: ImmutableList<CommunityChallengerUiModel>,
    maxChallengerCount: Int,
    currentChallengerCount: Int,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    onChallengerCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 64.dp,
                top = 8.dp,
            ),
    ) {
        // 미리보기 영역
        UText(
            text = "미리보기",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 분류 전에는 아이콘 선택 기본값인 자유로 표시
        val previewCategory = classifiedCategory ?: CommunityCategory.FREE

        CommunityThreadPreview(
            title = title,
            description = description,
            category = previewCategory,
            iconRes = previewCategory.previewIconRes(),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 스레드 제목 및 특징 입력 영역
        CommunityThreadForm(
            title = title,
            description = description,
            onTitleChanged = onTitleChanged,
            onDescriptionChanged = onDescriptionChanged,
        )

        Spacer(modifier = Modifier.height(32.dp))

        UText(
            text = communityRequiredLabel("아이콘"),
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 아이콘 선택 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            iconSelectCategories.forEach { category ->
                // 화면 폭을 넘지 않도록 폭을 균등하게 나누고,
                // 라벨이 줄바꿈되어도 가장 높은 아이템에 높이를 맞춤
                CommunityIconSelectItem(
                    label = category.label,
                    iconRes = category.previewIconRes(),
                    selected = previewCategory == category,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // 챌린저 영역 (생성: 초대할 챌린저 선택 / 수정: 현재 참여 중인 챌린저 관리)
        UText(
            text = if (mode is CommunityThreadEditorMode.Edit) "챌린저 명단" else "초대할 챌린저",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CommunityChallengerCard(
            challengers = challengers,
            maxCount = maxChallengerCount,
            currentCount = currentChallengerCount,
            onClick = onChallengerCardClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 스레드 삭제 버튼 (수정 모드 + 삭제 권한이 있을 때만)
        if (mode is CommunityThreadEditorMode.Edit && mode.canDelete) {
            CommunityDeleteButton(
                onClick = onDeleteClick,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // AI 카드 오버레이로 인해 선택 영역이 막히는 문제 방지
        // 스크롤 영역을 세로로 203만큼 늘림
        // 피그마 참고하였음.
        Spacer(modifier = Modifier.height(203.dp))
    }
}

/**
 * 아이콘 선택 영역에 표시할 카테고리 (Figma 표시 순서)
 */
private val iconSelectCategories = listOf(
    CommunityCategory.STUDY,
    CommunityCategory.FREE,
    CommunityCategory.PROJECT,
    CommunityCategory.QNA,
)

/**
 * 미리보기에 표시할 카테고리별 아이콘 리소스를 반환
 */
@DrawableRes
private fun CommunityCategory.previewIconRes(): Int {
    return when (this) {
        CommunityCategory.STUDY -> R.drawable.ic_category_study
        CommunityCategory.QNA -> R.drawable.ic_category_question
        CommunityCategory.PROJECT -> R.drawable.ic_category_part_notice
        CommunityCategory.FREE -> R.drawable.ic_category_free

        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> R.drawable.ic_category_study
    }
}
