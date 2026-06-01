package com.umc.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object UDomainFormat {
    /**
     * 서버의 ISO DateTime 문자열(2026-02-07T05:24:54) -> "2026.02.07" , "05:24:54"
     * 처럼 날짜와 시간 String으로 분리하는 확장 함수
     *
     * TODO: 02-24 추가 : UTC 판별을 해서 시간까지 쫘악 바꿔주는 로직 추가
     */
    fun String.parseDateTime(): Pair<String, String> {
        if (this.isBlank()) return Pair("", "")

        return try{
            //일단 Z로 안끝나면 붙여주기
            val normalizedInput = if (!this.endsWith("Z")) "${this}Z" else this

            //ISO_DATE_TIME 형식으로 파싱
            val utcTime = ZonedDateTime.parse(normalizedInput)

            //사용자의 현재 기기 시간대(Local)로 변환
            val localTime = utcTime.withZoneSameInstant(ZoneId.systemDefault())

            //날짜와 시간을 원하는 포맷으로 분리
            val date = localTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            Pair(date, time)

        } catch (e: Exception) {
            // 파싱 실패 시 예외 처리 (기존 String 자르기 방식으로 폴백)
            val date = this.split("T").getOrNull(0)?.replace("-", ".") ?: ""
            val time = this.split("T").getOrNull(1)?.take(5) ?: ""
            Pair(date, time)
        }
    }

    /**
     * UTC ISO 문자열 두 개를 KST(Asia/Seoul)로 변환했을 때
     * 같은 날 00:00:00.000 ~ 23:59:59.999 에 해당하면 true 반환.
     * e.g. 2025-12-08T15:00:00.000Z ~ 2025-12-09T14:59:59.999Z → KST 2025-12-09 00:00~23:59 → true
     */
    fun isAllDayInKst(startsAt: String, endsAt: String): Boolean {
        return try {
            val kst = ZoneId.of("Asia/Seoul")
            val normalizedStart = if (!startsAt.endsWith("Z")) "${startsAt}Z" else startsAt
            val normalizedEnd = if (!endsAt.endsWith("Z")) "${endsAt}Z" else endsAt

            val startKst = ZonedDateTime.parse(normalizedStart).withZoneSameInstant(kst)
            val endKst = ZonedDateTime.parse(normalizedEnd).withZoneSameInstant(kst)

            startKst.toLocalDate() == endKst.toLocalDate() &&
                startKst.hour == 0 && startKst.minute == 0 && startKst.second == 0 && startKst.nano == 0 &&
                endKst.hour == 23 && endKst.minute == 59 && endKst.second == 59 && endKst.nano == 999_000_000
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 날짜 문자열을 기기 로컬 시간대 00:00:00.000 기준으로 UTC ISO 문자열로 변환한다.
     */
    fun String.formatDateForServer(): String {
        val localDate = parseLocalDateOrNull(this) ?: return ""
        val localDateTime = localDate.atStartOfDay(ZoneId.systemDefault())
        val utcInstant = localDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant()

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .withZone(ZoneOffset.UTC)
            .format(utcInstant)
    }

    /**
     * 날짜 문자열을 기기 로컬 시간대 23:59:59.999 기준으로 UTC ISO 문자열로 변환한다.
     * 하루종일 일정의 endsAt 전송 시 사용.
     */
    fun String.formatAllDayEndForServer(): String {
        val localDate = parseLocalDateOrNull(this) ?: return ""
        val endOfDay = localDate.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault())
        val utcInstant = endOfDay.withZoneSameInstant(ZoneOffset.UTC).toInstant()

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .withZone(ZoneOffset.UTC)
            .format(utcInstant)
    }

    /**
     * 날짜/시간 문자열을 기기 로컬 시간대로 해석 후 UTC ISO 문자열로 변환한다.
     * timeString 예시: "09:30", "09:30:00"
     */
    fun String.formatDateTimeForServer(timeString: String): String {
        val localDate = parseLocalDateOrNull(this) ?: return ""
        val localTime = parseLocalTimeOrNull(timeString) ?: return ""

        val localDateTime = localDate.atTime(localTime).atZone(ZoneId.systemDefault())
        val utcInstant = localDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant()

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .withZone(ZoneOffset.UTC)
            .format(utcInstant)
    }

    private fun parseLocalDateOrNull(dateString: String): LocalDate? {
        val input = dateString.trim()

        val koreanRegex = """(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일""".toRegex()
        val dotRegex = """(\d{4})\.(\d{1,2})\.(\d{1,2})""".toRegex()
        val dashRegex = """(\d{4})-(\d{1,2})-(\d{1,2})""".toRegex()

        return when {
            koreanRegex.matches(input) -> {
                val (year, month, day) = koreanRegex.find(input)!!.destructured
                LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            dotRegex.matches(input) -> {
                val (year, month, day) = dotRegex.find(input)!!.destructured
                LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            dashRegex.matches(input) -> {
                val (year, month, day) = dashRegex.find(input)!!.destructured
                LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            else -> runCatching { LocalDate.parse(input) }.getOrNull()
        }
    }

    private fun parseLocalTimeOrNull(timeString: String): LocalTime? {
        val input = timeString.trim()
        if (input.isBlank()) return null

        return runCatching { LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm:ss")) }.getOrNull()
            ?: runCatching { LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm")) }.getOrNull()
    }
}