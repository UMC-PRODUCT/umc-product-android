package com.umc.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.WHITE,
                darkScrim = Color.BLACK,
            ),
        )
        setContent {
            UmcApp()
        }
    }
}

@Composable
private fun UmcApp() {
    UmcTheme {

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        /**백스택마다 감지**/
        val currentDestination = navBackStackEntry?.destination

        // 💡 현재 화면이 5개 메인 탭 화면 중 하나인지 확인 (Type-Safe 체크)
        val currentTab = when {
            currentDestination?.hasRoute<MainDestination.Home>() == true -> MainTab.Home
            // TODO: 공지/활동/커뮤니티 Destination이 분리되면 해당 Destination 타입으로 교체
            //currentDestination?.hasRoute<MainDestination.>() == true -> MainTab.Activity
            //currentDestination?.hasRoute<MainDestination.>() == true -> MainTab.Community
            currentDestination?.hasRoute<MainDestination.Notification>() == true -> MainTab.Notice
            currentDestination?.hasRoute<MainDestination.Mycard>() == true -> MainTab.My
            else -> null
        }
        val showBottomBar = currentTab != null
        /*
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding(),
        ) {
            val navController = rememberNavController()
            MainNavHost(
                navHostController = navController,
            )
        }

         */

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    UmcBottomNavigationBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            // Type-Safe 객체로 navigate 실행
                            navController.navigate(tab.destination) {
                                /**
                                 * 스택이 계속 쌓이는 것을 방지하기 위해 루트 화면까지
                                 * 기존 스택을 정리하고 saveState = true로 이전 화면 상태(스크롤 위치 등)를 보존
                                 * **/
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
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
    }
}


@Composable
private fun UmcBottomNavigationBar(
    currentTab: MainTab?,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = grey000(), // 피그마 하단바 배경색
        tonalElevation = 4.dp
    ) {
        MainTab.tabs.filterNotNull().forEach { tab ->
            val isSelected = currentTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                        ),
                        contentDescription = tab.title,
                        tint = if (isSelected) grey950() else grey400()
                    )
                },
                label = {
                    UText(
                        text = tab.title,
                        style = UmcTypographyTokens.Footnote,
                        color = if (isSelected) grey950() else grey400()
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent // 아이콘 뒤 선택 영역 하이라이트(원형) 제거
                )
            )
        }
    }
}