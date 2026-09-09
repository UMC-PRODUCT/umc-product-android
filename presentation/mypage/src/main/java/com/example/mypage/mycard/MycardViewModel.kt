package com.example.mypage.mycard

import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.mypage.dialog.ExchangeStep
import com.example.mypage.mypage.MypageEvent
import com.example.mypage.mypage.MypageUiState
import com.example.mypage.nearby.NearbyManager
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.NearbyUserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.toUserCard
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.appDataStore.usercard.GetUserCardUseCase
import com.umc.domain.usecase.appDataStore.usercard.SaveUserCardUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MycardViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getUserCardUseCase: GetUserCardUseCase, //유저 명함 가져오기
    private val getMemberProfileUseCase: GetMemberProfileUseCase, //유저 검색하기
    private val SaveUserCardUseCase: SaveUserCardUseCase, //명함 저장

) : BaseViewModel<MycardUiState, MycardEvent>(
    MycardUiState()){


    //초기 상태
    init {
        // 내 프로필 데이터 로드
        viewModelScope.launch {
            getUserInfo()
        }

        // DataStore 내에 저장되어 있는 명함 리스트를 실시간 수신하여 명함 수 갱신
        viewModelScope.launch {
            getUserCardUseCase().collect { cards ->
                updateState {
                    copy(
                        cardCount = cards.size
                    )
                }
            }
        }
    }

    /**
     * 서버 API를 호출하여 내 프로필 상세 정보를 조회하는 메서드
     */
    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState {
                        copy(
                            userInfo = userInfo,
                        )
                    }
                    generateMyUserCardQr(userInfo)
                    settingUserInfoToUI(userInfo)

                },
                errorCallback = {
                    // 에러 발생 시에도 qr 생성을 위해 null값으로 qr 생성
                    generateMyUserCardQr(null)
                }
            )
        }
    }


    /**
     * 서버에서 수신받은 UserInfo 객체를 파싱하여 최신 기수, 파트, 직함 텍스트를 연산하고 UI State에 기입하는 메서드
     *
     * @param userInfo 서버에서 받아온 내 정보 도메인 모델
     */
    fun settingUserInfoToUI(userInfo: UserInfo){

        // 기수별 정보 요약본 get
        val gisuSummaryList = userInfo.getGisuSummaryList()

        // 최신기수를 가져오기
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }

        latestGisu?.let { summary ->
            // 운영진/대표 역할(fromRoles) 또는 참가 기록(fromRecords) 중 대표 항목 추출
            val representativeItem = summary.fromRoles.firstOrNull() ?: summary.fromRecords.firstOrNull()

            val positionString = representativeItem?.let { item ->
                //파트명 변환 (UserPart Enum 활용, 없으면 빈 문자열)
                val partLabel = runCatching { UserPart.from(item.responsiblePart).label }
                    .getOrNull()?.let { "$it " } ?: ""

                //직함명 변환 (displayName이 null이면 원본 role 사용)
                val roleEnum = UserChallengerRole.from(item.role)
                val roleLabel = roleEnum.displayName ?: item.role

                //최종 포맷: "N기 Part Role"
                "$partLabel$roleLabel·${summary.gisu}기"
            } ?: "챌린저·${summary.gisu}기" // 예외 상황 대비 기본값

            updateState {
                copy(
                    myRecentInfoString = positionString,
                    githubLink = userInfo.profile.github,
                    linkedinLink = userInfo.profile.linkedIn,
                    blogLink = userInfo.profile.blog
                )
            }

        }

    }


    /**
     * 명함 뒷면에 표시될 내 회원 고유 QR 코드 인텐트 딥링크 URL을 생성하는 메서드
     *
     * @param userInfo 내 프로필 객체
     *
     * [유의사항]
     * 현재 애플리케이션이 없을 경우, 자동으로 playstore로 연결하는 딥링크를 준비했지만(qrDeepLinkUrl)
     * 디버깅 모드에서는 사용 불가 문제로 앱이 존재할 경우, 애플리케이션을 호출하는 qrDeepLinkUrlDebug를 사용 중입니다.
     */
    private fun generateMyUserCardQr(userInfo: UserInfo?) {
        val memberId = userInfo?.id ?: 21
        val packageName = "com.umc.product"

        val qrDeepLinkUrl = "intent://card?memberId=$memberId#Intent;" +
                "scheme=umc;" +
                "package=$packageName;" +
                "S.browser_fallback_url=https://play.google.com/store/apps/details?id=$packageName;" +
                "end"

        val qrDeepLinkUrlDebug = "https://api.university.neordinary.com/community/threads/card?memberId=\$memberId"

        updateState { copy(myQrcodeData = qrDeepLinkUrlDebug) }

    }

    /**
     * QR 코드 스캔 또는 딥링크를 통해 수신된 상대방 memberId로 유저 정보를 조회하는 메서드
     *
     * @param memberId 조회할 상대방 회원의 고유 ID
     */
    fun searchUser(memberId: Long) {

        //이미 처리한 거 중복 처리 방지
        if(uiState.value.processedTargetMemberId == memberId){
            return
        }

        updateState {
            copy(
                processedTargetMemberId = memberId
            )
        }

        viewModelScope.launch {
            resultResponse(
                response = getMemberProfileUseCase(memberId),
                successCallback = { userInfo ->
                    val targetCard = userInfo.toUserCard()
                    saveUserCard(targetCard)

                },
                errorCallback = {}
            )
        }
    }

    /**
     * 조회된 상대방 명함(UserCard)을 내 명함첩(DataStore)에 보관하고 성공 오버레이를 표시하는 메서드
     *
     * @param userCard 저장할 상대방 명함 도메인 모델
     */
    fun saveUserCard(userCard: UserCard) {
        Log.d("log_mypage", "saveUserCard: $userCard")
        viewModelScope.launch {
            SaveUserCardUseCase(userCard)

            updateState {
                copy(
                    isSuccessOverlayOpen = true,
                    receivedUserCard = userCard
                )
            }

        }
    }

    /**
     * 명함 교환 성공 전면 오버레이를 닫고 수신 상태를 초기화하는 메서드
     */
    fun dismissSuccessOverlay() {
        updateState {
            copy(
                isSuccessOverlayOpen = false,
                receivedUserCard = null
            )
        }
    }



}


data class MycardUiState(

    val userInfo: UserInfo = UserInfo(),

    //명함 관련 정보
    val myRecentInfoString: String = "",
    val myQrcodeData: String = "",
    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",

    val cardCount: Int = 0,

    //qr로 받을 때 유저 id 정보
    val processedTargetMemberId: Long? = null,

    //명함 성공 관련
    val isSuccessOverlayOpen: Boolean = false,
    val receivedUserCard: UserCard? = null,

    ) : UiState

sealed interface MycardEvent : UiEvent {
    //이동하기
    object NavigateToMypage: MycardEvent

}