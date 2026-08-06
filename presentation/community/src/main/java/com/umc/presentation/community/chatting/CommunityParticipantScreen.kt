package com.umc.presentation.community.chatting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UDialog
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red400
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole

@Composable
internal fun CommunityParticipantScreen(
    members: List<CommunityThreadMember>,
    memberCount: String,
    myMemberId: String,
    isOwner: Boolean,
    onBack: () -> Unit,
    onKickMember: (String) -> Unit,
    onTransferOwnership: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    var pendingKickMember by remember { mutableStateOf<CommunityThreadMember?>(null) }
    var pendingOwnershipMember by remember { mutableStateOf<CommunityThreadMember?>(null) }
    val sortedMembers = remember(members) {
        members.sortedWith(
            compareBy<CommunityThreadMember> { it.role != CommunityThreadRole.OWNER }
                .thenBy { it.name },
        )
    }

    Scaffold(
        containerColor = white(),
        topBar = {
            Surface(color = white()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = grey950(),
                        )
                    }
                    Text(
                        text = "참여자 관리",
                        modifier = Modifier.padding(start = 2.dp),
                        color = grey950(),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            item {
                Text(
                    text = "총 ${memberCount.ifBlank { members.size.toString() }}명",
                    color = grey950(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            items(sortedMembers, key = { it.memberId }) { member ->
                ParticipantRow(
                    member = member,
                    isMe = member.memberId == myMemberId,
                    showManagement = isOwner,
                    onKick = { pendingKickMember = member },
                    onTransferOwnership = { pendingOwnershipMember = member },
                )
            }
            if (isOwner) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(grey100())
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_error_filled),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(grey400())
                        )
                        Text(
                            text = "개설자만 참여자를 내보낼 수 있어요",
                            color = grey700(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }

    pendingKickMember?.let { member ->
        UBasicDialog(
            title = "${member.name.ifBlank { "해당 참여자" }}님을 내보낼까요?",
            content = "이 스레드에서 나가지며,\n다시 초대해야 참여할 수 있어요",
            negativeText = "취소",
            positiveText = "내보내기",
            type = DialogType.WARNING,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onPositive = {
                onKickMember(member.memberId)
                pendingKickMember = null
            },
            onNegative = { pendingKickMember = null },
            onDismissRequest = { pendingKickMember = null },
        )
    }

    pendingOwnershipMember?.let { member ->
        UDialog(
            title = "방장 변경",
            subtitle = "선택한 참여자를 방장으로 변경하시겠습니까?",
            isTwoButton = true,
            negativeText = "취소",
            positiveText = "변경하기",
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey800(),
            positiveBackgroundColor = indigo500(),
            positiveBorderColor = indigo500(),
            positiveTextColor = white(),
            onNegative = { pendingOwnershipMember = null },
            onPositive = {
                onTransferOwnership(member.memberId)
                pendingOwnershipMember = null
            },
            onDismissRequest = { pendingOwnershipMember = null },
        )
    }
}

@Composable
private fun ParticipantRow(
    member: CommunityThreadMember,
    isMe: Boolean,
    showManagement: Boolean,
    onKick: () -> Unit,
    onTransferOwnership: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(member.profileImageUrl)
        Column(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = member.name.ifBlank { "알 수 없음" },
                    color = grey950(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (member.role == CommunityThreadRole.OWNER) {
                    ParticipantBadge("개설자", grey800(), white())
                }
                if (isMe) ParticipantBadge("나", indigo100(), indigo500())
            }
            val part = member.part?.let { partTag(it).first }.orEmpty()
            val detail = listOfNotNull(
                part.takeIf(String::isNotBlank),
                member.generation?.takeIf(String::isNotBlank)?.let { "${it}기" },
            ).joinToString(" · ")
            if (detail.isNotBlank()) Text(detail, color = grey400(), fontSize = 11.sp)
        }
        Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_kebab),
                        contentDescription = "참여자 관리 메뉴",
                        tint = grey400(),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = white(),
                    shadowElevation = 8.dp,
                ) {
                    ParticipantMenuItem("프로필 보기", R.drawable.ic_person) {
                        menuExpanded = false
                    }
                    if (showManagement && !isMe && member.role != CommunityThreadRole.OWNER) {
                        ParticipantMenuItem("방장 변경", R.drawable.ic_swap_horizontal) {
                            menuExpanded = false
                            onTransferOwnership()
                        }
                        ParticipantMenuItem("내보내기", R.drawable.ic_block, red400()) {
                            menuExpanded = false
                            onKick()
                        }
                    }
                }
        }
    }
}

@Composable
private fun ParticipantMenuItem(
    text: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    color: Color = grey950(),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, color = color, fontSize = 14.sp)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun ParticipantBadge(text: String, background: Color, foreground: Color) {
    Text(
        text = text,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(background)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        color = foreground,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
