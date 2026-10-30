package com.umc.presentation.community.chatting

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UDialog
import com.umc.presentation.community.component.CommunityMemberSearchField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey50
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main100
import com.umc.component.theme.main500
import com.umc.component.theme.red100
import com.umc.component.theme.red400
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.component.theme.UmcTypographyTokens
import com.umc.presentation.community.component.chatting.CommunityChatProfileImage

@Composable
internal fun CommunityThreadInfoScreen(
    members: List<CommunityThreadMember>,
    memberProfiles: Map<String, CommunityParticipantProfile>,
    memberCount: String,
    myMemberId: String,
    isOwner: Boolean,
    canManage: Boolean = isOwner,
    onBack: () -> Unit,
    onKickMember: (String) -> Unit,
    onTransferOwnership: (String) -> Unit,
    toastData: UToastData? = null,
    onToastDismiss: () -> Unit = {},
) {
    BackHandler(onBack = onBack)
    var pendingKickMember by remember { mutableStateOf<CommunityThreadMember?>(null) }
    var pendingOwnershipMember by remember { mutableStateOf<CommunityThreadMember?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val sortedMembers = remember(members) {
        members.sortedWith(
            compareBy<CommunityThreadMember> { it.role != CommunityThreadRole.OWNER }
                .thenBy { it.name },
        )
    }
    val displayedMembers = remember(sortedMembers, memberProfiles, query) {
        filterCommunityParticipants(sortedMembers, memberProfiles, query)
    }

    Scaffold(
        containerColor = white(),
        snackbarHost = {
            UToastHost(
                data = toastData,
                onDismiss = onToastDismiss,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        },
        topBar = {
            Surface(color = white()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = AppStrings.CHAT_CD_BACK,
                            tint = grey950(),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = AppStrings.CHAT_THREAD_INFO,
                        modifier = Modifier.padding(start = 8.dp),
                        color = grey950(),
                        style = UmcTypographyTokens.Title2Bold,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 64.dp),
        ) {
            item {
                CommunityMemberSearchField(
                    value = query,
                    onValueChange = { query = it },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppStrings.CHAT_PARTICIPANT_MANAGEMENT,
                        color = grey950(),
                        style = UmcTypographyTokens.HeadlineBold,
                    )
                    Text(
                        text = AppStrings.CHAT_PARTICIPANT_COUNT_FORMAT.format(
                            memberCount.ifBlank { members.size.toString() },
                        ),
                        color = grey950(),
                        style = UmcTypographyTokens.Body,
                    )
                }
            }
            items(displayedMembers, key = { it.memberId }) { member ->
                ParticipantRow(
                    member = member,
                    profile = memberProfiles[member.memberId],
                    isMe = member.memberId == myMemberId,
                    showManagement = canManage,
                    canTransfer = isOwner,
                    onKick = { pendingKickMember = member },
                    onTransferOwnership = { pendingOwnershipMember = member },
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(grey100())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_error_filled),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(grey400()),
                    )
                    Text(
                        text = AppStrings.CHAT_OWNER_ONLY_KICK_GUIDE,
                        color = grey700(),
                        style = UmcTypographyTokens.Subheadline,
                    )
                }
            }
        }
    }

    pendingKickMember?.let { member ->
        UBasicDialog(
            title = AppStrings.CHAT_KICK_TITLE_FORMAT.format(
                member.name.ifBlank { AppStrings.CHAT_PARTICIPANT_DEFAULT },
            ),
            content = AppStrings.CHAT_KICK_DESCRIPTION,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_KICK,
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
            title = AppStrings.CHAT_TRANSFER_OWNER,
            subtitle = AppStrings.CHAT_TRANSFER_OWNER_DESCRIPTION,
            isTwoButton = true,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_TRANSFER_ACTION,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey800(),
            positiveBackgroundColor = main500(),
            positiveBorderColor = main500(),
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
    profile: CommunityParticipantProfile?,
    isMe: Boolean,
    showManagement: Boolean,
    canTransfer: Boolean,
    onKick: () -> Unit,
    onTransferOwnership: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityChatProfileImage(member.profileImageUrl, useThreadStyle = true)
        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = communityParticipantDisplayName(member, profile),
                    modifier = Modifier.weight(1f, fill = false),
                    color = grey950(),
                    style = UmcTypographyTokens.CalloutBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (member.role == CommunityThreadRole.OWNER) {
                    ParticipantBadge(AppStrings.CHAT_OWNER_BADGE, grey50(), grey600())
                }
                if (member.role == CommunityThreadRole.ADMIN) {
                    ParticipantBadge(AppStrings.CHAT_THREAD_INFO_ADMIN_BADGE, grey800(), white())
                }
                if (isMe) ParticipantBadge(AppStrings.CHAT_ME, main100(), main500())
            }
            val subtitle = listOfNotNull(member.part?.label, profile?.roleLabel?.takeIf { it.isNotBlank() })
                .joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = grey600(),
                    style = UmcTypographyTokens.Footnote,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // 관리 메뉴(방장 변경·내보내기)가 있는 행에만 ⋮ 를 보여 준다.
        if (!isMe && showManagement && member.role != CommunityThreadRole.OWNER) {
            Box(modifier = Modifier.size(48.dp)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_kebab),
                        contentDescription = AppStrings.CHAT_CD_PARTICIPANT_MENU,
                        tint = grey400(),
                        modifier = Modifier.size(24.dp),
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
                    if (canTransfer) {
                        ParticipantMenuItem(AppStrings.CHAT_TRANSFER_OWNER, R.drawable.ic_swap_horizontal) {
                            menuExpanded = false
                            onTransferOwnership()
                        }
                    }
                    ParticipantMenuItem(AppStrings.CHAT_KICK, R.drawable.ic_block, red400()) {
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
    @DrawableRes iconRes: Int,
    color: Color = grey950(),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, color = color, style = UmcTypographyTokens.Subheadline)
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = foreground,
        style = UmcTypographyTokens.Caption1Bold
    )
}
