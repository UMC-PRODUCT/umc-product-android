package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 더미 데이터 상태를 앱 생명주기 동안 유지하는 저장소
 * @Singleton이므로 ViewModel이 재생성되어도 상태가 초기화되지 않음
 * API 연동 후 삭제 가능
 */
@Singleton
class DummyAttendanceRepository @Inject constructor() {

    // ──────────────────────────────────────────────
    // User 세션
    // ──────────────────────────────────────────────
    private val _userSessions = MutableStateFlow(INITIAL_USER_SESSIONS)
    val userSessions: StateFlow<List<UserCheckAvailable>> = _userSessions.asStateFlow()

    fun updateUserSessionStatus(
        sheetId: Long,
        status: CheckAvailableStatus,
        isLocationCertified: Boolean?
    ) {
        _userSessions.value = _userSessions.value.map { session ->
            if (session.sheetId == sheetId) {
                session.copy(status = status, isLocationCertified = isLocationCertified)
            } else session
        }
    }

    // ──────────────────────────────────────────────
    // 출석 히스토리 — 초기 더미 3개 + 승인/반려 시 추가됨
    // ──────────────────────────────────────────────
    private val _userHistory = MutableStateFlow(INITIAL_USER_HISTORY)
    val userHistory: StateFlow<List<UserCheckHistory>> = _userHistory.asStateFlow()

    // ──────────────────────────────────────────────
    // Admin 세션
    // ──────────────────────────────────────────────
    private val _adminSessions = MutableStateFlow(INITIAL_ADMIN_SESSIONS)
    val adminSessions: StateFlow<List<AdminSessionCheck>> = _adminSessions.asStateFlow()

    // sessionId → sheetId 역방향 매핑 (더미 전용)
    private val sessionIdToSheetId = mapOf(
        1L to 101L,
        2L to 102L,
        3L to 103L,
        4L to 104L
    )

    // 유엠씨 유저 ID (더미 전용)
    private val DUMMY_USER_ID = 999L

    // ──────────────────────────────────────────────
    // Admin 세션 함수
    // ──────────────────────────────────────────────

    fun addPendingUser(sessionId: Long, user: AdminPendingUser) {
        _adminSessions.value = _adminSessions.value.map { session ->
            if (session.id == sessionId && session.pendingUsers.none { it.id == user.id }) {
                val updated = session.pendingUsers + user
                session.copy(pendingUsers = updated, pendingCount = updated.size)
            } else session
        }
    }

    fun updateAdminSessionAfterApproval(attendanceId: Long) {
        val targetSession = _adminSessions.value.find { session ->
            session.pendingUsers.any { it.id == attendanceId }
        }

        _adminSessions.value = _adminSessions.value.map { session ->
            val target = session.pendingUsers.find { it.id == attendanceId } ?: return@map session
            val updated = session.pendingUsers.filter { it.id != attendanceId }
            val newAttended = session.attendedChallengers + 1
            val newRate = if (session.totalChallengers > 0) (newAttended * 100) / session.totalChallengers else 0
            session.copy(
                pendingUsers = updated,
                pendingCount = updated.size,
                attendedChallengers = newAttended,
                attendanceRate = newRate
            )
        }

        // 유엠씨 승인 시: 히스토리 PRESENT 추가 + Available COMPLETED 전환
        if (attendanceId == DUMMY_USER_ID && targetSession != null) {
            addHistoryFromSession(targetSession, CheckHistoryStatus.PRESENT)
            val sheetId = sessionIdToSheetId[targetSession.id] ?: return
            updateUserSessionStatus(sheetId, CheckAvailableStatus.COMPLETED, isLocationCertified = true)
        }
    }

    fun updateAdminSessionAfterRejection(attendanceId: Long) {
        val targetSession = _adminSessions.value.find { session ->
            session.pendingUsers.any { it.id == attendanceId }
        }

        _adminSessions.value = _adminSessions.value.map { session ->
            val target = session.pendingUsers.find { it.id == attendanceId } ?: return@map session
            val updated = session.pendingUsers.filter { it.id != attendanceId }
            session.copy(pendingUsers = updated, pendingCount = updated.size)
        }

        // 유엠씨 반려 시: 히스토리 ABSENT 추가 + Available COMPLETED 전환
        if (attendanceId == DUMMY_USER_ID && targetSession != null) {
            addHistoryFromSession(targetSession, CheckHistoryStatus.ABSENT)
            val sheetId = sessionIdToSheetId[targetSession.id] ?: return
            updateUserSessionStatus(sheetId, CheckAvailableStatus.COMPLETED, isLocationCertified = false)
        }
    }

    /**
     * AdminSessionCheck 정보를 기반으로 히스토리 항목 생성 후 추가
     * 태그는 sessionId → sheetId → userSessions에서 조회하여 아이콘 일치
     * 이미 동일 세션 히스토리가 있으면 상태만 업데이트
     */
    private fun addHistoryFromSession(session: AdminSessionCheck, status: CheckHistoryStatus) {
        val sheetId = sessionIdToSheetId[session.id]
        val tags = _userSessions.value.find { it.sheetId == sheetId }?.tags

        val newHistory = UserCheckHistory(
            id = session.id.toInt(),
            title = session.title,
            startTime = session.startTime,
            endTime = session.endTime,
            status = status,
            tags = tags
        )

        val current = _userHistory.value
        val existingIndex = current.indexOfFirst { it.id == newHistory.id }

        _userHistory.value = if (existingIndex != -1) {
            current.toMutableList().also { it[existingIndex] = newHistory }
        } else {
            listOf(newHistory) + current
        }
    }

