package com.umc.domain.model.organization

import com.umc.domain.model.school.SchoolInfo

data class ChapterWithSchool(
    val chapterList: List<Chapter>,
    val schoolList: List<SchoolInfo>,
    /** 선택 기수의 지부별 학교 ID. 프로필의 지부나 다른 기수의 연결을 대신 사용하지 않는다. */
    val schoolIdsByChapter: Map<Int, List<Int>> = emptyMap(),
)
