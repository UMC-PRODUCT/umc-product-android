package com.umc.presentation.ui.act.study.submit.model

import com.umc.domain.model.curriculum.WorkbookSubmissionItem
import com.umc.domain.model.enums.UserPart

fun WorkbookSubmissionItem.toUiModel(
    weekNo: Int,
    isBestEnabled: Boolean,
    isReviewEnabled: Boolean,
): AdminActStudySubmitItemUiModel =
    AdminActStudySubmitItemUiModel(
        challengerWorkbookId = challengerWorkbookId,
        userId = challengerId,
        name = memberName.ifBlank { "홍길동" },
        nickname = challengerName.ifBlank { "" },
        partLabel = UserPart.ifGetSpring(part),
        weekText = "${weekNo}주차",
        studyTitle = workbookTitle,
        submitUrl = "",
        schoolName = schoolName.ifBlank { "서울대학교" },
        profileImageUrl = profileImageUrl,
        status = status,
        isBestEnabled = isBestEnabled,
        isReviewEnabled = isReviewEnabled
    )