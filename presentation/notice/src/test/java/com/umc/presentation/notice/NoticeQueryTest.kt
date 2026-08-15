package com.umc.presentation.notice

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 필터 칩 조합이 서버 쿼리로 어떻게 바뀌는지 고정한다.
 *
 * 핵심 불변식: 소속(chapterId·schoolId) / 파트 / 운영진 탭 중 **최대 하나만** 전송된다.
 * 여럿이 함께 나가면 조건이 AND로 누적돼 목록이 비고, 운영진 공지에서는
 * schoolId가 중앙/교내 운영진 공지를 가르는 기준이라 의미까지 어긋난다
 */
class NoticeQueryTest {

    /** 운영진 탭 계산은 role 조회라 여기서는 고정값으로 주입한다 */
    private val staffTab = "CENTRAL_MEMBER"

    private fun stateWith(
        orgChip: NoticeChipState? = null,
        subChip: NoticeSubChip = NoticeSubChip.ALL,
        part: UserPart? = null,
    ) = NoticeUiState(
        selectedGisu = 12L,
        orgChipList = listOfNotNull(
            NoticeChipState(text = AppStrings.ALL, isClicked = orgChip == null),
            orgChip,
        ),
        selectedOrgChipText = orgChip?.text ?: AppStrings.ALL,
        selectedSubChip = subChip,
        selectedPart = part,
    )

    private fun queryOf(state: NoticeUiState) = buildNoticeQuery(state, staffTab)

    private val chapterChip =
        NoticeChipState(text = "Ain 지부", chapterId = 3L, isClicked = true)
    private val schoolChip =
        NoticeChipState(text = "가천대학교", schoolId = 5L, isClicked = true)

    @Test
    fun `아무것도 안 고르면 기수와 챌린저 탭만 나간다`() {
        val query = queryOf(stateWith())

        assertEquals("CHALLENGER", query.noticeTab)
        assertNull(query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `지부 칩은 chapterId만 보낸다`() {
        val query = queryOf(stateWith(orgChip = chapterChip))

        assertEquals("CHALLENGER", query.noticeTab)
        assertEquals(3L, query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `학교 칩은 schoolId만 보낸다`() {
        val query = queryOf(stateWith(orgChip = schoolChip))

        assertEquals("CHALLENGER", query.noticeTab)
        assertNull(query.chapterId)
        assertEquals(5L, query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `파트를 고르면 소속은 함께 나가지 않는다`() {
        // 1차에 학교가 남아 있어도 파트만 전송되어야 한다
        val query = queryOf(
            stateWith(orgChip = schoolChip, subChip = NoticeSubChip.PART, part = UserPart.ANDROID)
        )

        assertEquals("CHALLENGER", query.noticeTab)
        assertEquals("ANDROID", query.part)
        assertNull(query.chapterId)
        assertNull(query.schoolId)
    }

    @Test
    fun `운영진 공지는 소속과 파트를 모두 버린다`() {
        // schoolId가 함께 나가면 중앙 운영진 공지가 교내 공지로 해석돼 의미가 어긋난다
        val query = queryOf(
            stateWith(orgChip = schoolChip, subChip = NoticeSubChip.STAFF)
        )

        assertNull(query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `운영진 공지는 챌린저 탭이 아니다`() {
        val query = queryOf(stateWith(subChip = NoticeSubChip.STAFF))

        assertEquals("CENTRAL_MEMBER", query.noticeTab)
    }

    @Test
    fun `모든 조합에서 필터 축은 하나를 넘지 않는다`() {
        val orgChips = listOf(null, chapterChip, schoolChip)
        val subChips = NoticeSubChip.entries

        orgChips.forEach { org ->
            subChips.forEach { sub ->
                val part = if (sub == NoticeSubChip.PART) UserPart.ANDROID else null
                val query = queryOf(stateWith(org, sub, part))

                val axes = listOfNotNull(
                    query.chapterId,
                    query.schoolId,
                    query.part,
                    query.noticeTab.takeIf { it != "CHALLENGER" },
                )
                assertTrue(
                    "org=${org?.text} sub=$sub 에서 축이 여러 개 전송됨: $axes",
                    axes.size <= 1,
                )
            }
        }
    }

    @Test
    fun `2차 선택이 남아 있으면 1차가 전체여도 2차 행은 계속 보인다`() {
        // 파트 선택 시 1차가 전체로 돌아가므로, 이 조건이 없으면 되돌릴 방법이 사라진다
        val state = stateWith(subChip = NoticeSubChip.PART, part = UserPart.ANDROID)

        assertTrue(state.isSubChipVisible)
    }

    @Test
    fun `아무 필터도 없으면 2차 행은 숨는다`() {
        assertFalse(stateWith().isSubChipVisible)
    }
}
