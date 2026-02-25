package com.umc.presentation.ui.act.study.common.mapper

import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

fun UserChallenger.toMemberUiModel(
    schoolName: String
): MemberUiModel {
    val partLabel = part.name
    val gisuLabel = "${generation}기"

    return MemberUiModel(
        challengerId = id,
        memberId = id,
        name = name,
        partLabel = partLabel,
        gisuLabel = gisuLabel,
        schoolName = schoolName,
        profileImageUrl = profileImage
    )
}