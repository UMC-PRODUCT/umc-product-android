package com.example.mypage.qrcode

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.viewModelScope
import com.example.mypage.nearby.NearbyManager
import com.example.mypage.nearby.NearbyManagerEvent
import com.example.mypage.qrcode.QrCodeUtils
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QR CODE + NearbyConnection을 이용한 연결
 * QR code에 기기값을 넣고, 해당 코드를 스캔 시, nearbyconnection에서 기기 필터링을 진행
 * 그 이후는 블루투스/wifi를 통한 연결
 *
 * **/

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val application: Application,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<QrCodeUiState, QrCodeEvent>(QrCodeUiState()) {

    //nearbyConnection
    //private var nearbyManager: NearbyManager? = null

    //private var localEndpointName = Build.MODEL

    init {
        getUserProfile()
    }

    //내 프로필 정보 로드 및 Nearby Advertising 시작 (QR 코드가 뜬 시점에서 광고 시작)
    private fun getUserProfile() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState { copy(userInfo = userInfo) }
                    //initNearbyAdvertising()
                    generateMyUserCardQr(userInfo)
                },
                errorCallback = {
                    emitEvent(QrCodeEvent.ShowToast("프로필 정보를 불러오지 못했습니다."))
                    //initNearbyAdvertising()
                    generateMyUserCardQr(null)
                }
            )
            stopLoading()
        }
    }

    /**
     * 내 프로필 정보를 바탕으로 UserCard 객체를 생성하고, 이를 JSON으로 직렬화하여 QR 데이터(myEndpointId)로 설정
     */
    private fun generateMyUserCardQr(userInfo: UserInfo?) {
        val myCard = UserCard(
            /**테스트 데이터**/
            name = userInfo?.name?.ifEmpty { "박유수" } ?: "박유수",
            nickname = userInfo?.nickname?.ifEmpty { "어헛차" } ?: "어헛차"
        )
        // QR 코드 인코딩용 JSON 데이터 생성
        val qrJsonContent = myCard.toJson()
        updateState { copy(myQrcodeData = qrJsonContent) }

    }
    


    //수신 상태 초기화 - 수신 다이얼로그 클릭 시 스캔한 데이터 초기화
    fun clearReceivedCard() {
        updateState {
            copy(
                receivedCard = null,
                scannedTargetQr = "" // 다음 스캔을 위해 초기화
            )
        }

        //다음 사람과의 교환을 위해 Advertising 상태 복구
        //nearbyManager?.startAdvertising()
    }

    /*
    //Nearby Advertising 시작 및 콜백 정의
    fun initNearbyAdvertising() {

        //QR 코드 안에 들어갈 내용 = 기기 모델명 (인코딩 깨짐 방지)
        val qrContent = "${localEndpointName}"
        updateState { copy(myEndpointId = qrContent) }

        if (nearbyManager == null) {
            nearbyManager = NearbyManager(application) { event ->
                when (event) {
                    is NearbyManagerEvent.EndpointFound -> {
                        //발견된 기기 목록 업데이트
                        updateState { copy(discoveredDevices = discoveredDevices.distinctBy { it.first } + (event.id to event.name)) }

                        //만약 내가 스캔한 QR의 식별자가 이 기기의 이름과 일치하면 즉시 자동 연결 시도!
                        val targetQr = uiState.value.scannedTargetQr
                        if (targetQr.isNotEmpty() && event.name.contains(targetQr)) {
                            nearbyManager?.requestConnection(event.id)
                        }
                    }
                    is NearbyManagerEvent.ConnectionSuccess -> {
                        //연결 성공 시 내 명함 카드 자동 전송 (양방향)
                        emitEvent(QrCodeEvent.ShowToast("기기 연결 성공! 내 명함을 전송합니다."))
                        updateState { copy(connectedEndpointId = event.id) }
                        sendUserCard(event.id)
                    }
                    is NearbyManagerEvent.UserCardReceived -> {
                        //명함 수신 시 UI State에 저정하여 다이얼로그 팝업 노출
                        updateState { copy(receivedCard = event.card) }
                        emitEvent(QrCodeEvent.ShowToast("🎉 ${event.card.name}님의 명함을 수신했습니다!"))
                    }
                    is NearbyManagerEvent.Error -> {
                        emitEvent(QrCodeEvent.ShowToast(event.message))
                    }
                    else -> {}
                }
            }
        }

        // 광고 시작
        nearbyManager?.startAdvertising()
    }

     */

    /**
     * 스캐너 열기 (내 광고 중단 후 상대 탐색 시작)
     */
    fun startScanner() {
        if (uiState.value.isScannerOpen) return
        //nearbyManager?.stopAdvertising()
        //nearbyManager?.startDiscovery()

        updateState { copy(isScannerOpen = true) }
    }

    fun closeScanner() {
        updateState { copy(isScannerOpen = false) }
    }

    /**
     * CameraX로 상대방 QR 스캔 완료 시 실행되는 메서드
     * @param scannedValue 스캔된 문자열 (상대 기기의 Build.MODEL)
     */
    fun onQrScanned(scannedValue: String) {
        Log.d("NearbyDebug", "0. QR 스캔 완료! 읽은 텍스트: '$scannedValue'")
        updateState {
            copy(
                scannedTargetQr = scannedValue,
                isScannerOpen = false
            )
        }

        //그냥 json을 읽은 다음 역직렬화
        try {
            //QR 데이터(JSON) -> UserCard 객체 직접 파싱
            val card = UserCard.fromJson(scannedValue)

            //파싱 성공 시 별도 네트워크/블루투스 연결 없이 즉시 UI State 반영 -> UDialog 팝업 뜸!
            updateState { copy(receivedCard = card) }
            emitEvent(QrCodeEvent.ShowToast("${card.name}님의 명함을 읽어왔습니다!"))
        } catch (e: Exception) {
            Log.e("QrScanDebug", "UserCard 파싱 실패: ${e.message}")
            emitEvent(QrCodeEvent.ShowToast("유효하지 않은 명함 QR 코드입니다."))
        }


        /*
        val currentDevices = uiState.value.discoveredDevices
        Log.d("NearbyDebug", "0-1. 현재 발견되어 있는 기기 목록: $currentDevices")

        emitEvent(QrCodeEvent.ShowToast("QR 스캔 완료! 주변 기기를 탐색해 연결합니다."))

        //이미 기존에 발견된 기기 목록에 스캔한 대상이 있다면 즉시 연결 요청
        val targetDevice = uiState.value.discoveredDevices.find { it.second.contains(scannedValue) }
        if (targetDevice != null) {
            nearbyManager?.requestConnection(targetDevice.first)
        }

         */
    }

    /*
    private fun sendUserCard(endpointId: String) {
        val userCard = UserCard(
            name = uiState.value.userInfo.name.ifEmpty { "박유수" },
            nickname = uiState.value.userInfo.nickname.ifEmpty { "어헛차" }
        )
        nearbyManager?.sendUserCard(endpointId, userCard)
    }

     */

    fun navigateBack() {
        emitEvent(QrCodeEvent.NavigateBack)
    }

    fun saveQrImage(bitmap: ImageBitmap?) {
        if (bitmap == null) return
        val success = QrCodeUtils.saveImageToGallery(application, bitmap, "MyUserCard_QR")
        if (success) {
            emitEvent(QrCodeEvent.ShowToast("갤러리에 저장되었습니다."))
        } else {
            emitEvent(QrCodeEvent.ShowToast("저장에 실패했습니다."))
        }
    }

    fun shareQrCode() {
        emitEvent(QrCodeEvent.ShareQrCode)
    }

    override fun onCleared() {
        super.onCleared()
        //nearbyManager?.stopAll()
    }
}

data class QrCodeUiState(
    val userInfo: UserInfo = UserInfo(),
    val nickname: String = "",
    val myQrcodeData: String = "",
    //val myEndpointId: String = "", //qrContent 값과 동일 = 기기 모델명 (필터링을 위한) -> UserCard 값
    val scannedTargetQr: String = "", //스캔한 qr코드의 값 = 기기 모델명 or ""
    val isScannerOpen: Boolean = false, //스캐너(카메라)가 열렸는지 확인
    //val discoveredDevices: List<Pair<String, String>> = emptyList(), //확인한 디바이스들로 (구글 API 연결 주소값, 기기명) 이 pair로 이루어짐
    val receivedCard: UserCard? = null,
    //val connectedEndpointId: String? = null //현재 연결된 구글 통신 API 주소값
) : UiState

sealed interface QrCodeEvent : UiEvent {
    object NavigateBack : QrCodeEvent
    object ShareQrCode : QrCodeEvent
    data class ShowToast(val message: String) : QrCodeEvent
}