package com.umc.presentation

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
        "홈",
        com.umc.component.R.drawable.ic_bottom_nav_home,
        com.umc.component.R.drawable.ic_bottom_nav_home_fill)
    object Notice : MainTab(
        MainDestination.Home,
        "공지",
        com.umc.component.R.drawable.ic_bottom_nav_notice,
        com.umc.component.R.drawable.ic_bottom_nav_notice_fill)
    object Activity : MainTab(
        MainDestination.Home,
        "활동",
        com.umc.component.R.drawable.ic_bottom_nav_calendar,
        com.umc.component.R.drawable.ic_bottom_nav_calendar_fill)
    object Community : MainTab(
        MainDestination.Home,
        "커뮤니티",
        com.umc.component.R.drawable.ic_bottom_nav_community,
        com.umc.component.R.drawable.ic_bottom_nav_community_fill)
    object My : MainTab(
        MainDestination.Mycard(),
        "MY",
        com.umc.component.R.drawable.ic_bottom_nav_mypage,
        com.umc.component.R.drawable.ic_bottom_nav_mypage_fill)

    companion object {
        val tabs by lazy { listOf(Home, Notice, Activity, Community, My) }
    }

}

