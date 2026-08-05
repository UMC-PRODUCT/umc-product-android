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
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
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
                    settingUserInfoToUI(userInfo)
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

    //UserInfo를 받아았을 때 이를 파싱해서 UI 요소로 분할하는 함수
    fun settingUserInfoToUI(userInfo: UserInfo){
        // 기수별 정보가 담긴 것.
        val gisuSummaryList = userInfo.getGisuSummaryList()

        // 최신기수를 가져오기
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }

        latestGisu?.let { summary ->
            //권위 or 챌린저에서 1개 선택
            val representativeItem = summary.fromRoles.firstOrNull() ?: summary.fromRecords.firstOrNull()

            val positionString = representativeItem?.let { item ->
                //파트명 변환 (UserPart Enum 활용, 없으면 빈 문자열)
                val partLabel = runCatching { UserPart.valueOf(item.responsiblePart ?: "").label }
                    .getOrNull()?.let { "$it " } ?: ""

                //직함명 변환 (displayName이 null이면 원본 role 사용)
                val roleEnum = UserChallengerRole.from(item.role)
                val roleLabel = roleEnum.displayName ?: item.role

                //최종 포맷: "N기 Part Role"
                "$partLabel$roleLabel·${summary.gisu}기"
            } ?: "챌린저·${summary.gisu}기" // 예외 상황 대비 기본값

            updateState {
                copy(
                    myRecentInfoString = positionString
                )
            }

        }

    }

    /**
     * 내 유저 ID가 담긴 딥링크를 생성한다.
     */
    private fun generateMyUserCardQr(userInfo: UserInfo?) {
        val memberId = userInfo?.id ?: 23
        val packageName = "com.umc.product"

        // QR 코드 인코딩용 JSON 데이터 생성
        // Android Intent URI 표준 규격
        val qrDeepLinkUrl = "intent://card?memberId=$memberId#Intent;" +
                "scheme=umc;" +
                "package=$packageName;" +
                "S.browser_fallback_url=https://play.google.com/store/apps/details?id=$packageName;" +
                "end"

        val qrDeepLinkUrlDebug = "umc://card?memberId=$memberId"

        updateState { copy(myQrcodeData = qrDeepLinkUrlDebug) }

    }


    //확인 버튼 클릭 -> 성공 오버레이 감추고 QR 화면으로 돌아감
    fun dismissSuccessOverlay() {
        updateState {
            copy(
                isSuccessOverlayOpen = false,
                receivedCard = null,
                scannedTargetQr = ""
            )
        }
    }

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
                isScannerOpen = false,
            )
        }

        //그냥 json을 읽은 다음 역직렬화
        try {
            //QR 데이터(JSON) -> UserCard 객체 직접 파싱
            val card = UserCard.fromJson(scannedValue)

            //파싱 성공 시 별도 네트워크/블루투스 연결 없이 즉시 UI State 반영 -> UDialog 팝업 뜸!
            updateState { 
                copy(
                    receivedCard = card,
                    isSuccessOverlayOpen = true
                ) 
            }
            //emitEvent(QrCodeEvent.ShowToast("${card.name}님의 명함을 읽어왔습니다!"))
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
    val myRecentInfoString: String = "",

    val myQrcodeData: String = "",
    val scannedTargetQr: String = "", //스캔한 qr코드의 값 = 기기 모델명 or ""
    val isScannerOpen: Boolean = false, //스캐너(카메라)가 열렸는지 확인
    val receivedCard: UserCard? = null,
    val isSuccessOverlayOpen: Boolean = false, //스캔 완료 오버레이 창 띄우기
) : UiState

sealed interface QrCodeEvent : UiEvent {
    object NavigateBack : QrCodeEvent
    object ShareQrCode : QrCodeEvent
    data class ShowToast(val message: String) : QrCodeEvent
}