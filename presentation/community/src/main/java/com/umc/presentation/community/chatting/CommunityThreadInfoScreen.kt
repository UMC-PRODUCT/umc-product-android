package com.umc.presentation.community.chatting

import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.semantics.Role
import com.umc.component.component.UButton
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
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
    onToggleMemberAdmin: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    onManageSelected: (Set<String>, CommunityParticipantBatchAction) -> Unit,
    isManagingParticipants: Boolean = false,
    managementRevision: Int = 0,
    managedParticipantIds: Set<String> = emptySet(),
    toastGeneration: Int = 0,
    toastData: UToastData? = null,
    onToastDismiss: () -> Unit = {},
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var selectedMemberIds by rememberSaveable(
        stateSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() }),
    ) { mutableStateOf(emptySet<String>()) }
    var showManagementSheet by remember { mutableStateOf(false) }
    var pendingBatch by remember { mutableStateOf<Pair<Set<String>, CommunityParticipantBatchAction>?>(null) }
    val viewerRole = if (isOwner) CommunityThreadRole.OWNER else if (canManage) CommunityThreadRole.ADMIN else CommunityThreadRole.MEMBER
    val manageableIds = remember(members, viewerRole, myMemberId) {
        communityManageableParticipants(members, viewerRole, myMemberId).mapTo(hashSetOf()) { it.memberId }
    }
    val cancelEditing = {
        isEditing = false; selectedMemberIds = emptySet<String>(); showManagementSheet = false; pendingBatch = null
    }
    val handleBack = { if (!isManagingParticipants) { if (isEditing) cancelEditing() else onBack() } }
    BackHandler(onBack = handleBack)
    LaunchedEffect(manageableIds, canManage) {
        selectedMemberIds = selectedMemberIds.intersect(manageableIds)
        if (!canManage) cancelEditing()
    }
    LaunchedEffect(managementRevision) {
        if (managementRevision > 0) {
            selectedMemberIds = selectedMemberIds - managedParticipantIds
            if (selectedMemberIds.isEmpty()) cancelEditing()
        }
    }
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
            key(toastGeneration) { UToastHost(
                data = toastData,
                onDismiss = onToastDismiss,
                modifier = Modifier.padding(bottom = 8.dp),
            ) }
        },
        bottomBar = {
            if (isEditing) Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                UButton(text = AppStrings.CHAT_CANCEL, onClick = cancelEditing,
                    enabled = !isManagingParticipants, modifier = Modifier.weight(1f).height(52.dp),
                    backgroundColor = grey100(), textColor = grey700(), textStyle = UmcTypographyTokens.HeadlineBold)
                UButton(text = if (isManagingParticipants) AppStrings.CHAT_MANAGING_PARTICIPANTS else AppStrings.CHAT_MANAGE_SELECTED_PARTICIPANTS,
                    onClick = { showManagementSheet = true }, enabled = selectedMemberIds.isNotEmpty() && !isManagingParticipants,
                    modifier = Modifier.weight(1f).height(52.dp), backgroundColor = main500(), textColor = white(), textStyle = UmcTypographyTokens.HeadlineBold)
            }
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
                    IconButton(onClick = handleBack) {
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
                        text = if (isEditing) AppStrings.CHAT_PARTICIPANT_MANAGEMENT else
                            AppStrings.CHAT_PARTICIPANT_MANAGEMENT_COUNT_FORMAT.format(
                                if (query.isNotBlank()) displayedMembers.size.toString() else memberCount.ifBlank { members.size.toString() }),
                        color = grey950(), style = UmcTypographyTokens.HeadlineBold,
                    )
                    if (canManage) Text(
                        text = if (isEditing) AppStrings.CHAT_CLEAR_PARTICIPANT_SELECTION else AppStrings.CHAT_EDIT_PARTICIPANTS,
                        color = main500(), style = UmcTypographyTokens.Body,
                        modifier = Modifier.clickable(enabled = !isManagingParticipants) {
                            if (isEditing) selectedMemberIds = emptySet() else isEditing = true
                        },
                    )
                }
            }
            items(displayedMembers, key = { it.memberId }) { member ->
                ParticipantRow(
                    member = member,
                    profile = memberProfiles[member.memberId],
                    isMe = member.memberId == myMemberId,
                    viewerRole = viewerRole,
                    isEditing = isEditing,
                    isSelected = member.memberId in selectedMemberIds,
                    isSelectable = member.memberId in manageableIds && !isManagingParticipants,
                    onToggleSelection = {
                        selectedMemberIds = if (member.memberId in selectedMemberIds) selectedMemberIds - member.memberId else selectedMemberIds + member.memberId
                    },
                    onViewProfile = { onViewProfile(member.memberId) },
                    onToggleAdmin = { onToggleMemberAdmin(member.memberId) },
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

    if (showManagementSheet && selectedMemberIds.isNotEmpty() && !isManagingParticipants) {
        SelectedParticipantManagementSheet(
            count = selectedMemberIds.size,
            actions = communityParticipantBatchActions(members.filter { it.memberId in selectedMemberIds }, viewerRole, myMemberId),
            onDismiss = { showManagementSheet = false },
            onAction = { action ->
                showManagementSheet = false
                if (action == CommunityParticipantBatchAction.ASSIGN_ADMIN) onManageSelected(selectedMemberIds.toSet(), action)
                else pendingBatch = selectedMemberIds.toSet() to action
            },
        )
    }
    pendingBatch?.let { (ids, action) ->
        val removingAdmin = action == CommunityParticipantBatchAction.REMOVE_ADMIN
        UDialog(
            title = if (removingAdmin) AppStrings.CHAT_REMOVE_ADMIN else AppStrings.CHAT_KICK_SELECTED_TITLE_FORMAT.format(ids.size),
            content = if (removingAdmin) AppStrings.CHAT_REMOVE_SELECTED_ADMIN_DESCRIPTION_FORMAT.format(ids.size) else AppStrings.CHAT_KICK_SELECTED_DESCRIPTION,
            isTwoButton = true, useCompactStyle = true,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = if (removingAdmin) AppStrings.CHAT_REMOVE_SELECTED_ADMIN_CONFIRM else AppStrings.CHAT_KICK,
            negativeBackgroundColor = grey100(), negativeTextColor = grey700(),
            positiveBackgroundColor = if (removingAdmin) main500() else red100(),
            positiveTextColor = if (removingAdmin) white() else red500(),
            onDismissRequest = { pendingBatch = null }, onNegative = { pendingBatch = null },
            onPositive = { pendingBatch = null; onManageSelected(ids.intersect(manageableIds), action) },
        )
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
            useCompactStyle = true,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_TRANSFER_ACTION,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey700(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedParticipantManagementSheet(
    count: Int,
    actions: Set<CommunityParticipantBatchAction>,
    onDismiss: () -> Unit,
    onAction: (CommunityParticipantBatchAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = white(), scrimColor = grey950().copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(48.dp)) {
                Box(Modifier.align(Alignment.TopCenter).padding(top = 16.dp).size(32.dp, 4.dp)
                    .background(grey600(), RoundedCornerShape(100.dp)))
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(min = 168.dp).verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 21.dp)) {
            Text(AppStrings.CHAT_MANAGE_PARTICIPANTS_COUNT_FORMAT.format(count),
                color = grey950(), style = UmcTypographyTokens.Title3Bold, modifier = Modifier.padding(bottom = 16.dp))
            CommunityParticipantBatchAction.entries.filter { it in actions }.forEach { action ->
                Text(
                    text = when (action) {
                        CommunityParticipantBatchAction.ASSIGN_ADMIN -> AppStrings.CHAT_ASSIGN_SELECTED_ADMIN
                        CommunityParticipantBatchAction.REMOVE_ADMIN -> AppStrings.CHAT_REMOVE_ADMIN
                        CommunityParticipantBatchAction.KICK -> AppStrings.CHAT_KICK
                    },
                    color = if (action == CommunityParticipantBatchAction.KICK) red500() else grey950(),
                    style = UmcTypographyTokens.Body,
                    modifier = Modifier.fillMaxWidth().clickable { onAction(action) }.padding(vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    member: CommunityThreadMember,
    profile: CommunityParticipantProfile?,
    isMe: Boolean,
    viewerRole: CommunityThreadRole,
    isEditing: Boolean,
    isSelected: Boolean,
    isSelectable: Boolean,
    onToggleSelection: () -> Unit,
    onViewProfile: () -> Unit,
    onToggleAdmin: () -> Unit,
    onKick: () -> Unit,
    onTransferOwnership: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val permissions = communityParticipantMenuPermissions(viewerRole, member.role, isMe)
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).heightIn(min = 64.dp)
            .then(if (isEditing) Modifier.toggleable(value = isSelected, enabled = isSelectable,
                role = Role.Checkbox, onValueChange = { onToggleSelection() }) else Modifier),
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
                if (isMe) ParticipantBadge(AppStrings.CHAT_ME, main100(), main500())
                if (member.role == CommunityThreadRole.OWNER) {
                    ParticipantBadge(AppStrings.CHAT_OWNER_BADGE, grey50(), grey600())
                }
                if (member.role == CommunityThreadRole.ADMIN) {
                    ParticipantBadge(AppStrings.CHAT_THREAD_INFO_ADMIN_BADGE, grey800(), white())
                }
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
        // 일반 참여자도 프로필 보기에 접근할 수 있다.
        if (isEditing) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Checkbox(checked = isSelected, onCheckedChange = null, enabled = isSelectable,
                    colors = CheckboxDefaults.colors(checkedColor = main500(), uncheckedColor = grey400(), checkmarkColor = white()))
            }
        } else {
            Box(modifier = Modifier.size(48.dp)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_kebab),
                        contentDescription = AppStrings.CHAT_CD_PARTICIPANT_MENU,
                        tint = grey400(),
                        modifier = Modifier.size(24.dp),
                    )
                }
                if (menuExpanded) Popup(
                    popupPositionProvider = remember {
                        object : PopupPositionProvider {
                            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize,
                                layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                                val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                                val y = if (anchorBounds.bottom + popupContentSize.height <= windowSize.height) anchorBounds.bottom
                                    else (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
                                return IntOffset(x, y)
                            }
                        }
                    },
                    onDismissRequest = { menuExpanded = false }, properties = PopupProperties(focusable = true),
                ) {
                    Surface(Modifier.width(160.dp), shape = RoundedCornerShape(16.dp), color = white(), shadowElevation = 8.dp) {
                        Column {
                    ParticipantMenuItem(AppStrings.CHAT_VIEW_PROFILE, R.drawable.ic_person) {
                        menuExpanded = false; onViewProfile()
                    }
                    if (permissions.canTransfer) {
                        ParticipantMenuItem(AppStrings.CHAT_TRANSFER_OWNER, R.drawable.ic_swap_horizontal) {
                            menuExpanded = false
                            onTransferOwnership()
                        }
                    }
                    if (permissions.canChangeAdmin) ParticipantMenuItem(
                        if (member.role == CommunityThreadRole.ADMIN) AppStrings.CHAT_REMOVE_ADMIN else AppStrings.CHAT_ASSIGN_ADMIN,
                        R.drawable.ic_shield_check,
                    ) { menuExpanded = false; onToggleAdmin() }
                    if (permissions.canKick) ParticipantMenuItem(AppStrings.CHAT_KICK, R.drawable.ic_block, red500()) {
                        menuExpanded = false
                        onKick()
                    }
                        }
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
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, color = color, style = UmcTypographyTokens.Subheadline)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
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
