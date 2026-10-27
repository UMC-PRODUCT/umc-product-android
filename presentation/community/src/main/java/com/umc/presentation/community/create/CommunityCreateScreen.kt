package com.umc.presentation.community.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey950
import com.umc.presentation.community.component.create.CommunityAiCard
import com.umc.presentation.community.component.create.CommunityChallengerCard
import com.umc.presentation.community.component.create.CommunityCreateTopBar
import com.umc.presentation.community.component.create.CommunityIconSelectItem
import com.umc.presentation.community.component.create.CommunityThreadForm
import com.umc.presentation.community.component.create.CommunityThreadPreview
import com.umc.presentation.community.R
import com.umc.presentation.community.bottomsheet.create.CommunityCreateMemberBottomSheet
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.persistentListOf

/**
 * 커뮤니티 스레드 생성 화면
 *
 * 챌린저 선택, 제목/특징 입력,
 * AI 카테고리 분류 및 스레드 생성을 위한 UI를 구성합니다.
 */
@Composable
fun CommunityCreateScreen(
    state: CommunityCreateState,
    onAction: (CommunityCreateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(grey000())
            .navigationBarsPadding(),
    ) {
        // 스레드 생성 화면 상단바
        CommunityCreateTopBar(
            title = "스레드 만들기",
            actionText = "완료",
            isActionEnabled = state.isCompleteEnabled,
            onBackClick = {
                onAction(CommunityCreateAction.OnBackClick)
            },
            onActionClick = {
                onAction(CommunityCreateAction.OnCompleteClick)
            },
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        )

        // 탑 바를 제외한 스크롤 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // 스레드 생성에 필요한 기본 입력 영역
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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

                // 분류 전에는 아이콘 선택 기본값인 스터디로 표시
                val previewCategory = state.classifiedCategory ?: CommunityCategory.STUDY

                CommunityThreadPreview(
                    title = state.title,
                    description = state.description,
                    category = previewCategory,
                    iconRes = previewCategory.previewIconRes(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 챌린저 선택 영역
//                UText(
//                    text = "초대할 챌린저",
//                    style = UmcTypographyTokens.HeadlineBold,
//                    color = grey950(),
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                CommunityChallengerCard(
//                    challengers = state.selectedChallengers,
//                    maxCount = state.maxChallengerCount,
//                    onClick = {
//                        onAction(
//                            CommunityCreateAction.OnChallengerCardClick
//                        )
//                    },
//                )

                //Spacer(modifier = Modifier.height(32.dp))

                // 스레드 제목 및 특징 입력 영역
                CommunityThreadForm(
                    title = state.title,
                    description = state.description,
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
                )

                Spacer(modifier = Modifier.height(32.dp))

                UText(
                    text = "아이콘",
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
                    iconSelectCategories.forEach { (category, label) ->
                        // 화면 폭을 넘지 않도록 폭을 균등하게 나누고,
                        // 라벨이 줄바꿈되어도 가장 높은 아이템에 높이를 맞춤
                        CommunityIconSelectItem(
                            label = label,
                            iconRes = category.previewIconRes(),
                            selected = previewCategory == category,
                            onClick = {
                                // TODO 아이콘 선택 액션 연결
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 챌린저 선택 영역
                UText(
                    text = "초대할 챌린저",
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                CommunityChallengerCard(
                    challengers = state.selectedChallengers,
                    maxCount = state.maxChallengerCount,
                    onClick = {
                        onAction(
                            CommunityCreateAction.OnChallengerCardClick
                        )
                    },
                )

                Spacer(modifier = Modifier.height(32.dp))


                // AI 카드 오버레이로 인해 선택 영역이 막히는 문제 방지
                // 스크롤 영역을 세로로 203만큼 늘림
                // 피그마 참고하였음.
                Spacer(modifier = Modifier.height(203.dp))

            }

            /**
             * AI 카테고리 분류 카드
             *
             * 입력 상태에 따라 안내, 로딩,
             * 분류 결과 및 재분류 상태를 표시합니다.
             */
            CommunityAiCard(
                aiState = state.aiState,
                classifiedCategory = state.classifiedCategory,
                canRequestClassification = state.canRequestClassification,
                onRequestClassificationClick = {
                    onAction(
                        CommunityCreateAction.OnRequestClassificationClick
                    )
                },
                onRetryClassificationClick = {
                    onAction(
                        CommunityCreateAction.OnRetryClassificationClick
                    )
                },
                onChangeEmojiClick = {
                    onAction(
                        CommunityCreateAction.OnChangeEmojiClick
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .then(
                        if (state.aiState == CommunityAiState.GUIDE) {
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
 * 아이콘 선택 영역에 표시할 카테고리와 라벨 (Figma 표시 순서)
 */
private val iconSelectCategories = listOf(
    CommunityCategory.STUDY to "스터디",
    CommunityCategory.FREE to "자유",
    CommunityCategory.PROJECT to "파트공지",
    CommunityCategory.QNA to "질문",
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