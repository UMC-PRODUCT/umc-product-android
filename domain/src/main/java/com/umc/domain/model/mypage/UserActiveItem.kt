package com.umc.domain.model.mypage

import com.umc.domain.model.enums.UserChallengerRole

data class UserActiveItem (
    val generation: String, //ex. 12기
    val partName: String,   //ex. Android Part
    val position: String,   //ex. 교내 파트장 (역할 태그 이름)
    val role: UserChallengerRole = UserChallengerRole.CHALLENGER, //역할 태그 색을 정한다
)
