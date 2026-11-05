package com.umc.presentation.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.umc.presentation.MainDestination
import com.umc.presentation.remotenotice.toScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** 딥링크로 연 다른 회원의 명함. route 는 MY 탭(Mycard)과 같아서 이름을 따로 붙인다 */
internal const val OTHER_MEMBER_CARD_SCREEN = "MemberCard"

/**
 * Firebase Analytics 에 보낼 화면 이름
 *
 * route 에서 경로 이름만 쓴다. route 인자(회원·챌린저 ID, 가입 토큰 등)는 화면 이름에 넣지 않는다.
 * 원격 공지가 대상 화면을 고를 때 쓰는 이름([toScreenName])과 같다.
 */
internal fun analyticsScreenName(route: String, isOtherMemberCard: Boolean): String =
    if (isOtherMemberCard) OTHER_MEMBER_CARD_SCREEN else route.toScreenName()

/**
 * 화면 이동마다 screen_view 를 한 번 기록한다.
 * 수집 여부는 :app 매니페스트의 firebase_analytics_collection_enabled(prod flavor 만 켬)가 정한다.
 */
@Composable
internal fun ScreenViewTracker(
    backStackEntry: NavBackStackEntry?,
    viewModel: ScreenViewTrackerViewModel = hiltViewModel(),
) {
    LaunchedEffect(backStackEntry?.id) {
        val entry = backStackEntry ?: return@LaunchedEffect
        val route = entry.destination.route ?: return@LaunchedEffect
        val isOtherMemberCard = entry.destination.hasRoute<MainDestination.Mycard>() &&
            entry.toRoute<MainDestination.Mycard>().memberId != null
        viewModel.onScreenShown(entry.id, analyticsScreenName(route, isOtherMemberCard))
    }
}

@HiltViewModel
class ScreenViewTrackerViewModel @Inject constructor(
    private val screenViewSink: ScreenViewSink,
) : ViewModel() {

    /**
     * 마지막으로 기록한 백스택 항목 id.
     *
     * 같은 탭을 다시 누르거나 회전·다크 모드 전환으로 화면이 다시 그려져도 항목 id 가 같아서 한 번만 남긴다.
     * 저장 상태(SavedState)에 넣지 않아 프로세스가 종료되면 사라진다. 그래서 복원된 화면은 다시 기록된다.
     * (자동 화면 보고를 꺼서 SDK 가 새 프로세스에서 현재 화면을 스스로 되살리지 않기 때문)
     */
    private var lastLoggedEntryId: String? = null

    fun onScreenShown(entryId: String, screenName: String) {
        if (entryId == lastLoggedEntryId) return
        lastLoggedEntryId = entryId
        screenViewSink.logScreenView(screenName)
    }
}
