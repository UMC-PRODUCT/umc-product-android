package com.umc.presentation.ui.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.model.mypage.UserOutLink
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserOutLinkUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserOutLinkUseCase
import com.umc.domain.usecase.member.UpdateMyProfileUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserOutLinkUseCase: GetUserOutLinkUseCase, //dataStore에서 불러오기
    private val updateUserOutLinkUseCase: UpdateUserOutLinkUseCase, //dataStore에 저장
    private val getUserInfoUseCase: GetUserInfoUseCase, //dataStore에서 유저 정보 불러오기
    private val uploadFileUseCase: UploadFileUseCase, //파일 업로드 하기(이미지 업로드)
    private val updateMyProfileUseCase: UpdateMyProfileUseCase, //프로필 정보 업데이트
    ) : BaseViewModel<ProfileFragmentUiState, ProfileFragmentEvent>(
    ProfileFragmentUiState()){

    //초기화 작업
    init {
        viewModelScope.launch {
            //DataStore에서 가져와서 넣기
            getUserOutLinkUseCase().collect { outLink ->
                updateState {
                    copy(
                        githubLink = outLink.github,
                        linkedinLink = outLink.linkedin,
                        blogLink = outLink.blog
                    )
                }
            }
        }
        viewModelScope.launch {
            getUserInfoUseCase().collect { userInfo ->
                updateState {
                    copy(
                        userInfo = userInfo,
                    )
                }
                settingUserInfoToUI(userInfo)
            }
        }

    }

    //유저 정보를 통해 활동 이력 및 기수파트 작성
    fun settingUserInfoToUI(userInfo: UserInfo){
        val gisuSummaryList = userInfo.getGisuSummaryList()

        //최종 리스트
        val activeHistory = mutableListOf<UserActiveItem>()

        //gisuSummaryList를 돌면서 만들기
        gisuSummaryList.forEach { summary->
            //1. 기수 별 분류 (여기에 roles/ challengers)
            val generationText = "${summary.gisu}기"

            //2. 운영진 기록이 있으면 싹다 만들기
            summary.fromRoles.forEach { roleItem ->
                    //담당 파트가 있으면 (웹 파트장)
                Log.d("log_mypage", "운영 IN: ${roleItem}")
                    val item = if (roleItem.responsiblePart != null) {
                        UserActiveItem(
                            generation = generationText,
                            partName = "${roleItem.responsiblePart} Part",
                            position = roleItem.role
                        )
                    }
                    //담당 파트가 없으면 (총괄)
                    else {
                        val roleLabel =
                            //label 없으면 enum 이름 그대로 쓰자.
                            UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                        UserActiveItem(
                            generation = generationText,
                            partName = roleLabel,
                            position = roleLabel
                        )
                    }
                    Log.d("log_mypage", "결과: ${item}")
                    activeHistory.add(item)
                }

            //3. 챌린저 기록이 있으면 싹 다 만들기
            summary.fromRecords.forEach { recordItem ->
                Log.d("log_mypage", "챌린저: ${recordItem}")
                val item = UserActiveItem(
                    generation = generationText,
                    partName = "${recordItem.responsiblePart} Part",
                    position = "챌린저"
                )
                Log.d("log_mypage", "결과: ${item}")
                activeHistory.add(item)
            }
        }

        //최신 파트(기수/파트) - 이거는 role 우선
        val latestHistory = activeHistory.firstOrNull()
        val latestPartAndGisu = latestHistory?.let {
            "${it.generation}/${it.partName.replace(" Part", "")}"
        } ?: "정보 없음"

        //이 정보를 저장
        updateState {
            copy(
                myActiveHistory = activeHistory,
                myPart = latestPartAndGisu
            )
        }


    }


    //프로필 이미지 수정했을 때 이벤트
    fun onClickProfileImage(){
        emitEvent(ProfileFragmentEvent.ClickProfileImage)
    }


    fun settingImage(uri: Uri){
        updateState {
            copy(
                userProfileImageUri = uri
            )
        }
    }
    

    //완료 누르고 뒤로 가기
    fun onClickComplete(){
        val nowUri = uiState.value.userProfileImageUri
        
        //1. 아래의 saveAndExit를 수행 - 3개의 outLink를 저장 (fragment에서 데이터를 가져
        emitEvent(ProfileFragmentEvent.ClickComplete)
        
        //2. 이미지 파일을 업로드하고, 바꾸기(여기서 이미지를 바꿀 경우 = Uri가 empty가 아닐 경우만 진행)
        if(nowUri != Uri.EMPTY){
            updateProfileImage(nowUri)
        }
        else{
            emitEvent(ProfileFragmentEvent.ClickBackPressed)
        }
    }

    // 완료 버튼을 눌렀을 때 호출될 저장 로직
    fun saveUserOutLink(github: String, linkedin: String, blog: String) {
        viewModelScope.launch {
            // DataStore에 저장하고 끝내기
            val nowOutLink = UserOutLink(github, linkedin, blog)
            updateUserOutLinkUseCase(nowOutLink)
        }
    }

    //viewModel에서 갤러리->이미지 가져온 후 처리 
    //서버에 파일 업로드 후, 해당 파일 ID를 프로필 정보에 적용
    fun updateProfileImage(uri: Uri){
        //uri을 이용해 파일 전송하기
        viewModelScope.launch {

            resultResponse(
                response = uploadFileUseCase(uri.toString(), UploadFileCategory.PROFILE_IMAGE),
                successCallback = {
                    Log.d("log_mypage", "성공! updateProfileImage: $it")
                    //성공이 됬으면, 프로필을 수정하기
                    launch {
                        resultResponse(
                            response = updateMyProfileUseCase(it.fileId),
                            successCallback = {
                                //이거 제일 마지막에 뒤로가기 실해
                                emitEvent(ProfileFragmentEvent.ClickBackPressed)
                            },
                            errorCallback = {
                                
                            }
                        )
                    }

                },
                errorCallback = {
                    emitEvent(ProfileFragmentEvent.MakeToast(
                        it.message))

                    Log.d("log_mypage", "실패! $it")
                }
            )
        }

    }


    //그냥 뒤로 가기
    fun onClickBackPressed(){
        emitEvent(ProfileFragmentEvent.ClickBackPressed)
    }



}


data class ProfileFragmentUiState(
    //유저 정보 (고정)
    val loginType: LoginType = LoginType.KAKAO,
    val myPart: String = "",
    val userInfo : UserInfo = UserInfo(),


    //유저 정보 (가변)
    val userProfileImageUri : Uri = Uri.EMPTY, //이미지 수정 시 먼저 보여주는 Uri (보내기 용도)

    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",


    //임시 정보
    val myActiveHistory: List<UserActiveItem> = emptyList(),

    ) : UiState

sealed interface ProfileFragmentEvent : UiEvent {
    
    //갤러리 터치 이벤트
    object ClickProfileImage : ProfileFragmentEvent

    //토스트 만들기
    data class MakeToast(val message: String) : ProfileFragmentEvent


    //완료 버튼을 눌렀을 때
    object ClickComplete : ProfileFragmentEvent

    //그냥 뒤로 가기
    object ClickBackPressed : ProfileFragmentEvent
}