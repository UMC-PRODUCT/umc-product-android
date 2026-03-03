package com.umc.domain.util

import com.umc.domain.model.UserInfo

data class AdminScope(
    val schoolId: Long,
    val part: String,
)

fun UserInfo.resolveAdminScope(): AdminScope? {
    val role = roles.firstOrNull {
        it.organizationType == "SCHOOL" && !it.responsiblePart.isNullOrBlank()
    } ?: roles.firstOrNull {
        !it.responsiblePart.isNullOrBlank()
    }

    val part = role?.responsiblePart?.trim() ?: return null

    return AdminScope(
        schoolId = schoolId,
        part = part
    )
}