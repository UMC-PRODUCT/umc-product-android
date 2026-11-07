package com.umc.presentation.analytics

import com.umc.domain.analytics.AnalyticsLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScreenViewTrackerTest {
    private val prefix = "com.umc.presentation.MainDestination."

    @Test fun `screen name drops package and route arguments`() {
        assertEquals("Home", analyticsScreenName("${prefix}Home", isOtherMemberCard = false))
        assertEquals("NoticeDetail", analyticsScreenName("${prefix}NoticeDetail/{noticeId}", isOtherMemberCard = false))
        assertEquals("ScheduleDetail", analyticsScreenName("${prefix}ScheduleDetail/{plusDay}?scheduleId={scheduleId}", isOtherMemberCard = false))
    }

    @Test fun `sign up route never sends tokens or password names`() {
        val route = "${prefix}SignUp/{signUpType}?oAuthVerificationToken={oAuthVerificationToken}" +
            "&emailVerificationToken={emailVerificationToken}&rawPassword={rawPassword}"
        val name = analyticsScreenName(route, isOtherMemberCard = false)
        assertEquals("SignUp", name)
        assertFalse(name.contains("Token") || name.contains("Password"))
    }

    @Test fun `same back stack entry is logged only once`() {
        val logger = FakeAnalyticsLogger()
        val viewModel = ScreenViewTrackerViewModel(logger)
        viewModel.onScreenShown("A", "Home")
        viewModel.onScreenShown("A", "Home")
        assertEquals(listOf("Home"), logger.screenNames)
        assertEquals(mapOf("screen_name" to "Home", "screen_class" to "Home"), logger.events.single().second)
    }

    @Test fun `returning to an entry after another screen is logged again`() {
        val logger = FakeAnalyticsLogger()
        val viewModel = ScreenViewTrackerViewModel(logger)
        viewModel.onScreenShown("A", "Home")
        viewModel.onScreenShown("B", "NoticeDetail")
        viewModel.onScreenShown("A", "Home")
        assertEquals(listOf("Home", "NoticeDetail", "Home"), logger.screenNames)
    }

    @Test fun `my card and other member card are told apart`() {
        val route = "${prefix}Mycard?memberId={memberId}&openExchangeDialog={openExchangeDialog}&viewProfileOnly={viewProfileOnly}"
        assertEquals("Mycard", analyticsScreenName(route, isOtherMemberCard = false))
        assertEquals(OTHER_MEMBER_CARD_SCREEN, analyticsScreenName(route, isOtherMemberCard = true))
    }

    private class FakeAnalyticsLogger : AnalyticsLogger {
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        val screenNames get() = events.filter { it.first == "screen_view" }.map { it.second.getValue("screen_name") }
        override fun log(name: String, params: Map<String, String>) {
            events += name to params
        }
    }
}
