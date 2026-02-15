package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class ChapterBySchoolListResponse(
    val chapters: List<ChapterInSchoolResponse>
)

@Serializable
data class ChapterInSchoolResponse(
    val chapterId: Int,
    val chapterName: String,
    val schools: List<SchoolNameResponse>
)