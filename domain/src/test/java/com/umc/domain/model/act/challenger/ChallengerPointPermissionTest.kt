package com.umc.domain.model.act.challenger

import com.umc.domain.model.CurrentGisuMemberInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.RewardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengerPointPermissionTest {
    private val targetGisuId = 11L
    private val targetSchoolId = 3L

    private fun role(type: String, gisuId: Long = targetGisuId, schoolId: Long = targetSchoolId) = UserRole(
        id = 0L, challengerId = 1L, roleType = type, chapterId = null, chapterName = null,
        organizationType = "SCHOOL", organizationId = schoolId, responsiblePart = null,
        gisuId = gisuId, gisu = gisuId,
    )

    private fun permission(vararg roles: UserRole, isSuperAdmin: Boolean = false) =
        ChallengerPointPermission.of(isSuperAdmin, roles.toList(), targetGisuId, targetSchoolId)

    @Test fun `super admin can do everything`() {
        assertEquals(ChallengerPointPermission(true, true, true, true), permission(isSuperAdmin = true))
    }

    @Test fun `central president and vice president can delete`() {
        listOf("CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT").forEach { type ->
            assertEquals(ChallengerPointPermission(true, true, true, true), permission(role(type)))
        }
    }

    @Test fun `central team members manage but cannot delete`() {
        listOf("CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER").forEach { type ->
            // v2 프로필 역할은 중앙 역할도 SCHOOL 로 채워지므로 학교와 무관해야 한다
            assertEquals(ChallengerPointPermission(true, true, true, false), permission(role(type, schoolId = 99L)))
        }
    }

    @Test fun `school president of target school manages but cannot delete`() {
        listOf("SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT").forEach { type ->
            assertEquals(ChallengerPointPermission(true, true, true, false), permission(role(type)))
        }
    }

    @Test fun `school president of another school has no permission`() {
        assertEquals(ChallengerPointPermission.NONE, permission(role("SCHOOL_PRESIDENT", schoolId = 99L)))
    }

    @Test fun `part leader etc admin and chapter president have no permission`() {
        listOf("SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN", "CHAPTER_PRESIDENT").forEach { type ->
            assertEquals(ChallengerPointPermission.NONE, permission(role(type)))
        }
    }

    @Test fun `central role of another gisu has no permission`() {
        assertEquals(ChallengerPointPermission.NONE, permission(role("CENTRAL_PRESIDENT", gisuId = 10L)))
    }

    @Test fun `unknown target gisu has no permission`() {
        val result = ChallengerPointPermission.of(false, listOf(role("CENTRAL_PRESIDENT")), null, targetSchoolId)
        assertEquals(ChallengerPointPermission.NONE, result)
    }

    @Test fun `server read keeps delete decision`() {
        val result = permission(role("SCHOOL_PRESIDENT", schoolId = 99L)).withServerReadAllowed()
        assertEquals(ChallengerPointPermission(true, true, true, false), result)
    }

    @Test fun `management tab is open only for roles that can read details in active gisu`() {
        fun user(vararg roleTypes: String, isSuperAdmin: Boolean = false) = UserInfo(
            isSuperAdmin = isSuperAdmin,
            currentGisuMemberInfo = CurrentGisuMemberInfo(
                gisuId = targetGisuId, generation = 11L, challenger = null, isAdmin = true, roleTypes = roleTypes.toList(),
            ),
        )
        assertTrue(user("CENTRAL_OPERATING_TEAM_MEMBER").canManageChallengerPoints())
        assertTrue(user("SCHOOL_VICE_PRESIDENT").canManageChallengerPoints())
        assertTrue(user(isSuperAdmin = true).canManageChallengerPoints())
        assertFalse(user("SCHOOL_PART_LEADER").canManageChallengerPoints())
        assertFalse(user("SCHOOL_ETC_ADMIN").canManageChallengerPoints())
        assertFalse(user("CHAPTER_PRESIDENT").canManageChallengerPoints())
        assertFalse(UserInfo().canManageChallengerPoints())
    }

    @Test fun `best workbook reward sends server v2 type worth two points`() {
        val bonus = RewardType.getBonusList()
        assertTrue(RewardType.BEST_WORKBOOK_V2 in bonus)
        assertEquals(2, RewardType.BEST_WORKBOOK_V2.score)
        assertFalse(bonus.any { it.name == "BEST_WORKBOOK" })
    }
}
