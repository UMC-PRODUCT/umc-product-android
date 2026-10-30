package com.umc.presentation.community.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey50
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main100
import com.umc.component.theme.main500
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

/**
 * 커뮤니티 목록에서 사용하는 개별 스레드 카드
 *
 * 스레드 제목, 카테고리, 내용 미리보기, 날짜,
 * 읽지 않은 메시지 수 및 스레드 상태를 표시합니다.
 *
 * 일반 클릭과 롱클릭 이벤트를 모두 지원합니다.
 * [useUpdatedListStyle]은 기본 목록의 Figma 시범 적용용이며 검색 결과는 기존 스타일을 유지합니다.
 */
@Composable
fun CommunityThreadItem(
    thread: CommunityThreadUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    useUpdatedListStyle: Boolean = false,
) {
    // 강퇴된 스레드는 열람 불가 안내 카드로 표시
    if (thread.isKicked) {
        CommunityKickedThreadItem(
            thread = thread,
            onClick = onClick,
            modifier = modifier,
        )
        return
    }

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
        shape = RoundedCornerShape(if (useUpdatedListStyle) 12.dp else 10.dp),
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
                color = main100(),
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
                        tint = main500(),
                        modifier = Modifier.size(if (useUpdatedListStyle) 24.dp else 22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 제목, 카테고리 칩, 내용 미리보기 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                verticalArrangement = if (useUpdatedListStyle) Arrangement.Center else Arrangement.Top,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UText(
                        text = thread.title,
                        style = if (useUpdatedListStyle) {
                            UmcTypographyTokens.HeadlineBold.copy(letterSpacing = (-0.41).sp)
                        } else UmcTypographyTokens.HeadlineBold,
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
                        useUpdatedListStyle = useUpdatedListStyle,
                    )

                    // 고정된 스레드인 경우 핀 아이콘 표시
                    if (thread.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_pin,
                            ),
                            contentDescription = "고정 스레드",
                            tint = if (useUpdatedListStyle) grey500() else grey400(),
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
                    style = if (useUpdatedListStyle) {
                        UmcTypographyTokens.Footnote.copy(letterSpacing = (-0.08).sp)
                    } else UmcTypographyTokens.Footnote,
                    color = grey600(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(if (useUpdatedListStyle) 24.dp else 12.dp))

            // 날짜 및 읽지 않은 메시지 수
            Column(
                modifier = Modifier.height(if (useUpdatedListStyle) 50.dp else 48.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = if (useUpdatedListStyle) {
                    Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                } else Arrangement.SpaceBetween,
            ) {
                UText(
                    text = thread.dayText,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )

                // 읽지 않은 메시지가 있을 때만 카운트 표시
                if (thread.unreadCount > 0) {
                    Surface(
                        modifier = if (useUpdatedListStyle) {
                            Modifier.height(24.dp).widthIn(min = 24.dp)
                        } else Modifier.size(24.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = grey800(),
                        shadowElevation = 0.dp,
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = if (useUpdatedListStyle) Modifier.padding(horizontal = 8.dp) else Modifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            UText(
                                text = thread.unreadCount.toString(),
                                style = if (useUpdatedListStyle) UmcTypographyTokens.Caption1Bold else UmcTypographyTokens.Caption2Bold,
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
 * 내가 강퇴된 스레드 카드 (Figma `Item/Community List` 강퇴 상태)
 *
 * grey50 배경에 제목을 흐리게(grey600) 표시하고,
 * 카테고리 칩·고정/알림 아이콘·날짜·안 읽은 수 대신 열람 불가 안내 문구를 보여줍니다.
 * 고정·알림·편집 등 관리 메뉴가 의미 없으므로 롱클릭은 받지 않습니다.
 */
@Composable
private fun CommunityKickedThreadItem(
    thread: CommunityThreadUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = grey50(),
        border = BorderStroke(1.dp, grey200()),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 카테고리에 맞는 아이콘 영역 (일반 카드와 동일)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = main100(),
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
                        tint = main500(),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 제목과 열람 불가 안내
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                UText(
                    text = thread.title,
                    style = UmcTypographyTokens.HeadlineBold.copy(letterSpacing = (-0.41).sp),
                    color = grey600(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                UText(
                    text = "더 이상 열람할 수 없는 스레드예요.",
                    style = UmcTypographyTokens.Footnote.copy(letterSpacing = (-0.08).sp),
                    color = grey500(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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

@Preview(showBackground = true)
@Composable
private fun CommunityThreadItemPreview() {
    UmcTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CommunityThreadItem(
                thread = CommunityThreadUiModel(
                    id = "1",
                    title = "iOS 3주차 과제 인증방",
                    contentPreview = "다들 과제 인증 완료했나요? 오늘 자정까지...",
                    category = CommunityCategory.STUDY,
                    dayText = "화요일",
                    unreadCount = 3,
                    isPinned = true,
                    isNotificationEnabled = true,
                    isJoined = true,
                ),
                onClick = {},
                onLongClick = {},
            )
            CommunityThreadItem(
                thread = CommunityThreadUiModel(
                    id = "2",
                    title = "정기 모임 일정 안내",
                    contentPreview = "이번 주 토요일 오후 2시 세션 진행합니다.",
                    category = CommunityCategory.PROJECT,
                    dayText = "어제",
                    unreadCount = 0,
                    isPinned = false,
                    isNotificationEnabled = false,
                    isJoined = true,
                ),
                onClick = {},
                onLongClick = {},
                useUpdatedListStyle = true,
            )
            // 강퇴된 스레드
            CommunityThreadItem(
                thread = CommunityThreadUiModel(
                    id = "3",
                    title = "iOS 3주차 과제 인증방",
                    contentPreview = "",
                    category = CommunityCategory.STUDY,
                    dayText = "",
                    isJoined = false,
                    isKicked = true,
                ),
                onClick = {},
                onLongClick = {},
                useUpdatedListStyle = true,
            )
        }
    }
}
