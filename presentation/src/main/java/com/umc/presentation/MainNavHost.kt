package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.umc.presentation.home.home.HomeRoute
import com.umc.presentation.home.schedule.add.ScheduleAddRoute
import com.umc.presentation.home.schedule.detail.ScheduleDetailRoute
import com.umc.presentation.login.LoginRoute
import com.umc.presentation.splash.SplashRoute

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,

        //시작 화면 (일단 임시로 home)
        startDestination = MainDestination.Home,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            SplashRoute(
                navigateToLogin = { navHostController.navigate(MainDestination.Login) }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute()
        }

        /**홈 화면 탭에 대한 내용입니다.**/
        //홈 화면
        composable<MainDestination.Home> {
            HomeRoute(
                onNavigateToNotice = {
                    //navHostController.navigate(MainDestination.Notice)
                     },
                onNavigateToScheduleAdd = {
                    navHostController.navigate(MainDestination.ScheduleAdd)
                },
                onNavigateToScheduleDetail = {
                    navHostController.navigate(MainDestination.ScheduleDetail(it.id, it.plusDay))
                },
                onNavigateToNotification = {}
            )
        }
        //일정 생성
        composable<MainDestination.ScheduleAdd> {
            ScheduleAddRoute(
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 수정
        composable<MainDestination.ScheduleEdit> { data ->
            //Destination 클래스 형태로 데이터를 추출
            val editRoute = data.toRoute<MainDestination.ScheduleEdit>()

            ScheduleAddRoute(
                //추출한 객체에서 scheduleId에 접근
                scheduleId = editRoute.scheduleId,
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 상세
        composable<MainDestination.ScheduleDetail>{ data ->
            val detailRoute = data.toRoute<MainDestination.ScheduleDetail>()

            ScheduleDetailRoute(
                scheduleId = detailRoute.scheduleId,
                plusDay = detailRoute.plusDay
            )
        }




    }
}