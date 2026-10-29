package com.umc.data.repository.challenger

import com.umc.data.dataSource.local.AppDataStore
import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.map
import com.umc.domain.model.enums.RoleType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 챌린저·회원을 검색할 때 어느 학교까지 보여줄지 정한다.
 *
 * 중앙운영사무국(활성 기수의 CENTRAL_* 역할)과 SUPER_ADMIN 만 전 학교를 보고,
 * 그 밖의 사용자는 운영진이든 아니든 본인 학교만 본다.
 */
sealed interface ChallengerSearchScope {
    data object AllSchools : ChallengerSearchScope

    // activeGisuId 는 기수를 지정하지 않은 검색을 활성 기수로 맞추는 데 쓴다(운영진 검색의 기본 동작과 같게)
    data class OwnSchool(val schoolId: Long, val activeGisuId: Long?) : ChallengerSearchScope
}

class ChallengerSearchScopeProvider @Inject constructor(
    private val appDataStore: AppDataStore,
    private val memberRemoteDataSource: MemberRemoteDataSource,
) {
    suspend fun current(): ApiState<ChallengerSearchScope> {
        val cached = appDataStore.userInfoFlow.first()
        // 로그인 직후처럼 내 정보가 아직 저장되지 않았으면 서버에서 받아 판단한다
        val userInfo = if (cached.id > 0 && cached.schoolId > 0) {
            ApiState.Success(cached)
        } else {
            memberRemoteDataSource.getMyProfile().map { it.toDomain() }
        }
        return when (userInfo) {
            is ApiState.Success -> userInfo.data.toScope()
            is ApiState.Fail -> userInfo
        }
    }

    private fun UserInfo.toScope(): ApiState<ChallengerSearchScope> {
        val isCentral = isSuperAdmin ||
            currentGisuMemberInfo?.roleTypes.orEmpty().any { it in CENTRAL_ROLE_TYPES }
        if (isCentral) return ApiState.Success(ChallengerSearchScope.AllSchools)
        // 학교를 모르면 전 학교로 넓히지 않고 실패로 돌린다
        if (schoolId <= 0) {
            return ApiState.Fail(FailState(code = "SCHOOL_UNKNOWN", message = "내 학교 정보를 불러오지 못했어요. 다시 시도해주세요."))
        }
        return ApiState.Success(ChallengerSearchScope.OwnSchool(schoolId, currentGisuMemberInfo?.gisuId))
    }

    private companion object {
        val CENTRAL_ROLE_TYPES = setOf(
            RoleType.CENTRAL_PRESIDENT,
            RoleType.CENTRAL_VICE_PRESIDENT,
            RoleType.CENTRAL_OPERATING_TEAM_MEMBER,
            RoleType.CENTRAL_EDUCATION_TEAM_MEMBER,
        ).map { it.name }.toSet()
    }
}
