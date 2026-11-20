package com.umc.presentation

import com.umc.component.theme.AppStrings

/**bottomNav에 들어갈 탭들 정의**/
sealed class MainTab (
    val destination: MainDestination,
    val title: String,
    val unselectedIcon: Int,
    val selectedIcon: Int
) {

    /**각 탭 정도 싱글톤으로 정의***/
    object Home : MainTab(
        MainDestination.Home,
        AppStrings.MAIN_TAB_HOME,
        com.umc.component.R.drawable.ic_bottom_nav_home,
        com.umc.component.R.drawable.ic_bottom_nav_home_fill)
    object Notice : MainTab(
        MainDestination.Notice,
        AppStrings.MAIN_TAB_NOTICE,
        com.umc.component.R.drawable.ic_bottom_nav_notice,
        com.umc.component.R.drawable.ic_bottom_nav_notice_fill)
    object Activity : MainTab(
        MainDestination.Act,
        AppStrings.MAIN_TAB_ACTIVITY,
        com.umc.component.R.drawable.ic_bottom_nav_calendar,
        com.umc.component.R.drawable.ic_bottom_nav_calendar_fill)
    object Community : MainTab(
        MainDestination.Community,
        AppStrings.COMMUNITY_TITLE,
        com.umc.component.R.drawable.ic_bottom_nav_community,
        com.umc.component.R.drawable.ic_bottom_nav_community_fill)
    object My : MainTab(
        MainDestination.Mycard(),
        AppStrings.MAIN_TAB_MY,
        com.umc.component.R.drawable.ic_bottom_nav_mypage,
        com.umc.component.R.drawable.ic_bottom_nav_mypage_fill)

    companion object {
        val tabs by lazy { listOf(Home, Notice, Activity, Community, My) }
    }

}

