package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class SchoolDetailResponse(
    val chapterId: Int,
    val chapterName: String,
    val schoolName: String,
    val schoolId: Int,
    val remark: String,
    val logoImageLink: String,
    val createdAt: String,
    val updatedAt: String
)