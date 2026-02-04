package com.umc.domain.model.mypage

import com.umc.domain.model.enums.SuggestionStatus

/**사용 안함**/
data class SuggestionItem(
    val status: SuggestionStatus,
    val date: String,
    val title: String,
    val content: String,
    val adminAnswer: String? = null // 답변이 없으면 null
)