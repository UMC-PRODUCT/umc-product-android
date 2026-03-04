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
            val systemDateTime = OffsetDateTime.parse(isoString)
                .atZoneSameInstant(ZoneId.systemDefault())
            systemDateTime.format(dateFormatter)
        } catch (e: Exception) {
            isoString.split("T")[0].replace("-", ".")
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
            val startTime = convertToSystemTime(startIso)
            val endTime = convertToSystemTime(endIso)
            "$startTime - $endTime"
        } catch (e: Exception) {
            "- : -"
        }
    }

    /**
     * UTC ISO 문자열을 시스템 시간대의 HH:mm 문자열로 변환
     */
    private fun convertToSystemTime(isoString: String): String {
        return try {
            val systemDateTime = OffsetDateTime.parse(isoString)
                .atZoneSameInstant(ZoneId.systemDefault())
            systemDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
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