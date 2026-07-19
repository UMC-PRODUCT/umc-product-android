package com.example.mypage.nearby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mypage.nearby.NearbyManagerEvent
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.mypage.UserCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    application: Application
) : BaseViewModel<NearbyUiState, NearbyEvent>(NearbyUiState()) {

    // manager 초기화 (context가 필요하므로 생성자에서 application 받음)
    private val manager = NearbyManager(application) { event ->
        // ManagerEvent를 받아 viewModel 값 업데이트
        when(event) {

            is NearbyManagerEvent.EndpointFound -> {
                updateState { copy(devices = devices.distinctBy { it.first } + (event.id to event.name)) }
            }
            is NearbyManagerEvent.AuthVerification -> {
                updateState { copy(pendingAuth = event) }
            }
            is NearbyManagerEvent.ConnectionSuccess -> {
                updateState { copy(connectedId = event.id, pendingAuth = null) }
            }
            is NearbyManagerEvent.UserCardReceived -> {
                updateState { copy(receivedCard = event.card) }
            }
        }
    }

    // 1:N 유저 탐색
    fun startDiscovery() {
        startLoading() // BaseViewModel의 로딩 기능 사용
        manager.startDiscovery()
        stopLoading()
    }

    // 연결 시도
    fun requestConnection(id: String) {
        manager.requestConnection(id)
    }

    // 인증 후 연결 승인
    fun accept(id: String) {
        manager.acceptConnection(id)
    }

    // 값 전송
    fun send(id: String, card: UserCard) {
        manager.sendUserCard(id, card)
    }

    override fun onCleared() {
        super.onCleared()
        manager.stopAll() // 연결 종료 추가
    }

}

data class NearbyUiState(
    val devices: List<Pair<String, String>> = emptyList(),
    val pendingAuth: NearbyManagerEvent.AuthVerification? = null,
    val connectedId: String? = null,
    val receivedCard: UserCard? = null,
    val status: String = "대기 중"
) : UiState

sealed interface NearbyEvent : UiEvent {
    data class ShowToast(val message: String) : NearbyEvent
    // 필요 시 추가
}