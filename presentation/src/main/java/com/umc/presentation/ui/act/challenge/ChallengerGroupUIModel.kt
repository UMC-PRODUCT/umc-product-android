package com.umc.presentation.ui.act.challenge

import com.umc.domain.model.act.challenger.UserPartCount
import com.umc.domain.model.enums.UserPart

data class ChallengerGroupUIModel(
    val part: UserPart,
    val items: List<UserChallengerUIModel>,
    val partCount: UserPartCount
)