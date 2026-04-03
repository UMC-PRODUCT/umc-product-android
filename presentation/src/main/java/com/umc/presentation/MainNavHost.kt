package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.umc.presentation.splash.SplashScreen
import com.umc.presentation.home.HomeScreen

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
            SplashScreen()
        }
        composable<MainDestination.Home> {
            HomeScreen()
        }
    }
}