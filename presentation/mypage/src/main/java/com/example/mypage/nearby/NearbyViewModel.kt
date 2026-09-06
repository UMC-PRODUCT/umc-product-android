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


/**
 * Nearby Connections 비동기 근거리 통신과 UI 계층(바텀시트, 다이얼로그, 오버레이)을 중계하는 뷰모델 클래스
 *
 * NearbyManager로부터 수신된 이벤트를 매핑하여 주변 기기 리스트(devices) 업데이트,
 * P2P 연결 성공 시 자동 명함(send) 전송, 수신된 카드(receivedCard)의 DataStore 저장 및 교환 성공 오버레이 상태를 관리합니다.
 *
 */
@HiltViewModel
class NearbyViewModel @Inject constructor(
    application: Application,
    private val SaveUserCardUseCase: SaveUserCardUseCase, //명함 저장
) : BaseViewModel<NearbyUiState, NearbyEvent>(NearbyUiState()) {

    // NearbyManager 인스턴스를 생성하고 내부 통신 이벤트를 수신하는 리스너 바인딩
    private val manager = NearbyManager(application) { event ->
        when(event) {
            // 주변 기기 감지 시 중복 체크 후 디바이스 목록에 추가
            is NearbyManagerEvent.EndpointFound -> {
                updateState {
                    //id 기준 중복 제거 후 추가
                    val currentList = devices.filterNot { it.first == event.id }
                    copy(devices = (currentList + (event.id to event.userInfo)).toImmutableList())
                }
            }

            // 자동 연결 승인 시 상태 갱신
            is NearbyManagerEvent.AuthVerification -> {
                updateState { copy(pendingAuth = event) }
            }

            // P2P 연결 성공 시 내 명함(UserCard)을 자동 전송
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
            // 상대방 명함 카드 수신 성공 시 저장 및 오버레이 노출
            is NearbyManagerEvent.UserCardReceived -> {
                updateState {
                    copy(
                        receivedCard = event.card,
                        isBottomSheetOpen = false, // 교환 성공 시 바텀시트 닫기
                        isSuccessOverlayOpen = true // 오버레이 띄우기
                    )
                }

                // 수신받은 상대방 명함을 AppDataStore 로컬 DB에 비동기 저장
                viewModelScope.launch {
                    SaveUserCardUseCase(event.card)
                }

                // 트랜잭션 완료 후 연결 및 소켓 세션 즉시 정리
                stopExchange()
            }

            // 현재 통신 진행 상태 메시지 업데이트
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



    /**
     * 내 프로필 정보(NearbyUserInfo)를 매니저에 전달하기 위해 상태에 설정하는 메서드
     */
    fun setMyUserInfo(userInfo: NearbyUserInfo, ) {
        updateState { copy(myUserInfo = userInfo) }
    }

    /**
     * 내 명함 모델(UserCard)을 전송용 상태(명함)로 설정하는 메서드
     */
    fun setMyUserCard(card: UserCard) {
        updateState { copy(myUserCard = card) }
    }

    /**
     * 명함 교환 바텀시트를 오픈하고 상태값을 초기화하는 메서드
     */
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

    /**
     * 바텀시트를 닫고 진행 중인 통신 자원을 정리하는 메서드
     */
    fun closeBottomSheet() {
        stopExchange()
        updateState { copy(isBottomSheetOpen = false) }
    }

    /**
     * 기기 광고(Advertising) 및 탐색(Discovery)을 동시에 개시하여 P2P 상호 감지를 시작하는 메서드
     */
    fun startAdvertisingAndDiscovery() {
        val myInfo = uiState.value.myUserInfo ?: NearbyUserInfo(name = "실패", info = "Connect 실패")

        updateState { copy(exchangeStep = ExchangeStep.DISCOVER_USERS, devices = persistentListOf()) }
        manager.startAdvertisingAndDiscovery(myInfo)
    }

    /**
     * 발견된 기기 목록 중 사용자가 클릭한 타겟 기기를 전송 확인 다이얼로그용 상태로 지정하는 메서드
     */
    fun selectTargetUser(target: Pair<String, NearbyUserInfo>?) {
        updateState { copy(selectedTargetUser = target) }
    }

    /**
     * 전송 확인 팝업에서 '전송하기' 클릭 시 선택된 타겟 디바이스로 연결 요청(requestConnection)을 수행하는 메서드
     */
    fun confirmAndConnect() {
        val target = uiState.value.selectedTargetUser ?: return
        val targetId = target.first
        val targetName = target.second.name

        updateState { copy(selectedTargetUser = null) } // 다이얼로그 닫기
        //emitEvent(NearbyEvent.ShowToast("${targetName}님에게 연결을 요청합니다..."))

        requestConnection(targetId)
    }

    /**
     * 교환 성공 오버레이를 닫고 내 명함 화면으로 완전히 되돌아가는 메서드
     */
    fun dismissSuccessOverlay() {
        updateState {
            copy(
                isSuccessOverlayOpen = false,
                receivedCard = null
            )
        }
    }

    /**
     * 교환 성공 오버레이에서 '계속 교환하기' 클릭 시 성공 화면을 닫고 즉시 탐색 모드를 재가동하는 메서드
     */
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



    /**
     * 수동 기기 광고 시작 메서드
     */
    fun startAdvertising(userInfo: NearbyUserInfo) {
        manager.startAdvertising(userInfo)
    }

    /**
     * 수동 1:N 유저 스캔 탐색 시작 메서드
     */
    fun startDiscovery() {
        manager.startDiscovery()
    }

    /**
     * 지정한 endpointId로 소켓 연결을 요청하는 메서드
     */
    fun requestConnection(id: String) {
        manager.requestConnection(id)
    }

    /**
     * 수신된 연결 요청을 승인하는 메서드
     * (현재는 NearbyManger 내부에서 자동 인증되도록 수정)
     */
    fun accept(id: String) {
        manager.acceptConnection(id)
    }

    /**
     * 명함 카드 데이터를 상대 기기로 발송하는 메서드
     */
    fun send(id: String, card: UserCard) {
        manager.sendUserCard(id, card)
    }

    /**
     * 뷰모델 소멸 시 근거리 통신 세션 및 백그라운드 스캔을 안전하게 전면 해제하는 메서드
     */
    override fun onCleared() {
        super.onCleared()
        manager.stopAll() // 연결 종료 추가
    }

    /**
     * 진행 중인 탐색 및 광고를 중단하고 선택 상태를 리셋하는 메서드
     */
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