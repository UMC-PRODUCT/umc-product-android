package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.umc.presentation.login.LoginRoute
import com.umc.presentation.splash.SplashRoute
import com.umc.presentation.study.UserStudyRoute
@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = MainDestination.Splash,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            SplashRoute(
                navigateToLogin = { navHostController.navigate(MainDestination.Login) },
                navigateToMain = {
                    // 테스트용: 메인 대신 스터디 화면으로 이동
                    navHostController.navigate(MainDestination.Study) {
                        popUpTo(MainDestination.Splash) { inclusive = true } // 뒤로가기 시 스플래쉬 안 돌아가게
                    }
                },
                navigateToInputCode = {
                    navHostController.navigate(MainDestination.Login) // 일단 로그인으로
                }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute()
        }

        composable<MainDestination.Study> {
            UserStudyRoute()
        }
    }
}