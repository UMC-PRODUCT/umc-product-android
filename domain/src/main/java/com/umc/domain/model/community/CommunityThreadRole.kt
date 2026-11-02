package com.umc.domain.model.community

enum class CommunityThreadRole {
    OWNER,
    ADMIN,
    MEMBER,
    UNKNOWN,
}

/**
 * 스레드 정보 수정·삭제 권한 여부
 *
 * 방장(OWNER)과 관리자(ADMIN)만 수정·삭제할 수 있습니다.
 */
fun CommunityThreadRole.canEditThread(): Boolean =
    this == CommunityThreadRole.OWNER || this == CommunityThreadRole.ADMIN
