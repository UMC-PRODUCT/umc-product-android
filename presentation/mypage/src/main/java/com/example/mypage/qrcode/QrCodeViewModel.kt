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

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val application: Application,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<QrCodeUiState, QrCodeEvent>(QrCodeUiState()) {

    //nearbyConnection
    private var nearbyManager: NearbyManager? = null

    private var localEndpointName = Build.MODEL

    init {
        getUserProfile()
    }

    //내 프로필 정보 로드 및 Nearby Advertising 시작
    private fun getUserProfile() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState { copy(userInfo = userInfo) }
                    initNearbyAdvertising()
                },
                errorCallback = {
                    emitEvent(QrCodeEvent.ShowToast("프로필 정보를 불러오지 못했습니다."))
                    initNearbyAdvertising()
                }
            )
            stopLoading()
        }
    }

    //수신 상태 초기화
    fun clearReceivedCard() {
        updateState {
            copy(
                receivedCard = null,
                scannedTargetQr = "" // 다음 스캔을 위해 초기화
            )
        }

        // 다음 사람과의 교환을 위해 Advertising 상태 복구
        nearbyManager?.startAdvertising()
    }

    //Nearby Advertising 시작 및 내 Endpoint ID 발급
    fun initNearbyAdvertising() {

        val qrContent = "${localEndpointName}"
        updateState { copy(myEndpointId = qrContent) }

        if (nearbyManager == null) {
            nearbyManager = NearbyManager(application) { event ->
                when (event) {
                    is NearbyManagerEvent.EndpointFound -> {
                        // 발견된 기기 목록 업데이트
                        updateState { copy(discoveredDevices = discoveredDevices.distinctBy { it.first } + (event.id to event.name)) }

                        // 만약 내가 스캔한 QR의 식별자가 이 기기의 이름과 일치하면 즉시 자동 연결 시도!
                        val targetQr = uiState.value.scannedTargetQr
                        if (targetQr.isNotEmpty() && event.name.contains(targetQr)) {
                            nearbyManager?.requestConnection(event.id)
                        }
                    }
                    is NearbyManagerEvent.ConnectionSuccess -> {
                        // 연결 성공 시 내 명함 카드 자동 전송
                        emitEvent(QrCodeEvent.ShowToast("기기 연결 성공! 내 명함을 전송합니다."))
                        updateState { copy(connectedEndpointId = event.id) }
                        sendUserCard(event.id)
                    }
                    is NearbyManagerEvent.UserCardReceived -> {
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

    // 스캐너 열기 (Discovery 시작)
    fun startScanner() {
        if (uiState.value.isScannerOpen) return
        nearbyManager?.stopAdvertising()
        nearbyManager?.startDiscovery()

        updateState { copy(isScannerOpen = true) }
    }

    fun closeScanner() {
        updateState { copy(isScannerOpen = false) }
    }

    // QR 스캔 성공 시
    fun onQrScanned(scannedValue: String) {
        Log.d("NearbyDebug", "0. QR 스캔 완료! 읽은 텍스트: '$scannedValue'")
        updateState {
            copy(
                scannedTargetQr = scannedValue,
                isScannerOpen = false
            )
        }
        val currentDevices = uiState.value.discoveredDevices
        Log.d("NearbyDebug", "0-1. 현재 발견되어 있는 기기 목록: $currentDevices")

        emitEvent(QrCodeEvent.ShowToast("QR 스캔 완료! 주변 기기를 탐색해 연결합니다."))

        val targetDevice = uiState.value.discoveredDevices.find { it.second.contains(scannedValue) }
        if (targetDevice != null) {
            nearbyManager?.requestConnection(targetDevice.first)
        }
    }

    private fun sendUserCard(endpointId: String) {
        val userCard = UserCard(
            name = uiState.value.userInfo.name.ifEmpty { "박유수" },
            nickname = uiState.value.userInfo.nickname.ifEmpty { "어헛차" }
        )
        nearbyManager?.sendUserCard(endpointId, userCard)
    }

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
        nearbyManager?.stopAll()
    }
}

data class QrCodeUiState(
    val userInfo: UserInfo = UserInfo(),
    val nickname: String = "",
    val myEndpointId: String = "",
    val scannedTargetQr: String = "",
    val isScannerOpen: Boolean = false,
    val discoveredDevices: List<Pair<String, String>> = emptyList(),
    val receivedCard: UserCard? = null,
    val connectedEndpointId: String? = null
) : UiState

sealed interface QrCodeEvent : UiEvent {
    object NavigateBack : QrCodeEvent
    object ShareQrCode : QrCodeEvent
    data class ShowToast(val message: String) : QrCodeEvent
}