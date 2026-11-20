package com.umc.domain.model

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object UDomainFormat {
    /**
     * 서버의 ISO 시각 문자열을 기기(또는 [zone]) 시간대의 날짜와 시각으로 나눈다.
     * 예: "2026-02-07T05:24:54"(UTC) → 서울 기준 ("2026.02.07", "14:24")
     *
     * - 오프셋이 없으면 서버 약속대로 UTC 로 보고, `Z`·`z`·`+09:00` 같은 오프셋이 있으면 그 오프셋을 따른다.
     * - 시각으로 읽을 수 없으면 문자열에서 날짜(yyyy-MM-dd)와 시각(HH:mm)만 찾아 시간대 변환 없이 돌려준다.
     *   찾지 못한 쪽은 빈 문자열이다. 어떤 입력에도 예외를 던지지 않는다.
     */
    fun String.parseDateTime(zone: ZoneId = ZoneId.systemDefault()): Pair<String, String> {
        val input = trim()
        if (input.isEmpty()) return "" to ""
        val local = parseServerTime(input)?.withZoneSameInstant(zone) ?: return fallback(input)
        return local.format(DATE) to local.format(TIME)
    }

    private fun parseServerTime(input: String): ZonedDateTime? {
        val timeStart = input.indexOf('T')
        if (timeStart < 0) return null
        val time = input.substring(timeStart + 1)
        val normalized = when {
            time.endsWith("z") -> input.dropLast(1) + "Z"
            ZONE_SUFFIX.containsMatchIn(time) -> input
            else -> input + "Z"
        }
        return runCatching { ZonedDateTime.parse(normalized) }.getOrNull()
    }

    private fun fallback(input: String): Pair<String, String> {
        val dateMatch = DATE_IN_TEXT.find(input)
        val date = dateMatch?.destructured?.let { (year, month, day) -> "$year.$month.$day" } ?: ""
        val rest = dateMatch?.let { input.substring(it.range.last + 1) } ?: input
        val time = TIME_IN_TEXT.find(rest)?.destructured?.let { (hour, minute) -> "$hour:$minute" } ?: ""
        return date to time
    }

    private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /** 시각 끝의 오프셋: Z, +09, +09:00, +0900, +09:00:00, 뒤에 붙는 [지역 ID]까지. */
    private val ZONE_SUFFIX = Regex("""(Z|[+-]\d{2}(:?\d{2}){0,2})(\[[^\]]+])?$""")
    private val DATE_IN_TEXT = Regex("""(\d{4})-(\d{2})-(\d{2})""")
    private val TIME_IN_TEXT = Regex("""(?<!\d)(\d{2}):(\d{2})""")
}
