package com.example.mypage.memberprofile

import com.umc.domain.model.mypage.UserReportReason
import com.umc.domain.usecase.member.ReportMemberUseCase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mypage.activityhistory.ActivityHistoryBuilder
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.member.BlockMemberUseCase
import com.umc.domain.usecase.member.GetBlockedMemberIdsUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.member.UnblockMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 상세 신고 내용 최대 글자 수 */
const val MEMBER_REPORT_DETAIL_MAX_LENGTH = 500

/** 사용자 신고 사유 (시안 순서) */
enum class MemberReportReason(val label: String) {
    HARASSMENT(AppStrings.MEMBER_REPORT_REASON_HARASSMENT),
    IMPERSONATION(AppStrings.MEMBER_REPORT_REASON_IMPERSONATION),
    SPAM(AppStrings.MEMBER_REPORT_REASON_SPAM),
    ETC(AppStrings.MEMBER_REPORT_REASON_ETC),
}

/**
 * 스레드 > 프로필 정보 보기 (타인 프로필 조회) 화면 ViewModel
 *
 * 수정할 수 없는 타인 정보를 보여주고, 메뉴에서 사용자 차단·차단 해제·신고를 처리한다.
 */
@HiltViewModel
class MemberProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportMemberUseCase: ReportMemberUseCase,
    private val getMemberProfileUseCase: GetMemberProfileUseCase, //회원 정보 조회
    private val getBlockedMemberIdsUseCase: GetBlockedMemberIdsUseCase, //내가 차단한 회원 목록
    private val blockMemberUseCase: BlockMemberUseCase, //회원 차단
    private val unblockMemberUseCase: UnblockMemberUseCase, //회원 차단 해제
    private val getUserInfoUseCase: GetUserInfoUseCase, //내 정보 (내 프로필이면 메뉴를 숨긴다)
    private val activityHistoryBuilder: ActivityHistoryBuilder, //활동 이력 목록 (마이페이지 활동 이력과 공용)
) : BaseViewModel<MemberProfileUiState, MemberProfileEvent>(MemberProfileUiState()) {

    private val memberId: Long? = savedStateHandle.get<String>("memberId")?.toLongOrNull()

    init {
        loadProfile()
        loadBlockState()
    }

    /** 조회 실패 안내에서 '다시 시도하기'를 눌렀을 때 호출 */
    fun onClickRetry() {
        if (uiState.value.isLoading) return
        loadProfile()
        loadBlockState()
    }

    private fun loadProfile() {
        val id = memberId
        if (id == null || id <= 0) {
            updateState { copy(isLoading = false, isLoadFailed = true) }
            return
        }
        updateState { copy(isLoading = true, isLoadFailed = false) }
        viewModelScope.launch {
            val myId = runCatching { getUserInfoUseCase().first().id }.getOrDefault(0L)
            resultResponse(
                response = getMemberProfileUseCase(id),
                successCallback = { userInfo ->
                    updateState {
                        copy(
                            userInfo = userInfo,
                            gisuPartText = gisuPartTextOf(userInfo),
                            isMine = myId != 0L && myId == userInfo.id,
                            isLoading = false,
                            isLoadFailed = false,
                        )
                    }
                    viewModelScope.launch {
                        val histories = activityHistoryBuilder.build(userInfo)
                        updateState { copy(histories = histories.toImmutableList(), isHistoryLoading = false) }
                    }
                },
                errorCallback = {
                    updateState { copy(isLoading = false, isLoadFailed = true, isHistoryLoading = false) }
                }
            )
        }
    }

    /**
     * 내가 이 회원을 차단했는지 확인해 메뉴(사용자 차단 / 차단 해제)를 정한다.
     * 서버는 이미 차단한 회원을 다시 차단하면 오류를 내므로, 확인하지 못했으면 차단 메뉴를 막아 둔다
     */
    private fun loadBlockState() {
        val id = memberId ?: return
        viewModelScope.launch {
            when (val result = getBlockedMemberIdsUseCase()) {
                is ApiState.Success -> updateState { copy(isBlocked = id in result.data, isBlockStateKnown = true) }
                is ApiState.Fail -> updateState { copy(isBlockStateKnown = false) }
            }
        }
    }

    /**
     * 최신 기수의 "N기/파트" 문구. 파트가 없는 운영진은 직함을 쓴다
     */
    private fun gisuPartTextOf(userInfo: UserInfo): String {
        val latest = userInfo.getGisuSummaryList().maxByOrNull { it.gisu } ?: return ""
        val item = (latest.fromRoles + latest.fromRecords)
            .firstOrNull { UserPart.from(it.responsiblePart) != UserPart.UNKNOWN }
        val label = item?.let { UserPart.from(it.responsiblePart).label }
            ?: latest.fromRoles.firstOrNull()?.let { UserChallengerRole.from(it.role).displayName }
            ?: return "${latest.gisu}기"
        return AppStrings.MEMBER_PROFILE_GISU_PART_FORMAT.format(latest.gisu, label)
    }

    // ---------------- 메뉴 ----------------

    fun onClickMenu() {
        updateState { copy(isMenuOpen = true) }
        // 차단 여부를 아직 확인하지 못했으면 메뉴를 열 때 다시 확인한다
        if (!uiState.value.isBlockStateKnown) loadBlockState()
    }
    fun onDismissMenu() = updateState { copy(isMenuOpen = false) }

    /** 메뉴의 사용자 차단 / 차단 해제 (차단 여부를 확인한 뒤에만 동작) */
    fun onClickBlockMenu() {
        if (!uiState.value.isBlockStateKnown) return
        updateState {
            copy(isMenuOpen = false, dialog = if (isBlocked) MemberProfileDialog.UNBLOCK else MemberProfileDialog.BLOCK)
        }
    }

    /** 메뉴의 사용자 신고 → 신고 사유 시트 */
    fun onClickReportMenu() = updateState {
        if (isProcessing || isMine) return@updateState this
        copy(isMenuOpen = false, isReportSheetOpen = true, reportReasons = persistentSetOf(), reportDetail = "")
    }

    fun onDismissDialog() = updateState { copy(dialog = null) }

    // ---------------- 차단 ----------------

    /** 차단·차단 해제 확인 창에서 확인을 눌렀을 때 */
    fun onConfirmBlock() {
        val id = memberId ?: return
        val dialog = uiState.value.dialog
        if (uiState.value.isProcessing) return
        updateState { copy(dialog = null, isProcessing = true) }
        viewModelScope.launch {
            val unblocking = dialog == MemberProfileDialog.UNBLOCK
            resultResponse(
                response = if (unblocking) unblockMemberUseCase(id) else blockMemberUseCase(id),
                successCallback = { blocked ->
                    updateState { copy(isBlocked = blocked, isBlockStateKnown = true, isProcessing = false) }
                    emitEvent(
                        MemberProfileEvent.ShowToast(
                            message = if (blocked) AppStrings.MEMBER_BLOCK_DONE else AppStrings.MEMBER_UNBLOCK_DONE,
                            isSuccess = true
                        )
                    )
                },
                errorCallback = {
                    updateState { copy(isProcessing = false) }
                    emitEvent(MemberProfileEvent.ShowToast(AppStrings.MEMBER_BLOCK_FAIL, isSuccess = false))
                }
            )
        }
    }

    // ---------------- 신고 ----------------

    fun onToggleReportReason(reason: MemberReportReason) = updateState {
        if (isProcessing) return@updateState this
        val updated = if (reason in reportReasons) reportReasons - reason else reportReasons + reason
        copy(reportReasons = updated.toImmutableSet())
    }

    fun onReportDetailChange(text: String) = updateState {
        if (isProcessing || text.length > MEMBER_REPORT_DETAIL_MAX_LENGTH) this else copy(reportDetail = text)
    }

    fun onDismissReportSheet() = updateState { copy(isReportSheetOpen = false) }

    /** 시트의 신고하기 → 신고 확인 창 */
    fun onClickReportSubmit() {
        if (!uiState.value.canSubmitReport) return
        updateState { copy(dialog = MemberProfileDialog.REPORT) }
    }

    /** 신고 확인 창에서 신고하기를 눌렀을 때 */
    fun onConfirmReport() {
        val snapshot = uiState.value
        val id = memberId?.takeIf { it > 0 } ?: return
        if (snapshot.dialog != MemberProfileDialog.REPORT || !snapshot.canSubmitReport || snapshot.isMine) return
        val reasons = snapshot.reportReasons.map { UserReportReason.valueOf(it.name) }.toSet()
        updateState { copy(dialog = null, isProcessing = true) }
        viewModelScope.launch {
            try {
                when (val result = reportMemberUseCase(id, reasons, snapshot.reportDetail)) {
                    is ApiState.Success -> {
                        updateState { copy(isReportSheetOpen = false, reportReasons = persistentSetOf(), reportDetail = "") }
                        emitEvent(MemberProfileEvent.ShowToast(AppStrings.MEMBER_REPORT_DONE, isSuccess = true))
                    }
                    is ApiState.Fail -> {
                        val message = if (result.failState.code == "COMMUNITY-0016") AppStrings.CHAT_REPORT_ALREADY_EXISTS
                            else result.failState.message.ifBlank { AppStrings.MEMBER_REPORT_FAIL }
                        emitEvent(MemberProfileEvent.ShowToast(message, isSuccess = false))
                    }
                }
            } finally {
                updateState { copy(isProcessing = false) }
            }
        }
    }
}

