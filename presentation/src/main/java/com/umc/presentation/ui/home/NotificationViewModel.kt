package com.umc.presentation.ui.home

import com.umc.domain.model.home.NotificationItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class NotificationViewModel @Inject
constructor() : BaseViewModel<NotificationFragmentUiState, NotificationFragmentEvent>(
    NotificationFragmentUiState()){

    init {
        updateState {
            copy(
                notifications = listOf(
                    NotificationItem( "중앙 해커톤 참전!", "축하합니다. 해커톤 본선 진출자로 선정되셨습니다.", "12시간 전"),
                    NotificationItem( "아이디어톤 결과 안내", "아이디어톤에 참여해주셔서 감사합니다. 결과를 확인하세요. 너는 탈락인가요? 하하하하하하하하하핳하", "1일 전"),
                    NotificationItem( "데모데이 일정 알림", "데모데이가 이번 주 일요일 오후 1시에 진행됩니다.", "2일 전")
                )
            )
        }
    }

    //뒤로가기
    fun onClickBackPressed(){
        emitEvent(NotificationFragmentEvent.MoveBackPressedEvent)
    }

    /**여기서 서버에서 가져오는 데이터 정의**/
    private fun getNotifications(){
        updateState {
            copy()
        }
    }

}


data class NotificationFragmentUiState(
    val dummyData: String = "",

    //일정 리스트에 들어갈 리스트들
    /**TODO. RoomDB에서 가져올 것이에요**/
    val notifications : List<NotificationItem> = emptyList()

) : UiState

sealed class NotificationFragmentEvent : UiEvent {
    object MoveBackPressedEvent : NotificationFragmentEvent()
}