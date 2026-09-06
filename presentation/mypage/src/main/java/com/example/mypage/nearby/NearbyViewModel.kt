package com.example.mypage.nearby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypage.dialog.ExchangeStep
import com.example.mypage.nearby.NearbyManagerEvent
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.NearbyUserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.usecase.appDataStore.usercard.SaveUserCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    application: Application,
    private val SaveUserCardUseCase: SaveUserCardUseCase, //명함 저장
) : BaseViewModel<NearbyUiState, NearbyEvent>(NearbyUiState()) {

    //manager 초기화
    private val manager = NearbyManager(application) { event ->
        //ManagerEvent를 받아 viewModel 값 업데이트
        when(event) {
            //기기 찾으면
            is NearbyManagerEvent.EndpointFound -> {
                updateState {
                    //id 기준 중복 제거 후 추가
                    val currentList = devices.filterNot { it.first == event.id }
                    copy(devices = (currentList + (event.id to event.userInfo)).toImmutableList())
                }
            }
            //인증 과정(현재는 자동)
            is NearbyManagerEvent.AuthVerification -> {
                updateState { copy(pendingAuth = event) }
            }
            //연결 설공 시(인증 성공) -> 자동 카드 전송
            is NearbyManagerEvent.ConnectionSuccess -> {
                updateState { copy(connectedId = event.id, pendingAuth = null, status = "연결 완료") }
                emitEvent(NearbyEvent.ShowToast("연결에 성공했습니다."))

                //전송 로직
                /**
                 * 중요!!!!!
                 * 현재 내 userInfo에서 필요한 정보만 추출해 nearUserInfo(전송용도)로 잡고
                 * 그 후에 UserCard에 데이터를 넣음
                 * 
                 * 즉, 차후 UserCard 데이터 변경에 따라 UserCard랑 nearUserInfo 수정 필요
                 * 
                 * **/
                /**차후 UserCard 데이터 변경 시 적용**/

                send(event.id, uiState.value.myUserCard ?: UserCard())


            }
            //카드 받을 경우
            is NearbyManagerEvent.UserCardReceived -> {
                updateState {
                    copy(
                        receivedCard = event.card,
                        isBottomSheetOpen = false, // 교환 성공 시 바텀시트 닫기
                        isSuccessOverlayOpen = true // 오버레이 띄우기
                    )
                }

                // AppDataStore에 카드 저장
                viewModelScope.launch {
                    SaveUserCardUseCase(event.card)
                }

                stopExchange() // 통신 완료 후 연결 및 스캔 종료
            }
            //현재 상태 업데이트
            is NearbyManagerEvent.StatusUpdate -> {
                updateState { copy(status = event.message) }
                //emitEvent(NearbyEvent.ShowToast(event.message))
            }
            //에러
            is NearbyManagerEvent.Error -> {
                updateState { copy(status = event.message) }
                emitEvent(NearbyEvent.ShowToast(event.message))
            }
        }
    }

    

    // 내 기본 정보 세팅 (화면 진입 시 호출)
    fun setMyUserInfo(userInfo: NearbyUserInfo, ) {
        updateState { copy(myUserInfo = userInfo) }
    }

    // 내 유저 카드 받아오기 (화면 진입 시 호출)
    fun setMyUserCard(card: UserCard) {
        updateState { copy(myUserCard = card) }
    }

    // 바텀시트 열기/닫기 제어
    fun openBottomSheet() {
        updateState {
            copy(
                isBottomSheetOpen = true,
                exchangeStep = ExchangeStep.SELECT_METHOD,
                devices = persistentListOf(),
                selectedTargetUser = null
            )
        }
    }

    fun closeBottomSheet() {
        stopExchange()
        updateState { copy(isBottomSheetOpen = false) }
    }

    //기기 광고 및 탐색 동시 시작
    fun startAdvertisingAndDiscovery() {
        val myInfo = uiState.value.myUserInfo ?: NearbyUserInfo(name = "실패", info = "Connect 실패")

        updateState { copy(exchangeStep = ExchangeStep.DISCOVER_USERS, devices = persistentListOf()) }
        manager.startAdvertisingAndDiscovery(myInfo)
    }

    /**다이얼로그와 관련된 데이터 다듬기(CardExchangeBottomSheet)**/
    //유저 목록에서 특정 멤버 클릭 (다이얼로그 팝업 준비)
    fun selectTargetUser(target: Pair<String, NearbyUserInfo>?) {
        updateState { copy(selectedTargetUser = target) }
    }

    //확인 다이얼로그 '전송하기' 클릭 시 실제 연결 시도
    fun confirmAndConnect() {
        val target = uiState.value.selectedTargetUser ?: return
        val targetId = target.first
        val targetName = target.second.name

        updateState { copy(selectedTargetUser = null) } // 다이얼로그 닫기
        //emitEvent(NearbyEvent.ShowToast("${targetName}님에게 연결을 요청합니다..."))

        requestConnection(targetId)
    }

    //확인 버튼 클릭 -> 성공 오버레이 감추고 MycardScreen으로 완전히 돌아감
    fun dismissSuccessOverlay() {
        updateState {
            copy(
                isSuccessOverlayOpen = false,
                receivedCard = null
            )
        }
    }

    //계속 교환하기 버튼 클릭 -> 성공 오버레이를 닫고 다시 탐색 다이얼로그(DISCOVER_USERS) 오픈!
    fun continueExchange() {
        updateState {
            copy(
                isSuccessOverlayOpen = false,
                receivedCard = null
            )
        }
        // 다시 Wi-Fi Aware 고속 교환 탐색 시작!
        startAdvertisingAndDiscovery()
        updateState { copy(isBottomSheetOpen = true) }
    }

    

    //기기 광고 시작
    fun startAdvertising(userInfo: NearbyUserInfo) {
        manager.startAdvertising(userInfo)
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

    //교환 종료
    fun stopExchange() {
        manager.stopAll()
        updateState {
            copy(
                exchangeStep = ExchangeStep.SELECT_METHOD,
                devices = persistentListOf(),
                selectedTargetUser = null
            )
        }
    }

}

data class NearbyUiState(
    val myUserCard : UserCard? = null, //내 Usercard
    val myUserInfo: NearbyUserInfo? = null, // 보여주기 용 내 정보
    val isBottomSheetOpen: Boolean = false, // botoomsheet 열림 여부
    val isSuccessOverlayOpen: Boolean = false,
    val exchangeStep: ExchangeStep = ExchangeStep.SELECT_METHOD, //현재 단계

    //<구글 고유 EndpointId, NearbyUserInfo>
    val devices: ImmutableList<Pair<String, NearbyUserInfo>> = persistentListOf(),
    val selectedTargetUser: Pair<String, NearbyUserInfo>? = null, // 다이얼로그용 선택 유저
    val pendingAuth: NearbyManagerEvent.AuthVerification? = null,

    val connectedId: String? = null,
    val receivedCard: UserCard? = null,
    val status: String = "대기 중"
) : UiState

sealed interface NearbyEvent : UiEvent {
    data class ShowToast(val message: String) : NearbyEvent
}