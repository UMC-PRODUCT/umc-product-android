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
import com.umc.domain.model.enums.NoticeTab
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.NoticeImageAttachment
import com.umc.domain.model.notice.NoticeVoteForm
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeTargetRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.usecase.ai.CheckAiFeatureStatusUseCase
import com.umc.domain.usecase.ai.RefineNoticeMarkdownUseCase
import com.umc.domain.usecase.ai.SummarizeNoticeMarkdownUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.AddNoticeImagesUseCase
import com.umc.domain.usecase.notice.AddNoticeLinksUseCase
import com.umc.domain.usecase.notice.AddNoticeVoteUseCase
import com.umc.domain.usecase.notice.CreateNoticeUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.UpdateNoticeUseCase
import com.umc.domain.usecase.organization.GetChapterListUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val updateNoticeUseCase: UpdateNoticeUseCase,
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val addNoticeImagesUseCase: AddNoticeImagesUseCase,
    private val addNoticeLinksUseCase: AddNoticeLinksUseCase,
    private val addNoticeVoteUseCase: AddNoticeVoteUseCase,
    private val checkAiFeatureStatusUseCase: CheckAiFeatureStatusUseCase,
    private val refineNoticeMarkdownUseCase: RefineNoticeMarkdownUseCase,
    private val summarizeNoticeMarkdownUseCase: SummarizeNoticeMarkdownUseCase,
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {

    companion object {
        const val MAX_IMAGE_COUNT = 10
        const val MAX_VOTE_OPTION_COUNT = 5
    }

    init {
        loadWriterRole()
        loadChapterList()
        loadSchoolList()
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
                val linkText = detail.links
                    .sortedBy { it.displayOrder }
                    .joinToString(", ") { it.url }
                updateState {
                    copy(
                        title = detail.title,
                        content = TextFieldValue(detail.content),
                        isLinkVisible = linkText.isNotBlank(),
                        linkText = linkText,
                        images = detail.images
                            .sortedBy { it.displayOrder }
                            .map { NoticeImageAttachment(uri = it.url, fileId = it.id.toString()) },
                    )
                }
            },
            errorCallback = {
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            },
        )
    }

    /** 작성자 권한을 계산하고 권한별 카테고리 목록 구성. 권한에 맞는 카테고리가 기본 선택됨 */
    private fun loadWriterRole() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            val roles = userInfo.roles.map { UserChallengerRole.from(it.roleType) }
            val writerRole = NoticeWriterRole.from(roles) ?: return@collect

            // 최고 관리자의 기수 카테고리 라벨용 현재(최신) 기수
            val currentRecord = userInfo.challengerRecords.maxByOrNull { it.gisu }

            val categories = createCategories(writerRole, currentRecord?.gisu)

            updateState {
                copy(
                    writerRole = writerRole,
                    availableCategories = categories,
                    activeGisuId = currentRecord?.gisuId?.toInt(),
                    writerSchoolId = userInfo.schoolId.takeIf { it > 0 }?.toInt(),
                )
            }

            // 권한에 맞는 첫 카테고리 자동 선택 (최고 관리자는 전체 기수).
            // 유저 정보 재수집 시 사용자가 바꾼 선택을 덮어쓰지 않도록 최초 1회만
            if (uiState.value.selectedCategory == null) {
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

    fun onContentChanged(content: TextFieldValue) {
        updateState { copy(content = content) }
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
                        content = MarkdownEditActions.insertText(content, summary),
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
        updateState { copy(content = MarkdownEditActions.applyHeading(content, heading)) }
    }

    fun onClickBold() {
        updateState { copy(content = MarkdownEditActions.toggleBold(content)) }
    }

    fun onClickItalic() {
        updateState { copy(content = MarkdownEditActions.toggleItalic(content)) }
    }

    fun onClickUnderline() {
        updateState { copy(content = MarkdownEditActions.toggleUnderline(content)) }
    }

    fun onClickStrikethrough() {
        updateState { copy(content = MarkdownEditActions.toggleStrikethrough(content)) }
    }

    fun onClickBullet() {
        updateState { copy(content = MarkdownEditActions.toggleBullet(content)) }
    }

    fun onClickQuote() {
        updateState { copy(content = MarkdownEditActions.toggleQuote(content)) }
    }

    /** 형광펜 색상 선택. 고른 색을 적용하고 다음 선택의 기본값으로 기억한다 */
    fun onSelectHighlight(color: MarkdownHighlightColor) {
        updateState {
            copy(
                highlightColor = color,
                content = MarkdownEditActions.toggleHighlight(content, color),
            )
        }
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
                        copy(images = images + NoticeImageAttachment(uri = uri, fileId = uploaded.fileId))
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
        updateState { copy(images = images - image) }
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

        if (state.isEditMode) {
            updateState { copy(isSubmitting = true) }
            resultResponse(
                response = updateNoticeUseCase(
                    state.editNoticeId,
                    NoticeUpdateRequest(
                        title = state.title.trim(),
                        content = state.content.text,
                    )
                ),
                successCallback = { attachExtras(state.editNoticeId) },
                errorCallback = {
                    updateState { copy(isSubmitting = false) }
                    emitEvent(NoticeWriteEvent.ShowError(it.message))
                }
            )
            return@launch
        }

        val target = buildTargetRequest(state) ?: run {
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
            successCallback = { noticeId -> attachExtras(noticeId) },
            errorCallback = {
                updateState { copy(isSubmitting = false) }
                emitEvent(NoticeWriteEvent.ShowError(it.message))
            }
        )
    }

    /**
     * 카테고리/게시판 분류 선택을 서버 수신 대상으로 매핑.
     *
     * noticeTab 스펙: CHALLENGER(일반) / CENTRAL_MEMBER(중앙운영진) /
     * SCHOOL_CORE(학교 회장단) / SCHOOL_PART_LEADER(파트장).
     * 운영진 공지 칩 선택 시 파트가 함께 선택되면 파트장 보드, 아니면 회장단 보드로 발행 (develop 정책과 동일)
     */
    private fun buildTargetRequest(state: NoticeWriteUiState): NoticeTargetRequest? {
        val category = state.selectedCategory ?: return null

        if (state.writerRole == NoticeWriterRole.SUPER_ADMIN) {
            return when (category.type) {
                WriteCategoryType.ALL_GISU -> NoticeTargetRequest(
                    targetGisuId = null,
                    targetChapterId = null,
                    targetSchoolId = null,
                    targetParts = emptyList(),
                    targetNoticeTab = NoticeTab.CHALLENGER.value,
                )

                WriteCategoryType.GISU -> NoticeTargetRequest(
                    targetGisuId = state.activeGisuId,
                    targetChapterId = state.selectedChapter?.id?.toInt(),
                    targetSchoolId = state.selectedSchool?.schoolId?.toInt(),
                    targetParts = listOfNotNull(state.selectedPart?.name),
                    targetNoticeTab = NoticeTab.CHALLENGER.value,
                )

                else -> NoticeTargetRequest(
                    targetGisuId = state.activeGisuId,
                    targetChapterId = null,
                    targetSchoolId = null,
                    targetParts = listOfNotNull(state.selectedPart?.name),
                    targetNoticeTab = when (category.type) {
                        WriteCategoryType.CENTRAL_STAFF -> NoticeTab.CENTRAL_MEMBER.value
                        WriteCategoryType.SCHOOL_CORE -> NoticeTab.SCHOOL_CORE.value
                        else -> NoticeTab.SCHOOL_PART_LEADER.value
                    },
                )
            }
        }

        // 학교 단위 권한은 자기 학교로 대상 고정
        val isSchoolLevelWriter = state.writerRole == NoticeWriterRole.SCHOOL_CORE ||
                state.writerRole == NoticeWriterRole.SCHOOL_PART_LEADER

        if (state.isStaffSelected) {
            // 운영진 공지: 학교 단위 권한 또는 파트 선택 시 파트장 보드, 아니면 회장단 보드
            val staffTab = if (isSchoolLevelWriter || state.selectedPart != null) {
                NoticeTab.SCHOOL_PART_LEADER
            } else {
                NoticeTab.SCHOOL_CORE
            }
            return NoticeTargetRequest(
                targetGisuId = state.activeGisuId,
                targetChapterId = null,
                targetSchoolId = if (isSchoolLevelWriter) state.writerSchoolId else null,
                targetParts = listOfNotNull(state.selectedPart?.name),
                targetNoticeTab = staffTab.value,
            )
        }

        // 일반 공지: 전체 선택 시 지부/학교/파트 필터 없이 발행
        val useFilters = !state.isAllSelected
        return NoticeTargetRequest(
            targetGisuId = state.activeGisuId,
            targetChapterId = state.selectedChapter?.id?.toInt().takeIf { useFilters },
            targetSchoolId = when {
                isSchoolLevelWriter -> state.selectedSchool?.schoolId?.toInt() ?: state.writerSchoolId
                useFilters -> state.selectedSchool?.schoolId?.toInt()
                else -> null
            },
            targetParts = if (useFilters) listOfNotNull(state.selectedPart?.name) else emptyList(),
            targetNoticeTab = NoticeTab.CHALLENGER.value,
        )
    }

    /** 공지 생성 후 이미지/링크/투표를 순서대로 연결 */
    private fun attachExtras(noticeId: Long) = viewModelScope.launch {
        val state = uiState.value
        var hasError = false

        if (state.images.isNotEmpty()) {
            resultResponse(
                response = addNoticeImagesUseCase(noticeId, state.images.map { it.fileId }),
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
        if (links.isNotEmpty()) {
            resultResponse(
                response = addNoticeLinksUseCase(noticeId, links),
                successCallback = { },
                errorCallback = { hasError = true },
            )
        }

        state.vote?.takeIf { it.canSubmit }?.let { vote ->
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
                successCallback = { },
                errorCallback = { hasError = true },
            )
        }

        updateState { copy(isSubmitting = false) }

        if (hasError) {
            emitEvent(NoticeWriteEvent.ShowError(AppStrings.NOTICE_WRITE_PARTIAL_FAIL))
        } else {
            emitEvent(NoticeWriteEvent.SubmitSuccess)
        }
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
    val content: TextFieldValue = TextFieldValue(),
    val sendNotification: Boolean = true,
    val images: List<NoticeImageAttachment> = emptyList(),
    val isUploadingImages: Boolean = false,
    val isLinkVisible: Boolean = false,
    val linkText: String = "",
    val vote: NoticeVoteForm? = null,
    val activeGisuId: Int? = null,
    val writerSchoolId: Int? = null,
    val isSubmitting: Boolean = false,
    val isAiRefineEnabled: Boolean = false,
    val isAiProcessing: Boolean = false,
    // 모델 다운로드가 진행 중일 때만 0~100, 추론 단계에서는 null
    val aiDownloadPercent: Int? = null,
    val highlightColor: MarkdownHighlightColor = MarkdownHighlightColor.PURPLE,
    val isEditMode: Boolean = false,
    val editNoticeId: Long = 0L,
) : UiState {
    // 게시판 분류가 있는 카테고리에서 하나라도 선택됐는지
    val hasBoardSelection: Boolean
        get() = isAllSelected || isStaffSelected
                || selectedChapter != null || selectedSchool != null || selectedPart != null

    // 수정 모드에서는 수신 대상을 바꿀 수 없으므로 카테고리/분류 조건 제외
    val enableRegister: Boolean
        get() = (isEditMode || (selectedCategory != null && (boardChips.isEmpty() || hasBoardSelection)))
                && title.isNotBlank()
                && content.text.isNotBlank()
                && !isSubmitting
}

sealed interface NoticeWriteEvent : UiEvent {
    data object SubmitSuccess : NoticeWriteEvent
    data class ShowError(val message: String) : NoticeWriteEvent
}
