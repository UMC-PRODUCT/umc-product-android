package com.umc.presentation.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object UFormat {

    // 출력할 날짜 포맷: 2026.03.03 (화)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)

    /**
     * ISO 8601 문자열에서 날짜만 추출하여 포맷팅
     * 입력: "2026-03-03T20:00:00Z" -> 출력: "2026.03.03 (화)"
     */
    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            // T 앞부분(날짜)만 분리
            val datePart = isoString.split("T")[0]
            val date = LocalDate.parse(datePart)
            date.format(dateFormatter)
        } catch (e: Exception) {
            isoString.replace("-", ".")
        }
    }

    /**
     * 시작/종료 ISO 문자열에서 시간만 추출하여 "HH:mm - HH:mm" 변환
     * 입력: "2026-03-03T20:00:00Z", "2026-03-04T15:00:00Z"
     * 출력: "20:00 - 15:00"
     */
    fun formatDuration(startIso: String?, endIso: String?): String {
        if (startIso.isNullOrBlank() || endIso.isNullOrBlank()) return "- : -"
        return try {
            // T 뒷부분에서 시간(HH:mm)만 추출
            val startTime = extractTime(startIso)
            val endTime = extractTime(endIso)

            "$startTime - $endTime"
        } catch (e: Exception) {
            "- : -"
        }
    }

    /**
     * ISO 문자열에서 HH:mm 부분만 떼어내는 보조 함수
     */
    private fun extractTime(isoString: String): String {
        return try {
            val timePart = isoString.split("T")[1].substring(0, 5) // HH:mm 까지만
            val time = LocalTime.parse(timePart)

            // 만약 종료 시간이 00:00이라면 24:00으로 표시하고 싶을 때
            if (time == LocalTime.MIDNIGHT && isoString.contains("endsAt")) "24:00"
            else timePart
        } catch (e: Exception) {
            "00:00"
        }
    }

    /**
     * 서버의 ISO DateTime 문자열(2026-02-07T05:24:54) -> "2026.02.07" , "05:24"
     * 후에 instant로 바뀌어도, T만 존재하면 분리가 가능하다.
     * 처럼 날짜와 시간 String으로 분리하는 확장 함수
     */
    fun parseDateTime(date: String): Pair<String, String> {
        if (date.isBlank()) return Pair("", "")

        val dateTimeParts = date.split("T")
        // 2026-02-07 -> 2026.02.07
        val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
        // 13:05:03 -> 13:05:03
        val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""

        return Pair(date, time)
    }


}