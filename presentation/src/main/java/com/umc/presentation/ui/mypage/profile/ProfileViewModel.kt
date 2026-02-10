package com.umc.presentation.ui.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.model.mypage.UserOutLink
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserOutLinkUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserOutLinkUseCase
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
    private val uploadFileUseCase: UploadFileUseCase,
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
            }
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

    //viewModel에서 갤러리->이미지 가져온 후 처리 이벤트
    fun updateProfileImage(uri: Uri){
        //uri을 이용해 파일 전송하기
        viewModelScope.launch {
            resultResponse(
                response = uploadFileUseCase(uri.toString(), UploadFileCategory.PROFILE_IMAGE),
                successCallback = {
                    Log.d("log_mypage", "성공! updateProfileImage: $it")
                },
                errorCallback = {
                    emitEvent(ProfileFragmentEvent.MakeToast(it))
                    Log.d("log_mypage", "실패! $it")
                }
            )
        }

    }

    //완료 누르고 뒤로 가기
    fun onClickComplete(){
        val nowUri = uiState.value.userProfileImageUri
        updateProfileImage(nowUri)

        //emitEvent(ProfileFragmentEvent.ClickComplete)
    }

    // 완료 버튼을 눌렀을 때 호출될 저장 로직
    fun saveAndExit(github: String, linkedin: String, blog: String) {
        viewModelScope.launch {
            // DataStore에 저장하고 끝내기
            val nowOutLink = UserOutLink(github, linkedin, blog)
            updateUserOutLinkUseCase(nowOutLink)

            emitEvent(ProfileFragmentEvent.ClickBackPressed)
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
    val myPart: String = "9기/Android",
    val userInfo : UserInfo = UserInfo(),


    //유저 정보 (가변)
    val userProfileImageUri : Uri = Uri.EMPTY, //이미지 수정 시 먼저 보여주는 Uri (보내기 용도)

    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",


    //임시 정보
    val myActiveHistory: List<UserActiveItem> = listOf(
        UserActiveItem(
            generation = "8기",
            partName = "Android Part",
            position = "챌린저"),
        UserActiveItem(
            generation = "9기",
            partName = "Android Part",
            position = "파트장"),
        UserActiveItem(
            generation = "9기",
            partName = "Android Part",
            position = "파트장"),
    ),

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