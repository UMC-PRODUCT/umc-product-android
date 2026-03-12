package com.umc.presentation.ui.act.study.common.picker.adapter

import com.umc.domain.model.enums.UserPart
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

sealed interface MemberRow {
    data class Header(val title: String) : MemberRow
    data class Item(val member: MemberUiModel) : MemberRow
}

fun buildMemberSectionRows(list: List<MemberUiModel>): List<MemberRow> {

    // enum 기반 파트 순서
    val order = listOf(
        UserPart.PLAN,
        UserPart.DESIGN,
        UserPart.WEB,
        UserPart.ANDROID,
        UserPart.IOS,
        UserPart.SPRINGBOOT,
        UserPart.NODEJS
    )

    val grouped = list.groupBy { UserPart.from(it.partLabel) }

    val rows = mutableListOf<MemberRow>()

    fun addSection(part: UserPart, items: List<MemberUiModel>) {
        if (items.isEmpty()) return
        rows += MemberRow.Header(part.label)
        rows += items.map { MemberRow.Item(it) }
    }

    // 우선순위 출력
    order.forEach { part ->
        addSection(part, grouped[part].orEmpty())
    }

    // 우선순위 없는 파트 뒤에 출력
    grouped.keys
        .minus(order.toSet())
        .sortedBy { it.label }
        .forEach { part ->
            addSection(part, grouped[part].orEmpty())
        }

    return rows
}