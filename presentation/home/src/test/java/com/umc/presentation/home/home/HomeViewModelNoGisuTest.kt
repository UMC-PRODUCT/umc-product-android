package com.umc.presentation.home.home

import androidx.lifecycle.SavedStateHandle
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.enums.UserType
import com.umc.domain.repository.OrganizationRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.repository.schedule.ScheduleRepository
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

/** 챌린저 기록 없이 메인에 들어온 프로덕트팀 계정(isUmcProduct)의 홈 상태 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelNoGisuTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    /** 모든 서버 호출이 실패로 끝나는 리포지토리. 기수 조회가 실패하면 홈은 userType 을 ACTIVE 로 확정한다 */
    private inline fun <reified T> failingRepository(): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
            when (method.name) {
                "toString" -> "failing ${T::class.java.simpleName}"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> ApiState.Fail(FailState(code = "TEST", message = "test"))
            }
        } as T

    private fun viewModel(): HomeViewModel {
        val organizationRepository = failingRepository<OrganizationRepository>()
        return HomeViewModel(
            savedStateHandle = SavedStateHandle(),
            getMyProfileUseCase = GetMyProfileUseCase(failingRepository<MemberRepository>()),
            getScheduleMonthUseCase = GetScheduleMonthUseCase(failingRepository<ScheduleRepository>()),
            getGisuInfoUseCase = GetGisuInfoUseCase(organizationRepository),
            getGisuListUseCase = GetGisuListUseCase(organizationRepository),
        )
    }

    private val challenger = UserInfo(
        challengerRecords = listOf(
            ChallengerRecord(
                challengerId = 10L, memberId = 1L, gisuId = 6L, gisu = 11L, chapterId = null, chapterName = null,
                part = null, challengerStatus = null, totalPoint = null, name = "", nickname = "", email = "",
                schoolId = 1L, schoolName = "", profileImageLink = "", status = "ACTIVE",
            )
        )
    )

    @Test fun `account without gisu shows profile without activity status card`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.settingUserInfoToUI(UserInfo(isUmcProduct = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HomeLoadState.SUCCESS, state.profileLoadState)
        assertNull(state.userType)
        assertTrue(state.gisuTag.isEmpty())
    }

    @Test fun `challenger with gisu still gets the activity status card`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.settingUserInfoToUI(challenger)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HomeLoadState.SUCCESS, state.profileLoadState)
        assertEquals(UserType.ACTIVE, state.userType)
        assertEquals(listOf("11기"), state.gisuTag)
    }
}