    /**
     * 더미 데이터를 초기 상태로 리셋 (개발/테스트 전용)
     */
    fun reset() {
        _userSessions.value = INITIAL_USER_SESSIONS
        _userHistory.value = INITIAL_USER_HISTORY
        _adminSessions.value = INITIAL_ADMIN_SESSIONS
    }

    companion object {
        private val INITIAL_USER_SESSIONS = listOf(
            UserCheckAvailable(
                id = 1L, sheetId = 101L,
                title = "9기 데모데이",
                tags = listOf(CategoryType.PRESENTATION),
                startTime = "11:00", endTime = "17:00",
                status = CheckAvailableStatus.BEFORE,
                latitude = 37.495608, longitude = 127.072235,
                address = "SETEC",
                isLocationCertified = null
            ),
            UserCheckAvailable(
                id = 2L, sheetId = 102L,
                title = "9기 OT",
                tags = listOf(CategoryType.ORIENTATION),
                startTime = "17:00", endTime = "18:00",
                status = CheckAvailableStatus.BEFORE,
                latitude = 37.655878, longitude = 127.063968,
                address = "서울여자대학교 50주년기념관 310호",
                isLocationCertified = null
            ),
            UserCheckAvailable(
                id = 3L, sheetId = 103L,
                title = "PM Day",
                tags = listOf(CategoryType.PROJECT),
                startTime = "14:00", endTime = "17:00",
                status = CheckAvailableStatus.PENDING,
                latitude = 37.582566, longitude = 127.010063,
                address = "한성대학교 상상관 203호",
                isLocationCertified = true
            ),
            UserCheckAvailable(
                id = 4L, sheetId = 104L,
                title = "너디너리 해커톤",
                tags = listOf(CategoryType.HACKATHON),
                startTime = "13:00", endTime = "07:00",
                status = CheckAvailableStatus.COMPLETED,
                latitude = 37.546760, longitude = 126.949971,
                address = "서울 창업허브 공덕 10층",
                isLocationCertified = true
            )
        )

        private val INITIAL_USER_HISTORY = listOf(
            UserCheckHistory(
                id = 101,
                title = "안드로이드 스터디",
                startTime = "18:00",
                endTime = "20:00",
                status = CheckHistoryStatus.PRESENT,
                tags = listOf(CategoryType.STUDY)
            ),
            UserCheckHistory(
                id = 102,
                title = "네트워킹데이",
                startTime = "12:00",
                endTime = "17:00",
                status = CheckHistoryStatus.LATE,
                tags = listOf(CategoryType.NETWORKING)
            ),
            UserCheckHistory(
                id = 103,
                title = "9기 LT",
                startTime = "13:00",
                endTime = "07:00",
                status = CheckHistoryStatus.ABSENT,
                tags = listOf(CategoryType.LEADERSHIP)
            )
        )

        private val INITIAL_ADMIN_SESSIONS = listOf(
            AdminSessionCheck(
                id = 1L, title = "9기 데모데이", date = "2025-05-20",
                startTime = "11:00", endTime = "17:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 70, totalChallengers = 10, attendedChallengers = 7,
                pendingCount = 2,
                pendingUsers = listOf(
                    AdminPendingUser(
                        id = 201L, name = "양지애", nickname = "나루",
                        university = "서울여자대학교", profileImageUrl = null,
                        requestTime = "11:15", hasLateReason = false,
                        lateReason = null
                    ),
                    AdminPendingUser(
                        id = 202L, name = "박유수", nickname = "어헛차",
                        university = "숭실대학교", profileImageUrl = null,
                        requestTime = "11:22", hasLateReason = true,
                        lateReason = "교통 혼잡으로 인해 지각하였습니다."
                    )
                )
            ),
            AdminSessionCheck(
                id = 2L, title = "9기 OT", date = "2025-05-21",
                startTime = "17:00", endTime = "18:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 50, totalChallengers = 10, attendedChallengers = 5,
                pendingCount = 1,
                pendingUsers = listOf(
                    AdminPendingUser(
                        id = 203L, name = "조경석", nickname = "조나단",
                        university = "명지대학교", profileImageUrl = null,
                        requestTime = "17:08", hasLateReason = true,
                        lateReason = "수업이 늦게 끝나서 지각하였습니다."
                    )
                )
            ),
            AdminSessionCheck(
                id = 3L, title = "9기 Sprint Review Day", date = "2025-05-19",
                startTime = "18:00", endTime = "20:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 70, totalChallengers = 10, attendedChallengers = 7,
                pendingCount = 0, pendingUsers = emptyList()
            ),
            AdminSessionCheck(
                id = 4L, title = "너디너리 해커톤", date = "2025-05-18",
                startTime = "13:00", endTime = "15:00",
                status = AdminSessionStatus.COMPLETED,
                attendanceRate = 90, totalChallengers = 10, attendedChallengers = 9,
                pendingCount = 0, pendingUsers = emptyList()
            )
        )
    }
}