package com.umc.presentation.ui.act.study.common.picker

import com.umc.presentation.ui.act.study.common.model.MemberUiModel

fun formatMemberSummary(selected: List<MemberUiModel>): String {
    if (selected.isEmpty()) return "스터디원을 추가하세요"
    if (selected.size == 1) return selected.first().name
    return "${selected.first().name} 외 ${selected.size - 1}명"
}

object MemberPickerFormatter {

    fun formatSummary(members: List<MemberUiModel>): String {
        if (members.isEmpty()) return ""
        if (members.size == 1) return members.first().name
        return "${members.first().name} 외 ${members.size - 1}명"
    }
}