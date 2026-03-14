package com.umc.presentation.ui.act.dialog

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.usecase.challenger.AddChallengerPointUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BottomSheetRewardPunishViewModel @Inject constructor(
    private val addChallengerPointUseCase: AddChallengerPointUseCase
) : BaseViewModel<BottomSheetRewardPunishUiState, BottomSheetRewardPunishEvent>(
BottomSheetRewardPunishUiState())
{


    //챌린저 아이디 저장
    fun setChallengerId(challengerId: Long){
        updateState { copy(challengerId = challengerId) }
    }

    //시작 시 상/벌점 모드 체크
    fun setRewardMode(isReward: Boolean){
        updateState { copy(isReward = isReward) }
    }
    
    //벌점 다이얼로그 시 벌점 카테고리 선택
    fun setPunishCategory(category: PunishCategory) {

        val punishList = RewardType.getPenaltyList()
        val resultList = when (category) {
            //운영자
            PunishCategory.ADMIN -> punishList.filter { it.category == PunishCategory.ADMIN }
            //회장단
            PunishCategory.CORE -> punishList.filter { it.category == PunishCategory.CORE }
            //전체
            PunishCategory.ALL -> punishList
        }

        updateState { copy(
            currentFilter = category,
            selectedItem = null,
            ) }
        setRewardList(resultList)
        Log.d("log_reward", "setPunishCategory: ${uiState.value.displayList}")
    }

    //선택한 상벌점 카테고리 선택
    fun setRewardType(item: RewardType) {
        val checkedItem = if (uiState.value.selectedItem == item) null else item
        updateState { copy(selectedItem = checkedItem) }
    }

    //상벌점 리스트 선택
    fun setRewardList(list: List<RewardType>) {
        updateState { copy(displayList = list) }
    }


    //textfield 변경
    fun onTextChanged(text: String) {
        updateState { copy(description = text) }
    }

    //상벌점 제출
    fun submitReward(){
        /**TODO: 수정**/
        val challengerId = uiState.value.challengerId
        val pointType = uiState.value.selectedItem?.name ?: ""
        val pointValue = uiState.value.selectedItem?.score ?: 0
        val description = uiState.value.description

        viewModelScope.launch {
            resultResponse(
                response = addChallengerPointUseCase(challengerId, pointType, pointValue, description),
                successCallback = {
                    emitEvent(BottomSheetRewardPunishEvent.SendSuccess)
                },
                errorCallback = { error ->
                    emitEvent(BottomSheetRewardPunishEvent.SendFail(error.message))
                }
            )
        }
    }

}


data class BottomSheetRewardPunishUiState(
    val challengerId : Long = -1L,

    val isReward : Boolean = true, //상점 or 벌점 모드로 진입했는지 여부(이지선다)
    val currentFilter: PunishCategory = PunishCategory.ALL, //벌점 시 필터링
    val displayList: List<RewardType> = emptyList(), //상/벌점 리스트
    val selectedItem: RewardType? = null, //선택한 거
    val description: String = "", //말말

    ): UiState {
        val submitOk: Boolean = selectedItem != null
}


sealed interface BottomSheetRewardPunishEvent: UiEvent {
    object SendSuccess : BottomSheetRewardPunishEvent
    data class SendFail(val message: String) : BottomSheetRewardPunishEvent
}