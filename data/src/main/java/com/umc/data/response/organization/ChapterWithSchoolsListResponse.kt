package com.umc.data.response.organization

import kotlinx.serialization.Serializable
import com.umc.data.response.organization.SchoolNameResponse.Companion.toModel
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.Chapter as ChapterModel

@Serializable
data class ChapterWithSchoolsListResponse(
    val chapters: List<ChapterInSchoolResponse>
)

@Serializable
data class ChapterInSchoolResponse(
    val chapterId: Int,
    val chapterName: String,
    val schools: List<SchoolNameResponse>
)

fun ChapterWithSchoolsListResponse.toModel(): ChapterWithSchool = ChapterWithSchool(
    chapterList = chapters.map { ChapterModel(it.chapterId, it.chapterName) },
    schoolList = chapters.flatMap { it.schools }.distinctBy { it.schoolId }.map { it.toModel() },
    schoolIdsByChapter = chapters.associate { chapter -> chapter.chapterId to chapter.schools.map { it.schoolId } },
)
