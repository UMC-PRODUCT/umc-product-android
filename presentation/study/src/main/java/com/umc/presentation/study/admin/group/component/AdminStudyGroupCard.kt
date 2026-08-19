package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel

/**
 * 관리자 스터디 그룹 목록에서 사용하는 그룹 카드
 *
 * 주요 기능
 * - 스터디 그룹 이름 및 파트 표시
 * - 생성일 및 현재 멤버 수 표시
 * - 담당 파트장 정보 및 프로필 이미지 표시
 * - 스터디원 목록 표시
 * - 그룹 정보 수정 / 삭제 메뉴 제공
 * - 스터디원 추가 및 일정 등록 기능 제공
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminStudyGroupCard(
    item: AdminStudyGroupItemUiModel,
    isSettingOpen: Boolean,
    onSettingClick: () -> Unit,
    onDismissSetting: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onAddMemberClick: () -> Unit,
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    grey000(),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            // 스터디 그룹 기본 정보 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 스터디 그룹 이름
                        UText(
                            text = item.title,
                            style = Title3Bold,
                            color = grey900()
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        // 스터디 그룹 파트
                        Box(
                            modifier = Modifier
                                .background(
                                    color = indigo100(),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                        ) {
                            UText(
                                text = item.partLabel,
                                style = Caption1Bold,
                                color = indigo600()
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    // 그룹 생성일 및 현재 멤버 수
                    UText(
                        text =
                            "${item.createdAtText}  |  멤버 ${item.memberCount}명",
                        style = Footnote,
                        color = grey500()
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                /**
                 * 그룹 설정 영역
                 *
                 * 설정 버튼 클릭 시
                 * 정보 수정 / 그룹 삭제 메뉴를 표시합니다.
                 */
                Box {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_setting_outline
                        ),
                        contentDescription = "설정",
                        tint = grey500(),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onSettingClick
                            )
                    )

                    DropdownMenu(
                        expanded = isSettingOpen,
                        onDismissRequest = onDismissSetting,
                        modifier = Modifier
                            .width(208.dp)
                            .background(
                                grey000(),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        AdminStudyGroupSettingPopupContent(
                            onEditClick = {
                                onEditClick()
                                onDismissSetting()
                            },
                            onDeleteClick = {
                                onDeleteClick()
                                onDismissSetting()
                            },
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // 담당 파트장 영역
            UText(
                text = "담당 파트장",
                style = CalloutBold,
                color = grey800()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        grey100(),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 담당 파트장 프로필 이미지
                AdminStudyGroupLeaderProfileImage(
                    profileImageUrl =
                        item.leaderProfileImageUrl,
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                // 담당 파트장 이름
                UText(
                    text = item.leaderName,
                    style = SubheadlineBold,
                    color = grey800()
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                // 담당 파트장 학교
                UText(
                    text = item.leaderUniv.ifBlank {
                        "중앙대"
                    },
                    style = Footnote,
                    color = grey500()
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // 파트장 표시 Badge
                Box(
                    modifier = Modifier
                        .background(
                            grey000(),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            grey200(),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 7.dp,
                            vertical = 3.dp
                        )
                ) {
                    UText(
                        text = "Leader",
                        style = Caption1Bold,
                        color = grey600()
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // 스터디원 영역
            UText(
                text = "스터디원",
                style = CalloutBold,
                color = grey800()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                /**
                 * 현재 스터디원 목록
                 *
                 * 멤버 수에 따라 자동으로 줄바꿈하여
                 * 프로필 이미지와 이름을 Chip 형태로 표시합니다.
                 */
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    item.members.forEach { member ->
                        AdminStudyGroupMemberChip(
                            member = member
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                // 스터디원 추가 버튼
                Icon(
                    painter = painterResource(
                        R.drawable.ic_add_filled
                    ),
                    contentDescription = "스터디원 추가",
                    tint = indigo500(),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onAddMemberClick()
                        }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // 스터디 그룹 일정 등록 버튼
            AdminStudyGroupScheduleButton(
                onClick = onAddScheduleClick
            )
        }
    }
}

/**
 * 담당 파트장의 프로필 이미지를 표시합니다.
 *
 * 프로필 이미지 URL이 존재하면 실제 이미지를 표시하고,
 * URL이 없는 경우 기본 프로필 이미지를 표시합니다.
 */
@Composable
private fun AdminStudyGroupLeaderProfileImage(
    profileImageUrl: String?,
) {
    if (!profileImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(26.dp),
        )
    }
}