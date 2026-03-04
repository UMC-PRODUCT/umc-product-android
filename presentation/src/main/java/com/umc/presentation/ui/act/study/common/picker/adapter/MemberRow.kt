package com.umc.presentation.ui.act.study.common.picker.adapter

import com.umc.presentation.ui.act.study.common.model.MemberUiModel

sealed interface MemberRow {
    data class Header(val title: String) : MemberRow
    data class Item(val member: MemberUiModel) : MemberRow
}

fun buildMemberSectionRows(list: List<MemberUiModel>): List<MemberRow> {
    // 사진처럼 “파트 섹션”을 만들기 위한 우선순위
    val order = listOf("PM", "DESIGN", "WEB", "ANDROID", "IOS", "SERVER", "PLAN")

    val grouped = list.groupBy { it.partLabel.uppercase() }
    val rows = mutableListOf<MemberRow>()

    fun addSection(key: String, items: List<MemberUiModel>) {
        if (items.isEmpty()) return
        rows += MemberRow.Header(key.toPartUiLabel())
        rows += items.map { MemberRow.Item(it) }
    }

    // 우선순위대로 출력
    order.forEach { key -> addSection(key, grouped[key].orEmpty()) }

    // 우선순위에 없는 파트는 뒤에 정렬해서 출력
    grouped.keys.minus(order.toSet()).sorted().forEach { key ->
        addSection(key, grouped[key].orEmpty())
    }

    return rows
}

private fun String.toPartUiLabel(): String = when (this.uppercase()) {
    "WEB" -> "Web"
    "ANDROID" -> "Android"
    "IOS" -> "iOS"
    "SERVER" -> "Server"
    "DESIGN" -> "Design"
    "PLAN" -> "Plan"
    "PM" -> "PM"
    else -> this
}