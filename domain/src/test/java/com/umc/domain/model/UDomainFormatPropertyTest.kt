package com.umc.domain.model

import com.umc.domain.model.UDomainFormat.parseDateTime
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** 서버 시각 문자열을 화면용 날짜·시각으로 바꾸는 parseDateTime 의 속성. */
class UDomainFormatPropertyTest {

    /** 2000~2100년. yyyy 가 연대(era) 기준이고 서울은 1987~88년에 서머타임이 있어 범위를 좁힌다. */
    private val instants = Arb.long(946_684_800L..4_102_444_800L)
    private val nanos = Arb.int(0..999_999_999)
    private val fractionDigits = Arb.int(0..9)
    private val zones = Arb.element(
        listOf("Asia/Seoul", "UTC", "America/New_York", "Asia/Kathmandu", "Australia/Lord_Howe").map(ZoneId::of),
    )

    /** 서버가 같은 순간을 쓸 수 있는 표기. 오프셋이 없으면 UTC 로 본다(서버 약속). */
    private val notations = Arb.element("", "Z", "z", "+00:00", "+09:00", "-03:30", "+0000", "+0900", "-0330")

    @Test
    fun `같은 순간은 어떤 표기로 와도 같은 날짜와 시각으로 보인다`() = runBlocking<Unit> {
        checkAll(1000, instants, nanos, fractionDigits, zones, notations) { seconds, nano, digits, zone, notation ->
            val instant = Instant.ofEpochSecond(seconds, nano.toLong())
            val offset = when (notation) {
                "", "Z", "z", "+00:00" -> ZoneOffset.UTC
                else -> ZoneOffset.of(notation)
            }
            val local = instant.atOffset(offset).toLocalDateTime()
            val fraction = if (digits == 0) "" else "." + "%09d".format(local.nano).take(digits)
            val text = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + fraction + notation

            // 소수 자릿수를 잘라 쓴 만큼만 다른 순간이 되므로, 기대값도 잘린 순간으로 만든다.
            val shown = local.withNano(if (digits == 0) 0 else ("%09d".format(local.nano).take(digits).padEnd(9, '0')).toInt())
                .atOffset(offset).atZoneSameInstant(zone)
            val expected = shown.format(DATE) to shown.format(TIME)
            assertEquals("입력 $text, 시간대 $zone", expected, text.parseDateTime(zone))
        }
    }

    @Test
    fun `어떤 입력에도 예외 없이 약속한 형식이나 빈 문자열을 돌려준다`() = runBlocking<Unit> {
        val shaped = Arb.element(
            "2026-02-07 05:24:54", "2026-02-07", "2026-02-07T", "T05:24", "2026-02-07T05", "2026-13-40T25:61:61",
            "2026-02-07T05:24:54+0900", "2026-02-07T05:24:54[Asia/Seoul]", "  2026-02-07T05:24:54Z  ", "어제 오후 3시",
            // ISO 파서가 받는 범위 끝의 연도. 시간대 변환에서 예외가 나거나 다섯 자리 연도가 된다.
            "+999999999-12-31T23:59:59Z", "+10000-01-01T00:00:00Z", "-999999999-01-01T00:00:00Z",
        )
        checkAll(1000, Arb.string(0..40), shaped) { random, sample ->
            listOf(random, sample).forEach { input ->
                val (date, time) = input.parseDateTime(ZoneId.of("Asia/Seoul"))
                assertTrue("입력 '$input' → 날짜 '$date'", date.isEmpty() || DATE_SHAPE.matches(date))
                assertTrue("입력 '$input' → 시각 '$time'", time.isEmpty() || TIME_SHAPE.matches(time))
            }
        }
    }

    @Test
    fun `콜론 없는 오프셋도 그 오프셋을 따른다`() {
        val seoul = ZoneId.of("Asia/Seoul")
        assertEquals("2026.02.07" to "14:24", "2026-02-07T05:24:54+0000".parseDateTime(seoul))
        assertEquals("2026.02.07" to "17:54", "2026-02-07T05:24:54-0330".parseDateTime(seoul))
        assertEquals("2026.02.07" to "05:24", "2026-02-07T05:24:54+0900[Asia/Seoul]".parseDateTime(seoul))
    }

    @Test
    fun `소문자 t 구분자와 지역 ID 앞의 소문자 z 도 시각으로 읽는다`() {
        val seoul = ZoneId.of("Asia/Seoul")
        assertEquals("2026.02.08" to "08:24", "2026-02-07t23:24:54Z".parseDateTime(seoul))
        assertEquals("2026.02.08" to "08:24", "2026-02-07t23:24:54".parseDateTime(seoul))
        assertEquals("2026.02.08" to "08:24", "2026-02-07T23:24:54z[UTC]".parseDateTime(seoul))
    }

    private companion object {
        val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val DATE_SHAPE = Regex("""\d{4}\.\d{2}\.\d{2}""")
        val TIME_SHAPE = Regex("""\d{2}:\d{2}""")
    }
}
