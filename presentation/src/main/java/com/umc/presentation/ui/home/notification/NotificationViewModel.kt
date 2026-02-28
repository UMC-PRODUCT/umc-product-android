package com.umc.presentation.ui.home.notification

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.home.NotificationItem
import com.umc.domain.usecase.appDataStore.notification.GetNotificationsUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotificationViewModel @Inject
constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : BaseViewModel<NotificationFragmentUiState, NotificationFragmentEvent>(
    NotificationFragmentUiState()
) {

    init {
        loadNotifications()
    }

    // DataStore에서 알림 목록 로드
    private fun loadNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase().collectLatest { notifications ->
                updateState {
                    copy(notifications = notifications)
                }
            }
        }
    }

    //뒤로가기
    fun onClickBackPressed() {
        emitEvent(NotificationFragmentEvent.MoveBackPressedEvent)
    }

}


data class NotificationFragmentUiState(
    val dummyData: String = "",

    // 알림 리스트 (DataStore에서 가져옴)
    val notifications: List<NotificationItem> = emptyList()

) : UiState

sealed class NotificationFragmentEvent : UiEvent {
    object MoveBackPressedEvent : NotificationFragmentEvent()
}
