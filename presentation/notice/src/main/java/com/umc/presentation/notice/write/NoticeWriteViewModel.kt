package com.umc.presentation.notice.write

import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.NoticeImageAttachment
import com.umc.domain.model.notice.NoticeVoteForm
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.notice.noticeRolesForGisu
import com.umc.domain.model.notice.noticeWriterRole
import com.umc.domain.model.notice.noticeWriterSchoolId
import com.umc.domain.model.UserInfo
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.usecase.ai.CheckAiFeatureStatusUseCase
import com.umc.domain.usecase.ai.RefineNoticeMarkdownUseCase
import com.umc.domain.usecase.ai.SummarizeNoticeMarkdownUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.AddNoticeImagesUseCase
import com.umc.domain.usecase.notice.AddNoticeLinksUseCase
import com.umc.domain.usecase.notice.AddNoticeVoteUseCase
import com.umc.domain.usecase.notice.CreateNoticeUseCase
import com.umc.domain.usecase.notice.DeleteNoticeVoteUseCase
import com.umc.domain.usecase.notice.UpdateNoticeImagesUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.UpdateNoticeUseCase
import com.umc.domain.usecase.organization.GetChapterWithSchoolUseCase
import com.umc.domain.usecase.organization.GetSearchGisusUseCase
import com.umc.domain.usecase.organization.GetActiveGisuUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getChapterWithSchoolUseCase: GetChapterWithSchoolUseCase,
    private val getSearchGisusUseCase: GetSearchGisusUseCase,
    private val getActiveGisuUseCase: GetActiveGisuUseCase,
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val updateNoticeUseCase: UpdateNoticeUseCase,
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val addNoticeImagesUseCase: AddNoticeImagesUseCase,
    private val addNoticeLinksUseCase: AddNoticeLinksUseCase,
    private val addNoticeVoteUseCase: AddNoticeVoteUseCase,
    private val updateNoticeImagesUseCase: UpdateNoticeImagesUseCase,
    private val deleteNoticeVoteUseCase: DeleteNoticeVoteUseCase,
    private val checkAiFeatureStatusUseCase: CheckAiFeatureStatusUseCase,
    private val refineNoticeMarkdownUseCase: RefineNoticeMarkdownUseCase,
    private val summarizeNoticeMarkdownUseCase: SummarizeNoticeMarkdownUseCase,
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    private var isLoadingTargetGisus = false
    private var hasLoadedTargetGisus = false
    private var cachedUserInfo: UserInfo? = null
    private var serverActiveGisu: GisuItem? = null
    private var resolvedAuthorityGisuId: Long? = null
    private var hasSelectedTargetGisu = false
    private var hasSelectedCategory = false
    private var loadingOrganizationGisuId: Int? = null
    private var loadedOrganizationGisuId: Int? = null
    private var organizationRequestToken = 0

    companion object {
        const val MAX_IMAGE_COUNT = 10
        const val MAX_VOTE_OPTION_COUNT = 5
    }

    init {
        loadWriterRole()
        loadActiveGisu()
        checkAiAvailability()
    }

    /** 온디바이스 AI(Prompt API) 지원 기기에서만 본문 롱클릭 AI 메뉴를 노출 */
    private fun checkAiAvailability() = viewModelScope.launch {
        val status = checkAiFeatureStatusUseCase()
        updateState { copy(isAiRefineEnabled = status.isUsable) }
    }

    /** 수정 모드 진입. 기존 공지 내용(제목/본문/링크/이미지)을 채워 넣는다 */
    fun initEditMode(noticeId: Long) = viewModelScope.launch {
        if (noticeId <= 0L || uiState.value.editNoticeId == noticeId) return@launch
        updateState { copy(isEditMode = true, editNoticeId = noticeId) }

        resultResponse(
            response = getNoticeDetailUseCase(noticeId),
            successCallback = { detail ->
                val serverVote = detail.vote?.let { vote ->
                    NoticeVoteForm(
                        title = vote.title,
                        options = vote.options.map { it.content },
                        isAnonymous = vote.isAnonymous,
                        allowMultipleChoice = vote.allowMultipleChoice,
                        startsAt = vote.startsAt.takeIf { it.isNotBlank() },
                        endsAt = vote.endsAtExclusive.takeIf { it.isNotBlank() },
                    )
                }
                val linkText = detail.links
                    .sortedBy { it.displayOrder }
                    .joinToString(", ") { it.url }
                updateState {
                    copy(
                        title = detail.title,
                        content = TextFieldValue(detail.content),
                        isLinkVisible = linkText.isNotBlank(),
                        linkText = linkText,
                        // 이미지 교체(PATCH)에는 이미지 행 id가 아니라 서버가 준 fileId를 보내야 한다.
                        // fileId가 없는 항목은 폼에 채울 수 없어 제외하고, 이 경우 사용자가 이미지를 직접 바꾸기 전에는
                        // 이미지 PATCH(전체 교체)를 보내지 않아 제외된 이미지가 저장 때 지워지지 않게 한다
                        hasUnrestoredImages = detail.images.any { it.fileId.isBlank() },
                        images = detail.images
                            .filter { it.fileId.isNotBlank() }
                            .sortedBy { it.displayOrder }
                            .map { NoticeImageAttachment(uri = it.url, fileId = it.fileId) }
                            .toImmutableList(),
                        // 투표도 수정 대상이라 폼으로 되돌려 채운다. 서버에 있는 투표와 같은 값을 따로 들고 있어야
                        // 저장 때 건드리지 않은 투표를 지웠다 다시 만들지 않는다
                        vote = serverVote,
                        serverVote = serverVote,
                        // 필독 여부를 들고 있지 않으면 수정 저장 시 고정이 풀린다
                        mustRead = detail.mustRead,
                    )
                }
            },
            errorCallback = {
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            },
        )
    }

    /** 활성 기수의 작성 권한으로 카테고리를 구성한다. 기존 선택이 없거나 유효하지 않으면 첫 항목을 선택한다. */
    private fun loadWriterRole() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            cachedUserInfo = userInfo
            applyUserInfo(userInfo)
        }
    }

    private fun loadActiveGisu() = viewModelScope.launch {
        resultResponse(
            response = getActiveGisuUseCase(),
            successCallback = { gisu ->
                serverActiveGisu = gisu.takeIf { it.gisuId > 0 }
                cachedUserInfo?.let(::applyUserInfo)
            },
        )
    }

    private fun applyUserInfo(userInfo: UserInfo) {
        // 서버 활성 기수를 우선하고, 응답이 없으면 프로필의 현재 기수 정보로 폴백한다.
        val activeGisuId = serverActiveGisu?.gisuId?.toLong() ?: userInfo.currentGisuMemberInfo?.gisuId
        val activeGeneration = serverActiveGisu?.generation?.toLong() ?: userInfo.currentGisuMemberInfo?.generation

        // roles는 전 기수 이력이 평탄화된 목록이다. 대상 기수를 바꾸어도 작성 권한은 활성 기수 역할로 판단한다.
        val writerRole = userInfo.noticeWriterRole(activeGisuId ?: 0L) ?: run {
            updateState {
                copy(
                    writerRole = null,
                    availableCategories = persistentListOf(),
                    selectedCategory = null,
                    boardChips = persistentListOf(),
                )
            }
            loadTargetOrganization(null)
            return
        }

        resolvedAuthorityGisuId = activeGisuId
        val canWriteAsSchoolCore = userInfo.noticeRolesForGisu(activeGisuId ?: 0L).any {
            it == UserChallengerRole.SCHOOL_PRESIDENT || it == UserChallengerRole.SCHOOL_VICE_PRESIDENT
        }
        val authorityGisu = serverActiveGisu ?: activeGisuId?.takeIf { it > 0 }?.let { id ->
            GisuItem(gisuId = id.toInt(), generation = activeGeneration?.toInt() ?: 0, isActive = true)
        }
        val knownGisus = (listOfNotNull(authorityGisu) + uiState.value.availableGisus)
            .filter { it.gisuId > 0 && it.generation > 0 }
            .distinctBy { it.gisuId }
            .sortedByDescending { it.generation }
        val selection = resolveNoticeWriteSelection(
            state = uiState.value,
            writerRole = writerRole,
            authorityGisuId = authorityGisu?.gisuId,
            knownGisus = knownGisus,
            hasSelectedTargetGisu = hasSelectedTargetGisu,
            hasSelectedCategory = hasSelectedCategory,
        )
        if (selection.resetUserSelection) {
            hasSelectedTargetGisu = false
            hasSelectedCategory = false
        }
        val targetGisuId = selection.targetGisuId
        val categories = createCategories(writerRole, knownGisus, targetGisuId, canWriteAsSchoolCore)
        val previousCategory = selection.previousCategory
        val selectedCategory = selectNoticeCategory(categories, previousCategory)
        val categoryChanged = selection.roleChanged ||
            uiState.value.canWriteAsSchoolCore != canWriteAsSchoolCore ||
            uiState.value.selectedCategory?.type != selectedCategory?.type
        val (chips, hint) = selectedCategory?.let {
            createBoardChips(writerRole, it.type, canWriteAsSchoolCore)
        } ?: (emptyList<BoardChipType>() to null)
        val targetChanged = uiState.value.targetGisuId != targetGisuId

        updateState {
            copy(
                writerRole = writerRole,
                canWriteAsSchoolCore = canWriteAsSchoolCore,
                availableCategories = categories.toImmutableList(),
                selectedCategory = selectedCategory,
                boardChips = if (categoryChanged) chips.toImmutableList() else boardChips,
                boardHint = if (categoryChanged) hint else boardHint,
                isAllSelected = if (categoryChanged || targetChanged) false else isAllSelected,
                isStaffSelected = if (categoryChanged || targetChanged) false else isStaffSelected,
                selectedPart = if (categoryChanged || targetChanged) null else selectedPart,
                selectedChapter = if (categoryChanged || targetChanged) null else selectedChapter,
                selectedSchool = if (categoryChanged || targetChanged) null else selectedSchool,
                // 작성 권한은 활성 기수 역할로 계산했으며, 이 값은 공지의 대상 기수만 나타낸다.
                targetGisuId = targetGisuId,
                authorityGisuId = activeGisuId?.toInt(),
                availableGisus = knownGisus.toImmutableList(),
                // 서버는 활성 기수의 학교 역할 소속과 대상 학교를 비교한다.
                writerSchoolId = userInfo.noticeWriterSchoolId(activeGisuId ?: 0L)?.toInt(),
                writerChapterId = userInfo.roles
                    .firstOrNull { it.gisuId == activeGisuId && it.roleType == UserChallengerRole.CHAPTER_PRESIDENT.name }
                    ?.let { it.organizationId ?: it.chapterId }?.toInt(),
            )
        }
        loadTargetOrganization(targetGisuId)

        if (writerRole == NoticeWriterRole.SUPER_ADMIN || writerRole == NoticeWriterRole.CENTRAL_CORE) {
            loadTargetGisus(activeGisuId)
        }
    }

    private fun loadTargetGisus(preferredGisuId: Long?) {
        // 프로필과 활성 기수 응답이 연달아 와도 기수 목록 요청은 한 번만 시작한다.
        if (isLoadingTargetGisus || hasLoadedTargetGisus) return
        isLoadingTargetGisus = true
        viewModelScope.launch {
            resultResponse(
                response = getSearchGisusUseCase(),
                successCallback = { result ->
                    val gisus = (result.gisuList + uiState.value.availableGisus)
                        .filter { it.gisuId > 0 && it.generation > 0 }
                        .distinctBy { it.gisuId }
                        .sortedByDescending { it.generation }
                    val role = uiState.value.writerRole
                    updateState {
                        val allowedGisus = role?.let { allowedNoticeTargetGisus(it, gisus, authorityGisuId) }.orEmpty()
                        val targetId = targetGisuId?.takeIf { id -> allowedGisus.any { it.gisuId == id } }
                            ?: preferredGisuId?.toInt()?.takeIf { id -> allowedGisus.any { it.gisuId == id } }
                            ?: allowedGisus.firstOrNull()?.gisuId
                        val categories = role?.let {
                            createCategories(it, gisus, targetId, canWriteAsSchoolCore)
                        }.orEmpty()
                        val previous = selectedCategory
                        val retainedCategory = selectNoticeCategory(categories, previous)
                        val categoryChanged = previous?.type != retainedCategory?.type
                        val (chips, hint) = if (role != null && retainedCategory != null) {
                            createBoardChips(role, retainedCategory.type, canWriteAsSchoolCore)
                        } else emptyList<BoardChipType>() to null
                        val targetChanged = targetGisuId != targetId
                        copy(
                            availableGisus = gisus.toImmutableList(),
                            availableCategories = categories.toImmutableList(),
                            selectedCategory = retainedCategory,
                            targetGisuId = targetId,
                            boardChips = if (categoryChanged) chips.toImmutableList() else boardChips,
                            boardHint = if (categoryChanged) hint else boardHint,
                            isAllSelected = if (categoryChanged || targetChanged) false else isAllSelected,
                            isStaffSelected = if (categoryChanged || targetChanged) false else isStaffSelected,
                            selectedPart = if (categoryChanged || targetChanged) null else selectedPart,
                            selectedChapter = if (categoryChanged || targetChanged) null else selectedChapter,
                            selectedSchool = if (categoryChanged || targetChanged) null else selectedSchool,
                        )
                    }
                    loadTargetOrganization(uiState.value.targetGisuId)
                    hasLoadedTargetGisus = true
                },
                errorCallback = { fail -> emitEvent(NoticeWriteEvent.ShowError(fail.message)) },
            )
            isLoadingTargetGisus = false
        }
    }

    /** 목록 조회가 실패했으면 선택기를 다시 열 때 재시도한다. */
    fun onOpenCategorySelector() {
        if (uiState.value.writerRole == NoticeWriterRole.SUPER_ADMIN ||
            uiState.value.writerRole == NoticeWriterRole.CENTRAL_CORE) {
            loadTargetGisus(resolvedAuthorityGisuId)
        }
    }

    fun onSelectGisu(gisu: GisuItem) {
        val state = uiState.value
        val role = state.writerRole ?: return
        if (state.isTargetLocked || gisu !in state.selectableTargetGisus) return
        hasSelectedTargetGisu = true
        val categories = createCategories(role, state.availableGisus, gisu.gisuId, state.canWriteAsSchoolCore)
        val selectedCategory = selectNoticeCategory(categories, state.selectedCategory)
        val categoryChanged = selectedCategory?.type != state.selectedCategory?.type
        val (chips, hint) = selectedCategory?.let {
            createBoardChips(role, it.type, state.canWriteAsSchoolCore)
        } ?: (emptyList<BoardChipType>() to null)
        updateState {
            copy(
                targetGisuId = gisu.gisuId,
                availableCategories = categories.toImmutableList(),
                selectedCategory = selectedCategory,
                boardChips = if (categoryChanged) chips.toImmutableList() else boardChips,
                boardHint = if (categoryChanged) hint else boardHint,
                isAllSelected = false,
                isStaffSelected = false,
                selectedChapter = null,
                selectedSchool = null,
                selectedPart = null,
            )
        }
        loadTargetOrganization(gisu.gisuId)
    }

    /** 카테고리는 대상 종류만 나타내며, 기수는 별도 선택값으로 관리한다. */
    private fun createCategories(
        writerRole: NoticeWriterRole,
        gisus: List<GisuItem>,
        targetGisuId: Int?,
        canWriteAsSchoolCore: Boolean = false,
    ): List<WriteCategory> {
        val generation = gisus.firstOrNull { it.gisuId == targetGisuId }?.generation?.toLong()
        return createCategoriesForGeneration(writerRole, generation, canWriteAsSchoolCore)
    }

    /** 작성자 권한별 공지 카테고리 목록. 선택한 대상 기수의 존재 여부로 특정 기수 항목을 제한한다. */
    private fun createCategoriesForGeneration(
        writerRole: NoticeWriterRole,
        currentGeneration: Long?,
        canWriteAsSchoolCore: Boolean = false,
    ): List<WriteCategory> {
        return when (writerRole) {
            NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE -> buildList {
                add(WriteCategory(WriteCategoryType.ALL_GISU, AppStrings.NOTICE_WRITE_ALL_GISU))
                if (currentGeneration != null) {
                    add(WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU))
                }
                add(WriteCategory(WriteCategoryType.CENTRAL_STAFF, AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL))
                add(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))
                add(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
            }

            NoticeWriterRole.CENTRAL_STAFF ->
                listOfNotNull(
                    if (currentGeneration != null) WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU) else null,
                    WriteCategory(WriteCategoryType.CENTRAL_STAFF, AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL),
                    WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE),
                    WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER),
                )

            // 지부장 전용 카테고리·noticeTab은 없다. 본인 지부 일반 공지는 특정 기수 카테고리로 발행한다.
            NoticeWriterRole.CHAPTER_PRESIDENT -> buildList {
                if (currentGeneration != null) {
                    add(WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU))
                }
                // 교내 회장을 겸하면 활성 기수의 학교 역할로 학교 대상 작성도 허용한다.
                if (canWriteAsSchoolCore) add(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))
            }

            NoticeWriterRole.SCHOOL_CORE ->
                listOf(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))

            NoticeWriterRole.SCHOOL_ETC_ADMIN ->
                listOf(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))

            NoticeWriterRole.SCHOOL_PART_LEADER ->
                listOf(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
        }
    }

    /**
     * (작성자 권한, 카테고리) 조합별 게시판 분류 칩 구성.
     * Super Admin·중앙 운영진·지부장·교내 운영진별 서버 작성 범위에 맞춰 분류가 달라진다.
     */
    private fun createBoardChips(
        writerRole: NoticeWriterRole,
        categoryType: WriteCategoryType,
        canWriteAsSchoolCore: Boolean = false,
    ): Pair<List<BoardChipType>, String?> {
        return if (writerRole == NoticeWriterRole.SUPER_ADMIN) {
            when (categoryType) {
                WriteCategoryType.SCHOOL_CORE ->
                    listOf(BoardChipType.SCHOOL) to null

                // 기수 카테고리: 지부/학교/파트 (지부·학교 동시 선택 불가)
                WriteCategoryType.GISU -> listOf(
                    BoardChipType.ALL, BoardChipType.CHAPTER, BoardChipType.SCHOOL, BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_GISU_CLASS_HINT

                WriteCategoryType.SCHOOL_PART_LEADER -> listOf(BoardChipType.SCHOOL, BoardChipType.PART) to null

                else -> emptyList<BoardChipType>() to null
            }
        } else {
            when (writerRole) {
                NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.CENTRAL_STAFF -> when (categoryType) {
                    WriteCategoryType.GISU -> listOf(BoardChipType.ALL, BoardChipType.PART) to null
                    WriteCategoryType.CENTRAL_STAFF -> listOf(
                        BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
                    ) to AppStrings.NOTICE_WRITE_CLASS_HINT
                    WriteCategoryType.SCHOOL_PART_LEADER -> listOf(BoardChipType.PART) to null
                    else -> emptyList<BoardChipType>() to null
                }

                NoticeWriterRole.CHAPTER_PRESIDENT -> when (categoryType) {
                    // 지부장 대상은 본인 지부로 고정하고, 지부 전체 또는 특정 파트만 고른다.
                    WriteCategoryType.GISU -> listOf(BoardChipType.ALL, BoardChipType.PART) to null
                    WriteCategoryType.SCHOOL_CORE -> if (canWriteAsSchoolCore) listOf(
                        BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
                    ) to AppStrings.NOTICE_WRITE_CLASS_HINT else emptyList<BoardChipType>() to null
                    else -> emptyList<BoardChipType>() to null
                }

                NoticeWriterRole.SCHOOL_CORE -> listOf(
                    BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.SCHOOL_ETC_ADMIN -> listOf(BoardChipType.PART) to null

                // 서버는 담당 파트와 무관하게 본인 학교의 특정 파트 작성을 허용한다.
                // 학교 선택을 주지 않고 발행 요청에는 활성 기수 학교 역할의 소속 ID를 사용한다.
                NoticeWriterRole.SCHOOL_PART_LEADER -> listOf(
                    BoardChipType.PART
                ) to AppStrings.NOTICE_WRITE_CLASS_HINT

                NoticeWriterRole.SUPER_ADMIN -> emptyList<BoardChipType>() to null
            }
        }
    }

    /** 선택한 대상 기수에 실제 소속된 지부·학교만 칩 목록에 제공한다. */
    private fun loadTargetOrganization(gisuId: Int?) {
        if (gisuId == null || gisuId <= 0) {
            organizationRequestToken++
            loadingOrganizationGisuId = null
            loadedOrganizationGisuId = null
            updateState { copy(chapterList = persistentListOf(), schoolList = persistentListOf()) }
            return
        }
        if (loadingOrganizationGisuId == gisuId || loadedOrganizationGisuId == gisuId) return

        val requestToken = ++organizationRequestToken
        loadingOrganizationGisuId = gisuId
        loadedOrganizationGisuId = null
        updateState { copy(chapterList = persistentListOf(), schoolList = persistentListOf()) }
        viewModelScope.launch {
            resultResponse(
                response = getChapterWithSchoolUseCase(gisuId),
                successCallback = { organization ->
                    // 기수 변경 전 요청이 늦게 끝나도 현재 기수의 선택지를 덮어쓰지 않는다.
                    if (requestToken == organizationRequestToken && uiState.value.targetGisuId == gisuId) {
                        loadingOrganizationGisuId = null
                        loadedOrganizationGisuId = gisuId
                        updateState {
                            copy(
                                chapterList = organization.chapterList.toImmutableList(),
                                schoolList = organization.schoolList.toImmutableList(),
                            )
                        }
                    }
                },
                errorCallback = { fail ->
                    if (requestToken == organizationRequestToken) {
                        loadingOrganizationGisuId = null
                        emitEvent(NoticeWriteEvent.ShowError(fail.message))
                    }
                },
            )
        }
    }

    /** 대상 기수의 지부·학교 목록 조회가 실패했으면 칩을 다시 열 때 재시도한다. */
    fun onOpenOrganizationSelector() {
        loadTargetOrganization(uiState.value.targetGisuId)
    }

    /** 카테고리 선택. 게시판 분류 구성을 갱신하고 기존 선택은 초기화 */
    fun onSelectCategory(category: WriteCategory) {
        val writerRole = uiState.value.writerRole ?: return
        if (uiState.value.isTargetLocked || category !in uiState.value.availableCategories) return
        hasSelectedCategory = true
        val (chips, hint) = createBoardChips(writerRole, category.type, uiState.value.canWriteAsSchoolCore)

        updateState {
            copy(
                selectedCategory = category,
                boardChips = chips.toImmutableList(),
                boardHint = hint,
                isAllSelected = false,
                isStaffSelected = false,
                selectedChapter = null,
                selectedSchool = null,
                selectedPart = null,
            )
        }
    }

    /** 전체는 나머지 분류와 배타적이다. 켜는 순간 다른 선택을 모두 해제한다 */
    fun onToggleAll() {
        updateState {
            val turningOn = !isAllSelected
            copy(
                isAllSelected = turningOn,
                isStaffSelected = if (turningOn) false else isStaffSelected,
                selectedChapter = if (turningOn) null else selectedChapter,
                selectedSchool = if (turningOn) null else selectedSchool,
                selectedPart = if (turningOn) null else selectedPart,
            )
        }
    }

    fun onToggleStaff() {
        updateState { copy(isStaffSelected = !isStaffSelected, isAllSelected = false) }
    }

    /** 지부 선택. 기수 카테고리에서는 학교와 동시 선택 불가 */
    fun onSelectChapter(chapter: Chapter) {
        if (uiState.value.isTargetLocked || uiState.value.chapterList.none { it.id == chapter.id }) return
        val isExclusive = uiState.value.selectedCategory?.type == WriteCategoryType.GISU
        updateState {
            copy(
                selectedChapter = chapter,
                selectedSchool = if (isExclusive) null else selectedSchool,
                isAllSelected = false,
            )
        }
    }

    /** 학교 선택. 기수 카테고리에서는 지부와 동시 선택 불가 */
    fun onSelectSchool(school: SchoolInfo) {
        val state = uiState.value
        if (state.isTargetLocked || state.schoolList.none { it.schoolId == school.schoolId }) return
        val isExclusive = uiState.value.selectedCategory?.type == WriteCategoryType.GISU
        updateState {
            copy(
                selectedSchool = school,
                selectedChapter = if (isExclusive) null else selectedChapter,
                isAllSelected = false,
            )
        }
    }

    fun onSelectPart(part: UserPart) {
        updateState { copy(selectedPart = part, isAllSelected = false) }
    }

    fun onTitleChanged(title: String) {
        updateState { copy(title = title) }
    }

    /** 개행이 들어오면 그 줄에서 열린 마크다운은 닫아서 다음 줄로 넘기지 않는다 */
    fun onContentChanged(content: TextFieldValue) {
        val adjusted = MarkdownEditActions.closeMarkersOnNewline(uiState.value.content, content)
        updateState { copy(content = adjusted) }
    }

    /** 알림 발송 여부 토글 (종 아이콘) */
    fun onToggleNotification() {
        updateState { copy(sendNotification = !uiState.value.sendNotification) }
    }

    // ---------------------------------------------------------------
    // 온디바이스 AI (본문 롱클릭 메뉴)
    // ---------------------------------------------------------------

    /** 본문 다듬기: 작성 중인 본문을 앱 마크다운 형식으로 재작성 */
    fun onClickAiRefine() = viewModelScope.launch {
        val state = uiState.value
        if (state.isAiProcessing) return@launch
        if (state.content.text.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.AI_EMPTY_CONTENT))
            return@launch
        }

        updateState { copy(isAiProcessing = true, aiDownloadPercent = null) }

        resultResponse(
            response = refineNoticeMarkdownUseCase(state.content.text) { percent ->
                updateState { copy(aiDownloadPercent = percent) }
            },
            successCallback = { refined ->
                updateState {
                    copy(
                        isAiProcessing = false,
                        aiDownloadPercent = null,
                        content = TextFieldValue(refined, TextRange(refined.length)),
                    )
                }
            },
            errorCallback = {
                updateState { copy(isAiProcessing = false, aiDownloadPercent = null) }
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            },
        )
    }

    /**
     * 붙여넣고 요약: 복사해 온 공지 전문을 요약해 커서 위치에 붙여넣는다.
     * 클립보드 읽기는 화면(Composable)에서 하고 여기로 넘긴다
     */
    fun onClickAiSummarize(clipboardText: String) = viewModelScope.launch {
        if (uiState.value.isAiProcessing) return@launch
        if (clipboardText.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.AI_EMPTY_CLIPBOARD))
            return@launch
        }

        updateState { copy(isAiProcessing = true, aiDownloadPercent = null) }

        resultResponse(
            response = summarizeNoticeMarkdownUseCase(clipboardText) { percent ->
                updateState { copy(aiDownloadPercent = percent) }
            },
            successCallback = { summary ->
                updateState {
                    copy(
                        isAiProcessing = false,
                        aiDownloadPercent = null,
                        content = MarkdownEditActions.insertText(content, summary).endComposition(),
                    )
                }
            },
            errorCallback = {
                updateState { copy(isAiProcessing = false, aiDownloadPercent = null) }
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            },
        )
    }

    // ---------------------------------------------------------------
    // 마크다운 툴바
    // ---------------------------------------------------------------

    /** 텍스트 크기 변경. 커서가 위치한 줄의 제목 prefix 교체 */
    fun onSelectHeading(heading: MarkdownHeading) {
        updateState { copy(content = MarkdownEditActions.applyHeading(content, heading).endComposition()) }
    }

    fun onClickBold() {
        updateState { copy(content = MarkdownEditActions.toggleBold(content).endComposition()) }
    }

    fun onClickItalic() {
        updateState { copy(content = MarkdownEditActions.toggleItalic(content).endComposition()) }
    }

    fun onClickUnderline() {
        updateState { copy(content = MarkdownEditActions.toggleUnderline(content).endComposition()) }
    }

    fun onClickStrikethrough() {
        updateState { copy(content = MarkdownEditActions.toggleStrikethrough(content).endComposition()) }
    }

    fun onClickBullet() {
        updateState { copy(content = MarkdownEditActions.toggleBullet(content).endComposition()) }
    }

    fun onClickQuote() {
        updateState { copy(content = MarkdownEditActions.toggleQuote(content).endComposition()) }
    }

    /** 형광펜 색상 선택. 고른 색을 적용하고 다음 선택의 기본값으로 기억한다 */
    fun onSelectHighlight(color: MarkdownHighlightColor) {
        val applied = MarkdownEditActions.toggleHighlight(uiState.value.content, color)
        // 선택 영역이 없으면 toggleHighlight가 원본을 그대로 돌려준다. 이때는 색도 기록하지 않는다
        if (applied == uiState.value.content) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.NOTICE_WRITE_HIGHLIGHT_NEEDS_SELECTION))
            return
        }
        updateState { copy(highlightColor = color, content = applied.endComposition()) }
    }

    // ---------------------------------------------------------------
    // 이미지 / 링크 / 투표 첨부
    // ---------------------------------------------------------------

    /** 선택한 이미지를 즉시 업로드 후 첨부 목록에 추가 (업로드 중 다이얼로그 노출) */
    fun onAddImages(uris: List<Uri>) = viewModelScope.launch {
        val current = uiState.value.images
        val newUris = uris
            .map { it.toString() }
            .filter { uri -> current.none { it.uri == uri } }
            .take(MAX_IMAGE_COUNT - current.size)
        if (newUris.isEmpty()) return@launch

        updateState { copy(isUploadingImages = true) }

        var hasError = false
        newUris.forEach { uri ->
            resultResponse(
                response = uploadFileUseCase(uri, UploadFileCategory.NOTICE_ATTACHMENT),
                successCallback = { uploaded ->
                    updateState {
                        copy(
                            hasUnrestoredImages = false,
                            images = (images + NoticeImageAttachment(
                                uri = uri,
                                fileId = uploaded.fileId,
                            )).toImmutableList()
                        )
                    }
                },
                errorCallback = { hasError = true },
            )
        }

        updateState { copy(isUploadingImages = false) }
        if (hasError) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.NOTICE_WRITE_IMAGE_UPLOAD_FAIL))
        }
    }

    fun onRemoveImage(image: NoticeImageAttachment) {
        updateState { copy(hasUnrestoredImages = false, images = (images - image).toImmutableList()) }
    }

    /** 링크 첨부 패널 표시 (툴바 링크 버튼) */
    fun onShowLinkPanel() {
        updateState { copy(isLinkVisible = true) }
    }

    /** 링크 첨부 패널 닫기 (패널 우상단 X) */
    fun onHideLinkPanel() {
        updateState { copy(isLinkVisible = false, linkText = "") }
    }

    fun onLinkTextChanged(text: String) {
        updateState { copy(linkText = text) }
    }

    /** 투표 생성/수정 완료. 바텀시트에서 편집한 최종 폼을 첨부 */
    fun onAttachVote(vote: NoticeVoteForm) {
        updateState { copy(vote = vote) }
    }

    fun onDeleteVote() {
        updateState { copy(vote = null) }
    }

    // ---------------------------------------------------------------
    // 등록
    // ---------------------------------------------------------------

    /** 공지 등록/수정: 본문 저장 후 이미지 → 링크 → 투표 순서로 연결 */
    fun onClickRegister() = viewModelScope.launch {
        val state = uiState.value
        if (!state.enableRegister || state.isSubmitting) return@launch

        // 수정 모드이거나, 이미 생성은 됐지만 일부 항목 연결에 실패해 다시 등록을 누른 경우에는
        // 새로 만들지 않고 만들어진 공지를 수정(PATCH)하는 경로로 재시도한다
        val existingId: Long? = if (state.isEditMode) state.editNoticeId else state.createdNoticeId
        if (existingId != null) {
            updateState { copy(isSubmitting = true) }
            resultResponse(
                response = updateNoticeUseCase(
                    existingId,
                    NoticeUpdateRequest(
                        title = state.title.trim(),
                        content = state.content.text,
                        mustRead = state.mustRead,
                    )
                ),
                successCallback = { attachExtras(existingId, isReplace = true) },
                errorCallback = {
                    updateState { copy(isSubmitting = false) }
                    emitEvent(NoticeWriteEvent.ShowError(it.message))
                }
            )
            return@launch
        }

        val target = buildNoticeTargetRequest(state) ?: run {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.NOTICE_WRITE_CATEGORY_PLACEHOLDER))
            return@launch
        }

        updateState { copy(isSubmitting = true) }

        resultResponse(
            response = createNoticeUseCase(
                NoticeCreateRequest(
                    title = state.title.trim(),
                    content = state.content.text,
                    shouldNotify = state.sendNotification,
                    targetInfo = target,
                )
            ),
            successCallback = { noticeId ->
                // 일부 항목 연결이 실패해도 다시 등록했을 때 중복 생성되지 않도록 생성된 ID를 기억한다
                updateState { copy(createdNoticeId = noticeId) }
                attachExtras(noticeId, isReplace = false)
            },
            errorCallback = {
                updateState { copy(isSubmitting = false) }
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            }
        )
    }

    /**
     * 공지 생성 후 이미지/링크/투표를 순서대로 연결.
     * [isReplace]는 수정이거나 부분 실패 후 재시도라서 이미 연결된 항목이 있을 수 있는 경우다
     */
    private fun attachExtras(noticeId: Long, isReplace: Boolean) = viewModelScope.launch {
        val state = uiState.value
        var hasError = false

        val imageIds = state.images.map { it.fileId }
        // 수정·재시도는 전체 교체(PATCH)로 보내야 기존 이미지가 중복되지 않고, 지운 것도 반영된다
        // 단, 서버 이미지 일부를 폼에 복원하지 못했고 사용자가 이미지를 건드리지 않았다면 보내지 않는다
        if (isReplace && state.hasUnrestoredImages) {
            // 이미지 변경 없음: 서버에 있는 이미지를 그대로 둔다
        } else if (isReplace) {
            resultResponse(
                response = updateNoticeImagesUseCase(noticeId, imageIds),
                successCallback = { },
                errorCallback = { hasError = true },
            )
        } else if (imageIds.isNotEmpty()) {
            resultResponse(
                response = addNoticeImagesUseCase(noticeId, imageIds),
                successCallback = { },
                errorCallback = { hasError = true },
            )
        }

        val links = state.linkText
            .takeIf { state.isLinkVisible }
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        // 링크는 원래 전체 교체 API라, 수정 시에는 비어 있어도 보내야 삭제가 반영된다
        if (links.isNotEmpty() || isReplace) {
            resultResponse(
                response = addNoticeLinksUseCase(noticeId, links),
                successCallback = { },
                errorCallback = { hasError = true },
            )
        }

        // 투표는 삭제하면 참여 결과도 함께 사라지므로, 서버에 있는 투표(serverVote)와 폼이 같으면
        // 지우지도 다시 만들지도 않는다. 공지당 1개라 바뀐 경우에만 기존 것을 먼저 지우고 새로 만든다
        val newVote = state.vote?.takeIf { it.canSubmit }
        if (state.vote != state.serverVote) {
            var isServerVoteRemoved = state.serverVote == null
            if (state.serverVote != null) {
                resultResponse(
                    response = deleteNoticeVoteUseCase(noticeId),
                    successCallback = {
                        isServerVoteRemoved = true
                        updateState { copy(serverVote = null) }
                    },
                    errorCallback = { hasError = true },
                )
            }

            // 지우기에 실패했다면 이미 투표가 있는 상태라 추가가 항상 실패하므로 시도하지 않는다
            if (isServerVoteRemoved) {
                newVote?.let { vote ->
                    resultResponse(
                        response = addNoticeVoteUseCase(
                            noticeId,
                            NoticeVoteRequest(
                                title = vote.title.trim().ifBlank { AppStrings.NOTICE_WRITE_VOTE_TITLE },
                                isAnonymous = vote.isAnonymous,
                                allowMultipleChoice = vote.allowMultipleChoice,
                                startsAt = vote.startsAt.orEmpty(),
                                endsAtExclusive = vote.endsAt.orEmpty(),
                                options = vote.validOptions,
                            )
                        ),
                        // 재시도 때 이미 만든 투표를 다시 추가하지 않도록 서버 상태를 기록한다
                        successCallback = { updateState { copy(serverVote = vote) } },
                        errorCallback = { hasError = true },
                    )
                }
            }
        }

        updateState { copy(isSubmitting = false) }

        if (hasError) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.NOTICE_WRITE_PARTIAL_FAIL))
        } else {
            emitEvent(NoticeWriteEvent.SubmitSuccess)
        }
    }
}

