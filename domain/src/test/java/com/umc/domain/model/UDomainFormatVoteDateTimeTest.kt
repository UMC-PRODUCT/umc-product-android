package com.umc.domain.model

import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.notice.NoticeVoteForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class UDomainFormatVoteDateTimeTest {

    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")

    @Test
    fun `KST 0~9시 작성 공지의 UTC 시각은 서울 시간대 기준 당일 날짜로 변환된다`() {
        assertEquals("2026.11.02" to "23:59", "2026-11-02T14:59:59Z".parseDateTime(seoul))
        assertEquals("2026.11.03" to "00:00", "2026-11-02T15:00:00Z".parseDateTime(seoul))
        assertEquals("2026.11.03" to "00:42", "2026-11-02T15:42:10Z".parseDateTime(seoul))
        assertEquals("2026.11.03" to "08:59", "2026-11-02T23:59:59Z".parseDateTime(seoul))
    }

    @Test
    fun `normalizeUtcDateTime은 소수초 생략과 마이크로초와 오프셋을 표준 UTC 밀리초 문자열로 정규화한다`() {
        assertEquals("2026-11-28T07:00:00.000Z", UDomainFormat.normalizeUtcDateTime("2026-11-28T07:00:00Z"))
        assertEquals("2026-11-28T07:00:00.123Z", UDomainFormat.normalizeUtcDateTime("2026-11-28T07:00:00.123456Z"))
        assertEquals("2026-11-28T07:00:00.000Z", UDomainFormat.normalizeUtcDateTime("2026-11-28T16:00:00+09:00"))
        assertEquals("2026-11-28T07:00:00.000Z", UDomainFormat.normalizeUtcDateTime("2026-11-28T07:00:00"))
        assertNull(UDomainFormat.normalizeUtcDateTime(null))
        assertNull(UDomainFormat.normalizeUtcDateTime("   "))
        assertEquals("not-a-date", UDomainFormat.normalizeUtcDateTime("  not-a-date  "))
    }

    @Test
    fun `formatVoteDateTime은 소수초 없는 서버 응답과 마이크로초 응답을 분 밀림 없이 표시한다`() {
        assertEquals(
            "2026.01.01 ･ 오후 4:00",
            UDomainFormat.formatVoteDateTime("2026-01-01T07:00:00Z", seoul),
        )
        assertEquals(
            "2026.01.01 ･ 오후 4:00",
            UDomainFormat.formatVoteDateTime("2026-01-01T07:00:00.987654Z", seoul),
        )
        assertEquals(
            "2026.01.02 ･ 오전 12:05",
            UDomainFormat.formatVoteDateTime("2026-01-01T15:05:00Z", seoul),
        )
        assertEquals(
            "2026.01.01 ･ 오후 12:05",
            UDomainFormat.formatVoteDateTime("2026-01-01T03:05:00Z", seoul),
        )
        assertNull(UDomainFormat.formatVoteDateTime(null, seoul))
        assertNull(UDomainFormat.formatVoteDateTime("", seoul))
        assertNull(UDomainFormat.formatVoteDateTime("2026-01-01", seoul))
    }

    @Test
    fun `parseDateTimePickerInitial은 자정과 정오를 포함한 12시간제 초기 입력값을 만든다`() {
        assertEquals(
            DateTimePickerInitial(dateDigits = "20260102", timeDigits12h = "1230", isAm = true),
            UDomainFormat.parseDateTimePickerInitial("2026-01-01T15:30:00Z", seoul),
        )
        assertEquals(
            DateTimePickerInitial(dateDigits = "20260101", timeDigits12h = "0905", isAm = true),
            UDomainFormat.parseDateTimePickerInitial("2026-01-01T00:05:00Z", seoul),
        )
        assertEquals(
            DateTimePickerInitial(dateDigits = "20260101", timeDigits12h = "1230", isAm = false),
            UDomainFormat.parseDateTimePickerInitial("2026-01-01T03:30:00Z", seoul),
        )
        assertEquals(
            DateTimePickerInitial(dateDigits = "20260101", timeDigits12h = "0400", isAm = false),
            UDomainFormat.parseDateTimePickerInitial("2026-01-01T07:00:00Z", seoul),
        )
        assertNull(UDomainFormat.parseDateTimePickerInitial(null, seoul))
        assertNull(UDomainFormat.parseDateTimePickerInitial("invalid", seoul))
    }

    @Test
    fun `NoticeVoteForm canSubmit은 소수초 표기가 달라도 동일 시각을 시작 이전으로 오판하지 않는다`() {
        val sameInstantDifferentFormat = NoticeVoteForm(
            title = "투표",
            options = listOf("A", "B"),
            startsAt = "2026-11-28T07:00:00.000Z",
            endsAt = "2026-11-28T07:00:00Z",
        )
        assertFalse(sameInstantDifferentFormat.canSubmit)

        val validRange = sameInstantDifferentFormat.copy(
            endsAt = "2026-11-28T08:00:00Z",
        )
        assertTrue(validRange.canSubmit)

        val invalidDate = sameInstantDifferentFormat.copy(
            startsAt = "invalid",
            endsAt = "2026-11-28T08:00:00Z",
        )
        assertFalse(invalidDate.canSubmit)
    }
}
