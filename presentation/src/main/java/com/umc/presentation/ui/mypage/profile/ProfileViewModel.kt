package com.umc.presentation.ui.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LinkType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.model.request.member.LinkItem
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.member.UpdateMyLinkUseCase
import com.umc.domain.usecase.member.UpdateMyProfileUseCase
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.domain.usecase.organization.GetSchoolNameUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase, //dataStore에서 유저 정보 불러오기
    private val uploadFileUseCase: UploadFileUseCase, //파일 업로드 하기(이미지 업로드)
    private val updateMyProfileUseCase: UpdateMyProfileUseCase, //프로필 정보 업데이트
    private val updateMyLinkUseCase: UpdateMyLinkUseCase, //링크 정보 업데이트
    private val getSchoolNameUseCase: GetSchoolNameUseCase, //학교 이름 가져오기
    private val getChapterDetailUseCase: GetChapterDetailUseCase, //지부 이름 가져오기
    private val getMyOAuthUseCase: GetMyOAuthUseCase, //내 OAuth 가져오기
    ) : BaseViewModel<ProfileFragmentUiState, ProfileFragmentEvent>(
    ProfileFragmentUiState()){

    //초기화 작업
    init {
        viewModelScope.launch {
            getUserInfoUseCase().collect { userInfo ->
                updateState {
                    copy(
                        userInfo = userInfo,
                        githubLink = userInfo.profile.github,
                        linkedinLink = userInfo.profile.linkedIn,
                        blogLink = userInfo.profile.blog
                    )
                }
                Log.d("log_mypage", "userInfo: $userInfo")
                settingUserInfoToUI(userInfo)
            }
        }

        getUserOAuth()

    }

    //소셜 정보(OAuth 받아오기)
    fun getUserOAuth(){
        viewModelScope.launch {
            resultResponse(
                response = getMyOAuthUseCase(),
                successCallback = { myOAuth ->
                    val platforms = myOAuth.map { LoginType.valueOf(it.provider) }
                    updateState {
                        copy(linkedPlatforms = platforms)
                    }

                },
                errorCallback = {}
            )
        }
    }

    //유저 정보를 통해 활동 이력 및 기수파트 작성
    fun settingUserInfoToUI(userInfo: UserInfo){
        
        viewModelScope.launch {
            //기수 정보 리스트 가져오기
            val gisuSummaryList = userInfo.getGisuSummaryList()

            //api 호출 시 coroutineScope를 사용하여 async를 호출 - 다 끝날때까지 기달
            val finalActiveHistory = coroutineScope {
                gisuSummaryList.flatMap { summary ->
                    val generationText = "${summary.gisu}기"

                    //운영진 기록
                    val roleJobs = summary.fromRoles.map { roleItem ->
                        async {
                            //파트가 있는 경우 (ex. 안드로이드 파트장)
                            if (roleItem.responsiblePart != null) {
                                UserActiveItem(
                                    generation = generationText,
                                    partName = "${roleItem.responsiblePart} Part",
                                    position = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                )
                            } 
                            //그 외 - 학교일 때
                            else if(roleItem.organizationType == "SCHOOL"){
                                //들어갈 값
                                var itemResult: UserActiveItem? = null
                                resultResponse(
                                    response = getSchoolNameUseCase(roleItem.organizationId),
                                    //학교 불러오기 성공이면, 학교 이름을 가져오기
                                    successCallback = { schoolInfo ->
                                        val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                        itemResult = UserActiveItem(generationText, "${schoolInfo.schoolName} $label",label, )
                                    },
                                    //못 불러오면 그대로 넣기
                                    errorCallback = {
                                        val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                        itemResult = UserActiveItem(generationText, label, label)
                                    }
                                )
                                itemResult!!
                            }
                            //그 외 - 지부일 때
                            else if(roleItem.organizationType == "CHAPTER"){
                                var itemResult: UserActiveItem? = null
                                resultResponse(
                                    response = getChapterDetailUseCase(roleItem.organizationId),
                                    successCallback = { chapterInfo ->
                                        //지부 이름 넣기
                                        val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                        itemResult = UserActiveItem(generationText, "${chapterInfo.name}지부 $label",label, )
                                    },
                                    errorCallback = {
                                        val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                        itemResult = UserActiveItem(generationText, label, label)
                                    }
                                )
                                itemResult!!
                            }
                            //그 외 - 파트 자리 없고, central만 있는 경우
                            else{
                                val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
                                val itemResult = UserActiveItem(generationText, label, "총괄")
                                itemResult
                            }
                        }
                    }

                    //챌린저 기록
                    val recordJobs = summary.fromRecords
                        .filter { it.responsiblePart != "ADMIN" }
                        .map { recordItem ->
                            async {
                                UserActiveItem(
                                    generation = generationText,
                                    partName = "${recordItem.responsiblePart} Part",
                                    position = "챌린저"
                                )
                            }
                        }

                    roleJobs + recordJobs
                }.awaitAll() // 모든 async 작업이 완료될 때까지 기다림
            }

            //모든 데이터가 수집된 후 UI 업데이트
            updateState {
                copy(myActiveHistory = finalActiveHistory)
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
        val request = UpdateLinkRequest(
            links = listOf(
                LinkItem(LinkType.GITHUB.label, github),
                LinkItem(LinkType.LINKEDIN.label, linkedin),
                LinkItem(LinkType.BLOG.label, blog),
                LinkItem(LinkType.PERSONAL.label, ""),
                LinkItem(LinkType.INSTAGRAM.label, "")
            )
        )

        viewModelScope.launch {
            // DataStore에 저장하고 끝내기
            resultResponse(
                response = updateMyLinkUseCase(request),
                successCallback = {},
                errorCallback = {}
            )
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
    val linkedPlatforms: List<LoginType> = emptyList(),
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