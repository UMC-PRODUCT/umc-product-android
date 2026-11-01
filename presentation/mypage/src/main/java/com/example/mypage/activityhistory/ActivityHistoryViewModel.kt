package com.example.mypage.activityhistory

import com.umc.component.theme.AppStrings
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.RolePartItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.domain.usecase.organization.GetSchoolNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

// 학교·지부 소속이 아닌 중앙 운영진 역할
private val CENTRAL_ROLES = setOf(
    UserChallengerRole.SUPER_ADMIN,
    UserChallengerRole.CENTRAL_PRESIDENT,
    UserChallengerRole.CENTRAL_VICE_PRESIDENT,
    UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
    UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
)

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getSchoolNameUseCase: GetSchoolNameUseCase, //학교 이름 가져오기
    private val getChapterDetailUseCase: GetChapterDetailUseCase, //지부 이름 가져오기
) : BaseViewModel<ActivityHistoryUiState, ActivityHistoryEvent>(
    ActivityHistoryUiState()
) {

    init {
        loadActivityHistory()
    }

    /**
     * 조회 실패 안내에서 '다시 시도하기'를 눌렀을 때 호출
     */
    fun onClickRetry() {
        //재시도 요청이 진행 중이면 중복 호출하지 않는다
        if (uiState.value.isLoading) return
        loadActivityHistory()
    }

    /**
     * 서버에서 내 프로필 정보를 받아 활동 이력을 만드는 메서드
     */
    private fun loadActivityHistory() {
        // 재시도 중에는 이전 실패 안내 대신 진행 표시를 보이고, 다시 실패하면 errorCallback 이 실패 상태로 되돌린다
        updateState { copy(isLoading = true, isLoadFailed = false) }
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    processActiveHistory(userInfo)
                },
                errorCallback = {
                    updateState { copy(isLoading = false, isLoadFailed = true) }
                }
            )
        }
    }

    /**
     * 유저 프로필 데이터에서 활동 기수, 운영진 및 챌린저 기록을 비동기로 수집하여 활동 이력 목록을 만드는 메서드
     *
     * @param userInfo 서버 수신 유저 프로필 모델
     */
    private fun processActiveHistory(userInfo: UserInfo) {

        viewModelScope.launch {
            //기수 정보 리스트 가져오기
            val gisuSummaryList = userInfo.getGisuSummaryList()

            //api 호출 시 coroutineScope를 사용하여 async를 호출 - 다 끝날때까지 기달
            val finalActiveHistory = coroutineScope {
                gisuSummaryList.flatMap { summary ->
                    val generationText = AppStrings.ACTIVITY_HISTORY_GENERATION_FORMAT.format(summary.gisu)
                    val hasRole = summary.fromRoles.isNotEmpty()

                    //운영진 기록
                    val roleJobs = summary.fromRoles.map { roleItem ->
                        async {
                            createRoleActiveItem(generationText, roleItem, userInfo)
                        }
                    }

                    //챌린저 기록
                    // 운영진 역할이 있는 기수의 파트 없는 레코드는 그 역할의 주인 레코드이므로 '미정 Part 챌린저' 행으로 또 그리지 않는다
                    val recordJobs = summary.fromRecords
                        .filter { it.responsiblePart != "ADMIN" }
                        .filter { !(hasRole && UserPart.from(it.responsiblePart) == UserPart.UNKNOWN) }
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
                copy(
                    histories = finalActiveHistory.toImmutableList(),
                    isLoading = false,
                    isLoadFailed = false,
                )
            }
        }
    }

    /**
     * 운영진 이력 정보(학교, 지부, 총괄 등)를 조회하여 UserActiveItem 객체를 생성하는 메서드
     */
    private suspend fun createRoleActiveItem(
        generationText: String,
        roleItem: RolePartItem,
        userInfo: UserInfo
    ): UserActiveItem {
        val role = UserChallengerRole.from(roleItem.role)
        val roleLabel = role.displayName ?: roleItem.role

        // v2 응답에는 역할의 조직 정보가 없어 모든 역할이 SCHOOL 로 복원되므로,
        // 학교 소속이 아닌 중앙 운영진·지부장은 역할 종류로 먼저 분기한다
        if (role in CENTRAL_ROLES) {
            return UserActiveItem(generationText, roleLabel, roleLabel)
        }
        if (role == UserChallengerRole.CHAPTER_PRESIDENT) {
            val chapterName = userInfo.challengerRecords
                .firstOrNull { it.challengerId == roleItem.challengerId }
                ?.chapterName
            return if (!chapterName.isNullOrBlank()) {
                UserActiveItem(generationText, AppStrings.ACTIVITY_HISTORY_CHAPTER_ROLE_FORMAT.format(chapterName, roleLabel), roleLabel)
            } else {
                UserActiveItem(generationText, roleLabel, roleLabel)
            }
        }

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
                    itemResult = UserActiveItem(generationText, AppStrings.ACTIVITY_HISTORY_CHAPTER_ROLE_FORMAT.format(chapterInfo.name, label),label, )
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
            val itemResult = UserActiveItem(generationText, label, label)
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
            position = AppStrings.ACTIVITY_HISTORY_CHALLENGER
        )
    }
}

data class ActivityHistoryUiState(
    val histories: ImmutableList<UserActiveItem> = persistentListOf(),
    val isLoading: Boolean = false, //내 정보 조회와 이력 가공이 끝나기 전까지
    // 내 정보 조회 실패 여부 (true 면 빈 목록 대신 실패 안내 + 다시 시도 표시)
    val isLoadFailed: Boolean = false,
) : UiState

sealed interface ActivityHistoryEvent : UiEvent
