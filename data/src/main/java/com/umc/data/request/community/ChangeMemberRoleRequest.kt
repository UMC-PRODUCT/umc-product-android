package com.umc.data.request.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.thread.CommunityThreadRole

data class ChangeMemberRoleRequest(@field:SerializedName("role") val role: CommunityThreadRole)
