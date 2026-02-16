package com.umc.presentation.ui.act.study.submit.model

import com.umc.domain.model.curriculum.WorkbookSubmissionItem

fun WorkbookSubmissionItem.toUiModel(weekNo: Int?): AdminActStudySubmitItemUiModel =
    AdminActStudySubmitItemUiModel(
        userId = challengerId,
        name = challengerName,
        nickname = "",
        partLabel = part.name,
        weekText = weekNo?.let { "${it}주차" } ?: "",
        studyTitle = workbookTitle,
        submitUrl = "",
        isBest = false
    )