/** 기존 선택이 유효하면 유지하고, 그렇지 않으면 첫 카테고리를 기본 선택한다. */
internal fun selectNoticeCategory(categories: List<WriteCategory>, previous: WriteCategory?): WriteCategory? =
    categories.firstOrNull { it.type == previous?.type } ?: categories.firstOrNull()

/** Super Admin·총괄단 외에는 권한을 부여받은 기수만 작성 대상으로 허용한다. */
internal fun allowedNoticeTargetGisus(
    writerRole: NoticeWriterRole,
    gisus: List<GisuItem>,
    authorityGisuId: Int?,
): List<GisuItem> = when (writerRole) {
    NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE -> gisus
    else -> gisus.filter { it.gisuId == authorityGisuId }
}

data class NoticeWriteUiState(
    val writerRole: NoticeWriterRole? = null,
    val canWriteAsSchoolCore: Boolean = false,
    val availableCategories: ImmutableList<WriteCategory> = persistentListOf(),
    val selectedCategory: WriteCategory? = null,
    val boardChips: ImmutableList<BoardChipType> = persistentListOf(),
    val boardHint: String? = null,
    val isAllSelected: Boolean = false,
    val isStaffSelected: Boolean = false,
    val selectedChapter: Chapter? = null,
    val selectedSchool: SchoolInfo? = null,
    val selectedPart: UserPart? = null,
    val chapterList: ImmutableList<Chapter> = persistentListOf(),
    val schoolList: ImmutableList<SchoolInfo> = persistentListOf(),
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(),
    val sendNotification: Boolean = true,
    /** 상세에서 불러온 필독 여부. 수정 저장 시 그대로 돌려보낸다 */
    val mustRead: Boolean = false,
    /** 지금 서버에 달려 있는 투표. 폼과 같으면 저장 때 건드리지 않고, 다르면 지우고 새로 만든다 */
    val serverVote: NoticeVoteForm? = null,
    /** 신규 작성에서 이미 생성된 공지 ID. 일부 항목 연결 실패 후 재등록 때 중복 생성을 막는다 */
    val createdNoticeId: Long? = null,
    val images: ImmutableList<NoticeImageAttachment> = persistentListOf(),
    /** 수정 진입 시 fileId가 없어 폼에 복원하지 못한 서버 이미지가 있는지. 사용자가 이미지를 바꾸면 false */
    val hasUnrestoredImages: Boolean = false,
    val isUploadingImages: Boolean = false,
    val isLinkVisible: Boolean = false,
    val linkText: String = "",
    val vote: NoticeVoteForm? = null,
    /** 서버 활성 기수의 작성 권한과 별개로 선택한 공지 대상 기수 ID */
    val targetGisuId: Int? = null,
    /** 작성 권한을 부여받은 기수. Super Admin·총괄단 외에는 이 기수에만 발행한다. */
    val authorityGisuId: Int? = null,
    val writerSchoolId: Int? = null,
    val writerChapterId: Int? = null,
    val availableGisus: ImmutableList<GisuItem> = persistentListOf(),
    val isSubmitting: Boolean = false,
    val isAiRefineEnabled: Boolean = false,
    val isAiProcessing: Boolean = false,
    // 모델 다운로드가 진행 중일 때만 0~100, 추론 단계에서는 null
    val aiDownloadPercent: Int? = null,
    /** 마지막으로 적용한 형광펜 색. 아직 쓴 적 없으면 null이라 메뉴에 체크가 없다 */
    val highlightColor: MarkdownHighlightColor? = null,
    val isEditMode: Boolean = false,
    val editNoticeId: Long = 0L,
) : UiState {
    // 화면 선택지와 선택 처리에서 같은 권한 정책을 사용한다. 권한이 없으면 선택지를 제공하지 않는다.
    val selectableTargetGisus: ImmutableList<GisuItem>
        get() = writerRole?.let { role ->
            allowedNoticeTargetGisus(role, availableGisus, authorityGisuId).toImmutableList()
        } ?: persistentListOf()

    // 전체 기수 공지는 특정 기수를 지정하지 않는다. 그 외에는 기수 간 발행 권한이 있는 역할만 선택기를 표시한다.
    val showTargetGisuSelector: Boolean
        get() = (writerRole == NoticeWriterRole.SUPER_ADMIN || writerRole == NoticeWriterRole.CENTRAL_CORE) &&
                selectedCategory?.type != WriteCategoryType.ALL_GISU

    // 게시판 분류가 있는 카테고리에서 하나라도 선택됐는지
    val hasBoardSelection: Boolean
        get() = isAllSelected || isStaffSelected
                || selectedChapter != null || selectedSchool != null || selectedPart != null

    // 수정 모드이거나 이미 공지가 만들어진 뒤(부분 실패 재등록)에는 수신 대상을 바꿀 수 없다
    val isTargetLocked: Boolean
        get() = isEditMode || createdNoticeId != null

    // 수신 대상이 잠겨 있으면 카테고리/분류 조건 제외
    val enableRegister: Boolean
        get() = (isTargetLocked || buildNoticeTargetRequest(this) != null)
                && title.isNotBlank()
                && content.text.isNotBlank()
                && !isSubmitting
}

sealed interface NoticeWriteEvent : UiEvent {
    data object SubmitSuccess : NoticeWriteEvent
    data class ShowError(val message: String) : NoticeWriteEvent
}
