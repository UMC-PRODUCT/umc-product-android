package com.umc.data.mapper.curriculum

import com.umc.data.remote.response.curriculum.WorkbookSubmissionItemResponse
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.domain.model.base.CursorPage
import com.umc.domain.model.curriculum.WorkbookSubmissionItem
import com.umc.domain.model.enums.UserPart

fun WorkbookSubmissionItemResponse.toDomain(): WorkbookSubmissionItem =
    WorkbookSubmissionItem(
        challengerWorkbookId = challengerWorkbookId,
        challengerId = challengerId,
        challengerName = challengerName,
        profileImageUrl = profileImageUrl.orEmpty(),
        schoolName = schoolName,
        part = UserPart.from(part),
        workbookTitle = workbookTitle,
        status = status,
    )

fun WorkbookSubmissionsResponse.toDomain(): CursorPage<WorkbookSubmissionItem> =
    CursorPage(
        content = content.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )
