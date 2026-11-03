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
    fun messageReportsSendReasonsArrayEvenForOneSelection() {
        CommunityMessageReportReason.entries.forEach { reason ->
            assertEquals("{\"reasons\":[\"${reason.name}\"]}", gson.toJson(ReportMessageRequest(listOf(reason))))
        }
        assertEquals("reasons", ReportMessageRequest::class.java.getDeclaredField("reasons")
            .getAnnotation(SerializedName::class.java)?.value)
    }

    @Test
    fun messageReportContainsAllSelectedReasonsInOnePayload() {
        val reasons = listOf(CommunityMessageReportReason.SPAM, CommunityMessageReportReason.ABUSE, CommunityMessageReportReason.ETC)
        assertEquals("{\"reasons\":[\"SPAM\",\"ABUSE\",\"ETC\"]}", gson.toJson(ReportMessageRequest(reasons)))
    }

    @Test
    fun messageReportReceiptReadsAllAcceptedReasons() {
        val json = """{"reportId":"1","messageId":"2","reason":"SPAM","reasons":["SPAM","ABUSE"],"createdAt":"2026-11-02T00:00:00Z"}"""
        val receipt = gson.fromJson(json, com.umc.domain.model.community.thread.CommunityMessageReportReceipt::class.java)
        assertEquals(listOf(CommunityMessageReportReason.SPAM, CommunityMessageReportReason.ABUSE), receipt.reasons)
    }
}
