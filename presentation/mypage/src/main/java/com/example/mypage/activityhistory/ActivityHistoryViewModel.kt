package com.example.mypage.activityhistory

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.example.mypage.mypage.CODE_ALREADY_USED
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val activityHistoryBuilder: ActivityHistoryBuilder, //활동 이력 목록 만들기 (타인 프로필과 공용)
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase, //챌린저 기록 추가 (6자리 코드)
) : BaseViewModel<ActivityHistoryUiState, ActivityHistoryEvent>(
    ActivityHistoryUiState()
) {

    init {
        loadActivityHistory()
    }

    /**
     * 조회 실패 안내에서 '다시 시도하기'를 눌렀을 때 호출
     */
    fun onClickRetry() {
        //재시도 요청이 진행 중이면 중복 호출하지 않는다
        if (uiState.value.isLoading) return
        loadActivityHistory()
    }

    /**
     * 서버에서 내 프로필 정보를 받아 활동 이력을 만드는 메서드
     */
    private fun loadActivityHistory() {
        // 재시도 중에는 이전 실패 안내 대신 진행 표시를 보이고, 다시 실패하면 errorCallback 이 실패 상태로 되돌린다
        updateState { copy(isLoading = true, isLoadFailed = false) }
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    viewModelScope.launch {
                        val histories = activityHistoryBuilder.build(userInfo)
                        //모든 데이터가 수집된 후 UI 업데이트
                        updateState {
                            copy(
                                histories = histories.toImmutableList(),
                                isLoading = false,
                                isLoadFailed = false,
                            )
                        }
                    }
                },
                errorCallback = {
                    updateState { copy(isLoading = false, isLoadFailed = true) }
                }
            )
        }
    }

    // ---------------- 챌린저 기록 추가 (운영진에게 받은 6자리 코드) ----------------

    fun onClickAddRecord() = updateState { copy(isAddCodeSheetOpen = true) }

    fun onCodeChanged(code: String) = updateState { copy(code = code) }

    /** 시트를 닫으면 입력도 비운다 (ViewModel 이 살아 있어 다시 열면 이전 입력이 남는다) */
    fun onDismissAddCode() = updateState { copy(isAddCodeSheetOpen = false, code = "") }

    fun onDismissUsedCodeDialog() = updateState { copy(isUsedCodeDialogOpen = false) }

    /**
     * 입력한 코드로 내 계정에 챌린저 기록을 추가하는 메서드.
     * 성공하면 활동 이력을 다시 불러와 새 기록을 바로 보여준다
     */
    fun addChallengerCode() {
        // 요청 진행 중에는 다시 보내지 않는다(연타하면 같은 코드가 중복 전송된다)
        if (uiState.value.isAddingCode) return
        updateState { copy(isAddingCode = true) }

        viewModelScope.launch {
            resultResponse(
                response = addChallengerRecordMemberUseCase(ChallengerRecordMemberRequest(code = uiState.value.code)),
                successCallback = {
                    updateState { copy(isAddingCode = false, isAddCodeSheetOpen = false, code = "") }
                    emitEvent(ActivityHistoryEvent.RecordAdded)
                    loadActivityHistory()
                },
                errorCallback = { failState ->
                    updateState { copy(isAddingCode = false, isAddCodeSheetOpen = false) }
                    if (failState.code == CODE_ALREADY_USED) {
                        // 이미 사용된 코드는 다시 입력해도 소용없으므로 문의 채널을 안내한다
                        updateState { copy(isUsedCodeDialogOpen = true) }
                    } else {
                        emitEvent(ActivityHistoryEvent.ShowToast(failState.message))
                    }
                }
            )
        }
    }
}

data class ActivityHistoryUiState(
    val histories: ImmutableList<UserActiveItem> = persistentListOf(),
    val isLoading: Boolean = false, //내 정보 조회와 이력 가공이 끝나기 전까지
    // 내 정보 조회 실패 여부 (true 면 빈 목록 대신 실패 안내 + 다시 시도 표시)
    val isLoadFailed: Boolean = false,

    // 챌린저 기록 추가
    val isAddCodeSheetOpen: Boolean = false,
    val code: String = "",
    val isAddingCode: Boolean = false, //코드 추가 요청 진행 중
    val isUsedCodeDialogOpen: Boolean = false, //이미 사용된 코드 안내
) : UiState

sealed interface ActivityHistoryEvent : UiEvent {
    object RecordAdded : ActivityHistoryEvent //기록 추가 성공
    data class ShowToast(val message: String) : ActivityHistoryEvent
}
