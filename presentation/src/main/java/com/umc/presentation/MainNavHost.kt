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
                    // TODO: 메인 화면 완성 후 연결
                },
                navigateToInputCode = {
                    // TODO: 코드 입력 화면 완성 후 연결
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