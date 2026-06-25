package com.umc.presentation.home.notification

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.NotificationItem
import com.umc.domain.usecase.appDataStore.notification.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : BaseViewModel<NotificationUiState, NotificationEvent>(
    NotificationUiState()
)
{
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
    /*
    * listOf(
                            NotificationItem(
                                title = "공지사항",
                                content = "공지사항 내용",
                                date = "2023.08.08"
                            ),
                            NotificationItem(
                                title = "공지사항",
                                content = "공지사항 내용",
                                date = "2023.08.08"
                            ),
                            NotificationItem(
                                title = "공지사항",
                                content = "공지사항 내용",
                                date = "2023.08.08"
                            ),
                        )
    *
    * */

    //뒤로가기
    fun onClickBackPressed() {
        emitEvent(NotificationEvent.MoveBackPressedEvent)
    }

}


data class NotificationUiState(
    val dummyData: String = "",

    // 알림 리스트 (DataStore에서 가져옴)
    val notifications: List<NotificationItem> = emptyList()

) : UiState

sealed class NotificationEvent : UiEvent {
    object MoveBackPressedEvent : NotificationEvent()
}