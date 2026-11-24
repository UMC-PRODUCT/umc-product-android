package com.umc.presentation.notice.write

import androidx.compose.ui.text.input.TextFieldValue
import com.umc.component.theme.AppStrings
import com.umc.domain.model.CurrentGisuMemberInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteTransitionTest {
    private val active = GisuItem(12, 12, true)
    private val past = GisuItem(9, 9)
    private val gisu = WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU)
    private val core = WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE)
    private val school = SchoolInfo(7, "학교")
    private val chapter = Chapter(5, "지부")
    private val state = NoticeWriteUiState(
        writerRole = NoticeWriterRole.SUPER_ADMIN,
        availableGisus = persistentListOf(active, past), authorityGisuId = 12, targetGisuId = 12,
        availableCategories = persistentListOf(gisu, core), selectedCategory = gisu,
        boardChips = persistentListOf(BoardChipType.SCHOOL), boardHint = "기존 힌트",
        isAllSelected = true, isStaffSelected = true, selectedPart = UserPart.PLAN,
        selectedSchool = school, selectedChapter = chapter,
        schoolList = persistentListOf(school), chapterList = persistentListOf(chapter),
        title = "작성 중인 제목", content = TextFieldValue("작성 중인 본문"), sendNotification = false,
    )

    private fun profile(
        user: UserInfo = UserInfo(isSuperAdmin = true),
        selectedGisu: Boolean = true,
        selectedCategory: Boolean = true,
        server: GisuItem? = active,
    ): (NoticeWriteUiState) -> NoticeWriteTargetTransition = { before ->
        applyProfileTarget(before, user, server, selectedGisu, selectedCategory)
    }

    private fun choices(value: NoticeWriteUiState): List<Any?> = listOf(
        value.isAllSelected, value.isStaffSelected, value.selectedPart, value.selectedChapter, value.selectedSchool,
    )

    private fun assertReset(value: NoticeWriteUiState) {
        assertEquals(listOf(false, false, null, null, null), choices(value))
    }

    private fun assertUnrelated(before: NoticeWriteUiState, after: NoticeWriteUiState) {
        assertEquals(before.title, after.title)
        assertEquals(before.content, after.content)
        assertEquals(before.sendNotification, after.sendNotification)
        assertEquals(before.chapterList, after.chapterList)
        assertEquals(before.schoolList, after.schoolList)
        assertEquals(before.isEditMode, after.isEditMode)
        assertEquals(before.createdNoticeId, after.createdNoticeId)
    }

    @Test
    fun `프로필과 목록 응답은 C T 조합에 따라 구성과 선택값을 각각 보존한다`() {
        listOf(false, true).forEach { categoryChanged ->
            listOf(false, true).forEach { targetChanged ->
                val before = state.copy(
                    selectedCategory = if (categoryChanged) null else gisu,
                    targetGisuId = if (targetChanged) 8 else 12,
                )
                listOf(
                    profile(),
                    { value: NoticeWriteUiState -> applyLoadedGisus(value, listOf(active, past), 12L) },
                ).forEach { transition ->
                    val result = transition(before)
                    val after = result.state
                    assertTrue(result.accepted)
                    assertFalse(result.resetUserSelection)
                    assertEquals(12, after.targetGisuId)
                    assertEquals(if (categoryChanged) WriteCategoryType.ALL_GISU else WriteCategoryType.GISU,
                        after.selectedCategory?.type)
                    if (categoryChanged) {
                        assertEquals(emptyList<BoardChipType>(), after.boardChips)
                        assertNull(after.boardHint)
                    } else {
                        assertEquals(before.boardChips, after.boardChips)
                        assertEquals(before.boardHint, after.boardHint)
                    }
                    if (categoryChanged || targetChanged) assertReset(after)
                    else assertEquals(choices(before), choices(after))
                    assertUnrelated(before, after)
                }
            }
        }
    }

    @Test
    fun `프로필 역할 변경은 카테고리 타입이 같아도 칩 구성과 직접 선택을 초기화한다`() {
        val before = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE,
            selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, AppStrings.NOTICE_WRITE_ALL_GISU))
        val result = profile()(before)
        assertEquals(WriteCategoryType.ALL_GISU, result.state.selectedCategory?.type)
        assertEquals(NoticeWriterRole.SUPER_ADMIN, result.state.writerRole)
        assertEquals(emptyList<BoardChipType>(), result.state.boardChips)
        assertNull(result.state.boardHint)
        assertReset(result.state)
        assertTrue(result.resetUserSelection)
    }

    @Test
    fun `겸임 여부만 바뀌어도 프로필은 같은 카테고리의 칩 구성을 갱신한다`() {
        listOf(false, true).forEach { previousDual ->
            val roles = listOf(role(UserChallengerRole.CHAPTER_PRESIDENT)) +
                if (!previousDual) listOf(role(UserChallengerRole.SCHOOL_PRESIDENT)) else emptyList()
            val before = state.copy(writerRole = NoticeWriterRole.CHAPTER_PRESIDENT,
                canWriteAsSchoolCore = previousDual)
            val result = profile(UserInfo(roles = roles))(before)
            assertEquals(!previousDual, result.state.canWriteAsSchoolCore)
            assertEquals(WriteCategoryType.GISU, result.state.selectedCategory?.type)
            assertEquals(listOf(BoardChipType.ALL, BoardChipType.PART), result.state.boardChips)
            assertReset(result.state)
            assertFalse(result.resetUserSelection)
        }
    }

    @Test
    fun `기수 선택은 같은 기수라도 선택값을 초기화하고 타입이 같으면 구성을 유지한다`() {
        listOf(active, past).forEach { selected ->
            val before = state.copy(selectedCategory = gisu.copy(label = "이전 라벨"))
            val result = selectTargetGisu(before, selected)
            assertTrue(result.accepted)
            assertEquals(selected.gisuId, result.state.targetGisuId)
            assertEquals(gisu, result.state.selectedCategory)
            assertEquals(before.boardChips, result.state.boardChips)
            assertEquals(before.boardHint, result.state.boardHint)
            assertReset(result.state)
            assertUnrelated(before, result.state)
        }
    }

    @Test
    fun `기수 선택으로 유지할 카테고리가 없어지면 첫 카테고리와 칩을 적용한다`() {
        val before = state.copy(writerRole = NoticeWriterRole.CHAPTER_PRESIDENT,
            canWriteAsSchoolCore = false, selectedCategory = core)
        val after = selectTargetGisu(before, active).state
        assertEquals(gisu, after.selectedCategory)
        assertEquals(listOf(BoardChipType.ALL, BoardChipType.PART), after.boardChips)
        assertNull(after.boardHint)
        assertReset(after)
    }

    @Test
    fun `카테고리 재선택은 구성과 선택값을 항상 갱신하고 기존 목록을 유지한다`() {
        val before = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE, targetGisuId = 9,
            selectedCategory = core)
        val result = selectTargetCategory(before, core)
        assertTrue(result.accepted)
        assertEquals(12, result.state.targetGisuId)
        assertEquals(core, result.state.selectedCategory)
        assertEquals(before.availableCategories, result.state.availableCategories)
        assertEquals(emptyList<BoardChipType>(), result.state.boardChips)
        assertNull(result.state.boardHint)
        assertReset(result.state)
        val challenger = selectTargetCategory(before, gisu).state
        assertEquals(9, challenger.targetGisuId)
        assertEquals(listOf(BoardChipType.ALL, BoardChipType.PART), challenger.boardChips)
    }

    @Test
    fun `잠금과 역할 부재 및 허용 목록 밖 사용자 입력은 원래 상태를 그대로 반환한다`() {
        val transitions = listOf(
            { value: NoticeWriteUiState -> selectTargetGisu(value, active) },
            { value: NoticeWriteUiState -> selectTargetCategory(value, gisu) },
        )
        listOf(state.copy(isEditMode = true), state.copy(createdNoticeId = 42), state.copy(writerRole = null))
            .forEach { before ->
                transitions.forEach { transition -> assertIgnored(before, transition) }
            }
        assertIgnored(state, { value: NoticeWriteUiState -> selectTargetGisu(value, GisuItem(8, 8)) })
        assertIgnored(state, { value: NoticeWriteUiState -> selectTargetGisu(value, active.copy(generation = 99)) })
        assertIgnored(state, { value: NoticeWriteUiState -> selectTargetCategory(value, gisu.copy(label = "목록 밖 라벨")) })
        assertIgnored(state.copy(writerRole = NoticeWriterRole.SCHOOL_CORE), { value: NoticeWriteUiState -> selectTargetGisu(value, past) })
    }

    @Test
    fun `자동 응답 경로에는 사용자 선택용 잠금 가드를 새로 적용하지 않는다`() {
        val before = state.copy(isEditMode = true, createdNoticeId = 42, targetGisuId = 8)
        listOf(
            profile(),
            { value: NoticeWriteUiState -> applyLoadedGisus(value, listOf(active), 12) },
        ).forEach { transition ->
            val result = transition(before)
            assertTrue(result.accepted)
            assertEquals(12, result.state.targetGisuId)
            assertReset(result.state)
            assertUnrelated(before, result.state)
        }
    }

    @Test
    fun `늦은 첫 활성 기수 응답은 직접 선택한 축만 유지한다`() {
        listOf(false, true).forEach { selectedGisu ->
            listOf(false, true).forEach { selectedCategory ->
                val before = state.copy(authorityGisuId = null, targetGisuId = 9, selectedCategory = core)
                val result = profile(
                    selectedGisu = selectedGisu, selectedCategory = selectedCategory,
                )(before)
                assertEquals(if (selectedGisu) 9 else 12, result.state.targetGisuId)
                assertEquals(if (selectedCategory) WriteCategoryType.SCHOOL_CORE else WriteCategoryType.ALL_GISU,
                    result.state.selectedCategory?.type)
                assertFalse(result.resetUserSelection)
            }
        }
    }

    @Test
    fun `확정된 권한 기수가 바뀌면 두 직접 선택 플래그 초기화를 요청한다`() {
        val before = state.copy(authorityGisuId = 9, targetGisuId = 9, selectedCategory = core)
        val result = profile()(before)
        assertTrue(result.resetUserSelection)
        assertEquals(12, result.state.targetGisuId)
        assertEquals(WriteCategoryType.ALL_GISU, result.state.selectedCategory?.type)
        assertReset(result.state)
    }

    @Test
    fun `프로필 권한 상실은 기존 네 필드만 비우고 나머지 선택과 메타데이터는 보존한다`() {
        val result = profile(UserInfo())(state)
        assertTrue(result.accepted)
        assertFalse(result.resetUserSelection)
        assertEquals(state.copy(writerRole = null, availableCategories = persistentListOf(),
            selectedCategory = null, boardChips = persistentListOf()), result.state)
    }

    @Test
    fun `기수 목록은 현재 대상과 선호 대상 및 첫 허용 대상 순서로 선택한다`() {
        listOf(9 to 9L, 8 to 9L, 8 to 8L).zip(listOf(9, 9, 12)).forEach { (input, expected) ->
            val result = applyLoadedGisus(state.copy(targetGisuId = input.first), listOf(active, past), input.second)
            assertEquals(expected, result.state.targetGisuId)
        }
        val staff = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE, selectedCategory = core, targetGisuId = 9)
        val after = applyLoadedGisus(staff, listOf(past, active), 9).state
        assertEquals(12, after.targetGisuId)
        assertEquals(staff.boardChips, after.boardChips)
        assertReset(after)
    }

    @Test
    fun `기수 응답을 먼저 합치고 필터 중복 제거 정렬하는 순서를 유지한다`() {
        val before = state.copy(availableGisus = persistentListOf(active.copy(generation = 99), past))
        val after = applyLoadedGisus(
            before, listOf(past, GisuItem(-1, 30), GisuItem(30, 0), active), null,
        ).state
        assertEquals(listOf(active, past), after.availableGisus)
        val profiled = profile()(before).state
        assertEquals(listOf(active, past), profiled.availableGisus)
    }

    @Test
    fun `역할 없는 목록 응답은 타입이 그대로 null이면 기존 보드 구성을 유지한다`() {
        val before = state.copy(writerRole = null, selectedCategory = null)
        val after = applyLoadedGisus(before, listOf(active), 12).state
        assertNull(after.targetGisuId)
        assertTrue(after.availableCategories.isEmpty())
        assertEquals(before.boardChips, after.boardChips)
        assertEquals(before.boardHint, after.boardHint)
        assertReset(after)
    }

    @Test
    fun `프로필 현재 기수 폴백과 서버 활성 기수 우선순위를 유지한다`() {
        val user = UserInfo(isSuperAdmin = true,
            currentGisuMemberInfo = CurrentGisuMemberInfo(9, 9, null, true, emptyList()))
        assertEquals(9, profile(user, server = null)(state).state.authorityGisuId)
        assertEquals(12, profile(user)(state).state.authorityGisuId)
        val unknown = profile(
            UserInfo(isSuperAdmin = true), server = null,
        )(state.copy(authorityGisuId = null)).state
        assertNull(unknown.authorityGisuId)
        assertEquals(12, unknown.targetGisuId)
    }

    @Test
    fun `전이 결과는 기존 발행 요청과 수신 요약에 그대로 연결된다`() {
        val before = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE,
            availableCategories = persistentListOf(core), selectedCategory = core, targetGisuId = 9)
        val after = selectTargetCategory(before, core).state
        val request = buildNoticeTargetRequest(after)
        assertEquals(12, request?.targetGisuId)
        assertEquals("SCHOOL_CORE", request?.targetNoticeTab)
        assertNull(request?.targetSchoolId)
        assertEquals(emptyList<String>(), request?.targetParts)
        assertEquals("12기 · UMC 전체 · 학교 회장단 대상", after.categoryHint)

        val local = state.copy(writerRole = NoticeWriterRole.SCHOOL_CORE, writerSchoolId = 7,
            writerSchoolName = "학교", availableCategories = persistentListOf(core))
        val selected = selectTargetCategory(local, core).state
        val staff = toggleNoticeStaffSelection(selected)
        assertEquals("SCHOOL_PART_LEADER", buildNoticeTargetRequest(staff)?.targetNoticeTab)
        assertEquals(7, buildNoticeTargetRequest(staff)?.targetSchoolId)
        assertEquals("12기 · 학교 · 학교 운영진 대상", staff.categoryHint)
        assertNull(staff.displayBoardHint)
    }

    private fun assertIgnored(
        before: NoticeWriteUiState,
        transition: (NoticeWriteUiState) -> NoticeWriteTargetTransition,
    ) {
        val result = transition(before)
        assertFalse(result.accepted)
        assertFalse(result.resetUserSelection)
        assertSame(before, result.state)
    }

    private fun role(type: UserChallengerRole) = UserRole(
        id = 1, challengerId = 1, roleType = type.name, chapterId = 5, chapterName = "지부",
        organizationType = "SCHOOL", organizationId = 7, responsiblePart = null, gisuId = 12, gisu = 12,
    )
}
