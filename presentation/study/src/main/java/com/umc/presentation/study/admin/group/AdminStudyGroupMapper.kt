package com.umc.presentation.study.admin.group

import com.umc.domain.model.organization.ManagedStudyGroup
import kotlinx.collections.immutable.toImmutableList

/**
 * 서버에서 조회한 ManagedStudyGroup을
 * 관리자 스터디 그룹 화면용 UI 모델로 변환합니다.
 *
 * 그룹 기본 정보뿐 아니라
 * 파트장과 스터디원의 학교 및 프로필 이미지 정보도
 * 함께 매핑합니다.
 */
fun ManagedStudyGroup.toUiModel(): AdminStudyGroupItemUiModel {

    /**
     * mentors 목록의 첫 번째 멤버를
     * 해당 그룹의 담당 파트장으로 사용합니다.
     */
    val leader = mentors.firstOrNull()

    return AdminStudyGroupItemUiModel(
        groupId = studyGroupId,
        title = name,

        // 트랙 기수면 트랙, 파트 기수면 파트를 화면 표시용 이름으로 변환
        partLabel = displayPart.label,

        // 서버 원본 파트 값 유지
        studyPart = studyPart,

        // 담당 파트장 이름
        leaderName = leader
            ?.memberName
            .orEmpty(),

        /**
         * Managed API에는 challengerId가 존재하지 않으므로
         * memberId를 화면 내부 식별값으로 사용합니다.
         */
        leaderChallengerId =
            leader?.memberId ?: 0L,

        // 담당 파트장 프로필 이미지
        leaderProfileImageUrl =
            leader?.profileImageUrl,

        /**
         * 서버의 그룹 멤버 목록을
         * 화면용 멤버 UI 모델로 변환합니다.
         */
        members = members.map { member ->
            AdminStudyGroupMemberUiModel(
                challengerId = member.memberId,
                name = member.memberName,
                school = member.schoolName,

                // 스터디원 프로필 이미지
                profileImageUrl = member.profileImageUrl,
            )
        }.toImmutableList(),

        /**
         * 멤버 변경 비교 및 API 요청 등에 사용할
         * 현재 멤버 ID 목록
         */
        memberChallengerIds = members.map { member ->
            member.memberId
        }.toImmutableList(),

        createdAtRaw = createdAt,

        // 현재 그룹의 스터디원 수
        memberCount = members.size,

        // 담당 파트장 학교
        leaderUniv = leader
            ?.schoolName
            .orEmpty(),
    )
}

// 표기는 UserPart 한 곳에서만 정의합니다. 여기서 다시 만들지 않습니다.