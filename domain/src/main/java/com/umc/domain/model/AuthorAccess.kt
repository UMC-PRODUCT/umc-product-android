package com.umc.domain.model

import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType

data class AuthorAccess(
    val resourceType: ResourceType, // Enum으로 관리하여 오타 방지
    val resourceId: Long,
    val permissions: List<PermissionItem>
)

/**
 * 개별 권한 항목 도메인 모델
 */
data class PermissionItem(
    val type: PermissionType, // READ, WRITE 등 Enum
    val hasPermission: Boolean
)
