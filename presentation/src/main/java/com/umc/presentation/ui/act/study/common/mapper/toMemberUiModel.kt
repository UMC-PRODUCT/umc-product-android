package com.umc.presentation.ui.act.study.common.mapper

import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

fun UserChallenger.toMemberUiModel(): MemberUiModel {
    val partLabel = part.name
    val gisuLabel = "$generation"

    return MemberUiModel(
        challengerId = id,
        name = name,
        nickname = nickname.orEmpty(),
        partLabel = partLabel,
        gisuLabel = gisuLabel,
        schoolName = schoolName,
        profileImageUrl = profileImage,
        memberId = memberId
    )
}