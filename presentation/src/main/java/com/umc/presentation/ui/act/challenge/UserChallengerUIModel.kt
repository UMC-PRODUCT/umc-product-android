package com.umc.presentation.ui.act.challenge

import com.umc.domain.model.act.challenger.UserChallenger

data class UserChallengerUIModel(
    val challenger: UserChallenger,
    val isLastInPart: Boolean = false // 파트 내 마지막 아이템인지 여부
) {
    val displayName: String = "${challenger.name}(${challenger.nickname})"
    val displayGeneration: String = "${challenger.generation}기"
}