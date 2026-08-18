package com.umc.presentation.community.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

/**
 * 커뮤니티 목록에서 사용하는 개별 스레드 카드
 *
 * 스레드 제목, 카테고리, 내용 미리보기, 날짜,
 * 읽지 않은 메시지 수 및 스레드 상태를 표시합니다.
 *
 * 일반 클릭과 롱클릭 이벤트를 모두 지원합니다.
 */
@Composable
fun CommunityThreadItem(
    thread: CommunityThreadUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // 스레드 카드
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(10.dp),
        color = grey000(),
        border = BorderStroke(1.dp, grey200()),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // 카테고리에 맞는 아이콘 영역
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = indigo100(),
                shadowElevation = 0.dp,
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            id = categoryIconRes(thread.category),
                        ),
                        contentDescription = null,
                        tint = indigo500(),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 제목, 카테고리 칩, 내용 미리보기 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UText(
                        text = thread.title,
                        style = UmcTypographyTokens.HeadlineBold,
                        color = grey950(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(
                            weight = 1f,
                            fill = false,
                        ),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 스레드 카테고리 표시
                    CommunityCategoryChip(
                        category = thread.category,
                    )

                    // 고정된 스레드인 경우 핀 아이콘 표시
                    if (thread.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_pin,
                            ),
                            contentDescription = "고정 스레드",
                            tint = grey400(),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // 알림이 꺼진 스레드인 경우 알림 OFF 아이콘 표시
                    if (!thread.isNotificationEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_notification_off,
                            ),
                            contentDescription = "알림 꺼짐",
                            tint = grey400(),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // 제목과 미리보기 사이 간격
                Spacer(modifier = Modifier.height(4.dp))

                // 최근 스레드 내용 미리보기
                UText(
                    text = thread.contentPreview,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 날짜 및 읽지 않은 메시지 수
            Column(
                modifier = Modifier.height(48.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                UText(
                    text = thread.dayText,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )

                // 읽지 않은 메시지가 있을 때만 카운트 표시
                if (thread.unreadCount > 0) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = grey800(),
                        shadowElevation = 0.dp,
                    ) {
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            UText(
                                text = thread.unreadCount.toString(),
                                style = UmcTypographyTokens.Caption2Bold,
                                color = grey000(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 스레드 카테고리에 대응하는 아이콘 리소스를 반환
 */
private fun categoryIconRes(
    category: CommunityCategory,
): Int {
    return when (category) {
        CommunityCategory.STUDY -> R.drawable.ic_book_filled
        CommunityCategory.PROJECT -> R.drawable.ic_community_question
        CommunityCategory.QNA -> R.drawable.ic_community_chat
        CommunityCategory.FREE -> R.drawable.ic_community_part_notice
        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> R.drawable.ic_community_chat
    }
}