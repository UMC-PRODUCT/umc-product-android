package com.umc.domain.model

import com.umc.domain.model.home.schedule.ScheduleMonthModel

object UDomainFormat {

    /**
    * 서버의 ISO DateTime 문자열(2026-02-07T05:24:54) -> "2026.02.07" , "05:24:54"
     * 처럼 날짜와 시간 String으로 분리하는 확장 함수
    */
    fun String.parseDateTime(): Pair<String, String> {
        if (this.isBlank()) return Pair("", "")

        val dateTimeParts = this.split("T")
        // 2026-02-07 -> 2026.02.07
        val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
        // 13:05:03 -> 13:05:03
        val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""

        return Pair(date, time)
    }



}