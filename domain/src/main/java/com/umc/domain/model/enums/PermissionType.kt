package com.umc.domain.model.enums

//리소스 접근 권한 확인용 조회 시 권한 유형 (ResourceType하고 연관)
enum class PermissionType {
    READ,
    WRITE,
    DELETE,
    APPROVE,
    CHECK,
    MANAGE
}