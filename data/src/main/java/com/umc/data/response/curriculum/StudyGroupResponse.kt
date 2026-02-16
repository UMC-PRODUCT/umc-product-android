package com.umc.data.response.curriculum

import com.umc.domain.model.curriculum.StudyGroup

data class StudyGroupResponse(
    val groupId: Long,
    val name: String
) {
    fun toDomain() = StudyGroup(
        id = groupId,
        name = name
    )
}
