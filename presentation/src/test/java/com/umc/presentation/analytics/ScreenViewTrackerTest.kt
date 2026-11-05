package com.umc.presentation.analytics

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
        val sink = FakeScreenViewSink()
        val viewModel = ScreenViewTrackerViewModel(sink)
        viewModel.onScreenShown("A", "Home")
        viewModel.onScreenShown("A", "Home")
        assertEquals(listOf("Home"), sink.names)
    }

    @Test fun `returning to an entry after another screen is logged again`() {
        val sink = FakeScreenViewSink()
        val viewModel = ScreenViewTrackerViewModel(sink)
        viewModel.onScreenShown("A", "Home")
        viewModel.onScreenShown("B", "NoticeDetail")
        viewModel.onScreenShown("A", "Home")
        assertEquals(listOf("Home", "NoticeDetail", "Home"), sink.names)
    }

    @Test fun `my card and other member card are told apart`() {
        val route = "${prefix}Mycard?memberId={memberId}&openExchangeDialog={openExchangeDialog}&viewProfileOnly={viewProfileOnly}"
        assertEquals("Mycard", analyticsScreenName(route, isOtherMemberCard = false))
        assertEquals(OTHER_MEMBER_CARD_SCREEN, analyticsScreenName(route, isOtherMemberCard = true))
    }

    private class FakeScreenViewSink : ScreenViewSink {
        val names = mutableListOf<String>()
        override fun logScreenView(screenName: String) {
            names += screenName
        }
    }
}
