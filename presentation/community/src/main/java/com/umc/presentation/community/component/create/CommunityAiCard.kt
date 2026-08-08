package com.umc.presentation.community.component.create

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow700
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory

@Composable
fun CommunityAiCard(
    aiState: CommunityAiState,
    classifiedCategory: CommunityCategory?,
    canRequestClassification: Boolean,
    onRequestClassificationClick: () -> Unit,
    onRetryClassificationClick: () -> Unit,
    onChangeEmojiClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
            ),
        shape = RoundedCornerShape(12.dp),
        color = indigo100(),
        shadowElevation = 0.dp,
    ) {
        when (aiState) {
            CommunityAiState.GUIDE -> {
                CommunityAiGuideContent()
            }

            CommunityAiState.LOADING -> {
                CommunityAiLoadingContent()
            }

            CommunityAiState.SUCCESS -> {
                CommunityAiResultContent(
                    category = classifiedCategory ?: CommunityCategory.FREE,
                    canRetry = true,
                    warningMessage = null,
                    onRetryClassificationClick = onRetryClassificationClick,
                    onChangeEmojiClick = onChangeEmojiClick,
                )
            }

            CommunityAiState.NEEDS_RECLASSIFICATION -> {
                CommunityAiResultContent(
                    category = classifiedCategory ?: CommunityCategory.FREE,
                    canRetry = true,
                    warningMessage = "특징이 바뀌었어요. 다시 분류를 추천해요",
                    onRetryClassificationClick = onRetryClassificationClick,
                    onChangeEmojiClick = onChangeEmojiClick,
                )
            }

            CommunityAiState.FAILED -> {
                CommunityAiResultContent(
                    category = CommunityCategory.FREE,
                    canRetry = false,
                    warningMessage = "이 기기에서는 AI 재분류를 할 수 없어요",
                    onRetryClassificationClick = onRetryClassificationClick,
                    onChangeEmojiClick = onChangeEmojiClick,
                )
            }
        }
    }
}

@Composable
private fun CommunityAiGuideContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AiIcon()

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = "Galaxy AI로 도와드릴게요",
                style = UmcTypographyTokens.HeadlineBold,
                color = indigo500(),
            )

            Spacer(modifier = Modifier.height(10.dp))

            UText(
                text = "스레드 특징을 입력하면\nAI가 카테고리, 아이콘을 정해줘요",
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
            )
        }
    }
}

@Composable
private fun CommunityAiLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        UText(
            text = "카테고리 분류",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        UText(
            text = "특징을 분석해 카테고리, 아이콘을 정하고 있어요",
            style = UmcTypographyTokens.Footnote,
            color = grey600(),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AiIcon()

            Spacer(modifier = Modifier.width(10.dp))

            CommunityAiProgressBar(
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CommunityAiProgressBar(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(
        label = "community_ai_loading",
    )

    val firstProgress = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "first_progress",
    )

    val secondProgress = transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "second_progress",
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommunityAiProgressLine(
            progress = firstProgress.value,
        )

        CommunityAiProgressLine(
            progress = secondProgress.value,
        )
    }
}

@Composable
private fun CommunityAiProgressLine(
    progress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(12.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            indigo500(),
                            green500(),
                        ),
                    ),
                    shape = RoundedCornerShape(100.dp),
                ),
        )
    }
}

@Composable
private fun CommunityAiResultContent(
    category: CommunityCategory,
    canRetry: Boolean,
    warningMessage: String?,
    onRetryClassificationClick: () -> Unit,
    onChangeEmojiClick: () -> Unit,
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AiIcon()

            Spacer(modifier = Modifier.width(10.dp))

            UText(
                text = "카테고리 분류",
                style = UmcTypographyTokens.SubheadlineBold,
                color = indigo500(),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = grey000(),
                shadowElevation = 0.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            id = categoryIconRes(category),
                        ),
                        contentDescription = null,
                        tint = indigo500(),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                UText(
                    text = categoryLabel(category),
                    style = UmcTypographyTokens.Title3Bold,
                    color = grey950(),
                )

                Spacer(modifier = Modifier.height(4.dp))

                UText(
                    text = categoryResultDescription(category),
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                    maxLines = 2,
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        CommunityAiResultButton(
            text = if (canRetry) {
                "다시 분류하기"
            } else {
                "재분류 불가"
            },
            enabled = canRetry,
            isPrimary = true,
            onClick = onRetryClassificationClick,
            modifier = Modifier.fillMaxWidth(),
        )

        if (warningMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = grey100(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_error_filled,
                    ),
                    contentDescription = null,
                    tint = grey500(),
                    modifier = Modifier.size(16.dp),
                )

                Spacer(modifier = Modifier.width(10.dp))

                UText(
                    text = warningMessage,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey700(),
                )
            }
        }
    }
}

@Composable
private fun CommunityAiResultButton(
    text: String,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val backgroundColor = when {
        !enabled -> grey100()
        isPrimary -> indigo500()
        else -> grey950()
    }

    val textColor = when {
        !enabled -> grey300()
        else -> grey000()
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.CalloutBold,
            color = textColor,
        )
    }
}

@Composable
private fun AiIcon() {
    Icon(
        painter = painterResource(
            id = R.drawable.ic_community_ai,
        ),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(32.dp),
    )
}

private fun categoryLabel(
    category: CommunityCategory,
): String {
    return when (category) {
        CommunityCategory.ALL -> "전체"
        CommunityCategory.UNREAD -> "안읽음"
        CommunityCategory.PROJECT -> "파트공지"
        CommunityCategory.STUDY -> "스터디"
        CommunityCategory.QNA -> "질문"
        CommunityCategory.FREE -> "자유"
    }
}

private fun categoryResultDescription(
    category: CommunityCategory,
): String {
    return when (category) {
        CommunityCategory.STUDY -> {
            "\"과제 인증 및 코드 피드백\" 성격으로 분류했어요"
        }

        CommunityCategory.PROJECT -> {
            "\"공지 및 일정 안내\" 성격으로 분류했어요"
        }

        CommunityCategory.QNA -> {
            "\"질문과 답변\" 성격으로 분류했어요"
        }

        CommunityCategory.FREE -> {
            "미지정 기기, 오프라인일 땐 기본 분류(자유)로 \n만들어요"
        }

        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> {
            "카테고리를 분류했어요"
        }
    }
}

private fun categoryIconRes(
    category: CommunityCategory,
): Int {
    return when (category) {
        CommunityCategory.STUDY -> {
            R.drawable.ic_book_filled
        }

        CommunityCategory.PROJECT -> {
            R.drawable.ic_community_part_notice
        }

        CommunityCategory.QNA -> {
            R.drawable.ic_community_question
        }

        CommunityCategory.FREE -> {
            R.drawable.ic_community_chat
        }

        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> {
            R.drawable.ic_community_part_notice
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityAiFailedPreview() {
    CommunityAiCard(
        aiState = CommunityAiState.FAILED,
        classifiedCategory = CommunityCategory.FREE,
        canRequestClassification = false,
        onRequestClassificationClick = {},
        onRetryClassificationClick = {},
        onChangeEmojiClick = {},
    )
}