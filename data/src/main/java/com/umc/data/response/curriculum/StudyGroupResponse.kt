package com.umc.data.response.curriculum

data class StudyGroupResponse(
    val groupId: Long,
    val name: String
) {
    fun toDomain() = StudyGroup(
        id = groupId,
        name = name
    )
}
