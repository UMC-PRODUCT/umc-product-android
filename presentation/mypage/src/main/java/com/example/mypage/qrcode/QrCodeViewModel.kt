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

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val application: Application,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<QrCodeUiState, QrCodeEvent>(QrCodeUiState()) {


    init {
        getUserProfile()
    }

    /**
     * 서버 API를 호출하여 내 프로필 정보를 수신하고 QR 딥링크 생성을 시작하는 메서드
     */
    private fun getUserProfile() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState { copy(userInfo = userInfo) }
                    generateMyUserCardQr(userInfo)
                    settingUserInfoToUI(userInfo)
                },
                errorCallback = {
                    emitEvent(QrCodeEvent.ShowToast("프로필 정보를 불러오지 못했습니다."))
                    generateMyUserCardQr(null)
                }
            )
            stopLoading()
        }
    }

    /**
     * 수신받은 UserInfo 도메인 객체를 파싱하여 최신 기수 및 직함 텍스트를 구성하는 메서드
     *
     * @param userInfo 서버에서 조회된 내 정보
     */
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
     * 내 회원 ID(memberId)를 매핑한 딥링크 스키마 텍스트 및 QR 비트맵 객체를 가공하는 메서드
     *
     * @param userInfo 내 프로필 데이터
     */
    private fun generateMyUserCardQr(userInfo: UserInfo?) {
        val memberId = userInfo?.id ?: 21
        val packageName = "com.umc.product"

        // QR 코드 인코딩용 JSON 데이터 생성
        // Android Intent URI 표준 규격
        /**TODO. 중요! 현재 디버그 단에서는 보안 문제로 테스트 불가 -> 일단 앱 내 딥링크로 수정**/
        val qrDeepLinkUrl = "intent://card?memberId=$memberId#Intent;" +
                "scheme=umc;" +
                "package=$packageName;" +
                "S.browser_fallback_url=https://play.google.com/store/apps/details?id=$packageName;" +
                "end"

        //val qrDeepLinkUrlDebug = "https://api.university.neordinary.com/community/threads/card?memberId=$memberId"
        val qrDeepLinkUrlDebug = "umc://card?memberId=$memberId"

        val bitmap = QrCodeUtils.generateQrCode(qrDeepLinkUrl, 600)
        val bitmapDebug = QrCodeUtils.generateQrCode(qrDeepLinkUrlDebug, 600)

        updateState { copy(
            myQrcodeData = qrDeepLinkUrlDebug,
            qrImageBitmap = bitmapDebug
        ) }

    }

    

    fun navigateBack() {
        emitEvent(QrCodeEvent.NavigateBack)
    }

    /**
     * 비트맵 이미지를 기기 갤러리 MediaStore 경로에 기입 저장하는 메서드
     *
     * @param bitmap 저장할 QR ImageBitmap
     */
    fun saveQrImage(bitmap: ImageBitmap?) {
        if (bitmap == null) return
        val success = QrCodeUtils.saveImageToGallery(application, bitmap, "MyUserCard_QR")
        if (success) {
            emitEvent(QrCodeEvent.ShowToast("갤러리에 저장되었습니다."))
        } else {
            emitEvent(QrCodeEvent.ShowToast("저장에 실패했습니다."))
        }
    }

    /**
     * QR 코드 이미지를 외부 애플리케이션으로 공유하는 이벤트를 발행하는 메서드
     */
    fun shareQrCode() {
        emitEvent(QrCodeEvent.ShareQrCode)
    }

    
}

data class QrCodeUiState(
    val userInfo: UserInfo = UserInfo(),
    val nickname: String = "",
    val myRecentInfoString: String = "",

    val myQrcodeData: String = "",
    val qrImageBitmap: ImageBitmap? = null

) : UiState

sealed interface QrCodeEvent : UiEvent {
    object NavigateBack : QrCodeEvent
    object ShareQrCode : QrCodeEvent
    data class ShowToast(val message: String) : QrCodeEvent
}