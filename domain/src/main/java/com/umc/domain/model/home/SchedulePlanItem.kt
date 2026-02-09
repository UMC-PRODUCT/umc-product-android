package com.umc.domain.model.home

//홈의 recyclerview를 표현할 때 필요한 data class
data class SchedulePlanItem(
    val id: Long,          // 1
    val title: String,    // "아이디어톤"
    val time: String,     // "10:00"
    val date: String,     // "2026.03.27"
    val dayOfWeek: String, // "MON"
    val day: String,       // "27"
    val dDay: String?,     // "D-1" (과거면 null)
    val isPast: Boolean,   // 과거 여부 (true면 회색, false면 파란색)
    val plusDay: Int,      // 시작 일정 기준으로 몇일이 지났는지 여부(02.06~02.08 등 연속 일정일 때 표시)
)