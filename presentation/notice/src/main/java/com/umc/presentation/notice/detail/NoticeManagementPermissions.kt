package com.umc.presentation.notice.detail

import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType

internal fun NoticeDetailUiState.withDetailLoadFailure(failure: FailState): NoticeDetailUiState =
    withManagementPermissionFailure(failure).copy(isLoading = false)

/** 같은 공지의 갱신 실패는 기존 권한을 유지한다. 명시적인 거부나 새 권한 응답은 즉시 반영한다. */
internal fun NoticeDetailUiState.withManagementPermissionResult(
    noticeId: Long,
    result: ApiState<AuthorAccess>,
): NoticeDetailUiState = when (result) {
    is ApiState.Success -> {
        val access = result.data
        // 다른 리소스 응답이나 누락된 권한을 작성자·역할 정보로 보완하지 않는다.
        val matchesNotice = access.resourceType == ResourceType.NOTICE && access.resourceId == noticeId
        copy(
            canEdit = matchesNotice && access.permissions.any { it.type == PermissionType.EDIT && it.hasPermission },
            canDelete = matchesNotice && access.permissions.any { it.type == PermissionType.DELETE && it.hasPermission },
        )
    }
    is ApiState.Fail -> withManagementPermissionFailure(result.failState)
}

private fun NoticeDetailUiState.withManagementPermissionFailure(failure: FailState): NoticeDetailUiState {
    // HTTP 상태 대신 서버 본문 코드가 올 수 있어, 미확인 업무 코드를 권한 거부로 추측하지 않는다.
    return if (failure.code == "403" || failure.code.endsWith("-403")) copy(canEdit = false, canDelete = false) else this
}
