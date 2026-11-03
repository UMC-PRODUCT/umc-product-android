package com.umc.domain.model.community

/**
 * 스레드에 대한 내 멤버십 상태
 *
 * 참여한 적 없는 스레드는 상태가 없습니다(null).
 */
enum class CommunityThreadMembershipState {
    /** 참여 중 */
    ACTIVE,

    /** 나감 */
    LEFT,

    /** 강퇴됐지만 아직 확인 전 (목록에 비활성으로 노출됨) */
    KICKED,

    /** 강퇴를 확인함 (목록 조회에서 제외됨) */
    KICKED_ACKNOWLEDGED,
}
