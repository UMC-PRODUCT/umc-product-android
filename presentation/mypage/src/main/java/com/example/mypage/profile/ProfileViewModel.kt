package com.example.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.component.util.isValidEmail
import com.umc.component.util.isValidWebUrl
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.LinkType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.model.mypage.BusinessCardContact
import com.umc.domain.model.mypage.BusinessCardLink
import com.umc.domain.model.request.member.EditBusinessCardRequest
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.member.EditMyBusinessCardUseCase
import com.umc.domain.usecase.member.GetMyBusinessCardEditUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.member.UpdateMyProfileUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val uploadFileUseCase: UploadFileUseCase, //파일 업로드 하기(이미지 업로드)
    private val updateMyProfileUseCase: UpdateMyProfileUseCase, //프로필 정보 업데이트
    private val getMyBusinessCardEditUseCase: GetMyBusinessCardEditUseCase, //연락처·링크·공개 여부 가져오기
    private val editMyBusinessCardUseCase: EditMyBusinessCardUseCase, //연락처·링크·공개 여부 저장
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
     * 서버에서 내 프로필(사진·이름·학교)과 명함 정보(연락처·링크·공개 여부)를 함께 받아 UI State에 바인딩하는 메서드.
     * 명함 저장은 링크 전체 교체라, 둘 중 하나라도 못 받으면 실패로 두고 저장을 막는다
     */
    private fun getUserInfo() {
        updateState { copy(isProfileLoading = true) }
        viewModelScope.launch {
            val profileResult = async { getMyProfileUseCase() }
            val cardResult = async { getMyBusinessCardEditUseCase() }
            val profile = profileResult.await()
            val card = cardResult.await()

            if (profile is ApiState.Success && card is ApiState.Success) {
                val savedCard = card.data
                updateState {
                    copy(
                        isProfileLoading = false,
                        userInfo = profile.data,
                        savedCard = savedCard,
                        contactEmail = savedCard.contact?.email.orEmpty(),
                        githubLink = savedCard.linkOf(LinkType.GITHUB).link,
                        linkedinLink = savedCard.linkOf(LinkType.LINKEDIN).link,
                        blogLink = savedCard.linkOf(LinkType.BLOG).link,
                        instagramLink = savedCard.linkOf(LinkType.INSTAGRAM).link,
                        personalLink = savedCard.linkOf(LinkType.PERSONAL).link,
                        privateLinkFields = ProfileLinkField.entries
                            .filterNot { savedCard.isPublic(it) }
                            .toImmutableSet(),
                        isProfileLoadFailed = false
                    )
                }
            } else {
                updateState { copy(isProfileLoading = false, isProfileLoadFailed = true) }
                emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LOAD_ERROR_TITLE))
            }
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
     * 상단 완료 버튼 클릭 시 연락처·링크 검증 -> 명함(연락처·링크·공개 여부) 저장 -> (선택된 경우) 프로필 이미지 업로드 순으로 진행하는 메서드.
     * 모두 성공했을 때만 화면을 닫고, 실패하면 안내만 하고 화면과 입력값을 유지한다.
     */
    fun onClickComplete(){
        val state = uiState.value

        //진행 중에는 재진입 차단 (연타 시 업로드/PATCH 중복 방지)
        if (state.isSaving) return

        //조회에 실패했거나 아직 불러오지 못한 빈 상태에서 저장하면 서버의 연락처·링크가 빈 값으로 덮어써지므로 차단
        val savedCard = state.savedCard
        if (state.isProfileLoadFailed || state.userInfo.id == 0L || savedCard == null) {
            emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LOAD_ERROR_TITLE))
            return
        }

        //바뀐 칸만 형식 검사 (예전에 저장된 값 때문에 완료가 막히지 않도록)
        val email = state.contactEmail.trim()
        if (email != savedCard.contact?.email.orEmpty() && email.isNotBlank() && !email.isValidEmail()) {
            emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_EMAIL_INVALID))
            return
        }
        val hasInvalidLink = ProfileLinkField.entries.any { field ->
            val type = field.linkType ?: return@any false
            val now = state.linkOf(field).trim()
            now != savedCard.linkOf(type).link && now.isNotBlank() && !now.isValidWebUrl()
        }
        if (hasInvalidLink) {
            emitEvent(ProfileEvent.MakeToast(AppStrings.PROFILE_LINK_INVALID))
            return
        }

        //링크는 전체 교체라 5개 타입을 모두 담는다
        val request = EditBusinessCardRequest(
            contact = BusinessCardContact(
                email = email,
                isPublic = ProfileLinkField.EMAIL !in state.privateLinkFields
            ),
            links = ProfileLinkField.entries.mapNotNull { field ->
                field.linkType?.let { type ->
                    BusinessCardLink(
                        type = type,
                        link = state.linkOf(field).trim(),
                        isPublic = field !in state.privateLinkFields
                    )
                }
            }
        )

        updateState { copy(isSaving = true) }
        saveBusinessCard(request, state.userProfileImageUri)
    }

    /**
     * 연락처·링크와 항목별 공개 여부를 서버에 저장하는 메서드.
     * 성공하면 선택된 프로필 이미지가 있을 때 이미지 업로드로 이어가고, 없으면 화면을 닫는다.
     */
    private fun saveBusinessCard(
        request: EditBusinessCardRequest,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            // 서버에 저장하기
            resultResponse(
                response = editMyBusinessCardUseCase(request),
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


    /**
     * 연락처·링크 입력값을 항목별로 반영하는 메서드
     */
    fun updateLink(field: ProfileLinkField, value: String) {
        updateState {
            when (field) {
                ProfileLinkField.EMAIL -> copy(contactEmail = value)
                ProfileLinkField.GITHUB -> copy(githubLink = value)
                ProfileLinkField.LINKEDIN -> copy(linkedinLink = value)
                ProfileLinkField.BLOG -> copy(blogLink = value)
                ProfileLinkField.INSTAGRAM -> copy(instagramLink = value)
                ProfileLinkField.PERSONAL -> copy(personalLink = value)
            }
        }
    }

    /**
     * 항목의 공개·비공개를 전환하는 메서드
     */
    fun toggleLinkVisibility(field: ProfileLinkField) {
        updateState {
            val updated = if (field in privateLinkFields) privateLinkFields - field else privateLinkFields + field
            copy(privateLinkFields = updated.toImmutableSet())
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

    val contactEmail: String = "", //명함 연락처 이메일 (로그인 이메일과 별개)
    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",
    val instagramLink: String = "",
    val personalLink: String = "",

    // 비공개로 고른 항목 (나머지는 공개). 완료를 누르면 연락처·링크와 함께 저장한다
    val privateLinkFields: ImmutableSet<ProfileLinkField> = persistentSetOf(),

    // 서버에 저장되어 있는 연락처·링크·공개 여부 (바뀐 칸 검사용). null 이면 아직 못 받아 저장할 수 없다
    val savedCard: BusinessCard? = null,

    // 프로필 조회 실패 여부 (true 면 빈 정보 대신 실패 안내 + 다시 시도 표시)
    val isProfileLoadFailed: Boolean = false,
    val isProfileLoading: Boolean = false, //프로필 조회 요청 진행 중
    // '완료' 저장 진행 중 여부 (중복 탭 방지, 진행 표시)
    val isSaving: Boolean = false,

    ) : UiState {

    /** 항목별 화면 표시값 */
    fun linkOf(field: ProfileLinkField): String = when (field) {
        ProfileLinkField.EMAIL -> contactEmail
        ProfileLinkField.GITHUB -> githubLink
        ProfileLinkField.LINKEDIN -> linkedinLink
        ProfileLinkField.BLOG -> blogLink
        ProfileLinkField.INSTAGRAM -> instagramLink
        ProfileLinkField.PERSONAL -> personalLink
    }
}

/** 서버에 저장된 항목의 공개 여부 (연락처를 아직 입력하지 않았으면 공개) */
private fun BusinessCard.isPublic(field: ProfileLinkField): Boolean {
    val type = field.linkType ?: return contact?.isPublic ?: true
    return linkOf(type).isPublic
}

sealed interface ProfileEvent : UiEvent {

    //갤러리 터치 이벤트
    object ClickProfileImage : ProfileEvent

    //토스트 만들기
    data class MakeToast(val message: String) : ProfileEvent


    //그냥 뒤로 가기
    object ClickBackPressed : ProfileEvent
}