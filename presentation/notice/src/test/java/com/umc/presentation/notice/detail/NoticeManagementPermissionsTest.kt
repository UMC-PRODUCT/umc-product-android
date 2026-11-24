package com.umc.presentation.notice.detail

import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.PermissionItem
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.NETWORK_ERROR_CODE
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeManagementPermissionsTest {
    private val allowed = NoticeDetailUiState(isLoading = false, canEdit = true, canDelete = true)

    @Test fun `네트워크 단절로 갱신에 실패해도 기존 메뉴를 유지한다`() {
        assertSame(allowed, allowed.withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE)))
    }

    @Test fun `서버 일시 오류로 갱신에 실패해도 기존 메뉴를 유지한다`() {
        assertSame(allowed, allowed.withManagementPermissionResult(99L, failure("503")))
    }

    @Test fun `삭제 실패와 후속 권한 조회 실패가 연속되어도 재시도 권한을 유지한다`() {
        val afterDeleteFailure = allowed.withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE))
        val afterRefreshFailure = afterDeleteFailure.withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE))
        assertTrue(afterRefreshFailure.canEdit)
        assertTrue(afterRefreshFailure.canDelete)
    }

    @Test fun `최초 권한 조회 실패에는 권한을 새로 부여하지 않는다`() {
        val state = NoticeDetailUiState().withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE))
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
    }

    @Test fun `명시적 권한 거부는 작성자여도 기존 관리 권한을 지운다`() {
        val state = allowed.copy(isAuthor = true).withManagementPermissionResult(99L, failure("403"))
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
        assertTrue(state.isAuthor)
    }

    @Test fun `권한 거부 뒤 재조회가 실패해도 이전 허용 상태를 복원하지 않는다`() {
        val denied = allowed.withManagementPermissionResult(99L, failure("403"))
        val state = denied.withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE))
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
    }

    @Test fun `갱신 응답의 거부 권한은 기존 허용 권한을 대체한다`() {
        val state = allowed.withManagementPermissionResult(99L, success(edit = false, delete = false))
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
    }

    @Test fun `수정만 허용하는 갱신 응답은 삭제 권한을 해제한다`() {
        val state = allowed.withManagementPermissionResult(99L, success(edit = true, delete = false))
        assertTrue(state.canEdit)
        assertFalse(state.canDelete)
    }

    @Test fun `삭제만 허용하는 갱신 응답은 수정 권한을 해제한다`() {
        val state = allowed.withManagementPermissionResult(99L, success(edit = false, delete = true))
        assertFalse(state.canEdit)
        assertTrue(state.canDelete)
    }

    @Test fun `명시적 권한이 없으면 기존 권한을 지운다`() {
        val state = allowed.withManagementPermissionResult(
            99L, ApiState.Success(AuthorAccess(ResourceType.NOTICE, 99L, emptyList())),
        )
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
    }

    @Test fun `다른 공지나 리소스 응답을 관리 권한으로 사용하지 않는다`() {
        val access = (success(edit = true, delete = true) as ApiState.Success).data
        listOf(access.copy(resourceId = 100L), access.copy(resourceType = ResourceType.SCHEDULE)).forEach {
            val state = allowed.withManagementPermissionResult(99L, ApiState.Success(it))
            assertFalse(state.canEdit)
            assertFalse(state.canDelete)
        }
    }

    @Test fun `실패 뒤 정상 갱신에 성공하면 권한을 다시 사용할 수 있다`() {
        val failed = NoticeDetailUiState().withManagementPermissionResult(99L, failure(NETWORK_ERROR_CODE))
        val state = failed.withManagementPermissionResult(99L, success(edit = true, delete = true))
        assertTrue(state.canEdit)
        assertTrue(state.canDelete)
    }

    @Test fun `관리 권한 갱신은 작성자와 삭제중 상태를 바꾸지 않는다`() {
        val before = allowed.copy(isAuthor = true, isDeleting = true, receiverText = "수신대상")
        val state = before.withManagementPermissionResult(99L, success(edit = false, delete = false))
        assertEquals(before.copy(canEdit = false, canDelete = false), state)
    }

    private fun failure(code: String): ApiState<AuthorAccess> = ApiState.Fail(FailState(code = code, message = "실패"))

    private fun success(edit: Boolean, delete: Boolean): ApiState<AuthorAccess> = ApiState.Success(
        AuthorAccess(ResourceType.NOTICE, 99L, listOf(
            PermissionItem(PermissionType.EDIT, edit),
            PermissionItem(PermissionType.DELETE, delete),
        )),
    )
}
