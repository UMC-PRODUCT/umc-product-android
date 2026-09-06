package com.example.mypage.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LinkType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.RolePartItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.model.request.member.LinkItem
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.member.UpdateMyLinkUseCase
import com.umc.domain.usecase.member.UpdateMyProfileUseCase
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.domain.usecase.organization.GetSchoolNameUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val uploadFileUseCase: UploadFileUseCase, //파일 업로드 하기(이미지 업로드)
    private val updateMyProfileUseCase: UpdateMyProfileUseCase, //프로필 정보 업데이트
    private val updateMyLinkUseCase: UpdateMyLinkUseCase, //링크 정보 업데이트
    private val getSchoolNameUseCase: GetSchoolNameUseCase, //학교 이름 가져오기
    private val getChapterDetailUseCase: GetChapterDetailUseCase, //지부 이름 가져오기
    private val getMyOAuthUseCase: GetMyOAuthUseCase, //내 OAuth 가져오기
) : BaseViewModel<ProfileUiState, ProfileEvent>(
    ProfileUiState()){

    //초기화 작업
    init {
        // 프로필 정보 및 소셜 플랫폼 연결 정보 로드
        getUserInfo()
        getUserOAuth()

    }

    /**
     * 서버에서 내 프로필 정보를 수신하여 UI State에 바인딩하고 활동 이력을 가공하는 메서드
     */
    private fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState {
                        copy(
                            userInfo = userInfo,
                            githubLink = userInfo.profile.github,
                            linkedinLink = userInfo.profile.linkedIn,
                            blogLink = userInfo.profile.blog
                        )
                    }
                    processActiveHistory(userInfo)


                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/

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
     * 유저 프로필 데이터에서 활동 기수, 운영진 및 챌린저 기록을 비동기로 수집하여 활동 이력 목록을 만드는 메서드
     *
     * @param userInfo 서버 수신 유저 프로필 모델
     */
    fun processActiveHistory(userInfo: UserInfo){

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
                            createRoleActiveItem(generationText, roleItem)
                        }
                    }

                    //챌린저 기록
                    val recordJobs = summary.fromRecords
                        .filter { it.responsiblePart != "ADMIN" }
                        .map { recordItem ->
                            async {
                                createChallengerActiveItem(generationText, recordItem)
                            }
                        }

                    roleJobs + recordJobs
                }.awaitAll() // 모든 async 작업이 완료될 때까지 기다림
            }

            //모든 데이터가 수집된 후 UI 업데이트
            updateState {
                copy(myActiveHistory = finalActiveHistory.toImmutableList())
            }
        }


    }

    /**
     * 운영진 이력 정보(학교, 지부, 총괄 등)를 조회하여 UserActiveItem 객체를 생성하는 메서드
     */
    private suspend fun createRoleActiveItem(generationText: String, roleItem: RolePartItem): UserActiveItem {
        //담당 파트가 있는 경우 (ex. 안드로이드 파트장)
        if (roleItem.responsiblePart != null) {
            return UserActiveItem(
                generation = generationText,
                partName = "${UserPart.from(roleItem.responsiblePart).label} Part",
                position = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
            )
        }
        //그 외 - 학교 단위 운영진인 경우
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
            return itemResult!!
        }
        //그 외 - 지부 단위 운영진인 경우
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
            return itemResult!!
        }
        //그 외 - 중앙/총괄 운영진인 경우
        else{
            val label = UserChallengerRole.from(roleItem.role).displayName ?: roleItem.role
            val itemResult = UserActiveItem(generationText, label, "총괄")
            return itemResult
        }
    }

    /**
     * 일반 챌린저 이력 아이템을 생성하는 메서드
     */
    private fun createChallengerActiveItem(generationText: String, recordItem: RolePartItem): UserActiveItem {
        return UserActiveItem(
            generation = generationText,
            partName = "${UserPart.from(recordItem.responsiblePart).label} Part",
            position = "챌린저"
        )
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
     * 상단 완료 버튼 클릭 시 외부 링크 저장 및 프로필 이미지 업로드를 트리거하는 메서드
     */
    fun onClickComplete(){
        val nowUri = uiState.value.userProfileImageUri

        //1. 아래의 saveAndExit를 수행 - 3개의 outLink를 저장 (fragment에서 데이터를 가져
        emitEvent(ProfileEvent.ClickComplete)

        //2. 이미지 파일을 업로드하고, 바꾸기(여기서 이미지를 바꿀 경우 = Uri가 empty가 아닐 경우만 진행)
        if(nowUri != Uri.EMPTY){
            updateProfileImage(nowUri)
        }
        else{
            emitEvent(ProfileEvent.ClickBackPressed)
        }
    }

    /**
     * 외부 소셜 링크 3종(GitHub, LinkedIn, Blog) 정보를 서버에 전송하여 업데이트하는 메서드
     */
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
            // 서버에 저장하기
            resultResponse(
                response = updateMyLinkUseCase(request),
                successCallback = {},
                errorCallback = {}
            )
        }
    }

    /**
     * 선택된 프로필 이미지 파일(URI)을 서버에 업로드하고 발급받은 fileId로 회원 프로필을 업데이트하는 메서드
     *
     * @param uri 업로드할 이미지 URI
     */
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
                                emitEvent(ProfileEvent.ClickBackPressed)
                            },
                            errorCallback = {

                            }
                        )
                    }

                },
                errorCallback = {
                    emitEvent(ProfileEvent.MakeToast(
                        it.message))

                    Log.d("log_mypage", "실패! $it")
                }
            )
        }

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


    //임시 정보
    val myActiveHistory: ImmutableList<UserActiveItem> = persistentListOf(),

    ) : UiState

sealed interface ProfileEvent : UiEvent {

    //갤러리 터치 이벤트
    object ClickProfileImage : ProfileEvent

    //토스트 만들기
    data class MakeToast(val message: String) : ProfileEvent


    //완료 버튼을 눌렀을 때
    object ClickComplete : ProfileEvent

    //그냥 뒤로 가기
    object ClickBackPressed : ProfileEvent
}