package com.example.mypage.mycard

import androidx.lifecycle.viewModelScope
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
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MycardViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기

) : BaseViewModel<MycardUiState, MycardEvent>(
    MycardUiState()){



    //초기 상태
    init {
        viewModelScope.launch {
            //유저 정보 가져오기
            getUserInfo()
        }
    }

    // 서버에서 내 정보 가져오기
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
                    /**TODO. 에러 토스트 메시지 등을 전송**/
                    generateMyUserCardQr(null)
                }
            )
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


    //QR코드 생성하기
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



}


data class MycardUiState(

    val userInfo: UserInfo = UserInfo(),

    //명함 관련 정보
    val myRecentInfoString: String = "",
    val myQrcodeData: String = "",
    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",

    ) : UiState

sealed interface MycardEvent : UiEvent {
    //이동하기
    object NavigateToMypage: MycardEvent


}