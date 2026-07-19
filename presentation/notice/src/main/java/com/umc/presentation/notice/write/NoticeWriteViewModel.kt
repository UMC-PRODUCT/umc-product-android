package com.umc.presentation.notice.write

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.organization.GetChapterListUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 공지 작성자 권한. 권한에 따라 선택 가능한 카테고리와 게시판 분류가 달라짐 */
enum class NoticeWriterRole(val accessRoles: Set<UserChallengerRole>) {
    SUPER_ADMIN(setOf(UserChallengerRole.SUPER_ADMIN)),
    CENTRAL_STAFF(
        setOf(
            UserChallengerRole.CENTRAL_PRESIDENT,
            UserChallengerRole.CENTRAL_VICE_PRESIDENT,
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
        )
    ),
    CHAPTER_PRESIDENT(setOf(UserChallengerRole.CHAPTER_PRESIDENT)),
    SCHOOL_CORE(
        setOf(
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
            UserChallengerRole.SCHOOL_ETC_ADMIN,
        )
    ),
    SCHOOL_PART_LEADER(setOf(UserChallengerRole.SCHOOL_PART_LEADER));

    companion object {
        /** 사용자 role 중 가장 높은 작성 권한 반환. 작성 권한이 없으면 null */
        fun from(roles: List<UserChallengerRole>): NoticeWriterRole? {
            return entries.firstOrNull { writer -> roles.any { it in writer.accessRoles } }
        }
    }
}

/** 공지 카테고리 종류 */
enum class WriteCategoryType {
    ALL_GISU,           // 전체 기수 (최고 관리자 전용)
    GISU,               // 특정 기수 (최고 관리자 전용)
    CENTRAL_STAFF,      // 중앙운영진
    CHAPTER_PRESIDENT,  // 지부장
    SCHOOL_CORE,        // 학교 회장단
    SCHOOL_PART_LEADER, // 학교 파트장
}

data class WriteCategory(
    val type: WriteCategoryType,
    val label: String,
)

