package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
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
    private val _userSessions = MutableStateFlow(
        listOf(
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
    )
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
    // Admin 세션
    // ──────────────────────────────────────────────
    private val _adminSessions = MutableStateFlow(
        listOf(
            AdminSessionCheck(
                id = 1L, title = "9기 데모데이", date = "2025-05-20",
                startTime = "11:00", endTime = "17:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 80, totalChallengers = 10, attendedChallengers = 8,
                pendingCount = 2,
                pendingUsers = listOf(
                    AdminPendingUser(
                        id = 201L, name = "김민준", nickname = "minjun",
                        university = "한국대학교", profileImageUrl = null,
                        requestTime = "11:15", hasLateReason = true,
                        lateReason = "교통 혼잡으로 인해 지각하였습니다."
                    ),
                    AdminPendingUser(
                        id = 202L, name = "이서연", nickname = "seoyeon",
                        university = "서울대학교", profileImageUrl = null,
                        requestTime = "11:22", hasLateReason = false, lateReason = null
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
                        id = 203L, name = "박지호", nickname = "jiho",
                        university = "연세대학교", profileImageUrl = null,
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
    )
    val adminSessions: StateFlow<List<AdminSessionCheck>> = _adminSessions.asStateFlow()

    fun addPendingUser(sessionId: Long, user: AdminPendingUser) {
        _adminSessions.value = _adminSessions.value.map { session ->
            if (session.id == sessionId && session.pendingUsers.none { it.id == user.id }) {
                val updated = session.pendingUsers + user
                session.copy(pendingUsers = updated, pendingCount = updated.size)
            } else session
        }
    }

    fun updateAdminSessionAfterApproval(attendanceId: Long) {
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
    }

    fun updateAdminSessionAfterRejection(attendanceId: Long) {
        _adminSessions.value = _adminSessions.value.map { session ->
            val target = session.pendingUsers.find { it.id == attendanceId } ?: return@map session
            val updated = session.pendingUsers.filter { it.id != attendanceId }
            session.copy(pendingUsers = updated, pendingCount = updated.size)
        }
    }
}