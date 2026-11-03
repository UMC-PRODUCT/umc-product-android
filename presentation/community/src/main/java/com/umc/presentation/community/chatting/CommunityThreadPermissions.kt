package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadRole

internal fun CommunityThreadRole?.canManageCommunityThread(): Boolean =
    this == CommunityThreadRole.OWNER || this == CommunityThreadRole.ADMIN
