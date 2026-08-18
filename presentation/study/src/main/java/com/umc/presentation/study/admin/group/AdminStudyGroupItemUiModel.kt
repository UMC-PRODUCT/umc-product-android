package com.umc.presentation.study.admin.group

/**
 * 관리자 스터디 그룹 목록에서 사용하는 그룹 UI 모델
 *
 * 서버의 스터디 그룹 데이터를
 * 화면에서 바로 사용할 수 있는 형태로 변환하여 저장합니다.
 *
 * 주요 정보
 * - 그룹 기본 정보
 * - 담당 파트장 정보
 * - 스터디원 정보
 * - 프로필 이미지
 * - 그룹 생성일
 */
data class AdminStudyGroupItemUiModel(

    /** 스터디 그룹 ID */
    val groupId: Long,

    /** 스터디 그룹 이름 */
    val title: String,

    /** 화면에 표시할 파트명 */
    val partLabel: String,

    /** 담당 파트장 이름 */
    val leaderName: String,

    /**
     * 담당 파트장 식별 ID
     *
     * 현재 ManagedStudyGroup API에는 challengerId가 없기 때문에
     * memberId 값을 사용합니다.
     */
    val leaderChallengerId: Long,

    /** 담당 파트장 프로필 이미지 URL */
    val leaderProfileImageUrl: String? = null,

    /** 현재 그룹에 포함된 스터디원 목록 */
    val members: List<AdminStudyGroupMemberUiModel> = emptyList(),

    /**
     * 현재 그룹 멤버 식별 ID 목록
     *
     * 현재 ManagedStudyGroup API에서는 memberId를 사용합니다.
     */
    val memberChallengerIds: List<Long> = emptyList(),

    /** 서버에서 전달받은 원본 생성일 */
    val createdAtRaw: String,

    /** 현재 그룹 멤버 수 */
    val memberCount: Int,

    /** 담당 파트장 학교 */
    val leaderUniv: String,

    /** API에서 사용하는 원본 파트 값 */
    val studyPart: String,
) {

    /**
     * 서버 생성일을 화면 표시 형식으로 변환합니다.
     *
     * 예)
     * 2026-08-18T12:30:00
     * ->
     * 생성일: 2026.08.18
     */
    val createdAtText: String
        get() = createdAtRaw
            .takeIf { it.length >= 10 }
            ?.let {
                "생성일: ${it.take(10).replace("-", ".")}"
            }
            ?: "생성일: -"
}