/** 게시판 분류 칩 종류. CHAPTER/SCHOOL/PART는 바텀시트 드롭다운 칩 */
enum class BoardChipType {
    ALL, STAFF, CHAPTER, SCHOOL, PART
}

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {

    init {
        loadWriterRole()
        loadChapterList()
        loadSchoolList()
    }

    /** 작성자 권한을 계산하고 권한별 카테고리 목록 구성. 최고 관리자 외에는 카테고리 자동 선택 */
    private fun loadWriterRole() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            val roles = userInfo.roles.map { UserChallengerRole.from(it.roleType) }
            val writerRole = NoticeWriterRole.from(roles) ?: return@collect

            // 최고 관리자의 기수 카테고리 라벨용 현재(최신) 기수
            val currentGeneration = userInfo.challengerRecords
                .maxByOrNull { it.gisu }
                ?.gisu

            val categories = createCategories(writerRole, currentGeneration)

            updateState {
                copy(
                    writerRole = writerRole,
                    availableCategories = categories,
                )
            }

            // 최고 관리자가 아니면 자기 권한 카테고리가 자동 선택됨
            if (writerRole != NoticeWriterRole.SUPER_ADMIN) {
                categories.firstOrNull()?.let { onSelectCategory(it) }
            }
        }
    }

    /** 작성자 권한별 선택 가능한 공지 카테고리 목록 */
    private fun createCategories(
        writerRole: NoticeWriterRole,
        currentGeneration: Long?,
    ): List<WriteCategory> {
        return when (writerRole) {
            NoticeWriterRole.SUPER_ADMIN -> buildList {
                add(WriteCategory(WriteCategoryType.ALL_GISU, AppStrings.NOTICE_WRITE_ALL_GISU))
                currentGeneration?.let {
                    add(WriteCategory(WriteCategoryType.GISU, "${it}기"))
                }
                add(WriteCategory(WriteCategoryType.CENTRAL_STAFF, AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL))
                add(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))
                add(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
            }

            NoticeWriterRole.CENTRAL_STAFF ->
                listOf(WriteCategory(WriteCategoryType.CENTRAL_STAFF, AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL))

            NoticeWriterRole.CHAPTER_PRESIDENT ->
                listOf(WriteCategory(WriteCategoryType.CHAPTER_PRESIDENT, AppStrings.NOTICE_WRITE_CATEGORY_CHAPTER))

            NoticeWriterRole.SCHOOL_CORE ->
                listOf(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))

            NoticeWriterRole.SCHOOL_PART_LEADER ->
                listOf(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
        }
    }

    /**
     * (작성자 권한, 카테고리) 조합별 게시판 분류 칩 구성.
     * 최고 관리자는 카테고리에 따라, 그 외 권한은 자기 권한에 따라 분류가 달라짐
     */
    private fun createBoardChips(
        writerRole: NoticeWriterRole,
        categoryType: WriteCategoryType,
    ): Pair<List<BoardChipType>, String?> {
        return if (writerRole == NoticeWriterRole.SUPER_ADMIN) {
            when (categoryType) {
                // 기수 카테고리: 지부/학교/파트 (지부·학교 동시 선택 불가)
                WriteCategoryType.GISU -> listOf(
                    BoardChipType.CHAPTER, BoardChipType.SCHOOL, BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_GISU_CLASS_HINT

                WriteCategoryType.SCHOOL_PART_LEADER -> listOf(BoardChipType.PART) to null

                else -> emptyList<BoardChipType>() to null
            }
        } else {
            when (writerRole) {
                NoticeWriterRole.CENTRAL_STAFF -> listOf(
                    BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART, BoardChipType.CHAPTER
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.CHAPTER_PRESIDENT -> listOf(
                    BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART, BoardChipType.SCHOOL
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.SCHOOL_CORE -> listOf(
                    BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.SCHOOL_PART_LEADER -> listOf(
                    BoardChipType.SCHOOL, BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.SUPER_ADMIN -> emptyList<BoardChipType>() to null
            }
        }
    }

    private fun loadChapterList() = viewModelScope.launch {
        resultResponse(
            response = getChapterListUseCase(),
            successCallback = { updateState { copy(chapterList = it) } }
        )
    }

    private fun loadSchoolList() = viewModelScope.launch {
        resultResponse(
            response = getAllSchoolUseCase(),
            successCallback = { updateState { copy(schoolList = it) } }
        )
    }

    /** 카테고리 선택. 게시판 분류 구성을 갱신하고 기존 선택은 초기화 */
    fun onSelectCategory(category: WriteCategory) {
        val writerRole = uiState.value.writerRole ?: return
        val (chips, hint) = createBoardChips(writerRole, category.type)

        updateState {
            copy(
                selectedCategory = category,
                boardChips = chips,
                boardHint = hint,
                isAllSelected = false,
                isStaffSelected = false,
                selectedChapter = null,
                selectedSchool = null,
                selectedPart = null,
            )
        }
    }

    fun onToggleAll() {
        updateState { copy(isAllSelected = !isAllSelected) }
    }

    fun onToggleStaff() {
        updateState { copy(isStaffSelected = !isStaffSelected) }
    }

    /** 지부 선택. 기수 카테고리에서는 학교와 동시 선택 불가 */
    fun onSelectChapter(chapter: Chapter) {
        val isExclusive = uiState.value.selectedCategory?.type == WriteCategoryType.GISU
        updateState {
            copy(
                selectedChapter = chapter,
                selectedSchool = if (isExclusive) null else selectedSchool,
            )
        }
    }

    /** 학교 선택. 기수 카테고리에서는 지부와 동시 선택 불가 */
    fun onSelectSchool(school: SchoolInfo) {
        val isExclusive = uiState.value.selectedCategory?.type == WriteCategoryType.GISU
        updateState {
            copy(
                selectedSchool = school,
                selectedChapter = if (isExclusive) null else selectedChapter,
            )
        }
    }

    fun onSelectPart(part: UserPart) {
        updateState { copy(selectedPart = part) }
    }

    fun onTitleChanged(title: String) {
        updateState { copy(title = title) }
    }

    fun onContentChanged(content: String) {
        updateState { copy(content = content) }
    }

    /** 알림 발송 여부 토글 (종 아이콘) */
    fun onToggleNotification() {
        updateState { copy(sendNotification = !uiState.value.sendNotification) }
    }

    fun onClickRegister() {
        // TODO: 공지 등록 API 연결 (카테고리/게시판 분류 -> 수신 대상 매핑)
    }
}

data class NoticeWriteUiState(
    val writerRole: NoticeWriterRole? = null,
    val availableCategories: List<WriteCategory> = emptyList(),
    val selectedCategory: WriteCategory? = null,
    val boardChips: List<BoardChipType> = emptyList(),
    val boardHint: String? = null,
    val isAllSelected: Boolean = false,
    val isStaffSelected: Boolean = false,
    val selectedChapter: Chapter? = null,
    val selectedSchool: SchoolInfo? = null,
    val selectedPart: UserPart? = null,
    val chapterList: List<Chapter> = emptyList(),
    val schoolList: List<SchoolInfo> = emptyList(),
    val title: String = "",
    val content: String = "",
    val sendNotification: Boolean = true,
) : UiState {
    // 게시판 분류가 있는 카테고리에서 하나라도 선택됐는지
    val hasBoardSelection: Boolean
        get() = isAllSelected || isStaffSelected
                || selectedChapter != null || selectedSchool != null || selectedPart != null

    val enableRegister: Boolean
        get() = selectedCategory != null
                && (boardChips.isEmpty() || hasBoardSelection)
                && title.isNotBlank()
                && content.isNotBlank()
}

sealed interface NoticeWriteEvent : UiEvent {
    // TODO: 공지 등록 API 연결 후 완료/에러 이벤트 정의
}
