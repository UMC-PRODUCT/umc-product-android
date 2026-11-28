package com.umc.domain.model

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 날짜·시간 선택 다이얼로그 초기 입력 상태.
 *
 * @property dateDigits `yyyyMMdd` 8자리 숫자 문자열
 * @property timeDigits12h 12시간제 `hhmm` 4자리 숫자 문자열 (`0100`~`1259`)
 * @property isAm 오전 여부 (`00:00`~`11:59`면 `true`, `12:00`~`23:59`면 `false`)
 */
data class DateTimePickerInitial(
    val dateDigits: String,
    val timeDigits12h: String,
    val isAm: Boolean,
)

object UDomainFormat {
    /** 기기 기본 시간대 기준으로 [parseDateTime]을 호출한다. */
    fun String.parseDateTime(): Pair<String, String> = parseDateTime(ZoneId.systemDefault())

    /**
     * 서버의 ISO 시각 문자열을 기기(또는 [zone]) 시간대의 날짜와 시각으로 나눈다.
     * 예: "2026-02-07T05:24:54"(UTC) → 서울 기준 ("2026.02.07", "14:24")
     *
     * - 오프셋이 없으면 서버 약속대로 UTC 로 보고, `Z`·`z`·`+09:00`·`+0900` 같은 오프셋이 있으면 그 오프셋을 따른다.
     * - 시각으로 읽을 수 없으면 문자열에서 날짜(yyyy-MM-dd)와 시각(HH:mm)만 찾아 시간대 변환 없이 돌려준다.
     *   찾지 못한 쪽은 빈 문자열이다. 어떤 입력에도 예외를 던지지 않는다.
     */
    fun String.parseDateTime(zone: ZoneId): Pair<String, String> {
        val input = trim()
        if (input.isEmpty()) return "" to ""
        val parsed = parseServerTime(input) ?: return fallback(input)
        // 범위 끝의 연도는 시간대 변환에서 예외가 나고, 다섯 자리 연도는 yyyy.MM.dd 모양을 벗어나므로 문자열에서 찾는 쪽으로 넘긴다.
        return runCatching { parsed.withZoneSameInstant(zone).let { it.format(DATE) to it.format(TIME) } }
            .getOrNull()
            ?.takeIf { (date, _) -> DATE_SHAPE.matches(date) }
            ?: fallback(input)
    }

    /**
     * 서버의 ISO 시각 문자열을 다이얼로그·비교용 표준 UTC 밀리초 형식(`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`)으로 정규화한다.
     * 공백이면 `null`, 파싱할 수 없는 비표준 문자열이면 앞뒤 공백만 제거한 원문을 돌려준다.
     */
    fun normalizeUtcDateTime(raw: String?): String? {
        val input = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val parsed = parseServerTime(input) ?: return input
        return runCatching {
            parsed.withZoneSameInstant(ZoneOffset.UTC)
                .takeIf { it.year in 1..9999 }
                ?.format(UTC_MILLIS)
        }.getOrNull() ?: input
    }

    /**
     * 두 ISO 시각 문자열을 Instant 기준으로 비교해 [start]가 [end]보다 앞선 시각인지 반환한다.
     * 소수초 유무(`...00Z` vs `...00.000Z`)나 오프셋 표기가 달라도 같은 순간이면 `false`다.
     */
    fun isDateTimeBefore(start: String?, end: String?): Boolean {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return false
        val startInstant = runCatching { parseServerTime(start.trim())?.toInstant() }.getOrNull() ?: return false
        val endInstant = runCatching { parseServerTime(end.trim())?.toInstant() }.getOrNull() ?: return false
        return startInstant.isBefore(endInstant)
    }

    /**
     * 서버·다이얼로그의 ISO 시각 문자열을 `"2025.01.01 ･ 오후 4:00"` 형태의 시간대 표기로 변환한다.
     * [zoneId]가 `null`이거나 유효하지 않으면 기기 기본 시간대를 사용한다. 파싱 실패 시 `null`을 반환한다.
     */
    fun formatVoteDateTime(raw: String?, zoneId: String? = null): String? =
        formatVoteDateTime(raw, resolveZone(zoneId))

    fun formatVoteDateTime(raw: String?, zone: ZoneId): String? {
        val input = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val parsed = parseServerTime(input) ?: return null
        return runCatching {
            parsed.withZoneSameInstant(zone)
                .takeIf { it.year in 1..9999 }
                ?.format(VOTE_DATE_TIME)
        }.getOrNull()
    }

    /**
     * ISO 시각 문자열을 날짜·시간 다이얼로그 초기 입력값([DateTimePickerInitial])으로 변환한다.
     * 자정(`00:xx`)은 오전 `12:xx`, 정오(`12:xx`)는 오후 `12:xx`로 매핑한다.
     */
    fun parseDateTimePickerInitial(raw: String?, zoneId: String? = null): DateTimePickerInitial? =
        parseDateTimePickerInitial(raw, resolveZone(zoneId))

    fun parseDateTimePickerInitial(raw: String?, zone: ZoneId): DateTimePickerInitial? {
        val input = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val zoned = runCatching {
            parseServerTime(input)?.withZoneSameInstant(zone)
        }.getOrNull()?.takeIf { it.year in 1..9999 } ?: return null

        val hour24 = zoned.hour
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return DateTimePickerInitial(
            dateDigits = "%04d%02d%02d".format(Locale.ROOT, zoned.year, zoned.monthValue, zoned.dayOfMonth),
            timeDigits12h = "%02d%02d".format(Locale.ROOT, hour12, zoned.minute),
            isAm = hour24 < 12,
        )
    }

    private fun resolveZone(zoneId: String?): ZoneId =
        zoneId?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: ZoneId.systemDefault()

    private fun parseServerTime(input: String): ZonedDateTime? {
        // ISO 파서는 날짜·시각 구분자의 대소문자를 가리지 않으므로 소문자 t 도 찾는다.
        val timeStart = input.indexOf('T', ignoreCase = true)
        if (timeStart < 0) return null
        val time = input.substring(timeStart + 1)
        val normalized = when {
            time.endsWith("z") -> input.dropLast(1) + "Z"
            // ISO 파서는 콜론 없는 오프셋(+0900, +090000)을 받지 않으므로 +09:00 꼴로 바꾼다.
            ZONE_SUFFIX.containsMatchIn(time) -> input.substring(0, timeStart + 1) + time.replace(COMPACT_OFFSET) { match ->
                val (hours, minutes, seconds, region) = match.destructured
                "$hours:$minutes" + (if (seconds.isEmpty()) "" else ":$seconds") + region
            }
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
    private val UTC_MILLIS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
    private val VOTE_DATE_TIME: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd ･ a h:mm", Locale.KOREAN)

    /** 시각 끝의 오프셋: Z(z), +09, +09:00, +0900, +09:00:00, 뒤에 붙는 [지역 ID]까지. */
    private val ZONE_SUFFIX = Regex("""([Zz]|[+-]\d{2}(:?\d{2}){0,2})(\[[^\]]+])?$""")
    private val COMPACT_OFFSET = Regex("""([+-]\d{2})(\d{2})(\d{2})?(\[[^\]]+])?$""")
    private val DATE_SHAPE = Regex("""\d{4}\.\d{2}\.\d{2}""")
    private val DATE_IN_TEXT = Regex("""(?<!\d)(\d{4})-(\d{2})-(\d{2})""")
    private val TIME_IN_TEXT = Regex("""(?<!\d)(\d{2}):(\d{2})""")
}
