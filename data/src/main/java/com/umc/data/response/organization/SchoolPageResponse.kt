package com.umc.data.response.organization

import com.umc.domain.model.school.SchoolInfo
import kotlinx.serialization.Serializable

@Serializable
data class SchoolPageResponse(
    val content: List<SchoolItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@Serializable
data class SchoolItemResponse(
    val schoolId: Int,
    val schoolName: String,
    val chapterId: Int,
    val chapterName: String,
    val createdAt: String,
    val isActive: Boolean
)

@Serializable
data class SchoolListResponse(
    val schools: List<SchoolNameResponse>
)

@Serializable
data class SchoolNameResponse(
    val schoolId: Int,
    val schoolName: String
) {
    companion object {
        fun SchoolNameResponse.toModel(): SchoolInfo {
            return SchoolInfo(
                schoolId = schoolId,
                schoolName = schoolName
            )
        }
    }
}