package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserChallengerViewModel @Inject constructor(
    private val getChallengerDetailUseCase: GetChallengerDetailUseCase
) : BaseViewModel<UserChallengerUiState, UserChallengerEvent>(UserChallengerUiState()) {

    init { loadInitialData() }

    private fun loadInitialData() {
        val dummyList = listOf(
            // PM 파트
            UserChallenger(101, "김유엠", "유엠씨대장", 12, UserPart.PM, UserChallengerRole.LEADER),
            UserChallenger(102, "이길동", "피엠조아", 12, UserPart.PM, UserChallengerRole.MEMBER),

            // Design 파트
            UserChallenger(3, "박디자인", "피그마마스터", 12, UserPart.DESIGN, UserChallengerRole.PART_LEADER),
            UserChallenger(4, "최화가", "캔바장인", 12, UserPart.DESIGN, UserChallengerRole.MEMBER),

            // Web 파트
            UserChallenger(5, "정웹", "리액트왕", 12, UserPart.WEB, UserChallengerRole.PART_LEADER),
            UserChallenger(6, "한자바", "뷰스크립트", 12, UserPart.WEB, UserChallengerRole.MEMBER),
            UserChallenger(7, "고쿼리", "넥스트제이에스", 12, UserPart.WEB, UserChallengerRole.MEMBER),

            // iOS 파트
            UserChallenger(8, "임애플", "스위프트", 12, UserPart.IOS, UserChallengerRole.SUB_LEADER),
            UserChallenger(9, "강아이", "엑스코드", 12, UserPart.IOS, UserChallengerRole.MEMBER),

            // Android 파트
            UserChallenger(10, "조안드", "코틀린", 12, UserPart.ANDROID, UserChallengerRole.PART_LEADER),
            UserChallenger(11, "권젯팩", "컴포즈", 12, UserPart.ANDROID, UserChallengerRole.MEMBER),
            UserChallenger(12, "성구글", "픽셀러버", 12, UserPart.ANDROID, UserChallengerRole.MEMBER),

            // SpringBoot 파트
            UserChallenger(13, "배엔드", "스프링", 12, UserPart.SPRING_BOOT, UserChallengerRole.PART_LEADER),
            UserChallenger(14, "백자바", "제이피에이", 12, UserPart.SPRING_BOOT, UserChallengerRole.MEMBER),
        UserChallenger(15, "유디비", "마이바티스", 12, UserPart.SPRING_BOOT, UserChallengerRole.MEMBER),

            // Node.js 파트
            UserChallenger(16, "노드정", "익스프레스", 12, UserPart.NODE_JS, UserChallengerRole.PART_LEADER),
            UserChallenger(17, "신서버", "네스트", 12, UserPart.NODE_JS, UserChallengerRole.MEMBER)
        )
        updateState { copy(allChallengers = dummyList, filteredChallengers = dummyList) }
    }

    fun filterList(query: String) {
        val filtered = uiState.value.allChallengers.filter {
            it.name.contains(query) || it.nickname.contains(query)
        }
        updateState { copy(filteredChallengers = filtered) }
    }

    fun navigateToDetail(id: Int) {
        viewModelScope.launch {
            when (val result = getChallengerDetailUseCase(id.toLong())) {
                is ApiState.Success -> {
                    emitEvent(UserChallengerEvent.NavigateToDetail(result.data))
                }
                is ApiState.Fail -> {
                    emitEvent(UserChallengerEvent.ShowErrorToast(result.failState.message))
                }
            }
        }
    }
}

data class UserChallengerUiState(
    val allChallengers: List<UserChallenger> = emptyList(),
    val filteredChallengers: List<UserChallenger> = emptyList()
) : UiState

sealed interface UserChallengerEvent : UiEvent {
    data class NavigateToDetail(val model: ChallengerInfoDialogModel) : UserChallengerEvent
    data class ShowErrorToast(val message: String) : UserChallengerEvent
}