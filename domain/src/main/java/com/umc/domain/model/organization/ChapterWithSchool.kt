package com.umc.domain.model.organization

import com.umc.domain.model.school.SchoolInfo

data class ChapterWithSchool(
    val chapterList: List<Chapter>,
    val schoolList: List<SchoolInfo>
)