/** 화면에 띄울 확인 창 종류 */
enum class MemberProfileDialog { BLOCK, UNBLOCK, REPORT }

data class MemberProfileUiState(
    val userInfo: UserInfo = UserInfo(),
    val gisuPartText: String = "", //"12기/PM"
    val histories: ImmutableList<UserActiveItem> = persistentListOf(),
    val isLoading: Boolean = true,
    val isLoadFailed: Boolean = false,
    val isHistoryLoading: Boolean = true,
    val isMine: Boolean = false, //내 프로필이면 차단·신고 메뉴를 숨긴다

    val isBlocked: Boolean = false,
    val isBlockStateKnown: Boolean = false, //차단 여부를 서버에서 확인했는지 (확인 전에는 차단 메뉴를 막는다)
    val isMenuOpen: Boolean = false,
    val dialog: MemberProfileDialog? = null,
    val isProcessing: Boolean = false, //차단·해제·신고 요청 진행 중

    val isReportSheetOpen: Boolean = false,
    val reportReasons: ImmutableSet<MemberReportReason> = persistentSetOf(),
    val reportDetail: String = "",
) : UiState {
    /** 사유를 하나 이상 고르고 상세 내용을 입력해야 신고할 수 있다 */
    val canSubmitReport: Boolean
        get() = !isProcessing && !isMine && reportReasons.isNotEmpty() && reportDetail.isNotBlank() && reportDetail.length <= MEMBER_REPORT_DETAIL_MAX_LENGTH
}

sealed interface MemberProfileEvent : UiEvent {
    data class ShowToast(val message: String, val isSuccess: Boolean) : MemberProfileEvent
}
