package com.umc.presentation.notice

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 필터 칩이 서버 쿼리로 어떻게 바뀌는지 고정한다.
 *
 * 칩은 한 줄에 평면으로 놓이고 항상 하나만 선택되며, 칩 하나가 파라미터 하나에 대응한다.
 * 여러 축이 함께 나가면 조건이 AND로 누적돼 목록이 비고, 중앙운영사무국 공지에서는
 * schoolId가 중앙/교내 운영진 공지를 가르는 기준이라 의미까지 어긋난다
 */
class NoticeQueryTest {

    /** 중앙운영사무국 칩이 요청하는 탭 */
    private val staffTab = "CENTRAL_MEMBER"

    private val chapterChip = NoticeChipState(text = "Ain 지부", chapterId = 3L)
    private val schoolChip = NoticeChipState(text = "가천대학교", schoolId = 5L)
    private val centralChip =
        NoticeChipState(text = AppStrings.NOTICE_CENTRAL_CHIP, isStaffNoticeChip = true)
    private val partChip = NoticeChipState(
        text = UserPart.ANDROID.label,
        part = UserPart.ANDROID.name,
        hanBottomSheet = true,
    )

    /** [selected]만 선택된 칩 목록을 가진 상태 */
    private fun stateWith(selected: NoticeChipState? = null) = NoticeUiState(
        selectedGisu = 12L,
        chipList = listOf(
            NoticeChipState(text = AppStrings.ALL, isClicked = selected == null),
            centralChip.copy(isClicked = selected?.text == centralChip.text),
            chapterChip.copy(isClicked = selected?.text == chapterChip.text),
            schoolChip.copy(isClicked = selected?.text == schoolChip.text),
            partChip.copy(isClicked = selected?.text == partChip.text),
        ),
        selectedChipText = selected?.text ?: AppStrings.ALL,
    )

    private fun queryOf(selected: NoticeChipState? = null) =
        buildNoticeQuery(stateWith(selected), staffTab)

    @Test
    fun `전체는 기수와 챌린저 탭만 보낸다`() {
        val query = queryOf()

        assertEquals("CHALLENGER", query.noticeTab)
        assertNull(query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `지부 칩은 chapterId만 보낸다`() {
        val query = queryOf(chapterChip)

        assertEquals("CHALLENGER", query.noticeTab)
        assertEquals(3L, query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `학교 칩은 schoolId만 보낸다`() {
        val query = queryOf(schoolChip)

        assertEquals("CHALLENGER", query.noticeTab)
        assertEquals(5L, query.schoolId)
        assertNull(query.chapterId)
        assertNull(query.part)
    }

    @Test
    fun `파트 칩은 part만 보낸다`() {
        val query = queryOf(partChip)

        assertEquals("CHALLENGER", query.noticeTab)
        assertEquals("ANDROID", query.part)
        assertNull(query.chapterId)
        assertNull(query.schoolId)
    }

    @Test
    fun `중앙운영사무국 칩은 탭만 바꾸고 소속과 파트를 보내지 않는다`() {
        // schoolId가 함께 나가면 교내 운영진 공지로 분류돼 중앙 공지를 볼 수 없다
        val query = queryOf(centralChip)

        assertEquals(staffTab, query.noticeTab)
        assertNull(query.chapterId)
        assertNull(query.schoolId)
        assertNull(query.part)
    }

    @Test
    fun `어떤 칩을 골라도 필터 축은 하나를 넘지 않는다`() {
        val chips = listOf(null, centralChip, chapterChip, schoolChip, partChip)

        chips.forEach { chip ->
            val query = queryOf(chip)
            val axes = listOfNotNull(
                query.chapterId,
                query.schoolId,
                query.part,
                query.noticeTab.takeIf { it != "CHALLENGER" },
            )

            assertTrue("${chip?.text ?: "전체"} 에서 축이 여러 개 전송됨: $axes", axes.size <= 1)
        }
    }

    @Test
    fun `운영진 공지가 아니면 탭은 항상 CHALLENGER 고정이다`() {
        listOf(null, chapterChip, schoolChip, partChip).forEach { chip ->
            assertEquals("CHALLENGER", queryOf(chip).noticeTab)
        }
    }
}
