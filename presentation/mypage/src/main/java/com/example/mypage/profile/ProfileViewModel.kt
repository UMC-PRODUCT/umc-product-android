package com.example.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.component.util.isValidWebUrl
import com.umc.domain.model.ProfileInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LinkType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.request.member.LinkItem
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.member.UpdateMyLinkUseCase
import com.umc.domain.usecase.member.UpdateMyProfileUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val uploadFileUseCase: UploadFileUseCase, //파일 업로드 하기(이미지 업로드)
    private val updateMyProfileUseCase: UpdateMyProfileUseCase, //프로필 정보 업데이트
    private val updateMyLinkUseCase: UpdateMyLinkUseCase, //링크 정보 업데이트
    private val getMyOAuthUseCase: GetMyOAuthUseCase, //내 OAuth 가져오기
) : BaseViewModel<ProfileUiState, ProfileEvent>(
    ProfileUiState()){

    //초기화 작업
    init {
        // 프로필 정보 및 소셜 플랫폼 연결 정보 로드
        loadProfile()
    }

    /**
     * 프로필 정보와 소셜 플랫폼 연결 정보를 (다시) 불러오는 메서드
     */
    private fun loadProfile() {
        // 실패 플래그는 성공했을 때만 내린다(재시도 중에 빈 기본값 화면이 다시 보이거나 저장되지 않도록)
        getUserInfo()
        getUserOAuth()
    }

    /**
     * 프로필 조회 실패 안내에서 '다시 시도하기'를 눌렀을 때 호출
     */
    fun onClickRetry() {
        //재시도 요청이 진행 중이면 중복 호출하지 않는다
        if (uiState.value.isProfileLoading) return
        loadProfile()
    }

    /**
     * 서버에서 내 프로필 정보를 수신하여 UI State에 바인딩하는 메서드
     */
    private fun getUserInfo() {
        updateState { copy(isProfileLoading = true) }
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState {
                        copy(
                            isProfileLoading = false,
                            userInfo = userInfo,
                            githubLink = userInfo.profile.github,
                            linkedinLink = userInfo.profile.linkedIn,
                            blogLink = userInfo.profile.blog,
                            isProfileLoadFailed = false
                        )
                    }
                },
                errorCallback = {
                    updateState { copy(isProfileLoading = false, isProfileLoadFailed = true) }
                    emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LOAD_ERROR_TITLE))
                }
            )
        }
    }


    /**
     * 연동되어 있는 소셜 계정 제공자 플랫폼 리스트를 조회하는 메서드
     */
    fun getUserOAuth(){
        viewModelScope.launch {
            resultResponse(
                response = getMyOAuthUseCase(),
                successCallback = { myOAuth ->
                    val platforms = myOAuth.map { LoginType.valueOf(it.provider) }
                    updateState {
                        copy(linkedPlatforms = platforms.toImmutableList())
                    }

                },
                errorCallback = {}
            )
        }
    }

    /**
     * 프로필 사진 클릭 이벤트를 발행하여 Photo Picker를 여는 메서드
     */
    fun onClickProfileImage(){
        emitEvent(ProfileEvent.ClickProfileImage)
    }


    /**
     * 사용자가 선택한 새 프로필 사진 URI를 UI State에 저장하는 메서드
     *
     * @param uri 선택한 미디어 URI
     */
    fun settingImage(uri: Uri){
        updateState {
            copy(
                userProfileImageUri = uri
            )
        }
    }


    /**
     * 상단 완료 버튼 클릭 시 외부 링크 검증 -> 저장 -> (선택된 경우) 프로필 이미지 업로드 순으로 진행하는 메서드.
     * 모두 성공했을 때만 화면을 닫고, 실패하면 안내만 하고 화면과 입력값을 유지한다.
     */
    fun onClickComplete(){
        val state = uiState.value

        //진행 중에는 재진입 차단 (연타 시 업로드/PATCH 중복 방지)
        if (state.isSaving) return

        //프로필 조회에 실패했거나 아직 불러오지 못한(id == 0) 빈 상태에서 저장하면 서버 링크가 빈 값으로 덮어써지므로 차단
        if (state.isProfileLoadFailed || state.userInfo.id == 0L) {
            emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LOAD_ERROR_TITLE))
            return
        }

        //바뀐 칸만 URL 형식 검사 (예전에 저장된 값 때문에 완료가 막히지 않도록)
        val profile = state.userInfo.profile
        val github = state.githubLink.trim()
        val linkedin = state.linkedinLink.trim()
        val blog = state.blogLink.trim()
        val hasInvalidLink = listOf(
            github to profile.github,
            linkedin to profile.linkedIn,
            blog to profile.blog
        ).any { (now, saved) -> now != saved && now.isNotBlank() && !now.isValidWebUrl() }
        if (hasInvalidLink) {
            emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LINK_INVALID))
            return
        }

        updateState { copy(isSaving = true) }
        saveUserOutLink(github, linkedin, blog, state.userInfo.profile, state.userProfileImageUri)
    }

    /**
     * 외부 링크 5종 정보를 서버에 전송하여 업데이트하는 메서드.
     * 성공하면 선택된 프로필 이미지가 있을 때 이미지 업로드로 이어가고, 없으면 화면을 닫는다.
     */
    private fun saveUserOutLink(
        github: String,
        linkedin: String,
        blog: String,
        savedProfile: ProfileInfo,
        imageUri: Uri
    ) {
        // 화면에서 수정하지 않는 Instagram/개인 링크는 서버 저장값을 그대로 보내 덮어쓰기를 막는다
        val request = UpdateLinkRequest(
            links = listOf(
                LinkItem(LinkType.GITHUB.label, github),
                LinkItem(LinkType.LINKEDIN.label, linkedin),
                LinkItem(LinkType.BLOG.label, blog),
                LinkItem(LinkType.PERSONAL.label, savedProfile.personal),
                LinkItem(LinkType.INSTAGRAM.label, savedProfile.instagram)
            )
        )

        viewModelScope.launch {
            // 서버에 저장하기
            resultResponse(
                response = updateMyLinkUseCase(request),
                successCallback = {
                    //이미지를 바꿀 경우 = Uri가 empty가 아닐 경우만 업로드 진행
                    if (imageUri != Uri.EMPTY) {
                        updateProfileImage(imageUri)
                    } else {
                        emitEvent(ProfileEvent.ClickBackPressed)
                    }
                },
                errorCallback = {
                    onSaveFailed(AppStrings.PROFILE_SAVE_FAIL)
                }
            )
        }
    }

    /**
     * 선택된 프로필 이미지 파일(URI)을 서버에 업로드하고 발급받은 fileId로 회원 프로필을 업데이트하는 메서드
     *
     * @param uri 업로드할 이미지 URI
     */
    private fun updateProfileImage(uri: Uri){
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
                                emitEvent(ProfileEvent.ClickBackPressed)
                            },
                            errorCallback = {
                                onSaveFailed(AppStrings.PROFILE_SAVE_FAIL)
                            }
                        )
                    }

                },
                errorCallback = {
                    onSaveFailed(it.message.ifBlank { AppStrings.PROFILE_SAVE_FAIL })

                    Log.d("log_mypage", "실패! $it")
                }
            )
        }

    }

    /**
     * 저장 도중 실패했을 때 진행 상태를 풀고 안내만 띄우는 메서드 (화면과 입력값은 유지)
     */
    private fun onSaveFailed(message: String) {
        updateState { copy(isSaving = false) }
        emitEvent(ProfileEvent.MakeToast(message))
    }


    fun updateGithubLink(github: String){
        updateState {
            copy(githubLink = github)
        }
    }
    fun updateLinkedinLink(linkedin: String){
        updateState {
            copy(linkedinLink = linkedin)
        }
    }
    fun updateBlogLink(blog: String){
        updateState {
            copy(blogLink = blog)
        }
    }

    //그냥 뒤로 가기
    fun onClickBackPressed(){
        emitEvent(ProfileEvent.ClickBackPressed)
    }



}


data class ProfileUiState(
    //유저 정보 (고정)
    val linkedPlatforms: ImmutableList<LoginType> = persistentListOf(),
    val userInfo : UserInfo = UserInfo(),


    //유저 정보 (가변)
    val userProfileImageUri : Uri = Uri.EMPTY, //이미지 수정 시 먼저 보여주는 Uri (보내기 용도)

    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",

    // 프로필 조회 실패 여부 (true 면 빈 정보 대신 실패 안내 + 다시 시도 표시)
    val isProfileLoadFailed: Boolean = false,
    val isProfileLoading: Boolean = false, //프로필 조회 요청 진행 중
    // '완료' 저장 진행 중 여부 (중복 탭 방지, 진행 표시)
    val isSaving: Boolean = false,

    ) : UiState

sealed interface ProfileEvent : UiEvent {

    //갤러리 터치 이벤트
    object ClickProfileImage : ProfileEvent

    //토스트 만들기
    data class MakeToast(val message: String) : ProfileEvent


    //그냥 뒤로 가기
    object ClickBackPressed : ProfileEvent
}