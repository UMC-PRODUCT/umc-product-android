package com.umc.presentation

import com.umc.component.fcm.CommunityNotification
import com.umc.component.fcm.FcmTokenSynchronizer
import javax.inject.Inject
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.presentation.remotenotice.RemoteNoticeHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var tokenSynchronizer: FcmTokenSynchronizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PendingDeepLink.initialize(this)
        // 외부 링크로 처음 열린 경우 NavController 가 곧바로 목적지를 열지 않도록 링크를 보류한다.
        // 앱은 항상 스플래시(인증 판정)부터 시작하고, 홈에 도착한 뒤 원래 목적지로 이동한다.
        if (savedInstanceState != null) {
            PendingDeepLink.uri = savedInstanceState.getString("pendingDeepLink")?.let(android.net.Uri::parse)
        }
        if (savedInstanceState == null) acceptDeepLink(intent)
        // 자동 딥링크 처리는 막고 인증 상태에 맞춰 MainNavHost에서 소비한다.
        intent?.data = null
        // 상태바는 투명으로 두고 뒤에 비치는 Scaffold 배경색(UmcApp)이 화면별로 칠한다.
        // 아이콘 명암은 시스템 다크모드를 따라 자동으로 바뀐다
        enableEdgeToEdge()
        setContent {
            UmcApp()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingDeepLink", PendingDeepLink.uri?.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        tokenSynchronizer.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptDeepLink(intent)
        intent.data = null
    }

    private fun acceptDeepLink(incoming: Intent?) {
        val payload = listOf("schemaVersion", "type", "threadId", "notificationEventId", "deepLink", "deeplink")
            .associateWith { incoming?.getStringExtra(it).orEmpty() }
            .filterValues { it.isNotEmpty() }
        val route = CommunityNotification.parse(payload)
        val eventId = incoming?.getStringExtra("notificationEventId") ?: route?.eventId
        val preferences = getSharedPreferences("fcm_routes", MODE_PRIVATE)
        val seen = preferences.getStringSet("consumed", emptySet()).orEmpty()
        if (eventId != null && eventId in seen) return
        val link = route?.uri() ?: incoming?.data ?: return
        if (eventId != null) {
            preferences.edit().putStringSet("consumed", (seen.toList().takeLast(199) + eventId).toSet()).apply()
        }
        PendingDeepLink.uri = link
    }

}

@Composable
private fun UmcApp() {
    UmcTheme {

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        /**백스택마다 감지**/
        val currentDestination = navBackStackEntry?.destination

        // 💡 현재 화면이 5개 메인 탭(1뎁스) 화면 중 하나인지 확인 (Type-Safe 체크)
        val currentTab = when {
            currentDestination?.hasRoute<MainDestination.Home>() == true -> MainTab.Home
            currentDestination?.hasRoute<MainDestination.Notice>() == true -> MainTab.Notice
            currentDestination?.hasRoute<MainDestination.Act>() == true -> MainTab.Activity
            currentDestination?.hasRoute<MainDestination.Community>() == true -> MainTab.Community
            // 내 명함일 때만 MY 탭으로 취급 (memberId가 있으면 딥링크로 열린 타인 명함 = 2뎁스)
            currentDestination?.hasRoute<MainDestination.Mycard>() == true ->
                if (navBackStackEntry?.toRoute<MainDestination.Mycard>()?.memberId == null) MainTab.My else null
            else -> null
        }
        val showBottomBar = currentTab != null

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                // 상태바 뒤 영역 색. 홈은 헤더 없이 grey100 배경이라 같은 색으로 맞추고, 나머지는 grey50
                containerColor = if (currentTab == MainTab.Home) grey100() else grey50(),
                bottomBar = {
                    if (showBottomBar) {
                        UmcBottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = { tab ->
                                // Type-Safe 객체로 navigate 실행
                                navController.navigate(tab.destination) {
                                    /**
                                     * 스택이 계속 쌓이는 것을 방지하기 위해 홈(메인 영역 루트)까지
                                     * 기존 스택을 정리하고 saveState = true로 이전 화면 상태(스크롤 위치 등)를 보존
                                     * (그래프 시작점은 스플래시라 findStartDestination은 사용 불가)
                                     * **/
                                    popUpTo(MainDestination.Home) {
                                        saveState = true
                                        inclusive = false // Home 화면 자체는 백스택에 남겨둠
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                // Scaffold 패딩을 적용하여 바텀바와 화면 내용이 겹치지 않게 처리
                MainNavHost(
                    navHostController = navController,
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                )
            }

            // 원격 설정으로 켠 안내. 이용을 막는 화면(BLOCKING)은 하단바까지 덮어야 해서 Scaffold 바깥에 둔다
            RemoteNoticeHost(currentRoute = currentDestination?.route)
        }
    }
}


/**
 * 피그마 커스텀 하단바 (전체 72dp = 상단 구분선 1dp + 콘텐츠 71dp)
 * 선택: 아이콘 grey900 + 라벨 grey950 / 비선택: grey400
 */
@Composable
private fun UmcBottomNavigationBar(
    currentTab: MainTab?,
    onTabSelected: (MainTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey50())
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 1.dp, color = grey200())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(71.dp)
        ) {
            MainTab.tabs.forEach { tab ->
                val isSelected = currentTab == tab

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 리플 없이 플랫하게
                        ) { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                        ),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) grey900() else grey400()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    UText(
                        text = tab.title,
                        style = UmcTypographyTokens.Footnote,
                        color = if (isSelected) grey950() else grey400()
                    )
                }
            }
        }
    }
}
