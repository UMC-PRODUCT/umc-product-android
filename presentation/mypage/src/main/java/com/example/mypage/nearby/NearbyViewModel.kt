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

    //manager 초기화
    private val manager = NearbyManager(application) { event ->
        //ManagerEvent를 받아 viewModel 값 업데이트
        when(event) {
            //기기 찾으면
            is NearbyManagerEvent.EndpointFound -> {
                updateState { copy(devices = devices.distinctBy { it.first } + (event.id to event.name)) }
            }
            //인증 과정(현재는 자동)
            is NearbyManagerEvent.AuthVerification -> {
                updateState { copy(pendingAuth = event) }
            }
            //연결 설공 시(인증 성공)
            is NearbyManagerEvent.ConnectionSuccess -> {
                updateState { copy(connectedId = event.id, pendingAuth = null, status = "연결 완료") }
                emitEvent(NearbyEvent.ShowToast("연결에 성공했습니다."))
            }
            //데이터 받을 경우
            is NearbyManagerEvent.UserCardReceived -> {
                updateState { copy(receivedCard = event.card) }
                emitEvent(NearbyEvent.ShowToast("유저 카드를 수신했습니다."))
            }
            //현재 상태 업데이트
            is NearbyManagerEvent.StatusUpdate -> {
                updateState { copy(status = event.message) }
                emitEvent(NearbyEvent.ShowToast(event.message))
            }
            //에러
            is NearbyManagerEvent.Error -> {
                updateState { copy(status = event.message) }
                emitEvent(NearbyEvent.ShowToast(event.message))
            }
        }
    }

    //기기 광고 시작
    fun startAdvertising() {
        manager.startAdvertising()
    }

    //1:N 유저 탐색
    fun startDiscovery() {
        manager.startDiscovery()
    }

    //연결 시도
    fun requestConnection(id: String) {
        manager.requestConnection(id)
    }

    //인증 후 연결 승인 (현재는 NearbyManger 내부에서 자동 인증되도록 수정)
    fun accept(id: String) {
        manager.acceptConnection(id)
    }

    //값 전송
    fun send(id: String, card: UserCard) {
        manager.sendUserCard(id, card)
    }

    //리셋
    override fun onCleared() {
        super.onCleared()
        manager.stopAll() // 연결 종료 추가
    }

}

data class NearbyUiState(
    val devices: List<Pair<String, String>> = emptyList(), //탐지된 기기명 <구글 통신 API 고유 주소, 기기명>
    val pendingAuth: NearbyManagerEvent.AuthVerification? = null,
    val connectedId: String? = null,
    val receivedCard: UserCard? = null,
    val status: String = "대기 중"
) : UiState

sealed interface NearbyEvent : UiEvent {
    data class ShowToast(val message: String) : NearbyEvent
}