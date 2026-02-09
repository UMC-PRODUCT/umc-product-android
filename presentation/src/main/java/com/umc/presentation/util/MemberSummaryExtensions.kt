// MemberSummaryExtensions.kt
package com.umc.presentation.util

import com.umc.presentation.ui.act.study.common.model.MemberUiModel

fun List<MemberUiModel>.toSummaryText(
    emptyText: String
): String {
    if (isEmpty()) return emptyText
    if (size == 1) return first().name
    return "${first().name} 외 ${size - 1}명"
}
