package com.umc.data.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityThreadRole
import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityThreadRequestSerializationTest {
    private val gson = Gson()

    @Test
    fun memberRoleRequestsUseTheServerRoleKey() {
        listOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN, CommunityThreadRole.MEMBER).forEach { role ->
            assertEquals("{\"role\":\"${role.name}\"}", gson.toJson(ChangeMemberRoleRequest(role)))
        }
        // The explicit JSON name must remain available when R8 renames the backing field.
        assertEquals("role", ChangeMemberRoleRequest::class.java.getDeclaredField("role")
            .getAnnotation(SerializedName::class.java)?.value)
    }

    @Test
    fun reportRequestsUseTheServerReasonKey() {
        CommunityMessageReportReason.entries.forEach { reason ->
            assertEquals("{\"reason\":\"${reason.name}\"}", gson.toJson(ReportMessageRequest(reason)))
        }
        assertEquals("reason", ReportMessageRequest::class.java.getDeclaredField("reason")
            .getAnnotation(SerializedName::class.java)?.value)
    }
}
