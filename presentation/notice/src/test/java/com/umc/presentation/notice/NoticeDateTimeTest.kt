package com.umc.presentation.notice

import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteForm
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.notice.write.formatVoteDateTime
import com.umc.presentation.notice.write.toVoteForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class NoticeDateTimeTest {

    @Test
    fun `formatNoticeDate는 기기 시간대를 기준으로 UTC 자정 이후 한국 시각을 당일 날짜로 표시한다`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
            assertEquals("2026.11.03", formatNoticeDate("2026-11-02T15:42:10Z"))
            assertEquals("2026.11.02", formatNoticeDate("2026-11-02T14:59:59Z"))
            assertEquals("2026.02.07", formatNoticeDate("2026-02-07"))
            assertEquals("", formatNoticeDate(""))
            assertEquals("", formatNoticeDate("invalid"))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun `서버 투표 응답을 toVoteForm으로 변환하면 소수초가 없어도 시트 표시와 피커 포맷 비교가 일치한다`() {
        val serverVote = NoticeVote(
            voteId = 10L,
            title = "회식 메뉴",
            isAnonymous = true,
            allowMultipleChoice = false,
            startsAt = "2026-11-28T07:00:00Z",
            endsAtExclusive = "2026-11-29T14:59:00Z",
            options = listOf(
                NoticeVoteOption(optionId = 1L, content = "치킨"),
                NoticeVoteOption(optionId = 2L, content = "피자"),
            ),
        )

        val converted = serverVote.toVoteForm()
        val pickerConfirmed = NoticeVoteForm(
            title = "회식 메뉴",
            options = listOf("치킨", "피자"),
            isAnonymous = true,
            allowMultipleChoice = false,
            startsAt = "2026-11-28T07:00:00.000Z",
            endsAt = "2026-11-29T14:59:00.000Z",
        )

        assertEquals(pickerConfirmed, converted)
        assertEquals(
            "2026.11.28 ･ 오후 4:00",
            formatVoteDateTime(serverVote.startsAt, zoneId = "Asia/Seoul"),
        )
        assertEquals(
            "2026.11.29 ･ 오후 11:59",
            formatVoteDateTime(converted.endsAt, zoneId = "Asia/Seoul"),
        )
        assertNull(formatVoteDateTime(null, zoneId = "Asia/Seoul"))
    }
}
