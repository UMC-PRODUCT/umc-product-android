package com.umc.presentation.community.analytics

import com.umc.domain.model.community.thread.CommunityMessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityAnalyticsTest {
    private val eventNames = listOf(
        CommunityAnalytics.THREAD_CREATE_ATTEMPTED,
        CommunityAnalytics.THREAD_CREATED,
        CommunityAnalytics.THREAD_CREATE_FAILED,
        CommunityAnalytics.THREAD_DELETED,
        CommunityAnalytics.AI_CLASSIFY_ATTEMPTED,
        CommunityAnalytics.AI_CLASSIFY_COMPLETED,
        CommunityAnalytics.AI_CLASSIFY_FAILED,
        CommunityAnalytics.LIST_VIEWED,
        CommunityAnalytics.THREAD_VIEWED,
        CommunityAnalytics.MESSAGE_SENT,
        CommunityAnalytics.SUMMARY_ATTEMPTED,
        CommunityAnalytics.SUMMARY_COMPLETED,
        CommunityAnalytics.SUMMARY_FAILED,
    )
    private val paramNames = listOf(
        CommunityAnalytics.PARAM_CATEGORY,
        CommunityAnalytics.PARAM_HAS_INVITEES,
        CommunityAnalytics.PARAM_IS_JOINED,
        CommunityAnalytics.PARAM_MESSAGE_TYPE,
        CommunityAnalytics.PARAM_HAS_REPLY,
        CommunityAnalytics.PARAM_HAS_MENTION,
        CommunityAnalytics.PARAM_TRIGGER,
        CommunityAnalytics.PARAM_METHOD,
    )

    // GA4: 영문으로 시작, 영문·숫자·밑줄, 40자 이하, firebase_·google_·ga_ 접두사 금지
    private val validName = Regex("^[A-Za-z][A-Za-z0-9_]{0,39}$")
    private val reservedPrefixes = listOf("firebase_", "google_", "ga_")

    @Test fun `event and parameter names follow analytics naming rules`() {
        (eventNames + paramNames).forEach { name ->
            assertTrue(name, validName.matches(name))
            assertFalse(name, reservedPrefixes.any(name::startsWith))
        }
        assertEquals(eventNames.size, eventNames.toSet().size)
    }

    @Test fun `event names that iOS already sends stay identical`() {
        assertEquals("community_thread_created", CommunityAnalytics.THREAD_CREATED)
        assertEquals("community_thread_deleted", CommunityAnalytics.THREAD_DELETED)
        assertEquals("community_thread_viewed", CommunityAnalytics.THREAD_VIEWED)
        assertEquals("community_message_sent", CommunityAnalytics.MESSAGE_SENT)
        assertEquals("community_summary_attempted", CommunityAnalytics.SUMMARY_ATTEMPTED)
    }

    @Test fun `category becomes the lowercase server value like iOS`() {
        assertEquals("study", CommunityAnalytics.categoryValue("STUDY"))
        assertEquals("qna", CommunityAnalytics.categoryValue("QNA"))
        assertEquals("project", CommunityAnalytics.categoryValue(" project "))
        assertEquals("free", CommunityAnalytics.categoryValue("FREE"))
        assertEquals("free", CommunityAnalytics.categoryValue("UNKNOWN"))
    }

    @Test fun `parameter values are strings and never carry content`() {
        assertEquals(
            mapOf("category" to "qna", "has_invitees" to "true"),
            CommunityAnalytics.threadCreatedParams("QNA", hasInvitees = true),
        )
        assertEquals(
            mapOf("message_type" to "image", "has_reply" to "false", "has_mention" to "true"),
            CommunityAnalytics.messageSentParams(CommunityMessageType.IMAGE, hasReply = false, hasMention = true),
        )
        assertEquals(
            mapOf("trigger" to "manual", "method" to "model", "category" to "study"),
            CommunityAnalytics.classifyParams(CommunityAnalytics.TRIGGER_MANUAL, CommunityAnalytics.METHOD_MODEL, "STUDY"),
        )
        assertEquals(
            mapOf("trigger" to "auto", "method" to "rule"),
            CommunityAnalytics.classifyParams(CommunityAnalytics.TRIGGER_AUTO, CommunityAnalytics.METHOD_RULE),
        )
    }
}
