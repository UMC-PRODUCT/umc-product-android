package com.umc.data.mapper.curriculum

import com.umc.data.response.curriculum.WeeklyBestWorkbooksResponse
import com.umc.domain.model.curriculum.WeeklyBestWorkbook
import com.umc.domain.model.curriculum.WeeklyBestWorkbookPage

fun WeeklyBestWorkbooksResponse.toDomain(): WeeklyBestWorkbookPage {
    return WeeklyBestWorkbookPage(
        content = content.map { item ->
            WeeklyBestWorkbook(
                weeklyBestWorkbookId =
                    item.weeklyBestWorkbookEntityId,
                memberId = item.memberId,
                studyGroupId = item.studyGroupId,
                decidedMemberId = item.decidedMemberId,
                reason = item.reason,
                challengerWorkbookIds =
                    item.challengerWorkbooks.map {
                        it.challengerWorkbookId
                    },
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext,
        hasPrevious = hasPrevious,
    )
}