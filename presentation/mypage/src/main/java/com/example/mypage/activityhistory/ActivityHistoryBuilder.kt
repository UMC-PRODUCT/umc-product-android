package com.example.mypage.activityhistory

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.RolePartItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.domain.usecase.organization.GetSchoolNameUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

// 학교·지부 소속이 아닌 중앙 운영진 역할
private val CENTRAL_ROLES = setOf(
    UserChallengerRole.SUPER_ADMIN,
    UserChallengerRole.CENTRAL_PRESIDENT,
    UserChallengerRole.CENTRAL_VICE_PRESIDENT,
    UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
    UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
)

/**
 * 회원 정보에서 기수별 운영진·챌린저 활동 이력 목록을 만든다.
 *
 * 마이페이지 활동 이력과 타인 프로필 조회가 같은 결과를 보여주도록 한 곳에 둔다.
 * 학교·지부 단위 운영진은 소속 이름을 조회해 붙이고, 조회에 실패하면 직함만 쓴다.
 */
class ActivityHistoryBuilder @Inject constructor(
    private val getSchoolNameUseCase: GetSchoolNameUseCase, //학교 이름 가져오기
    private val getChapterDetailUseCase: GetChapterDetailUseCase, //지부 이름 가져오기
) {

    /**
     * 유저 프로필 데이터에서 활동 기수, 운영진 및 챌린저 기록을 비동기로 수집하여 활동 이력 목록을 만드는 메서드
     *
     * @param userInfo 서버 수신 유저 프로필 모델
     */
    suspend fun build(userInfo: UserInfo): List<UserActiveItem> = coroutineScope {
        //기수 정보 리스트 가져오기
        val gisuSummaryList = userInfo.getGisuSummaryList()

        gisuSummaryList.flatMap { summary ->
            val generationText = "${summary.gisu}기"
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
                UserActiveItem(generationText, "${chapterName}지부 $roleLabel", roleLabel)
            } else {
                UserActiveItem(generationText, roleLabel, roleLabel)
            }
        }

        //담당 파트가 있는 경우 (ex. 안드로이드 파트장)
        if (roleItem.responsiblePart != null) {
            return UserActiveItem(
                generation = generationText,
                partName = "${UserPart.from(roleItem.responsiblePart).label} Part",
                position = roleLabel
            )
        }
        //그 외 - 학교 단위 운영진인 경우 (학교 이름을 못 불러오면 직함만 넣는다)
        if (roleItem.organizationType == "SCHOOL") {
            return when (val result = getSchoolNameUseCase(roleItem.organizationId)) {
                is ApiState.Success -> UserActiveItem(generationText, "${result.data.schoolName} $roleLabel", roleLabel)
                is ApiState.Fail -> UserActiveItem(generationText, roleLabel, roleLabel)
            }
        }
        //그 외 - 지부 단위 운영진인 경우 (지부 이름을 못 불러오면 직함만 넣는다)
        if (roleItem.organizationType == "CHAPTER") {
            return when (val result = getChapterDetailUseCase(roleItem.organizationId)) {
                is ApiState.Success -> UserActiveItem(generationText, "${result.data.name}지부 $roleLabel", roleLabel)
                is ApiState.Fail -> UserActiveItem(generationText, roleLabel, roleLabel)
            }
        }
        //그 외 - 중앙/총괄 운영진인 경우
        return UserActiveItem(generationText, roleLabel, roleLabel)
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
}
