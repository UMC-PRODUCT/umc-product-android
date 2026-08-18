package com.umc.presentation.community.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey950
import com.umc.presentation.community.component.create.CommunityAiCard
import com.umc.presentation.community.component.create.CommunityChallengerCard
import com.umc.presentation.community.component.create.CommunityCreateTopBar
import com.umc.presentation.community.component.create.CommunityThreadForm
import com.umc.presentation.community.bottomsheet.create.CommunityCreateMemberBottomSheet
import com.umc.presentation.community.model.CommunityAiState

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
                // 챌린저 선택 영역
                UText(
                    text = "챌린저 명단",
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                )

                Spacer(modifier = Modifier.height(8.dp))

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