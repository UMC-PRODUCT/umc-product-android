package com.umc.presentation.ui.act.study.submit.model

import com.umc.domain.model.curriculum.WorkbookSubmissionItem

fun WorkbookSubmissionItem.toUiModel(weekNo: Int): AdminActStudySubmitItemUiModel =
    AdminActStudySubmitItemUiModel(
        challengerWorkbookId = challengerWorkbookId,
        userId = challengerId,
        name = memberName.ifBlank { "홍길동" },
        nickname = challengerName.ifBlank { "" },
        partLabel = part.name,
        weekText = "${weekNo}주차",
        studyTitle = workbookTitle,
        submitUrl = "",
        schoolName = schoolName.ifBlank { "서울대학교" },
        profileImageUrl = profileImageUrl,
        status = status
    )